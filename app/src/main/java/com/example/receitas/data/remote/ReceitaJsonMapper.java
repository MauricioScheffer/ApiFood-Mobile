package com.example.receitas.data.remote;

import com.example.receitas.model.Receita;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Converte Receita <-> JSON no formato do backend PHP.
 * As chaves do JSON são iguais às colunas da tabela MySQL.
 */
public final class ReceitaJsonMapper {

    private ReceitaJsonMapper() {
    }

    public static JSONObject toJson(Receita r) throws JSONException {
        JSONObject o = new JSONObject();
        if (!r.isNova()) {
            o.put("id", r.getId());
        }
        o.put("nome", r.getNome());
        o.put("categoria", r.getCategoria());
        o.put("origem", textoOuNull(r.getOrigem()));
        o.put("tempo_preparo", r.getTempoPreparo());
        o.put("porcoes", r.getPorcoes());
        o.put("dificuldade", r.getDificuldade());
        o.put("ingredientes", r.getIngredientes());
        o.put("modo_preparo", r.getModoPreparo());
        o.put("imagem_url", textoOuNull(r.getImagemUrl()));
        o.put("favorita", r.isFavorita() ? 1 : 0);
        o.put("data_receita", r.getDataReceita());
        o.put("id_meal_api", textoOuNull(r.getIdMealApi()));
        return o;
    }

    public static Receita fromJson(JSONObject o) throws JSONException {
        Receita r = new Receita();
        r.setId(o.getLong("id"));
        r.setNome(o.getString("nome"));
        r.setCategoria(o.getString("categoria"));
        r.setOrigem(texto(o, "origem"));
        r.setTempoPreparo(o.getInt("tempo_preparo"));
        r.setPorcoes(o.getInt("porcoes"));
        r.setDificuldade(o.getString("dificuldade"));
        r.setIngredientes(o.getString("ingredientes"));
        r.setModoPreparo(o.getString("modo_preparo"));
        r.setImagemUrl(texto(o, "imagem_url"));
        // O PHP pode devolver 1/0, "1"/"0" ou true/false
        String fav = o.optString("favorita", "0");
        r.setFavorita("1".equals(fav) || "true".equalsIgnoreCase(fav));
        r.setDataReceita(texto(o, "data_receita"));
        r.setIdMealApi(texto(o, "id_meal_api"));
        return r;
    }

    public static List<Receita> fromJsonArray(String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        List<Receita> lista = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            lista.add(fromJson(array.getJSONObject(i)));
        }
        return lista;
    }

    private static String texto(JSONObject o, String campo) {
        return o.isNull(campo) ? null : o.optString(campo, null);
    }

    private static Object textoOuNull(String valor) {
        return (valor == null || valor.trim().isEmpty()) ? JSONObject.NULL : valor;
    }
}
