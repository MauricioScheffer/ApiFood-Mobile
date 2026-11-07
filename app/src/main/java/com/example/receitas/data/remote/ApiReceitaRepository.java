package com.example.receitas.data.remote;

import android.content.Context;

import com.example.receitas.data.ReceitaRepository;
import com.example.receitas.model.Receita;
import com.example.receitas.network.HttpClient;
import com.example.receitas.util.AppExecutors;
import com.example.receitas.util.NetworkUtils;
import com.example.receitas.util.ResultCallback;

import org.json.JSONObject;

import java.util.List;

/**
 * Implementação do CRUD usando o banco MySQL.
 *
 * O Android NÃO se conecta direto ao MySQL (não há driver seguro/suportado para isso).
 * Por isso o app conversa com um backend PHP (pasta backend-php/) que acessa o MySQL:
 *
 *   App Android  --HTTP/JSON-->  receitas.php  --PDO-->  MySQL
 *
 * Endpoints:
 *   GET    receitas.php          lista todas
 *   GET    receitas.php?id=1     busca uma
 *   POST   receitas.php          cria (corpo JSON) -> {"id": 10}
 *   PUT    receitas.php?id=1     atualiza (corpo JSON)
 *   DELETE receitas.php?id=1     exclui
 */
public class ApiReceitaRepository implements ReceitaRepository {

    private final Context context;
    private final String endpoint;

    public ApiReceitaRepository(Context context, String urlServidor) {
        this.context = context.getApplicationContext();
        String base = urlServidor.endsWith("/") ? urlServidor : urlServidor + "/";
        this.endpoint = base + "receitas.php";
    }

    @Override
    public void listar(ResultCallback<List<Receita>> callback) {
        AppExecutors.run(() -> ReceitaJsonMapper.fromJsonArray(HttpClient.get(endpoint)),
                callback, this::erro);
    }

    @Override
    public void buscarPorId(long id, ResultCallback<Receita> callback) {
        AppExecutors.run(() -> ReceitaJsonMapper.fromJson(new JSONObject(HttpClient.get(endpoint + "?id=" + id))),
                callback, this::erro);
    }

    @Override
    public void inserir(Receita receita, ResultCallback<Long> callback) {
        AppExecutors.run(() -> {
            String resposta = HttpClient.post(endpoint, ReceitaJsonMapper.toJson(receita).toString());
            return new JSONObject(resposta).getLong("id");
        }, callback, this::erro);
    }

    @Override
    public void atualizar(Receita receita, ResultCallback<Boolean> callback) {
        AppExecutors.run(() -> {
            HttpClient.put(endpoint + "?id=" + receita.getId(), ReceitaJsonMapper.toJson(receita).toString());
            return true;
        }, callback, this::erro);
    }

    @Override
    public void excluir(long id, ResultCallback<Boolean> callback) {
        AppExecutors.run(() -> {
            HttpClient.delete(endpoint + "?id=" + id);
            return true;
        }, callback, this::erro);
    }

    private String erro(Exception e) {
        return NetworkUtils.mensagemDeErro(context, e);
    }
}
