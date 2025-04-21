package com.example.denunciaai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.denunciaai.api.ApiClient;
import com.example.denunciaai.api.ApiService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnDenuncia = findViewById(R.id.btnDenuncia);
        Button btnAcessoPolicial = findViewById(R.id.btnAcessoPolicial);

        btnDenuncia.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DenunciaActivity.class);
            startActivity(intent);
        });

        btnAcessoPolicial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActivityLoginPolicial.class);
            startActivity(intent);
        });
    }
}
