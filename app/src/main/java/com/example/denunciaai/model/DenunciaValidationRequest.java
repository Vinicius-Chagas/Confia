package com.example.denunciaai.model;

import com.google.gson.annotations.SerializedName;

public class DenunciaValidationRequest {
    @SerializedName("denunciaId")
    private Integer denunciaId;

    @SerializedName("action")
    private String action; // "validar" ou "rejeitar"

    @SerializedName("observacoes")
    private String observacoes; // Observações opcionais do policial

    public DenunciaValidationRequest(Integer denunciaId, String action) {
        this.denunciaId = denunciaId;
        this.action = action;
    }

    public DenunciaValidationRequest(Integer denunciaId, String action, String observacoes) {
        this.denunciaId = denunciaId;
        this.action = action;
        this.observacoes = observacoes;
    }

    public Integer getDenunciaId() {
        return denunciaId;
    }

    public void setDenunciaId(Integer denunciaId) {
        this.denunciaId = denunciaId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
} 