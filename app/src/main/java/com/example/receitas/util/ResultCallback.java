package com.example.receitas.util;

/**
 * Callback genérico usado por todas as operações assíncronas do app
 * (banco de dados e rede). Os métodos SEMPRE são chamados na thread principal,
 * então dentro deles já é seguro atualizar a interface.
 */
public interface ResultCallback<T> {

    void onSuccess(T result);

    void onError(String message);
}
