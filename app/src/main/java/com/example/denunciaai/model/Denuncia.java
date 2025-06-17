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

    @SerializedName("datetime")
    private String datetime;

    @SerializedName("userUUID")
    private String userUUID;

    @SerializedName("status")
    private String status; // "pendente", "validada", "rejeitada"

    // Constructor for creating new Denuncia (without ID)
    public Denuncia(String descricao, String categoria, Double latitude, Double longitude, String datetime, String userUUID) {
        this.descricao = descricao;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;
        this.userUUID = userUUID;
        this.status = "pendente"; // Status padrão
    }

    // Constructor for receiving Denuncia from API or Mock (with ID)
    public Denuncia(Integer id, String categoria, String descricao, Double latitude, Double longitude, String datetime) {
        this.id = id;
        this.categoria = categoria;
        this.descricao = descricao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;
        this.status = "pendente"; // Status padrão
    }

    // Constructor completo com todos os campos
    public Denuncia(Integer id, String categoria, String descricao, Double latitude, Double longitude, String datetime, String userUUID, String status) {
        this.id = id;
        this.categoria = categoria;
        this.descricao = descricao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.datetime = datetime;
        this.userUUID = userUUID;
        this.status = status;
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
        return datetime;
    }

    public void setDateTime(String datetime) {
        this.datetime = datetime;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Denuncia{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' + // Use single quotes around string values for clarity
                ", categoria='" + categoria + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", dateTime='" + datetime + '\'' +
                ", userUUID='" + userUUID + '\'' +
                ", status='" + status + '\'' +
                '}'; // Include all fields
    }
}
