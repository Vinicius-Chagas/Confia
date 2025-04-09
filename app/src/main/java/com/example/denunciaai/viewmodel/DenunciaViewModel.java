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

        // ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // Call<Object> call = apiService.enviarDenuncia(denuncia);

        isLoading.setValue(false);
        submitSuccess.setValue(true);

        // call.enqueue(new Callback<Object>() {
        //     @Override
        //     public void onResponse(Call<Object> call, Response<Object> response) {
        //         isLoading.setValue(false);
        //         if (response.isSuccessful()) {
        //             submitSuccess.setValue(true);
        //         } else {
        //             errorMessage.setValue("Erro ao enviar denúncia: " + response.code());
        //             submitSuccess.setValue(true);
        //         }
        //     }

        //     @Override
        //     public void onFailure(Call<Object> call, Throwable t) {
        //         isLoading.setValue(false);
        //         errorMessage.setValue("Erro ao enviar denúncia: " + t.getMessage());
        //         submitSuccess.setValue(true); // Add this line
        //     }
        // });
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
}
