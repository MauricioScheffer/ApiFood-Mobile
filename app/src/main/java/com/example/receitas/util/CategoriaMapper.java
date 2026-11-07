package com.example.receitas.util;

import android.content.Context;

import com.example.receitas.R;

import java.util.Arrays;
import java.util.List;

/**
 * Converte as categorias da TheMealDB (em inglês) para as categorias do app,
 * que ficam no array "categorias" do strings.xml.
 */
public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    /** Lista de categorias do app (sem o item "Selecione..."). */
    public static List<String> categoriasDoApp(Context ctx) {
        return Arrays.asList(ctx.getResources().getStringArray(R.array.categorias));
    }

    /** Recebe a categoria da API (ex.: "Seafood") e devolve a equivalente do app. */
    public static String daApi(Context ctx, String categoriaApi) {
        String[] cat = ctx.getResources().getStringArray(R.array.categorias);
        // Índices do array R.array.categorias (veja strings.xml)
        if (categoriaApi == null) return cat[9];
        switch (categoriaApi) {
            case "Beef":
            case "Lamb":
            case "Pork":
            case "Goat":
                return cat[0]; // Carnes
            case "Chicken":
                return cat[1]; // Aves
            case "Seafood":
                return cat[2]; // Peixes e frutos do mar
            case "Pasta":
                return cat[3]; // Massas
            case "Dessert":
                return cat[4]; // Sobremesas
            case "Breakfast":
                return cat[5]; // Café da manhã
            case "Starter":
                return cat[6]; // Entradas
            case "Vegetarian":
            case "Vegan":
                return cat[7]; // Vegetariana
            case "Side":
                return cat[8]; // Acompanhamentos
            default:
                return cat[9]; // Outros
        }
    }
}
