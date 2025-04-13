package com.example.denunciaai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.denunciaai.api.ApiClient;
import com.example.denunciaai.api.ApiService;
import com.example.denunciaai.model.LoginRequest;
import com.example.denunciaai.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLoginPolicial extends AppCompatActivity {
    
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView btnBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_policial);
        
        initViews();
        setupListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                // loginPolice();
                Intent intent = new Intent(ActivityLoginPolicial.this, ActivityListaDenuncias.class);
                startActivity(intent);
                finish(); 
            }
        });
    }
    
    private boolean validateInputs() {
        boolean isValid = true;
        
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Digite seu nome de usuário");
            etUsername.setBackgroundResource(R.drawable.input_error);
            isValid = false;
        } else {
            etUsername.setBackgroundResource(R.drawable.input);
        }
        
        String password = etPassword.getText().toString().trim();
        if (password.isEmpty()) {
            etPassword.setError("Digite sua senha");
            etPassword.setBackgroundResource(R.drawable.input_error);
            isValid = false;
        } else {
            etPassword.setBackgroundResource(R.drawable.input);
        }
        
        return isValid;
    }
    
    private void loginPolice() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        LoginRequest loginRequest = new LoginRequest(username, password);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<LoginResponse> call = apiService.loginPolice(loginRequest);
        
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    // Save auth token
                    saveAuthToken(response.body().getFormattedToken());
                    
                    // Navigate to lista denuncias activity
                    Intent intent = new Intent(ActivityLoginPolicial.this, ActivityListaDenuncias.class);
                    startActivity(intent);
                    finish(); // Close login activity
                } else {
                    int statusCode = response.code();
                    if (statusCode == 401) {
                        Toast.makeText(ActivityLoginPolicial.this, 
                                "Credenciais inválidas", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ActivityLoginPolicial.this, 
                                "Erro no login: " + statusCode, Toast.LENGTH_LONG).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                Toast.makeText(ActivityLoginPolicial.this, 
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void saveAuthToken(String token) {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("auth_token", token);
        editor.apply();
    }
}
