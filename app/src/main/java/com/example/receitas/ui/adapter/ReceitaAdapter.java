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
import com.example.receitas.model.Receita;
import com.example.receitas.util.ReceitaFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter customizado do RecyclerView de receitas salvas (requisito R5).
 * Cada item mostra imagem, nome, categoria, detalhes e a estrela de favorita.
 */
public class ReceitaAdapter extends RecyclerView.Adapter<ReceitaAdapter.ViewHolder> {

    /** Eventos de clique repassados para a Activity. */
    public interface Listener {
        void onClick(Receita receita);

        void onLongClick(Receita receita);

        void onFavoritaClick(Receita receita);
    }

    private final List<Receita> itens = new ArrayList<>();
    private final Listener listener;

    public ReceitaAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Substitui a lista exibida. */
    public void setItens(List<Receita> novosItens) {
        itens.clear();
        itens.addAll(novosItens);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receita, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receita r = itens.get(position);

        holder.tvNome.setText(r.getNome());
        holder.tvCategoria.setText(ReceitaFormatter.categoriaEOrigem(holder.itemView.getContext(), r));
        holder.tvDetalhes.setText(ReceitaFormatter.resumo(holder.itemView.getContext(), r));
        holder.ivFavorita.setImageResource(r.isFavorita() ? R.drawable.ic_star : R.drawable.ic_star_border);

        Glide.with(holder.ivImagem)
                .load(r.getImagemUrl())
                .placeholder(R.drawable.ic_receita_placeholder)
                .error(R.drawable.ic_receita_placeholder)
                .centerCrop()
                .into(holder.ivImagem);

        holder.itemView.setOnClickListener(v -> listener.onClick(r));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(r);
            return true;
        });
        holder.ivFavorita.setOnClickListener(v -> listener.onFavoritaClick(r));
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    /** Guarda as referências das views de um item (evita findViewById repetido). */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivImagem;
        final TextView tvNome;
        final TextView tvCategoria;
        final TextView tvDetalhes;
        final ImageView ivFavorita;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagem = itemView.findViewById(R.id.ivImagem);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvDetalhes = itemView.findViewById(R.id.tvDetalhes);
            ivFavorita = itemView.findViewById(R.id.ivFavorita);
        }
    }
}
