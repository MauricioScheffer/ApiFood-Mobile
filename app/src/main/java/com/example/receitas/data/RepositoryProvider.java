package com.example.receitas.data;

import android.content.Context;
import android.util.Log;

import com.example.receitas.data.local.SqliteReceitaRepository;
import com.example.receitas.data.remote.ApiReceitaRepository;
import com.example.receitas.util.PrefsManager;

/**
 Decide qual banco de dados usar (SQLite local ou MySQL via backend PHP)
 de acordo com a Configuração salva.
 */
public final class RepositoryProvider {

    private static final String TAG = "RepositoryProvider";

    private RepositoryProvider() {
    }

    public static ReceitaRepository get(Context context) {
        PrefsManager prefs = new PrefsManager(context);
        if (prefs.isUsandoMySql()) {
            Log.d(TAG, "Usando MySQL em " + prefs.getUrlServidor());
            return new ApiReceitaRepository(context, prefs.getUrlServidor());
        }
        Log.d(TAG, "Usando SQLite local");
        return new SqliteReceitaRepository(context);
    }
}
