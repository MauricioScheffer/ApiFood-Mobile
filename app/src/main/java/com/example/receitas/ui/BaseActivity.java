package com.example.receitas.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.receitas.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * Activity base: concentra o que é comum a todas as telas, evitando código repetido.
 *  - Menu de opções da ActionBar (Atualizar, Configurações, Sobre) — requisito R4;
 *  - Botão "voltar" na ActionBar;
 *  - Atalhos para Toast e Snackbar.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Tag para o Logcat: usa o nome da classe filha (ex.: "MainActivity"). */
    protected final String TAG = getClass().getSimpleName();

    /** Chave usada pelas telas para devolver uma mensagem de resultado à tela anterior. */
    public static final String EXTRA_MENSAGEM = "extra_mensagem";

    /** Mostra a seta de voltar na ActionBar. Chame no onCreate das telas internas. */
    protected void habilitarVoltar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /** Telas que não têm o que atualizar podem sobrescrever e retornar false. */
    protected boolean podeAtualizar() {
        return true;
    }

    /** Chamado quando o usuário toca em "Atualizar" no menu. */
    protected void onAtualizar() {
        // Implementado pelas telas que precisam
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comum, menu);
        menu.findItem(R.id.action_atualizar).setVisible(podeAtualizar());
        // Não faz sentido abrir Configurações estando nela
        menu.findItem(R.id.action_configuracoes).setVisible(!(this instanceof ConfiguracoesActivity));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (id == R.id.action_atualizar) {
            onAtualizar();
            return true;
        } else if (id == R.id.action_configuracoes) {
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            return true;
        } else if (id == R.id.action_sobre) {
            mostrarSobre();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void mostrarSobre() {
        String versao = "1.0";
        try {
            versao = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            android.util.Log.w(TAG, "Versão não encontrada", e);
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.sobre_titulo)
                .setMessage(getString(R.string.sobre_mensagem, getString(R.string.app_name), versao))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /** true enquanto a tela ainda está aberta (evita atualizar a UI de uma tela já fechada). */
    protected boolean isTelaAtiva() {
        return !isFinishing() && !isDestroyed();
    }

    protected void mostrarToast(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }

    protected void mostrarSnackbar(String mensagem) {
        View raiz = findViewById(R.id.raiz);
        if (raiz == null) {
            raiz = findViewById(android.R.id.content);
        }
        Snackbar.make(raiz, mensagem, Snackbar.LENGTH_LONG).show();
    }

    /** Mostra a mensagem devolvida por outra tela (EXTRA_MENSAGEM), se houver. */
    protected void mostrarMensagemDoResultado(Intent data) {
        if (data != null && data.hasExtra(EXTRA_MENSAGEM)) {
            mostrarSnackbar(data.getStringExtra(EXTRA_MENSAGEM));
        }
    }
}
