package com.example.receitas.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.receitas.R;
import com.example.receitas.data.remote.ApiReceitaRepository;
import com.example.receitas.model.Receita;
import com.example.receitas.util.PrefsManager;
import com.example.receitas.util.ResultCallback;
import com.example.receitas.util.Validador;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * CONFIGURAÇÕES — preferências salvas em SharedPreferences (R6):
 * nome do usuário, tema (claro/escuro) e onde salvar as receitas (SQLite ou MySQL).
 */
public class ConfiguracoesActivity extends BaseActivity {

    // Posições do Spinner "fontes_dados" (strings.xml)
    private static final int POS_SQLITE = 0;
    private static final int POS_MYSQL = 1;

    private TextInputLayout tilNomeUsuario, tilUrlServidor;
    private TextInputEditText etNomeUsuario, etUrlServidor;
    private RadioGroup rgTema;
    private Spinner spFonteDados;
    private View layoutServidor;
    private Button btnTestarConexao;

    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);
        habilitarVoltar();

        prefs = new PrefsManager(this);

        tilNomeUsuario = findViewById(R.id.tilNomeUsuario);
        tilUrlServidor = findViewById(R.id.tilUrlServidor);
        etNomeUsuario = findViewById(R.id.etNomeUsuario);
        etUrlServidor = findViewById(R.id.etUrlServidor);
        rgTema = findViewById(R.id.rgTema);
        spFonteDados = findViewById(R.id.spFonteDados);
        layoutServidor = findViewById(R.id.layoutServidor);
        btnTestarConexao = findViewById(R.id.btnTestarConexao);

        spFonteDados.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layoutServidor.setVisibility(position == POS_MYSQL ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnTestarConexao.setOnClickListener(v -> testarConexao());
        findViewById(R.id.btnSalvarConfig).setOnClickListener(v -> salvar());

        if (savedInstanceState == null) {
            carregarPreferencias();
        }
    }

    @Override
    protected boolean podeAtualizar() {
        return false;
    }

    /** Preenche a tela com os valores salvos. */
    private void carregarPreferencias() {
        etNomeUsuario.setText(prefs.getNomeUsuario());
        etUrlServidor.setText(prefs.getUrlServidor());

        switch (prefs.getTema()) {
            case PrefsManager.TEMA_CLARO:
                rgTema.check(R.id.rbTemaClaro);
                break;
            case PrefsManager.TEMA_ESCURO:
                rgTema.check(R.id.rbTemaEscuro);
                break;
            default:
                rgTema.check(R.id.rbTemaSistema);
        }
        spFonteDados.setSelection(prefs.isUsandoMySql() ? POS_MYSQL : POS_SQLITE);
    }

    private boolean isMySqlSelecionado() {
        return spFonteDados.getSelectedItemPosition() == POS_MYSQL;
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private boolean validar() {
        String erroNome = Validador.textoObrigatorio(this, texto(etNomeUsuario), 2, 40);
        tilNomeUsuario.setError(erroNome);

        String erroUrl = isMySqlSelecionado() ? Validador.urlObrigatoria(this, texto(etUrlServidor)) : null;
        tilUrlServidor.setError(erroUrl);

        return erroNome == null && erroUrl == null;
    }

    private void salvar() {
        if (!validar()) {
            mostrarSnackbar(getString(R.string.form_com_erros));
            return;
        }

        String tema;
        int marcado = rgTema.getCheckedRadioButtonId();
        if (marcado == R.id.rbTemaClaro) {
            tema = PrefsManager.TEMA_CLARO;
        } else if (marcado == R.id.rbTemaEscuro) {
            tema = PrefsManager.TEMA_ESCURO;
        } else {
            tema = PrefsManager.TEMA_SISTEMA;
        }

        prefs.setNomeUsuario(texto(etNomeUsuario));
        prefs.setTema(tema);
        prefs.setFonteDados(isMySqlSelecionado() ? PrefsManager.FONTE_MYSQL : PrefsManager.FONTE_SQLITE);
        if (isMySqlSelecionado()) {
            prefs.setUrlServidor(texto(etUrlServidor));
        }
        Log.i(TAG, "Preferências salvas: tema=" + tema + ", fonte=" + prefs.getFonteDados());

        mostrarToast(getString(R.string.configuracoes_salvas));
        PrefsManager.aplicarTema(tema); // troca claro/escuro na hora
        finish();
    }

    /** Faz um GET no backend PHP para verificar se o MySQL está acessível. */
    private void testarConexao() {
        String erroUrl = Validador.urlObrigatoria(this, texto(etUrlServidor));
        tilUrlServidor.setError(erroUrl);
        if (erroUrl != null) return;

        btnTestarConexao.setEnabled(false);
        btnTestarConexao.setText(R.string.testando_conexao);

        new ApiReceitaRepository(this, texto(etUrlServidor)).listar(new ResultCallback<List<Receita>>() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                if (!isTelaAtiva()) return;
                restaurarBotaoTeste();
                mostrarSnackbar(getString(R.string.conexao_ok, receitas.size()));
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                restaurarBotaoTeste();
                mostrarSnackbar(message);
            }
        });
    }

    private void restaurarBotaoTeste() {
        btnTestarConexao.setEnabled(true);
        btnTestarConexao.setText(R.string.btn_testar_conexao);
    }
}
