package com.example.receitas.data.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.receitas.model.Receita;

import java.util.ArrayList;
import java.util.List;

import static com.example.receitas.data.local.DatabaseHelper.*;

/** Os métodos são síncronos — quem chama (SqliteReceitaRepository) roda em background.  */
public class ReceitaDao {

    private static final String TAG = "ReceitaDao";

    private final DatabaseHelper helper;

    public ReceitaDao(DatabaseHelper helper) {
        this.helper = helper;
    }

    public List<Receita> listarTodas() {
        List<Receita> lista = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String ordem = COL_FAVORITA + " DESC, " + COL_NOME + " COLLATE NOCASE ASC";
        try (Cursor c = db.query(TABELA, null, null, null, null, null, ordem)) {
            while (c.moveToNext()) {
                lista.add(fromCursor(c));
            }
        }
        Log.d(TAG, "listarTodas: " + lista.size() + " registros");
        return lista;
    }

    public Receita buscarPorId(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.query(TABELA, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /** INSERT  */
    public long inserir(Receita r) {
        long id = helper.getWritableDatabase().insert(TABELA, null, toContentValues(r));
        Log.d(TAG, "inserir: id=" + id);
        return id;
    }

    /** UPDATE */
    public int atualizar(Receita r) {
        int linhas = helper.getWritableDatabase().update(TABELA, toContentValues(r),
                COL_ID + " = ?", new String[]{String.valueOf(r.getId())});
        Log.d(TAG, "atualizar id=" + r.getId() + ": " + linhas + " linha(s)");
        return linhas;
    }

    /** DELETE. */
    public int excluir(long id) {
        int linhas = helper.getWritableDatabase().delete(TABELA,
                COL_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "excluir id=" + id + ": " + linhas + " linha(s)");
        return linhas;
    }

    //  Conversões Receita <-> banco
    private ContentValues toContentValues(Receita r) {
        ContentValues v = new ContentValues();
        v.put(COL_NOME, r.getNome());
        v.put(COL_CATEGORIA, r.getCategoria());
        v.put(COL_ORIGEM, r.getOrigem());
        v.put(COL_TEMPO, r.getTempoPreparo());
        v.put(COL_PORCOES, r.getPorcoes());
        v.put(COL_DIFICULDADE, r.getDificuldade());
        v.put(COL_INGREDIENTES, r.getIngredientes());
        v.put(COL_MODO_PREPARO, r.getModoPreparo());
        v.put(COL_IMAGEM, r.getImagemUrl());
        v.put(COL_FAVORITA, r.isFavorita() ? 1 : 0);
        v.put(COL_DATA, r.getDataReceita());
        v.put(COL_ID_MEAL, r.getIdMealApi());
        return v;
    }

    private Receita fromCursor(Cursor c) {
        Receita r = new Receita();
        r.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        r.setNome(c.getString(c.getColumnIndexOrThrow(COL_NOME)));
        r.setCategoria(c.getString(c.getColumnIndexOrThrow(COL_CATEGORIA)));
        r.setOrigem(c.getString(c.getColumnIndexOrThrow(COL_ORIGEM)));
        r.setTempoPreparo(c.getInt(c.getColumnIndexOrThrow(COL_TEMPO)));
        r.setPorcoes(c.getInt(c.getColumnIndexOrThrow(COL_PORCOES)));
        r.setDificuldade(c.getString(c.getColumnIndexOrThrow(COL_DIFICULDADE)));
        r.setIngredientes(c.getString(c.getColumnIndexOrThrow(COL_INGREDIENTES)));
        r.setModoPreparo(c.getString(c.getColumnIndexOrThrow(COL_MODO_PREPARO)));
        r.setImagemUrl(c.getString(c.getColumnIndexOrThrow(COL_IMAGEM)));
        r.setFavorita(c.getInt(c.getColumnIndexOrThrow(COL_FAVORITA)) == 1);
        r.setDataReceita(c.getString(c.getColumnIndexOrThrow(COL_DATA)));
        r.setIdMealApi(c.getString(c.getColumnIndexOrThrow(COL_ID_MEAL)));
        return r;
    }
}
