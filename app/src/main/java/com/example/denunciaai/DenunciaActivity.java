package com.example.denunciaai;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.denunciaai.viewmodel.DenunciaViewModel;
import com.example.denunciaain.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DenunciaActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private DenunciaViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;
    
    private Spinner spinnerTipoAtividade;
    private Button btnUsarLocalizacao;
    private TextView tvCoordenadasAtual;
    private EditText etDataHora;
    private EditText etDescricao;
    private Button btnEnviarDenuncia;
    private ProgressBar progressBar;
    private TextView btnBack;

    private Calendar selectedCalendar;
    private final String[] categorias = {"Selecione", "Atividade Suspeita", "Vandalismo", "Roubo", "Outro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denuncia);
        
        viewModel = new ViewModelProvider(this).get(DenunciaViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        selectedCalendar = Calendar.getInstance();
        
        initViews();
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
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        
        // Setup spinner with custom layouts for white text
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_white,
                getResources().getStringArray(R.array.tipos_de_atividade));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white);
        spinnerTipoAtividade.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        spinnerTipoAtividade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    viewModel.setSelectedCategory(categorias[position]);
                } else {
                    viewModel.setSelectedCategory("");
                }
                updateSubmitButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                viewModel.setSelectedCategory("");
                updateSubmitButtonState();
            }
        });

        btnUsarLocalizacao.setOnClickListener(v -> requestLocation());

        etDataHora.setOnClickListener(v -> showDateTimePickers());

        etDescricao.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setDescription(etDescricao.getText().toString());
                updateSubmitButtonState();
            }
        });

        btnEnviarDenuncia.setOnClickListener(v -> {
            viewModel.setDescription(etDescricao.getText().toString());
            viewModel.submitDenuncia();
        });
    }

    private void observeViewModel() {
        viewModel.getSelectedCategory().observe(this, category -> updateSubmitButtonState());
        
        viewModel.getLatitude().observe(this, latitude -> {
            updateCoordinatesUI();
            updateSubmitButtonState();
        });

        viewModel.getLongitude().observe(this, longitude -> {
            updateCoordinatesUI();
            updateSubmitButtonState();
        });

        viewModel.getDateTimeString().observe(this, dateTime -> {
            etDataHora.setText(dateTime);
            updateSubmitButtonState();
        });

        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnEnviarDenuncia.setEnabled(!isLoading);
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
    }

    private void showSuccessDialog() {
        SuccessDialogFragment dialog = new SuccessDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "SuccessDialog");
    }

    private void updateCoordinatesUI() {
        if (viewModel.getLatitude().getValue() != null && viewModel.getLongitude().getValue() != null) {
            tvCoordenadasAtual.setText(getString(R.string.coordenadas_formato, 
                    viewModel.getLatitude().getValue(), 
                    viewModel.getLongitude().getValue()));
            tvCoordenadasAtual.setVisibility(View.VISIBLE);
        }
    }

    private void updateSubmitButtonState() {
        boolean isValid = true;
        
        // Check category
        if (viewModel.getSelectedCategory().getValue() == null || 
                viewModel.getSelectedCategory().getValue().isEmpty()) {
            isValid = false;
        }
        
        // Check date/time
        if (viewModel.getDateTimeString().getValue() == null || 
                viewModel.getDateTimeString().getValue().isEmpty()) {
            isValid = false;
        }
        
        // Check description
        String description = etDescricao.getText().toString();
        if (description.isEmpty()) {
            isValid = false;
        }
        
        btnEnviarDenuncia.setEnabled(isValid);
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

    @SuppressLint("MissingPermission") // Permission check is done in requestLocation()
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        viewModel.setLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, R.string.error_location, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> 
                        Toast.makeText(this, R.string.error_location, Toast.LENGTH_SHORT).show());
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
