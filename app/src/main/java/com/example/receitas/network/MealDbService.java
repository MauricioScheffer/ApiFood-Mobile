package com.example.receitas.network;

import android.content.Context;
import android.util.Log;

import com.example.receitas.R;
import com.example.receitas.model.Meal;
import com.example.receitas.util.AppExecutors;
import com.example.receitas.util.NetworkUtils;
import com.example.receitas.util.ResultCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Consumo da API REST pública TheMealDB (https://www.themealdb.com/api.php) — requisito R7.
 * Todas as chamadas rodam em background e devolvem o resultado na thread principal.
 */
public class MealDbService {

    private static final String TAG = "MealDbService";
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";

    private final Context context;

    public MealDbService(Context context) {
        this.context = context.getApplicationContext();
    }

    /** search.php?s=termo — busca receitas pelo nome. */
    public void buscarPorNome(String termo, ResultCallback<List<Meal>> callback) {
        AppExecutors.run(() -> {
            String url = BASE_URL + "search.php?s=" + URLEncoder.encode(termo, "UTF-8");
            return parseListaMeals(HttpClient.get(url));
        }, callback, this::mensagemErro);
    }

    /** filter.php?c=categoria — lista receitas de uma categoria (só id, nome e imagem). */
    public void filtrarPorCategoria(String categoria, ResultCallback<List<Meal>> callback) {
        AppExecutors.run(() -> {
            String url = BASE_URL + "filter.php?c=" + URLEncoder.encode(categoria, "UTF-8");
            return parseListaMeals(HttpClient.get(url));
        }, callback, this::mensagemErro);
    }

    /** list.php?c=list — nomes de todas as categorias da API. */
    public void listarCategorias(ResultCallback<List<String>> callback) {
        AppExecutors.run(() -> {
            JSONArray meals = arrayMeals(HttpClient.get(BASE_URL + "list.php?c=list"));
            List<String> categorias = new ArrayList<>();
            for (int i = 0; i < meals.length(); i++) {
                categorias.add(meals.getJSONObject(i).getString("strCategory"));
            }
            return categorias;
        }, callback, this::mensagemErro);
    }

    /** lookup.php?i=id — detalhes completos de uma receita. */
    public void detalhes(String idMeal, ResultCallback<Meal> callback) {
        AppExecutors.run(() -> {
            String url = BASE_URL + "lookup.php?i=" + URLEncoder.encode(idMeal, "UTF-8");
            return primeiraOuErro(parseListaMeals(HttpClient.get(url)));
        }, callback, this::mensagemErro);
    }

    /** random.php — uma receita aleatória (usada como "Receita do dia" na Home). */
    public void aleatoria(ResultCallback<Meal> callback) {
        AppExecutors.run(() -> primeiraOuErro(parseListaMeals(HttpClient.get(BASE_URL + "random.php"))),
                callback, this::mensagemErro);
    }

    // ------------------------------------------------------------------
    // Conversão JSON -> objetos Java
    // ------------------------------------------------------------------

    /** A API sempre devolve {"meals": [...]} ou {"meals": null} quando não encontra nada. */
    private JSONArray arrayMeals(String json) throws JSONException {
        JSONObject raiz = new JSONObject(json);
        if (!raiz.has("meals")) {
            throw new JSONException("Resposta sem o campo 'meals'");
        }
        if (raiz.isNull("meals")) {
            return new JSONArray();
        }
        return raiz.getJSONArray("meals");
    }

    private List<Meal> parseListaMeals(String json) throws JSONException {
        JSONArray array = arrayMeals(json);
        List<Meal> lista = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            lista.add(parseMeal(array.getJSONObject(i)));
        }
        Log.d(TAG, "Receitas recebidas da API: " + lista.size());
        return lista;
    }

    private Meal parseMeal(JSONObject obj) {
        Meal meal = new Meal();
        meal.setId(texto(obj, "idMeal"));
        meal.setNome(texto(obj, "strMeal"));
        meal.setCategoria(texto(obj, "strCategory"));
        meal.setArea(texto(obj, "strArea"));
        meal.setInstrucoes(texto(obj, "strInstructions"));
        meal.setImagemUrl(texto(obj, "strMealThumb"));
        meal.setYoutubeUrl(texto(obj, "strYoutube"));

        // A API tem 20 pares de campos: strIngredient1..20 e strMeasure1..20
        for (int i = 1; i <= 20; i++) {
            String ingrediente = texto(obj, "strIngredient" + i);
            if (ingrediente.isEmpty()) {
                continue;
            }
            String medida = texto(obj, "strMeasure" + i);
            meal.getIngredientes().add(medida.isEmpty() ? ingrediente : medida + " - " + ingrediente);
        }
        return meal;
    }

    /** Lê um campo de texto tratando ausência e null como "". */
    private static String texto(JSONObject obj, String campo) {
        if (!obj.has(campo) || obj.isNull(campo)) {
            return "";
        }
        return obj.optString(campo, "").trim();
    }

    private Meal primeiraOuErro(List<Meal> lista) throws IOException {
        if (lista.isEmpty()) {
            throw new HttpException(404, context.getString(R.string.erro_receita_nao_encontrada));
        }
        return lista.get(0);
    }

    private String mensagemErro(Exception e) {
        if (!NetworkUtils.isConectado(context)) {
            return context.getString(R.string.erro_sem_internet);
        }
        return NetworkUtils.mensagemDeErro(context, e);
    }
}
