package com.example.receitas;

import android.app.Application;
import android.util.Log;

import com.example.receitas.util.PrefsManager;

/**
 * Classe Application: executada uma vez quando o app abre, antes de qualquer Activity.
 * Aqui aplicamos o tema (claro/escuro) salvo nas preferências.
 */
public class ReceitasApp extends Application {

    private static final String TAG = "ReceitasApp";

    @Override
    public void onCreate() {
        super.onCreate();
        String tema = new PrefsManager(this).getTema();
        PrefsManager.aplicarTema(tema);
        Log.i(TAG, "App iniciado com tema " + tema);
    }
}
