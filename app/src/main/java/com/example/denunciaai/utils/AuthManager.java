package com.example.denunciaai.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    
    private static AuthManager instance;
    private SharedPreferences preferences;
    
    private AuthManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Obtém o token de autorização
     * @return Token de autorização ou null se não existir
     */
    public String getAuthToken() {
        return preferences.getString(KEY_AUTH_TOKEN, null);
    }
    
    /**
     * Salva o token de autorização
     * @param token Token para salvar
     */
    public void saveAuthToken(String token) {
        preferences.edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply();
    }
    
    /**
     * Remove o token de autorização
     */
    public void clearAuthToken() {
        preferences.edit()
                .remove(KEY_AUTH_TOKEN)
                .apply();
    }
    
    /**
     * Verifica se existe um token de autorização
     * @return true se existe token, false caso contrário
     */
    public boolean hasAuthToken() {
        return preferences.getString(KEY_AUTH_TOKEN, null) != null;
    }
    
    /**
     * Obtém o token formatado para usar no header Authorization
     * @return Token formatado como "Bearer token" ou null se não existir
     */
    public String getFormattedAuthToken() {
        String token = getAuthToken();
        return token != null ? "Bearer " + token : null;
    }
} 