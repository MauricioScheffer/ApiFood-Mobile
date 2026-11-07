package com.example.receitas.data;

import com.example.receitas.model.Receita;
import com.example.receitas.util.ResultCallback;

import java.util.List;

/**
 * Contrato do CRUD de receitas (requisito R6).
 *
 * As telas só conhecem esta interface. Existem duas implementações:
 *  - SqliteReceitaRepository: banco local no aparelho (SQLite) — padrão;
 *  - ApiReceitaRepository: banco MySQL acessado pelo backend PHP.
 * A escolha é feita em Configurações e resolvida pelo RepositoryProvider.
 * Assim, trocar de banco nao exige mudar nenhuma Activity.
 */
public interface ReceitaRepository {

    /** READ (todos) */
    void listar(ResultCallback<List<Receita>> callback);

    /** READ (um) */
    void buscarPorId(long id, ResultCallback<Receita> callback);

    /** CREATE — devolve o id gerado. */
    void inserir(Receita receita, ResultCallback<Long> callback);

    /** UPDATE */
    void atualizar(Receita receita, ResultCallback<Boolean> callback);

    /** DELETE */
    void excluir(long id, ResultCallback<Boolean> callback);
}
