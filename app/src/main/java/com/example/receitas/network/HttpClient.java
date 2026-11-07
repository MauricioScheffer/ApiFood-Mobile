package com.example.receitas.network;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP mínimo usando HttpURLConnection (nativo do Java, sem bibliotecas extras).
 * ATENÇÃO: os métodos são bloqueantes — devem ser chamados fora da thread principal
 * (o app faz isso através de AppExecutors).
 */
public final class HttpClient {

    private static final String TAG = "HttpClient";
    private static final int TIMEOUT_MS = 10_000;

    private HttpClient() {
    }

    public static String get(String url) throws IOException {
        return request("GET", url, null);
    }

    public static String post(String url, String jsonBody) throws IOException {
        return request("POST", url, jsonBody);
    }

    public static String put(String url, String jsonBody) throws IOException {
        return request("PUT", url, jsonBody);
    }

    public static String delete(String url) throws IOException {
        return request("DELETE", url, null);
    }

    /** Executa a requisição e devolve o corpo da resposta como texto. */
    public static String request(String method, String url, String jsonBody) throws IOException {
        Log.d(TAG, method + " " + url);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");

            if (jsonBody != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            int status = conn.getResponseCode();
            boolean sucesso = status >= 200 && status < 300;
            String corpo = lerTudo(sucesso ? conn.getInputStream() : conn.getErrorStream());
            Log.d(TAG, "Resposta " + status + " (" + corpo.length() + " caracteres)");

            if (!sucesso) {
                throw new HttpException(status, extrairMensagemErro(corpo));
            }
            return corpo;
        } finally {
            conn.disconnect();
        }
    }

    private static String lerTudo(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                sb.append(linha).append('\n');
            }
        }
        return sb.toString().trim();
    }

    /** O backend PHP devolve erros no formato {"erro": "mensagem"}. */
    private static String extrairMensagemErro(String corpo) {
        try {
            String erro = new JSONObject(corpo).optString("erro", "");
            return erro.isEmpty() ? null : erro;
        } catch (Exception e) {
            return null;
        }
    }
}
