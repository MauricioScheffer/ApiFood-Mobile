package com.example.receitas.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.receitas.R;
import com.example.receitas.data.ReceitaRepository;
import com.example.receitas.data.RepositoryProvider;
import com.example.receitas.model.Receita;
import com.example.receitas.util.DateUtils;
import com.example.receitas.util.ReceitaFormatter;
import com.example.receitas.util.ResultCallback;

/**
 * DETALHES de uma receita salva. Recebe o id pelo Intent (EXTRA_RECEITA_ID).
 * Daqui o usuário pode editar (abre o formulário), excluir (com confirmação) ou compartilhar.
 */
public class ReceitaDetalheActivity extends BaseActivity {

    public static final String EXTRA_RECEITA_ID = "extra_receita_id";

    private View scrollConteudo;
    private ProgressBar pbCarregando;
    private ImageView ivImagem;
    private TextView tvNome, tvFavorita, tvCategoria, tvInfo, tvData, tvIngredientes, tvModoPreparo;

    private ReceitaRepository repository;
    private long receitaId;
    private Receita receita;

    // Volta da edição: recarrega e mostra a mensagem enviada pelo formulário
    private final ActivityResultLauncher<Intent> edicaoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mostrarMensagemDoResultado(result.getData());
                    carregar();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receita_detalhe);
        habilitarVoltar();

        scrollConteudo = findViewById(R.id.scrollConteudo);
        pbCarregando = findViewById(R.id.pbCarregando);
        ivImagem = findViewById(R.id.ivImagem);
        tvNome = findViewById(R.id.tvNome);
        tvFavorita = findViewById(R.id.tvFavorita);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvInfo = findViewById(R.id.tvInfo);
        tvData = findViewById(R.id.tvData);
        tvIngredientes = findViewById(R.id.tvIngredientes);
        tvModoPreparo = findViewById(R.id.tvModoPreparo);

        findViewById(R.id.btnEditar).setOnClickListener(v -> editar());
        findViewById(R.id.btnExcluir).setOnClickListener(v -> confirmarExclusao());
        findViewById(R.id.btnCompartilhar).setOnClickListener(v -> compartilhar());

        // Dado recebido da tela anterior via extras (R1)
        receitaId = getIntent().getLongExtra(EXTRA_RECEITA_ID, 0);
        Log.d(TAG, "Abrindo detalhes da receita id=" + receitaId);

        repository = RepositoryProvider.get(this);
        carregar();
    }

    @Override
    protected void onAtualizar() {
        carregar();
    }

    private void carregar() {
        pbCarregando.setVisibility(View.VISIBLE);
        repository.buscarPorId(receitaId, new ResultCallback<Receita>() {
            @Override
            public void onSuccess(Receita r) {
                if (!isTelaAtiva()) return;
                receita = r;
                pbCarregando.setVisibility(View.GONE);
                exibir(r);
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                mostrarToast(message);
                finish();
            }
        });
    }

    private void exibir(Receita r) {
        setTitle(r.getNome());
        tvNome.setText(r.getNome());
        tvFavorita.setVisibility(r.isFavorita() ? View.VISIBLE : View.GONE);
        tvCategoria.setText(ReceitaFormatter.categoriaEOrigem(this, r));
        tvInfo.setText(ReceitaFormatter.resumo(this, r));
        tvData.setText(getString(R.string.detalhe_data, DateUtils.bancoParaTela(r.getDataReceita())));
        tvIngredientes.setText(r.getIngredientes());
        tvModoPreparo.setText(r.getModoPreparo());

        Glide.with(this)
                .load(r.getImagemUrl())
                .placeholder(R.drawable.ic_receita_placeholder)
                .error(R.drawable.ic_receita_placeholder)
                .centerCrop()
                .into(ivImagem);

        scrollConteudo.setVisibility(View.VISIBLE);
    }

    private void editar() {
        if (receita == null) return;
        Intent intent = new Intent(this, ReceitaFormActivity.class);
        intent.putExtra(ReceitaFormActivity.EXTRA_RECEITA_ID, receita.getId());
        edicaoLauncher.launch(intent);
    }

    /** AlertDialog de confirmação antes de uma operação crítica (R4). */
    private void confirmarExclusao() {
        if (receita == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_exclusao_titulo)
                .setMessage(getString(R.string.confirmar_exclusao_msg, receita.getNome()))
                .setPositiveButton(R.string.btn_excluir, (dialog, which) -> excluir())
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void excluir() {
        repository.excluir(receita.getId(), new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean ok) {
                if (!isTelaAtiva()) return;
                Log.i(TAG, "Receita excluída: " + receita);
                // Devolve a mensagem para a lista exibir (retorno de dados — R1)
                Intent resultado = new Intent();
                resultado.putExtra(EXTRA_MENSAGEM, getString(R.string.receita_excluida));
                setResult(RESULT_OK, resultado);
                finish();
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                mostrarSnackbar(message);
            }
        });
    }

    /** Compartilha a receita como texto (WhatsApp, e-mail etc.) usando um Intent implícito. */
    private void compartilhar() {
        if (receita == null) return;
        String texto = getString(R.string.compartilhar_texto, receita.getNome(),
                receita.getIngredientes(), receita.getModoPreparo(), getString(R.string.app_name));
        Intent envio = new Intent(Intent.ACTION_SEND);
        envio.setType("text/plain");
        envio.putExtra(Intent.EXTRA_SUBJECT, receita.getNome());
        envio.putExtra(Intent.EXTRA_TEXT, texto);
        startActivity(Intent.createChooser(envio, getString(R.string.compartilhar_titulo)));
    }
}
