package com.example.receitas.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.receitas.R;
import com.example.receitas.model.Meal;

import java.util.ArrayList;
import java.util.List;

/** Adapter da grade de receitas vindas da API TheMealDB (imagem + nome). */
public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    public interface Listener {
        void onClick(Meal meal);
    }

    private final List<Meal> itens = new ArrayList<>();
    private final Listener listener;

    public MealAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItens(List<Meal> novosItens) {
        itens.clear();
        itens.addAll(novosItens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = itens.get(position);
        holder.tvNome.setText(meal.getNome());
        // "/preview" devolve uma miniatura menor (mais rápida) da TheMealDB
        String url = meal.getImagemUrl().isEmpty() ? null : meal.getImagemUrl() + "/preview";
        Glide.with(holder.ivMeal)
                .load(url)
                .placeholder(R.drawable.ic_receita_placeholder)
                .error(R.drawable.ic_receita_placeholder)
                .centerCrop()
                .into(holder.ivMeal);
        holder.itemView.setOnClickListener(v -> listener.onClick(meal));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivMeal;
        final TextView tvNome;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMeal = itemView.findViewById(R.id.ivMeal);
            tvNome = itemView.findViewById(R.id.tvNomeMeal);
        }
    }
}
