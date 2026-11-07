package com.example.receitas.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.receitas.util.DateUtils;

/**
 * Cria e versiona o banco SQLite local "receitas.db".
 * Os nomes das colunas são os mesmos da tabela MySQL (database/schema.sql).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DB_NOME = "receitas.db";
    // Ao mudar a estrutura da tabela, aumente a versão (o onUpgrade será chamado)
    public static final int DB_VERSAO = 1;

    public static final String TABELA = "receitas";
    public static final String COL_ID = "id";
    public static final String COL_NOME = "nome";
    public static final String COL_CATEGORIA = "categoria";
    public static final String COL_ORIGEM = "origem";
    public static final String COL_TEMPO = "tempo_preparo";
    public static final String COL_PORCOES = "porcoes";
    public static final String COL_DIFICULDADE = "dificuldade";
    public static final String COL_INGREDIENTES = "ingredientes";
    public static final String COL_MODO_PREPARO = "modo_preparo";
    public static final String COL_IMAGEM = "imagem_url";
    public static final String COL_FAVORITA = "favorita";
    public static final String COL_DATA = "data_receita";
    public static final String COL_ID_MEAL = "id_meal_api";

    private static final String SQL_CRIAR_TABELA =
            "CREATE TABLE " + TABELA + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NOME + " TEXT NOT NULL, "
                    + COL_CATEGORIA + " TEXT NOT NULL, "
                    + COL_ORIGEM + " TEXT, "
                    + COL_TEMPO + " INTEGER NOT NULL, "
                    + COL_PORCOES + " INTEGER NOT NULL, "
                    + COL_DIFICULDADE + " TEXT NOT NULL, "
                    + COL_INGREDIENTES + " TEXT NOT NULL, "
                    + COL_MODO_PREPARO + " TEXT NOT NULL, "
                    + COL_IMAGEM + " TEXT, "
                    + COL_FAVORITA + " INTEGER NOT NULL DEFAULT 0, "
                    + COL_DATA + " TEXT NOT NULL, "
                    + COL_ID_MEAL + " TEXT)";

    // Singleton: uma única instância do helper para o app inteiro
    private static DatabaseHelper instancia;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instancia == null) {
            instancia = new DatabaseHelper(context.getApplicationContext());
        }
        return instancia;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NOME, null, DB_VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Criando banco de dados " + DB_NOME);
        db.execSQL(SQL_CRIAR_TABELA);
        inserirExemplos(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Estratégia simples para trabalho acadêmico: recria a tabela.
        // Em produção, use ALTER TABLE para não perder os dados.
        Log.w(TAG, "Atualizando banco da versão " + oldVersion + " para " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA);
        onCreate(db);
    }

    /** Duas receitas de exemplo para o app não abrir vazio. */
    private void inserirExemplos(SQLiteDatabase db) {
        ContentValues brigadeiro = new ContentValues();
        brigadeiro.put(COL_NOME, "Brigadeiro");
        brigadeiro.put(COL_CATEGORIA, "Sobremesas");
        brigadeiro.put(COL_ORIGEM, "Brasil");
        brigadeiro.put(COL_TEMPO, 20);
        brigadeiro.put(COL_PORCOES, 20);
        brigadeiro.put(COL_DIFICULDADE, "FACIL");
        brigadeiro.put(COL_INGREDIENTES, "1 lata de leite condensado\n1 colher (sopa) de manteiga\n3 colheres (sopa) de chocolate em pó\nChocolate granulado");
        brigadeiro.put(COL_MODO_PREPARO, "Em uma panela, misture o leite condensado, a manteiga e o chocolate. Cozinhe em fogo baixo, mexendo sempre, até desgrudar do fundo. Deixe esfriar, enrole e passe no granulado.");
        brigadeiro.put(COL_FAVORITA, 1);
        brigadeiro.put(COL_DATA, DateUtils.hojeBanco());
        db.insert(TABELA, null, brigadeiro);

        ContentValues arroz = new ContentValues();
        arroz.put(COL_NOME, "Arroz branco soltinho");
        arroz.put(COL_CATEGORIA, "Acompanhamentos");
        arroz.put(COL_ORIGEM, "Brasil");
        arroz.put(COL_TEMPO, 25);
        arroz.put(COL_PORCOES, 4);
        arroz.put(COL_DIFICULDADE, "FACIL");
        arroz.put(COL_INGREDIENTES, "2 xícaras de arroz\n4 xícaras de água quente\n1 dente de alho\n2 colheres (sopa) de óleo\nSal a gosto");
        arroz.put(COL_MODO_PREPARO, "Refogue o alho no óleo, junte o arroz e frite por 2 minutos. Adicione a água quente e o sal, tampe parcialmente e cozinhe em fogo baixo até secar.");
        arroz.put(COL_FAVORITA, 0);
        arroz.put(COL_DATA, DateUtils.hojeBanco());
        db.insert(TABELA, null, arroz);
    }
}
