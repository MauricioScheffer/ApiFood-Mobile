package com.example.receitas.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import com.example.receitas.R;

import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.example.receitas.network.HttpException;

/** Utilitários de rede: checar conexão e traduzir exceções em mensagens para o usuário. */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    /** true se o aparelho tem alguma conexão com a internet ativa. */
    public static boolean isConectado(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network rede = cm.getActiveNetwork();
        if (rede == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(rede);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    /** Converte a exceção de uma requisição em uma mensagem clara (requisito R7). */
    public static String mensagemDeErro(Context context, Exception e) {
        if (e instanceof UnknownHostException) {
            return context.getString(R.string.erro_sem_internet);
        }
        if (e instanceof SocketTimeoutException) {
            return context.getString(R.string.erro_timeout);
        }
        if (e instanceof ConnectException) {
            return context.getString(R.string.erro_servidor_indisponivel);
        }
        if (e instanceof JSONException) {
            return context.getString(R.string.erro_resposta_invalida);
        }
        if (e instanceof HttpException) {
            HttpException http = (HttpException) e;
            if (http.getMensagemServidor() != null) {
                return http.getMensagemServidor();
            }
            return context.getString(R.string.erro_http, http.getCodigo());
        }
        if (e instanceof IOException) {
            return context.getString(R.string.erro_rede_generico);
        }
        return context.getString(R.string.erro_inesperado);
    }
}
