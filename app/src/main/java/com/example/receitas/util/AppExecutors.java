package com.example.receitas.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executa tarefas FORA da thread principal (requisito R7) e devolve o resultado
 * na thread principal através de um Handler.
 *
 * Uso:
 *   AppExecutors.run(() -> dao.listar(), callback, "Erro ao listar");
 */
public final class AppExecutors {

    private static final String TAG = "AppExecutors";

    // Pool pequeno de threads de background, compartilhado pelo app inteiro
    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    /** Converte uma exceção em mensagem amigável para o usuário. */
    public interface ErrorMapper {
        String map(Exception e);
    }

    /**
     * Executa {@code task} em background e entrega o resultado (ou o erro) no callback,
     * já na thread principal.
     */
    public static <T> void run(Callable<T> task, ResultCallback<T> callback, ErrorMapper errorMapper) {
        IO.execute(() -> {
            try {
                T result = task.call();
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                Log.e(TAG, "Falha na tarefa em background", e);
                String message = errorMapper.map(e);
                MAIN.post(() -> callback.onError(message));
            }
        });
    }
}
