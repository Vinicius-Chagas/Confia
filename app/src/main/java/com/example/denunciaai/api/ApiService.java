package com.example.denunciaai.api;


import com.example.denunciaai.model.Denuncia;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("denuncia")
    Call<Object> enviarDenuncia(@Body Denuncia denuncia);
}
