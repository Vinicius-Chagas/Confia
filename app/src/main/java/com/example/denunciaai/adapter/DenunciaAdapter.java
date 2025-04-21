package com.example.denunciaai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.denunciaai.R;
import com.example.denunciaai.model.Denuncia;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DenunciaAdapter extends RecyclerView.Adapter<DenunciaAdapter.DenunciaViewHolder> {
    
    private List<Denuncia> denuncias;
    private OnDenunciaClickListener listener;
    
    public DenunciaAdapter(List<Denuncia> denuncias, OnDenunciaClickListener listener) {
        this.denuncias = denuncias;
        this.listener = listener;
    }
    
    public void updateDenuncias(List<Denuncia> newDenuncias) {
        this.denuncias = newDenuncias;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public DenunciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_denuncia, parent, false);
        return new DenunciaViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DenunciaViewHolder holder, int position) {
        Denuncia denuncia = denuncias.get(position);
        holder.bind(denuncia);
    }
    
    @Override
    public int getItemCount() {
        return denuncias.size();
    }
    
    class DenunciaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoria;
        private TextView tvDateTime;

        DenunciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDenunciaClick(denuncias.get(position));
                }
            });
        }
        
        void bind(Denuncia denuncia) {
            tvCategoria.setText(denuncia.getCategoria());
            
            // Format date
            String formattedDate = formatDateForDisplay(denuncia.getDateTime());
            tvDateTime.setText(formattedDate);

        }
        
        private String formatDateForDisplay(String isoDateTime) {
            try {
                if (isoDateTime == null) {
                    System.out.println("Received null isoDateTime for formatting."); // Log the null input
                    // You can return a default string, like empty string or a placeholder
                    return ""; // Or return "Data indisponível" or similar
                }
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoFormat.parse(isoDateTime);
                
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
                return (date != null) ? displayFormat.format(date) : isoDateTime;
            } catch (ParseException e) {
                return isoDateTime;
            }
        }
    }
    
    public interface OnDenunciaClickListener {
        void onDenunciaClick(Denuncia denuncia);
    }
}
