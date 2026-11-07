package com.example.receitas.network;

import java.io.IOException;

/** Erro HTTP (status diferente de 2xx), com a mensagem enviada pelo servidor quando houver. */
public class HttpException extends IOException {

    private final int codigo;
    private final String mensagemServidor;

    public HttpException(int codigo, String mensagemServidor) {
        super("HTTP " + codigo + (mensagemServidor != null ? ": " + mensagemServidor : ""));
        this.codigo = codigo;
        this.mensagemServidor = mensagemServidor;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getMensagemServidor() {
        return mensagemServidor;
    }
}
