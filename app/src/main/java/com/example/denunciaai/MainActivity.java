package com.example.denunciaai;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.denunciaai.R;

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
            Toast.makeText(this, "Funcionalidade Acesso Policial não implementada", Toast.LENGTH_SHORT).show();
        });
    }
}
