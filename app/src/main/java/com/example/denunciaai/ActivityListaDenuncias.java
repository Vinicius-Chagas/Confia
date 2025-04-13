package com.example.denunciaai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.denunciaai.adapter.DenunciaAdapter;
import com.example.denunciaai.api.ApiClient;
import com.example.denunciaai.api.ApiService;
import com.example.denunciaai.model.Denuncia;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityListaDenuncias extends AppCompatActivity implements DenunciaAdapter.OnDenunciaClickListener {

    private RecyclerView recyclerView;
    private DenunciaAdapter adapter;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmptyList;
    private TextView btnBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_denuncias);
        
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Load denuncias initially
        loadDenuncias();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning to this activity
        loadDenuncias();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewDenuncias);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmptyList = findViewById(R.id.tvEmptyList);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void setupRecyclerView() {
        adapter = new DenunciaAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            // Return to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        swipeRefresh.setOnRefreshListener(() -> {
            loadDenuncias();
        });
    }
    
    private void loadDenuncias() {
        String authToken = getAuthToken();
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Não autorizado, faça login novamente", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyList.setVisibility(View.GONE);
        
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Denuncia>> call = apiService.getAllDenuncias(authToken);
        
        call.enqueue(new Callback<List<Denuncia>>() {
            @Override
            public void onResponse(Call<List<Denuncia>> call, Response<List<Denuncia>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Denuncia> denuncias = response.body();
                    adapter.updateDenuncias(denuncias);
                    
                    if (denuncias.isEmpty()) {
                        tvEmptyList.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyList.setVisibility(View.GONE);
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(ActivityListaDenuncias.this, 
                                "Sessão expirada, faça login novamente", Toast.LENGTH_LONG).show();
                        redirectToLogin();
                    } else {
                        Toast.makeText(ActivityListaDenuncias.this, 
                                "Erro ao carregar denúncias: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<List<Denuncia>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ActivityListaDenuncias.this, 
                        "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private String getAuthToken() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        return prefs.getString("auth_token", null);
    }
    
    private void redirectToLogin() {
        // Clear auth token
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        prefs.edit().remove("auth_token").apply();
        
        // Navigate to login
        Intent intent = new Intent(this, ActivityLoginPolicial.class);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onDenunciaClick(Denuncia denuncia) {
        Intent intent = new Intent(this, DenunciaActivity.class);
        intent.putExtra(DenunciaActivity.EXTRA_IS_VIEW_MODE, true);
        intent.putExtra(DenunciaActivity.EXTRA_DENUNCIA_ID, denuncia.getId());
        startActivity(intent);
    }
}
