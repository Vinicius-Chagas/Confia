package com.example.denunciaai.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // Import Serializable

// Implement Serializable
public class Denuncia implements Serializable {
    @SerializedName("id")
    private Integer id;
    
    @SerializedName("descricao")
    private String descricao;
    
    @SerializedName("categoria")
    private String categoria;
    
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;
    
    @SerializedName("dateTime")
    private String dateTime;

    // Constructor for creating new Denuncia (without ID)
    public Denuncia(String descricao, String categoria, Double latitude, Double longitude, String dateTime) {
        this.descricao = descricao;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
    }

    // Constructor for receiving Denuncia from API or Mock (with ID)
    public Denuncia(Integer id, String categoria, String descricao, Double latitude, Double longitude, String dateTime) {
        this.id = id;
        this.categoria = categoria;
        this.descricao = descricao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
