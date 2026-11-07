package com.example.receitas.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Centraliza o acesso às preferências do usuário (SharedPreferences) — requisito R6.
 * Toda chave fica declarada aqui, então nenhuma tela precisa conhecer os nomes das chaves.
 */
public class PrefsManager {

    private static final String ARQUIVO = "receitas_prefs";

    private static final String KEY_NOME = "nome_usuario";
    private static final String KEY_TEMA = "tema";
    private static final String KEY_FONTE_DADOS = "fonte_dados";
    private static final String KEY_URL_SERVIDOR = "url_servidor";
    private static final String KEY_ULTIMA_BUSCA = "ultima_busca_api";

    // Valores possíveis de tema
    public static final String TEMA_SISTEMA = "SISTEMA";
    public static final String TEMA_CLARO = "CLARO";
    public static final String TEMA_ESCURO = "ESCURO";

    // Onde o CRUD de receitas é salvo
    public static final String FONTE_SQLITE = "SQLITE";
    public static final String FONTE_MYSQL = "MYSQL";

    /** 10.0.2.2 é o endereço do computador visto de dentro do emulador Android. */
    public static final String URL_SERVIDOR_PADRAO = "http://10.0.2.2/receitas-api/";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public String getNomeUsuario() {
        return prefs.getString(KEY_NOME, "");
    }

    public void setNomeUsuario(String nome) {
        prefs.edit().putString(KEY_NOME, nome).apply();
    }

    public String getTema() {
        return prefs.getString(KEY_TEMA, TEMA_SISTEMA);
    }

    public void setTema(String tema) {
        prefs.edit().putString(KEY_TEMA, tema).apply();
    }

    public String getFonteDados() {
        return prefs.getString(KEY_FONTE_DADOS, FONTE_SQLITE);
    }

    public void setFonteDados(String fonte) {
        prefs.edit().putString(KEY_FONTE_DADOS, fonte).apply();
    }

    public boolean isUsandoMySql() {
        return FONTE_MYSQL.equals(getFonteDados());
    }

    public String getUrlServidor() {
        return prefs.getString(KEY_URL_SERVIDOR, URL_SERVIDOR_PADRAO);
    }

    public void setUrlServidor(String url) {
        prefs.edit().putString(KEY_URL_SERVIDOR, url).apply();
    }

    public String getUltimaBusca() {
        return prefs.getString(KEY_ULTIMA_BUSCA, "");
    }

    public void setUltimaBusca(String termo) {
        prefs.edit().putString(KEY_ULTIMA_BUSCA, termo).apply();
    }

    /** Aplica o tema salvo (claro/escuro/sistema) no app inteiro. */
    public static void aplicarTema(String tema) {
        int modo;
        switch (tema) {
            case TEMA_CLARO:
                modo = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case TEMA_ESCURO:
                modo = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                modo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(modo);
    }
}
