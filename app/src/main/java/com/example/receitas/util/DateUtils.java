package com.example.receitas.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Conversão de datas entre o formato exibido ao usuário (dd/MM/yyyy)
 * e o formato salvo no banco (yyyy-MM-dd, que é o tipo DATE do MySQL e ordena corretamente).
 */
public final class DateUtils {

    public static final String FORMATO_TELA = "dd/MM/yyyy";
    public static final String FORMATO_BANCO = "yyyy-MM-dd";

    private DateUtils() {
    }

    private static SimpleDateFormat formato(String padrao) {
        SimpleDateFormat sdf = new SimpleDateFormat(padrao, Locale.getDefault());
        sdf.setLenient(false); // rejeita datas como 31/02/2025
        return sdf;
    }

    /** Retorna a data ou null se o texto não for uma data válida no formato dd/MM/yyyy. */
    public static Date parseTela(String texto) {
        if (texto == null || !texto.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return null;
        }
        try {
            return formato(FORMATO_TELA).parse(texto);
        } catch (ParseException e) {
            return null;
        }
    }

    /** dd/MM/yyyy -> yyyy-MM-dd (retorna null se inválida). */
    public static String telaParaBanco(String texto) {
        Date data = parseTela(texto);
        return data == null ? null : formato(FORMATO_BANCO).format(data);
    }

    /** yyyy-MM-dd -> dd/MM/yyyy (retorna "" se vazia ou inválida). */
    public static String bancoParaTela(String iso) {
        if (iso == null || iso.isEmpty()) {
            return "";
        }
        try {
            // O MySQL pode devolver "2025-01-31" ou "2025-01-31 00:00:00"
            Date data = formato(FORMATO_BANCO).parse(iso.length() > 10 ? iso.substring(0, 10) : iso);
            return formato(FORMATO_TELA).format(data);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String hojeTela() {
        return formato(FORMATO_TELA).format(new Date());
    }

    public static String hojeBanco() {
        return formato(FORMATO_BANCO).format(new Date());
    }

    public static String formatarTela(int ano, int mes, int dia) {
        Calendar c = Calendar.getInstance();
        c.set(ano, mes, dia);
        return formato(FORMATO_TELA).format(c.getTime());
    }

    /** true se a data é posterior ao dia de hoje. */
    public static boolean isFutura(Date data) {
        Calendar fimDeHoje = Calendar.getInstance();
        fimDeHoje.set(Calendar.HOUR_OF_DAY, 23);
        fimDeHoje.set(Calendar.MINUTE, 59);
        fimDeHoje.set(Calendar.SECOND, 59);
        return data.after(fimDeHoje.getTime());
    }
}
