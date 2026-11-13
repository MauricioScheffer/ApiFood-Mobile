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

import com.bumptech.glide.Glide;
import com.example.receitas.R;
import com.example.receitas.data.RepositoryProvider;
import com.example.receitas.model.Meal;
import com.example.receitas.model.Receita;
import com.example.receitas.network.MealDbService;
import com.example.receitas.util.PrefsManager;
import com.example.receitas.util.ResultCallback;

import java.util.List;

/**
 HOME
 Mostra saudação com o nome salvo nas preferências, um resumo das receitas
 e a "Receita do dia" buscada na API TheMealDB.
 */
public class MainActivity extends BaseActivity {

    private TextView tvSaudacao;
    private TextView tvResumo;
    private TextView tvFonteDados;
    private ImageView ivReceitaDia;
    private ProgressBar pbReceitaDia;
    private TextView tvReceitaDiaNome;
    private TextView tvReceitaDiaInfo;

    private PrefsManager prefs;
    private MealDbService mealDbService;
    private Meal receitaDoDia;

    // Abre o cadastro e recebe a mensagem de volta (transição com retorno de dados — R1)
    private final ActivityResultLauncher<Intent> cadastroLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mostrarMensagemDoResultado(result.getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        prefs = new PrefsManager(this);
        mealDbService = new MealDbService(this);

        tvSaudacao = findViewById(R.id.tvSaudacao);
        tvResumo = findViewById(R.id.tvResumo);
        tvFonteDados = findViewById(R.id.tvFonteDados);
        ivReceitaDia = findViewById(R.id.ivReceitaDia);
        pbReceitaDia = findViewById(R.id.pbReceitaDia);
        tvReceitaDiaNome = findViewById(R.id.tvReceitaDiaNome);
        tvReceitaDiaInfo = findViewById(R.id.tvReceitaDiaInfo);

        findViewById(R.id.btnMinhasReceitas).setOnClickListener(v ->
                startActivity(new Intent(this, ReceitaListActivity.class)));

        findViewById(R.id.btnNovaReceita).setOnClickListener(v ->
                cadastroLauncher.launch(new Intent(this, ReceitaFormActivity.class)));

        findViewById(R.id.btnExplorar).setOnClickListener(v ->
                startActivity(new Intent(this, ExplorarActivity.class)));

        findViewById(R.id.cardReceitaDia).setOnClickListener(v -> abrirReceitaDoDia());

        carregarReceitaDoDia();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onResume: atualiza sempre que volta para a Home (ex.: depois de mudar o nome)
        atualizarSaudacao();
        carregarResumo();
    }

    @Override
    protected void onAtualizar() {
        carregarReceitaDoDia();
        carregarResumo();
    }

    private void atualizarSaudacao() {
        String nome = prefs.getNomeUsuario();
        tvSaudacao.setText(nome.isEmpty()
                ? getString(R.string.saudacao_sem_nome)
                : getString(R.string.saudacao_com_nome, nome));

        tvFonteDados.setText(prefs.isUsandoMySql()
                ? getString(R.string.fonte_mysql_label, prefs.getUrlServidor())
                : getString(R.string.fonte_sqlite_label));
    }

    /** Conta receitas e favoritas no banco escolhido. */
    private void carregarResumo() {
        tvResumo.setText(R.string.carregando);
        RepositoryProvider.get(this).listar(new ResultCallback<List<Receita>>() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                if (!isTelaAtiva()) return;
                int favoritas = 0;
                for (Receita r : receitas) {
                    if (r.isFavorita()) favoritas++;
                }
                tvResumo.setText(getString(R.string.resumo_receitas, receitas.size(), favoritas));
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                tvResumo.setText(message);
            }
        });
    }

    /** Busca uma receita aleatória na API (em background). */
    private void carregarReceitaDoDia() {
        pbReceitaDia.setVisibility(View.VISIBLE);
        tvReceitaDiaInfo.setText(R.string.carregando);

        mealDbService.aleatoria(new ResultCallback<Meal>() {
            @Override
            public void onSuccess(Meal meal) {
                if (!isTelaAtiva()) return;
                receitaDoDia = meal;
                pbReceitaDia.setVisibility(View.GONE);
                tvReceitaDiaNome.setText(meal.getNome());
                tvReceitaDiaInfo.setText(R.string.receita_do_dia_toque);
                Glide.with(MainActivity.this)
                        .load(meal.getImagemUrl())
                        .placeholder(R.drawable.ic_receita_placeholder)
                        .error(R.drawable.ic_receita_placeholder)
                        .centerCrop()
                        .into(ivReceitaDia);
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                Log.w(TAG, "Receita do dia indisponível: " + message);
                receitaDoDia = null;
                pbReceitaDia.setVisibility(View.GONE);
                tvReceitaDiaNome.setText("");
                tvReceitaDiaInfo.setText(message);
            }
        });
    }

    private void abrirReceitaDoDia() {
        if (receitaDoDia == null) {
            carregarReceitaDoDia(); // sem receita carregada: tenta de novo
            return;
        }
        Intent intent = new Intent(this, MealDetalheActivity.class);
        intent.putExtra(MealDetalheActivity.EXTRA_ID_MEAL, receitaDoDia.getId());
        intent.putExtra(MealDetalheActivity.EXTRA_NOME_MEAL, receitaDoDia.getNome());
        startActivity(intent);
    }
}
