package com.example.receitas.util;

import android.util.LruCache;

import com.example.receitas.model.Meal;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduz do inglês para o português (PT-BR) o conteúdo que vem da TheMealDB
 * (nome, ingredientes e instruções), usando o ML Kit Translate.
 *
 * A tradução roda NO APARELHO: o modelo (~30 MB) é baixado uma vez e depois funciona offline.
 * Se algo falhar (sem internet na primeira vez, modelo indisponível...), quem chamou
 * recebe onError e continua mostrando o texto original em inglês.
 * Os callbacks são chamados na thread principal, como no restante do app.
 */
public final class TraducaoService {

    private static final int TAMANHO_CACHE = 500;

    private static final LruCache<String, String> CACHE = new LruCache<>(TAMANHO_CACHE);
    private static Translator translator;
    private static boolean modeloPronto = false;

    private TraducaoService() {
    }

    private static synchronized Translator getTranslator() {
        if (translator == null) {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.PORTUGUESE)
                    .build();
            translator = Translation.getClient(options);
        }
        return translator;
    }

    /** Garante que o modelo está no aparelho (baixa na primeira vez). */
    private static void garantirModelo(Runnable aoTerminar, ResultCallback<?> aoFalhar) {
        if (modeloPronto) {
            aoTerminar.run();
            return;
        }
        getTranslator().downloadModelIfNeeded(new DownloadConditions.Builder().build())
                .addOnSuccessListener(v -> {
                    modeloPronto = true;
                    aoTerminar.run();
                })
                .addOnFailureListener(e -> aoFalhar.onError("Modelo de tradução indisponível"));
    }

    /** Traduz uma lista de textos mantendo a ordem. Textos vazios são mantidos como estão. */
    public static void traduzirLista(List<String> textos, ResultCallback<List<String>> callback) {
        garantirModelo(() -> traduzirItem(textos, 0, new ArrayList<>(), callback), callback);
    }

    private static void traduzirItem(List<String> entrada, int indice, List<String> saida,
                                     ResultCallback<List<String>> callback) {
        if (indice == entrada.size()) {
            callback.onSuccess(saida);
            return;
        }
        String original = entrada.get(indice);
        if (original == null || original.trim().isEmpty()) {
            saida.add(original == null ? "" : original);
            traduzirItem(entrada, indice + 1, saida, callback);
            return;
        }
        String emCache = CACHE.get(original);
        if (emCache != null) {
            saida.add(emCache);
            traduzirItem(entrada, indice + 1, saida, callback);
            return;
        }
        getTranslator().translate(original)
                .addOnSuccessListener(traduzido -> {
                    CACHE.put(original, traduzido);
                    saida.add(traduzido);
                    traduzirItem(entrada, indice + 1, saida, callback);
                })
                .addOnFailureListener(e -> callback.onError("Falha ao traduzir"));
    }

    /**
     * Traduz nome, ingredientes e instruções da receita e grava o resultado no próprio objeto.
     * As instruções são traduzidas linha a linha (parágrafos), o que evita textos muito longos.
     */
    public static void traduzirMeal(Meal meal, ResultCallback<Meal> callback) {
        List<String> entrada = new ArrayList<>();
        entrada.add(meal.getNome());
        entrada.addAll(meal.getIngredientes());
        String[] linhas = meal.getInstrucoes().split("\\r?\\n", -1);
        for (String linha : linhas) {
            entrada.add(linha);
        }
        int qtdIngredientes = meal.getIngredientes().size();

        traduzirLista(entrada, new ResultCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> saida) {
                meal.setNome(saida.get(0));
                List<String> ingredientes = new ArrayList<>(saida.subList(1, 1 + qtdIngredientes));
                meal.getIngredientes().clear();
                meal.getIngredientes().addAll(ingredientes);
                StringBuilder sb = new StringBuilder();
                for (int i = 1 + qtdIngredientes; i < saida.size(); i++) {
                    if (i > 1 + qtdIngredientes) sb.append('\n');
                    sb.append(saida.get(i));
                }
                meal.setInstrucoes(sb.toString());
                callback.onSuccess(meal);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /** Traduz só o nome de cada receita (usado nas listas e na receita do dia). */
    public static void traduzirNomes(List<Meal> meals, ResultCallback<List<Meal>> callback) {
        List<String> nomes = new ArrayList<>();
        for (Meal m : meals) {
            nomes.add(m.getNome());
        }
        traduzirLista(nomes, new ResultCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> saida) {
                for (int i = 0; i < meals.size(); i++) {
                    meals.get(i).setNome(saida.get(i));
                }
                callback.onSuccess(meals);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }
}
