package com.example.receitas.data;

import com.example.receitas.model.Receita;
import com.example.receitas.util.ResultCallback;

import java.util.List;

/**
 * Contrato do CRUD de receitas (requisito R6).
 SqliteReceitaRepository: banco local no aparelho (SQLite) — padrão;
 ApiReceitaRepository: banco MySQL acessado pelo backend PHP.
 */
public interface ReceitaRepository {

    /** READ (todos) */
    void listar(ResultCallback<List<Receita>> callback);

    /** READ (um) */
    void buscarPorId(long id, ResultCallback<Receita> callback);

    /** CREATE devolve o id gerado. */
    void inserir(Receita receita, ResultCallback<Long> callback);

    /** UPDATE */
    void atualizar(Receita receita, ResultCallback<Boolean> callback);

    /** DELETE */
    void excluir(long id, ResultCallback<Boolean> callback);
}
