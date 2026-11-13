package com.example.receitas.data.local;

import android.content.Context;

import com.example.receitas.R;
import com.example.receitas.data.ReceitaRepository;
import com.example.receitas.model.Receita;
import com.example.receitas.util.AppExecutors;
import com.example.receitas.util.ResultCallback;

import java.util.List;

/**
 * Implementação do CRUD usando o SQLite do celularzin.
 */
public class SqliteReceitaRepository implements ReceitaRepository {

    private final Context context;
    private final ReceitaDao dao;

    public SqliteReceitaRepository(Context context) {
        this.context = context.getApplicationContext();
        this.dao = new ReceitaDao(DatabaseHelper.getInstance(this.context));
    }

    @Override
    public void listar(ResultCallback<List<Receita>> callback) {
        AppExecutors.run(dao::listarTodas, callback, this::erroBanco);
    }

    @Override
    public void buscarPorId(long id, ResultCallback<Receita> callback) {
        AppExecutors.run(() -> {
            Receita r = dao.buscarPorId(id);
            if (r == null) {
                throw new IllegalStateException(context.getString(R.string.erro_receita_nao_encontrada));
            }
            return r;
        }, callback, this::erroBanco);
    }

    @Override
    public void inserir(Receita receita, ResultCallback<Long> callback) {
        AppExecutors.run(() -> {
            long id = dao.inserir(receita);
            if (id <= 0) {
                throw new IllegalStateException(context.getString(R.string.erro_banco_local));
            }
            return id;
        }, callback, this::erroBanco);
    }

    @Override
    public void atualizar(Receita receita, ResultCallback<Boolean> callback) {
        AppExecutors.run(() -> dao.atualizar(receita) == 1, callback, this::erroBanco);
    }

    @Override
    public void excluir(long id, ResultCallback<Boolean> callback) {
        AppExecutors.run(() -> dao.excluir(id) == 1, callback, this::erroBanco);
    }

    private String erroBanco(Exception e) {
        // IllegalStateException carrega uma mensagem já pronta para o usuário
        if (e instanceof IllegalStateException && e.getMessage() != null) {
            return e.getMessage();
        }
        return context.getString(R.string.erro_banco_local);
    }
}
