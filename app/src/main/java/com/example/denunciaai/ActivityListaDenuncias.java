package com.example.denunciaai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button btnVoltar;
    
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
        btnVoltar = findViewById(R.id.btnVoltar);
    }
    
    private void setupRecyclerView() {
        adapter = new DenunciaAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnVoltar.setOnClickListener(v -> {
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
        try {
            /*
            // --- MOCK DATA START ---
            progressBar.setVisibility(View.VISIBLE);
            tvEmptyList.setVisibility(View.GONE);

            // Simulate network delay (optional)
            new android.os.Handler().postDelayed(() -> {
                try {
                    List<Denuncia> mockDenuncias = new ArrayList<>();
                    mockDenuncias.add(new Denuncia(1, "Atividade suspeita", "Descrição da atividade suspeita na Rua Amazonas.", -23.5505, -46.6333, "2023-10-27T23:45:00Z"));
                    mockDenuncias.add(new Denuncia(2, "Perturbação do sossego", "Festa barulhenta na Av. Paulista.", -23.5616, -46.6559, "2023-10-28T02:15:00Z"));
                    mockDenuncias.add(new Denuncia(3, "Vandalismo", "Pichação na Praça da Liberdade.", -23.5600, -46.6360, "2023-10-28T15:20:00Z"));
                    mockDenuncias.add(new Denuncia(4, "Roubo", "Relato de roubo na Rua Oliveira.", -23.5700, -46.6400, "2023-10-28T19:10:00Z"));
                    mockDenuncias.add(new Denuncia(5, "Assalto", "Assalto a mão armada ocorrido perto do metrô.", -23.5558, -46.6396, "2023-10-28T21:05:00Z"));

                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    adapter.updateDenuncias(mockDenuncias);

                    if (mockDenuncias.isEmpty()) {
                        tvEmptyList.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyList.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    handleError(e);
                }
            }, 1500); // 1.5 second delay

            // --- MOCK DATA END ---
            */
            // --- ORIGINAL API CALL (Commented out) ---
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
                    try {
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
                    } catch (Exception e) {
                        handleError(e);
                    }
                }

                @Override
                public void onFailure(Call<List<Denuncia>> call, Throwable t) {
                    handleError(t);
                }
            });
             // --- END OF ORIGINAL API CALL ---
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Throwable t) {
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        tvEmptyList.setVisibility(View.VISIBLE);

        String errorMessage = t.getMessage() != null ? t.getMessage() : "Erro desconhecido";
        Toast.makeText(this, "Erro ao carregar denúncias: " + errorMessage, Toast.LENGTH_LONG).show();

        // Log the error for debugging purposes
        t.printStackTrace();
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
        // Pass the entire Denuncia object
        intent.putExtra(DenunciaActivity.EXTRA_DENUNCIA_OBJECT, denuncia); 
        startActivity(intent);
    }
}
