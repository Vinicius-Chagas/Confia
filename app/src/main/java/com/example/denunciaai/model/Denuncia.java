package com.example.denunciaai.model;

import com.google.gson.annotations.SerializedName;

public class Denuncia {
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

    public Denuncia(String descricao, String categoria, Double latitude, Double longitude, String dateTime) {
        this.descricao = descricao;
        this.categoria = categoria;
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
