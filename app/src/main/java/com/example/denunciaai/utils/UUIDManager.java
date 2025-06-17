package com.example.denunciaai.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public class UUIDManager {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_UUID = "user_uuid";
    
    private static UUIDManager instance;
    private SharedPreferences preferences;
    
    private UUIDManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized UUIDManager getInstance(Context context) {
        if (instance == null) {
            instance = new UUIDManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Obtém o UUID do usuário. Se não existir, cria um novo.
     * @return UUID do usuário
     */
    public String getUserUUID() {
        String uuid = preferences.getString(KEY_UUID, null);
        if (uuid == null) {
            // Gera um novo UUID se não existir
            uuid = UUID.randomUUID().toString();
            saveUserUUID(uuid);
        }
        return uuid;
    }
    
    /**
     * Salva o UUID do usuário
     * @param uuid UUID para salvar
     */
    private void saveUserUUID(String uuid) {
        preferences.edit()
                .putString(KEY_UUID, uuid)
                .apply();
    }
    
    /**
     * Remove o UUID (útil para testes ou reset)
     */
    public void clearUserUUID() {
        preferences.edit()
                .remove(KEY_UUID)
                .apply();
    }
    
    /**
     * Verifica se já existe um UUID salvo
     * @return true se existe UUID, false caso contrário
     */
    public boolean hasUserUUID() {
        return preferences.getString(KEY_UUID, null) != null;
    }
} 