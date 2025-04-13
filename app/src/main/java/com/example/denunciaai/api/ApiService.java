package com.example.denunciaai.api;

import com.example.denunciaai.model.Denuncia;
import com.example.denunciaai.model.LoginRequest;
import com.example.denunciaai.model.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    @POST("denuncia")
    Call<Object> enviarDenuncia(@Body Denuncia denuncia);
    
    @GET("denuncias/{id}")
    Call<Denuncia> getDenunciaById(@Path("id") int id);
    
    @GET("denuncias")
    Call<List<Denuncia>> getAllDenuncias(@Header("Authorization") String token);
    
    @PUT("denuncias/{id}/concluir")
    Call<Object> markDenunciaAsConcluded(@Path("id") int id);
    
    @POST("police/login")
    Call<LoginResponse> loginPolice(@Body LoginRequest loginRequest);
}
