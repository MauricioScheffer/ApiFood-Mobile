package com.example.receitas.util;

import android.content.Context;

import com.example.receitas.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Regras de validação dos formulários (requisito R3).
 * Cada método devolve a mensagem de erro (para exibir no campo) ou null se o valor é válido.
 * Mantendo as regras aqui, qualquer tela reaproveita a mesma validação.
 */
public final class Validador {

    private Validador() {
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Campo obrigatório com tamanho mínimo e máximo. */
    public static String textoObrigatorio(Context ctx, String texto, int minimo, int maximo) {
        if (vazio(texto)) {
            return ctx.getString(R.string.erro_obrigatorio);
        }
        int tamanho = texto.trim().length();
        if (tamanho < minimo) {
            return ctx.getString(R.string.erro_minimo_caracteres, minimo);
        }
        if (tamanho > maximo) {
            return ctx.getString(R.string.erro_maximo_caracteres, maximo);
        }
        return null;
    }

    /** Campo opcional: só valida o tamanho máximo quando preenchido. */
    public static String textoOpcional(Context ctx, String texto, int maximo) {
        if (!vazio(texto) && texto.trim().length() > maximo) {
            return ctx.getString(R.string.erro_maximo_caracteres, maximo);
        }
        return null;
    }

    /** Número inteiro obrigatório dentro da faixa [minimo, maximo]. */
    public static String inteiroNaFaixa(Context ctx, String texto, int minimo, int maximo) {
        if (vazio(texto)) {
            return ctx.getString(R.string.erro_obrigatorio);
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return ctx.getString(R.string.erro_numero_invalido);
        }
        if (valor < minimo || valor > maximo) {
            return ctx.getString(R.string.erro_faixa, minimo, maximo);
        }
        return null;
    }

    /** Data obrigatória no formato dd/MM/yyyy e que não esteja no futuro. */
    public static String dataNaoFutura(Context ctx, String texto) {
        if (vazio(texto)) {
            return ctx.getString(R.string.erro_obrigatorio);
        }
        Date data = DateUtils.parseTela(texto.trim());
        if (data == null) {
            return ctx.getString(R.string.erro_data_invalida);
        }
        if (DateUtils.isFutura(data)) {
            return ctx.getString(R.string.erro_data_futura);
        }
        return null;
    }

    /** URL opcional: se preenchida, precisa começar com http:// ou https:// e ser válida. */
    public static String urlOpcional(Context ctx, String texto) {
        if (vazio(texto)) {
            return null;
        }
        return urlObrigatoria(ctx, texto);
    }

    public static String urlObrigatoria(Context ctx, String texto) {
        if (vazio(texto)) {
            return ctx.getString(R.string.erro_obrigatorio);
        }
        String url = texto.trim();
        boolean temProtocolo = url.startsWith("http://") || url.startsWith("https://");
        if (!temProtocolo || url.contains(" ")) {
            return ctx.getString(R.string.erro_url_invalida);
        }
        if (url.length() > 500) {
            return ctx.getString(R.string.erro_maximo_caracteres, 500);
        }
        try {
            // Aceita domínios (site.com), IPs (10.0.2.2) e porta (:8000)
            String host = new URL(url).getHost();
            if (host == null || host.isEmpty()) {
                return ctx.getString(R.string.erro_url_invalida);
            }
        } catch (MalformedURLException e) {
            return ctx.getString(R.string.erro_url_invalida);
        }
        return null;
    }
}
