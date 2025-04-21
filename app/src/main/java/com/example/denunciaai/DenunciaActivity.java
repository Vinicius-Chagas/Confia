package com.example.denunciaai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.denunciaai.viewmodel.DenunciaViewModel;
import android.content.Context;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.denunciaai.model.Denuncia;

public class DenunciaActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    public static final String EXTRA_IS_VIEW_MODE = "is_view_mode";
    public static final String EXTRA_DENUNCIA_ID = "denuncia_id"; 
    public static final String EXTRA_DENUNCIA_OBJECT = "denuncia_object";

    private DenunciaViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    
    private Spinner spinnerTipoAtividade;
    private Button btnUsarLocalizacao;
    private TextView tvCoordenadasAtual;
    private EditText etDataHora;
    private EditText etDescricao;
    private Button btnEnviarDenuncia;
    private Button btnMarcarConcluida;  // New button for marking as concluded
    private ProgressBar progressBar;
    private TextView btnBack;

    private LocationCallback locationCallback;
    private Handler timeoutHandler;

    private Calendar selectedCalendar;
    private boolean isViewMode = false;
    private final String[] categorias = {"Selecione", "Atividade Suspeita", "Vandalismo", "Roubo", "Outro"};
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
        
        viewModel = new ViewModelProvider(this).get(DenunciaViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        selectedCalendar = Calendar.getInstance();
        
        // Check if we're in view mode
        isViewMode = getIntent().getBooleanExtra(EXTRA_IS_VIEW_MODE, false);
        viewModel.setViewMode(isViewMode);
        
        if (isViewMode) {
            // Try to get the full Denuncia object first
            Denuncia denuncia = (Denuncia) getIntent().getSerializableExtra(EXTRA_DENUNCIA_OBJECT);
            
            if (denuncia != null) {
                // If object received, populate ViewModel directly
                viewModel.populateViewData(denuncia);
            } else {
                // Fallback: If object not found, try getting ID and fetching
                int denunciaId = getIntent().getIntExtra(EXTRA_DENUNCIA_ID, -1);
                if (denunciaId != -1) {
                    // viewModel.setDenunciaId(denunciaId); // setDenunciaId is now called within populateViewData or fetchDenunciaById
                    viewModel.fetchDenunciaById(denunciaId); // Fetch if only ID is available
                } else {
                    Toast.makeText(this, "Dados da denúncia não fornecidos", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }
        
        initViews();
        setupViewModeUI();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        spinnerTipoAtividade = findViewById(R.id.spinnerTipoAtividade);
        btnUsarLocalizacao = findViewById(R.id.btnUsarLocalizacao);
        tvCoordenadasAtual = findViewById(R.id.tvCoordenadasAtual);
        etDataHora = findViewById(R.id.etDataHora);
        etDescricao = findViewById(R.id.etDescricao);
        btnEnviarDenuncia = findViewById(R.id.btnEnviarDenuncia);
        btnMarcarConcluida = findViewById(R.id.btnMarcarConcluida);  // Add this to your layout
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        
        // Setup spinner with custom layouts for white text
        spinnerAdapter = new ArrayAdapter<>( // Use the class variable here
                this,
                R.layout.spinner_item_white,
                getResources().getStringArray(R.array.tipos_de_atividade)); // Adapter uses the resource array
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        spinnerTipoAtividade.setAdapter(spinnerAdapter);
    }
    
    private void setupViewModeUI() {
        if (isViewMode) {
            setTitle("Visualizar Denúncia");
            
            // Disable all input fields in view mode
            spinnerTipoAtividade.setEnabled(false);
            btnUsarLocalizacao.setEnabled(false);
            etDataHora.setEnabled(false);
            etDescricao.setEnabled(false);
            
            // Change button visibility
            btnEnviarDenuncia.setVisibility(View.GONE);
            btnMarcarConcluida.setVisibility(View.VISIBLE);
        } else {
            setTitle("Nova Denúncia");
            
            // Hide the "Marcar como Concluída" button in create mode
            btnMarcarConcluida.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        spinnerTipoAtividade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // Flag to prevent listener reacting to programmatic changes by the observer
            private boolean isProgrammaticSelection = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // If this selection was triggered by the observer, just reset the flag and do nothing
                if (isProgrammaticSelection) {
                    System.out.println("Spinner Listener: Ignoring programmatic selection.");
                    isProgrammaticSelection = false; // Reset for next user interaction
                    return;
                }

                // If in view mode, the user shouldn't be changing anything, so do nothing
                if (isViewMode) {
                    System.out.println("Spinner Listener: In view mode, ignoring selection change.");
                    return;
                }

                // --- Handle USER selection (not programmatic, not view mode) ---
                String selectedCategory = "";
                if (position > 0) {
                    // Get the selected item directly from the adapter - SAFER!
                    selectedCategory = parent.getItemAtPosition(position).toString();
                    spinnerTipoAtividade.setBackgroundResource(R.drawable.input);
                    System.out.println("Spinner Listener: User selected '" + selectedCategory + "'. Updating ViewModel.");
                } else {
                    // User selected the default "Selecione" item
                    spinnerTipoAtividade.setBackgroundResource(R.drawable.input_error); // Or input if default is valid
                    System.out.println("Spinner Listener: User selected default item. Updating ViewModel with empty string.");
                }

                // Only update ViewModel if the value actually changed due to USER interaction
                // This check might be redundant now due to the isProgrammaticSelection flag, but adds safety
                if (!selectedCategory.equals(viewModel.getSelectedCategory().getValue())) {
                    viewModel.setSelectedCategory(selectedCategory);
                } else {
                    System.out.println("Spinner Listener: Selected value ('"+selectedCategory+"') matches ViewModel. No update needed.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Only react if not in view mode
                if (!isViewMode) {
                    System.out.println("Spinner Listener: Nothing selected. Updating ViewModel with empty string.");
                    viewModel.setSelectedCategory("");
                    spinnerTipoAtividade.setBackgroundResource(R.drawable.input_error);
                }
            }

            // Helper method called from the observer before setting selection
            public void prepareProgrammaticSelect() {
                System.out.println("Spinner Listener: Preparing for programmatic selection.");
                isProgrammaticSelection = true;
            }
        });

        btnUsarLocalizacao.setOnClickListener(v -> requestLocation());

        etDataHora.setOnClickListener(v -> {
            if (!isViewMode) {
                showDateTimePickers();
            }
        });

        etDescricao.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String description = etDescricao.getText().toString();
                viewModel.setDescription(description);
                if (!description.isEmpty()) {
                    etDescricao.setBackgroundResource(R.drawable.input);
                }
            }
        });

        btnEnviarDenuncia.setOnClickListener(v -> {
            if (validateFields()) {
                viewModel.setDescription(etDescricao.getText().toString());
                viewModel.submitDenuncia();
            }
        });
        
        // Setup listener for the "Marcar como Concluída" button
        btnMarcarConcluida.setOnClickListener(v -> {
            int denunciaId = viewModel.getDenunciaId().getValue();
            if (denunciaId != -1) {
                viewModel.markAsConcluded(denunciaId);
            } else {
                Toast.makeText(DenunciaActivity.this, 
                        "Não foi possível concluir a denúncia", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFields() {
        if (isViewMode) {
            return true;  // No validation needed in view mode
        }
        
        boolean isValid = true;
        
        // Validate spinner (category)
        String category = viewModel.getSelectedCategory().getValue();
        if (category == null || category.isEmpty() || category.equals("Selecione uma atividade")) {
            spinnerTipoAtividade.setBackgroundResource(R.drawable.input_error);
            isValid = false;
        } else {
            spinnerTipoAtividade.setBackgroundResource(R.drawable.input);
        }
        
        // Validate date/time
        String dateTime = viewModel.getDateTimeString().getValue();
        if (dateTime == null || dateTime.isEmpty()) {
            etDataHora.setBackgroundResource(R.drawable.input_error);
            isValid = false;
        } else {
            etDataHora.setBackgroundResource(R.drawable.input);
        }
        
        // Validate description
        String description = etDescricao.getText().toString();
        if (description.isEmpty()) {
            etDescricao.setBackgroundResource(R.drawable.input_error);
            isValid = false;
        } else {
            etDescricao.setBackgroundResource(R.drawable.input);
        }
        
        System.out.println("Validating fields: " + isValid);
        
        // Show toast if validation fails
        if (!isValid) {
            Toast.makeText(this, "Por favor, verifique os campos destacados e tente novamente", 
                    Toast.LENGTH_LONG).show();
        }
        
        return isValid;
    }

    private void observeViewModel() {
        viewModel.getSelectedCategory().observe(this, category -> {
            if (isViewMode && spinnerAdapter != null) {
                System.out.println("Activity Observer received category (in view mode): " + category);

                if (category != null && !category.isEmpty()) {
                    int position = spinnerAdapter.getPosition(category);
                    if (position >= 0) {
                        System.out.println("Activity Category '" + category + "' found at position: " + position + ". Setting spinner selection.");

                        // Prepare the listener to ignore the upcoming programmatic selection
                        if (spinnerTipoAtividade.getOnItemSelectedListener() instanceof AdapterView.OnItemSelectedListener) {
                            // You might need to cast to your specific anonymous class or create a named class
                            // For simplicity, let's assume direct access or refactor listener to a named class
                            // If using anonymous class, direct casting is complex. A simpler flag in Activity might work:
                            // this.isObserverSettingSpinner = true; // Set flag before setSelection
                        }
                        // --- IMPORTANT: Call setSelection ---
                        spinnerTipoAtividade.setSelection(position);
                        // this.isObserverSettingSpinner = false; // Reset flag after (needs corresponding check in listener)

                        // SAFER APPROACH: Refactor listener to a named class or use a flag as shown above.
                        // If you refactor OnItemSelectedListener to be a named inner class or separate class,
                        // you can add the prepareProgrammaticSelect() method and call it easily:
                        // ((MyCustomSpinnerListener) spinnerTipoAtividade.getOnItemSelectedListener()).prepareProgrammaticSelect();
                        // spinnerTipoAtividade.setSelection(position);


                    } else {
                        System.out.println("Activity Category '" + category + "' from backend not found... Setting spinner to default.");
                        // ((MyCustomSpinnerListener) spinnerTipoAtividade.getOnItemSelectedListener()).prepareProgrammaticSelect(); // Prepare listener
                        spinnerTipoAtividade.setSelection(0); // Set to default
                    }
                } else {
                    System.out.println("Activity Observer received null or empty category... Setting spinner to default.");
                    // ((MyCustomSpinnerListener) spinnerTipoAtividade.getOnItemSelectedListener()).prepareProgrammaticSelect(); // Prepare listener
                    spinnerTipoAtividade.setSelection(0); // Set to default
                }
            }  else if (!isViewMode) {
                // This block is for when NOT in view mode (creating a new denuncia)
                // The UI spinner position should be controlled by user interaction via the listener,
                // which then updates the ViewModel. The observer is primarily for the initial state
                // or potentially if the category was loaded asynchronously in create mode.
                System.out.println("Activity Observer received category (in create mode): " + category);
                // No programmatic setSelection here in create mode, let the user/listener control it.
                // However, you might want to set the spinner to the default "Selecione" if the ViewModel category is null/empty initially in create mode.
                if (category == null || category.isEmpty()) {
                    spinnerTipoAtividade.setSelection(0);
                }
            }
        });
        
        viewModel.getLatitude().observe(this, latitude -> {
            updateCoordinatesUI();
        });

        viewModel.getLongitude().observe(this, longitude -> {
            updateCoordinatesUI();
        });

        viewModel.getDateTimeString().observe(this, dateTime -> {
            etDataHora.setText(dateTime);
            if (dateTime != null && !dateTime.isEmpty()) {
                etDataHora.setBackgroundResource(R.drawable.input);
            }
        });
        
        viewModel.getDescription().observe(this, description -> {
            if (!etDescricao.getText().toString().equals(description)) {
                etDescricao.setText(description);
            }
        });

        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnEnviarDenuncia.setEnabled(!isLoading);
            btnMarcarConcluida.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.isSubmitSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                showSuccessDialog();
            }
        });
        
        viewModel.isMarkAsConcludedSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Denúncia marcada como concluída com sucesso!", 
                        Toast.LENGTH_SHORT).show();
                finish(); // Return to the list
            }
        });

        viewModel.getDateTimeString().observe(this, dateTime -> {
            System.out.println("Activity Observer received formatted date time: " + dateTime); // Log the formatted date
            etDataHora.setText(dateTime);
            if (dateTime != null && !dateTime.isEmpty()) {
                etDataHora.setBackgroundResource(R.drawable.input);
            } else if (isViewMode) { // Optional: Handle empty date string visually in view mode
                etDataHora.setBackgroundResource(R.drawable.input_error); // Or some other indicator
            }
        });
    }

    private void showSuccessDialog() {
        Intent intent = new Intent(DenunciaActivity.this, SuccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateCoordinatesUI() {
        if (viewModel.getLatitude().getValue() != null && viewModel.getLongitude().getValue() != null) {
            tvCoordenadasAtual.setText(getString(R.string.coordenadas_formato, 
                    viewModel.getLatitude().getValue(), 
                    viewModel.getLongitude().getValue()));
            tvCoordenadasAtual.setVisibility(View.VISIBLE);
        }
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

@SuppressLint("MissingPermission")
private void getLastLocation() {
    // First check if location is enabled
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        Toast.makeText(this, "Por favor, ative a localização do dispositivo", Toast.LENGTH_LONG).show();
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        return;
    }
    
    Toast.makeText(this, "Obtendo localização...", Toast.LENGTH_SHORT).show();
    
    // Create location request with appropriate settings
    LocationRequest locationRequest = LocationRequest.create();
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    locationRequest.setInterval(10000);  // 10 seconds
    locationRequest.setFastestInterval(5000); // 5 seconds
    locationRequest.setNumUpdates(1);
    
    // Set up a timeout
    timeoutHandler = new Handler(Looper.getMainLooper());
    timeoutHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
            if (locationCallback != null) {
                Toast.makeText(DenunciaActivity.this, "Tempo esgotado ao obter localização", Toast.LENGTH_SHORT).show();
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        }
    }, 30000); // 30 second timeout
    
    // Keep reference to callback to prevent GC
    locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            // Clear the timeout
            timeoutHandler.removeCallbacksAndMessages(null);
            
            if (locationResult == null || locationResult.getLocations().isEmpty()) {
                Toast.makeText(DenunciaActivity.this, 
                    "Não foi possível obter localização", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Get the latest location
            Location location = locationResult.getLocations().get(0);
            Toast.makeText(DenunciaActivity.this, 
                "Localização obtida com sucesso!", Toast.LENGTH_SHORT).show();
                
            viewModel.setLocation(location.getLatitude(), location.getLongitude());
            
            // Remove updates after we get the location
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    };
    
    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest, 
            locationCallback, 
            Looper.getMainLooper()
        ).addOnFailureListener(e -> {
            Toast.makeText(DenunciaActivity.this, 
                "Erro ao obter localização: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        });
    } catch (Exception e) {
        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

// Add this method to your activity
@Override
protected void onDestroy() {
    super.onDestroy();
    
    // Clean up location resources
    if (fusedLocationClient != null && locationCallback != null) {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    
    if (timeoutHandler != null) {
        timeoutHandler.removeCallbacksAndMessages(null);
    }
}
    private void showDateTimePickers() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // After date is set, show time picker
                    showTimePicker();
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute);
                    
                    // Format the date and time
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
                    String formattedDateTime = sdf.format(selectedCalendar.getTime());
                    
                    viewModel.setDateTimeString(formattedDateTime);
                },
                selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
