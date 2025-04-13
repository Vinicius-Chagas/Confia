package com.example.denunciaai.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.denunciaai.api.ApiClient;
import com.example.denunciaai.api.ApiService;
import com.example.denunciaai.model.Denuncia;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DenunciaViewModel extends ViewModel {
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<Double> latitude = new MutableLiveData<>();
    private final MutableLiveData<Double> longitude = new MutableLiveData<>();
    private final MutableLiveData<String> dateTimeString = new MutableLiveData<>();
    private final MutableLiveData<String> description = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isViewMode = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> denunciaId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> markAsConcludedSuccess = new MutableLiveData<>(false);

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
    }

    public LiveData<Double> getLatitude() {
        return latitude;
    }

    public LiveData<Double> getLongitude() {
        return longitude;
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude.setValue(latitude);
        this.longitude.setValue(longitude);
    }

    public LiveData<String> getDateTimeString() {
        return dateTimeString;
    }

    public void setDateTimeString(String dateTime) {
        dateTimeString.setValue(dateTime);
    }

    public LiveData<String> getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description.setValue(description);
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<Boolean> isSubmitSuccess() {
        return submitSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isViewMode() {
        return isViewMode;
    }

    public void setViewMode(boolean viewMode) {
        isViewMode.setValue(viewMode);
    }

    public LiveData<Integer> getDenunciaId() {
        return denunciaId;
    }

    public void setDenunciaId(int id) {
        denunciaId.setValue(id);
    }

    public LiveData<Boolean> isMarkAsConcludedSuccess() {
        return markAsConcludedSuccess;
    }

    public boolean validateForm() {
        if (selectedCategory.getValue() == null || selectedCategory.getValue().isEmpty()) {
            errorMessage.setValue("Selecione um tipo de atividade");
            return false;
        }

        if (dateTimeString.getValue() == null || dateTimeString.getValue().isEmpty()) {
            errorMessage.setValue("Selecione a data e hora da ocorrência");
            return false;
        }

        if (description.getValue() == null || description.getValue().isEmpty()) {
            errorMessage.setValue("Insira uma descrição da ocorrência");
            return false;
        }

        return true;
    }

    public void submitDenuncia() {
        if (!validateForm()) {
            return;
        }

        isLoading.setValue(true);

        // Convert the display date format to ISO 8601
        String isoDateTime = convertToISODateTime(dateTimeString.getValue());

        Denuncia denuncia = new Denuncia(
                description.getValue(),
                selectedCategory.getValue(),
                latitude.getValue(),
                longitude.getValue(),
                isoDateTime
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Object> call = apiService.enviarDenuncia(denuncia);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    submitSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Erro ao enviar denúncia: " + response.code());

                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Erro ao enviar denúncia: " + t.getMessage());
                System.out.println(t.getMessage());

            }
        });
    }

    public void fetchDenunciaById(int id) {
        isLoading.setValue(true);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Denuncia> call = apiService.getDenunciaById(id);
        
        call.enqueue(new Callback<Denuncia>() {
            @Override
            public void onResponse(Call<Denuncia> call, Response<Denuncia> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Denuncia denuncia = response.body();
                    // Use the new method to populate data
                    populateViewData(denuncia);
                } else {
                    errorMessage.setValue("Erro ao buscar denúncia: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Denuncia> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Erro ao buscar denúncia: " + t.getMessage());
            }
        });
    }
    
    public void markAsConcluded(int id) {
        isLoading.setValue(true);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Object> call = apiService.markDenunciaAsConcluded(id);
        
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    markAsConcludedSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Erro ao marcar denúncia como concluída: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Erro ao marcar denúncia como concluída: " + t.getMessage());
            }
        });
    }

    private String convertToISODateTime(String displayDateTime) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            Date date = displayFormat.parse(displayDateTime);
            
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            return date != null ? isoFormat.format(date) : null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // New method to populate LiveData from a Denuncia object
    public void populateViewData(Denuncia denuncia) {
        denunciaId.setValue(denuncia.getId());
        selectedCategory.setValue(denuncia.getCategoria());
        description.setValue(denuncia.getDescricao());
        latitude.setValue(denuncia.getLatitude());
        longitude.setValue(denuncia.getLongitude());
        
        // Format date/time for display
        dateTimeString.setValue(formatDateForDisplay(denuncia.getDateTime()));
    }
    
    // Helper method to format ISO date to display format (extracted logic)
    private String formatDateForDisplay(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "";
        }
        try {
            // Input format (ISO 8601)
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Assuming API returns UTC
            Date date = isoFormat.parse(isoDateTime);

            // Output format (dd/MM/yyyy HH:mm)
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            // displayFormat.setTimeZone(TimeZone.getDefault()); // Convert to local time zone for display

            if (date != null) {
                return displayFormat.format(date);
            }
        } catch (ParseException e) {
            // If parsing fails, return the original string or handle error
            System.err.println("Error parsing date: " + isoDateTime + " - " + e.getMessage());
            return isoDateTime; // Fallback to original string
        }
        return isoDateTime; // Fallback
    }
}
