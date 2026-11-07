package com.example.receitas.util;

import android.content.Context;

import com.example.receitas.R;
import com.example.receitas.model.Receita;

/** Textos de exibição de uma Receita, reaproveitados pela lista e pelos detalhes. */
public final class ReceitaFormatter {

    private ReceitaFormatter() {
    }

    /** Converte o código salvo no banco ("FACIL"...) no texto do strings.xml. */
    public static String dificuldade(Context ctx, String codigo) {
        if (Receita.DIFICULDADE_DIFICIL.equals(codigo)) {
            return ctx.getString(R.string.dificuldade_dificil);
        }
        if (Receita.DIFICULDADE_MEDIO.equals(codigo)) {
            return ctx.getString(R.string.dificuldade_medio);
        }
        return ctx.getString(R.string.dificuldade_facil);
    }

    /** Ex.: "30 min • 4 porção(ões) • Fácil" */
    public static String resumo(Context ctx, Receita r) {
        return ctx.getString(R.string.item_detalhes,
                r.getTempoPreparo(), r.getPorcoes(), dificuldade(ctx, r.getDificuldade()));
    }

    /** Ex.: "Sobremesas • Brasil" (ou só a categoria se não houver origem). */
    public static String categoriaEOrigem(Context ctx, Receita r) {
        if (r.getOrigem() == null || r.getOrigem().trim().isEmpty()) {
            return r.getCategoria();
        }
        return ctx.getString(R.string.detalhe_categoria_origem, r.getCategoria(), r.getOrigem());
    }
}
