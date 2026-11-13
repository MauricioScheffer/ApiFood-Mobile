package com.example.receitas.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.receitas.R;
import com.example.receitas.model.Meal;
import com.example.receitas.model.Receita;
import com.example.receitas.network.MealDbService;
import com.example.receitas.util.CategoriaMapper;
import com.example.receitas.util.DateUtils;
import com.example.receitas.util.ResultCallback;
import com.example.receitas.util.TraducaoService;

/**
 DETALHES de uma receita da TheMealDB (lookup.php?i=ID).
 O botão "Salvar nas minhas receitas" abre o formulário já preenchido,
 Enviando um objeto Receita pelo Intent.
 */
public class MealDetalheActivity extends BaseActivity {

    public static final String EXTRA_ID_MEAL = "extra_id_meal";
    public static final String EXTRA_NOME_MEAL = "extra_nome_meal";

    private View scrollConteudo;
    private ProgressBar pbCarregando;
    private View layoutErro;
    private TextView tvErro;
    private ImageView ivImagem;
    private TextView tvNome, tvCategoriaArea, tvIngredientes, tvInstrucoes;
    private View btnYoutube;

    private MealDbService service;
    private String idMeal;
    private Meal meal;

    // O formulário devolve "Receita salva com sucesso!" quando o usuário salva
    private final ActivityResultLauncher<Intent> importarLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mostrarMensagemDoResultado(result.getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detalhe);
        habilitarVoltar();

        scrollConteudo = findViewById(R.id.scrollConteudo);
        pbCarregando = findViewById(R.id.pbCarregando);
        layoutErro = findViewById(R.id.layoutErro);
        tvErro = findViewById(R.id.tvErro);
        ivImagem = findViewById(R.id.ivImagem);
        tvNome = findViewById(R.id.tvNome);
        tvCategoriaArea = findViewById(R.id.tvCategoriaArea);
        tvIngredientes = findViewById(R.id.tvIngredientes);
        tvInstrucoes = findViewById(R.id.tvInstrucoes);
        btnYoutube = findViewById(R.id.btnYoutube);

        findViewById(R.id.btnSalvarNasMinhas).setOnClickListener(v -> importar());
        btnYoutube.setOnClickListener(v -> abrirYoutube());
        findViewById(R.id.btnTentarNovamente).setOnClickListener(v -> carregar());

        // Dados recebidos da tela anterior (extras)
        idMeal = getIntent().getStringExtra(EXTRA_ID_MEAL);
        String nome = getIntent().getStringExtra(EXTRA_NOME_MEAL);
        if (nome != null) setTitle(nome);

        service = new MealDbService(this);
        carregar();
    }

    @Override
    protected void onAtualizar() {
        carregar();
    }

    private void carregar() {
        if (idMeal == null) {
            finish();
            return;
        }
        pbCarregando.setVisibility(View.VISIBLE);
        layoutErro.setVisibility(View.GONE);

        service.detalhes(idMeal, new ResultCallback<Meal>() {
            @Override
            public void onSuccess(Meal resultado) {
                if (!isTelaAtiva()) return;
                meal = resultado;
                pbCarregando.setVisibility(View.GONE);
                exibir(resultado);
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                pbCarregando.setVisibility(View.GONE);
                tvErro.setText(message);
                layoutErro.setVisibility(View.VISIBLE);
            }
        });
    }

    private void exibir(Meal m) {
        setTitle(m.getNome());
        tvNome.setText(m.getNome());
        tvCategoriaArea.setText(getString(R.string.detalhe_categoria_origem, CategoriaMapper.categoriaApiPtBr(this, m.getCategoria()),
                CategoriaMapper.origemApiPtBr(this, m.getArea())));
        tvIngredientes.setText(m.getIngredientesComoTexto());
        tvInstrucoes.setText(m.getInstrucoes());
        btnYoutube.setVisibility(m.getYoutubeUrl().isEmpty() ? View.GONE : View.VISIBLE);

        Glide.with(this)
                .load(m.getImagemUrl())
                .placeholder(R.drawable.ic_receita_placeholder)
                .error(R.drawable.ic_receita_placeholder)
                .centerCrop()
                .into(ivImagem);

        scrollConteudo.setVisibility(View.VISIBLE);
        traduzir(m);
    }

    /**
     * Mostra primeiro o texto original e, quando a tradução (no aparelho) termina,
     * troca nome, ingredientes e instruções por PT-BR. Se falhar, mantém o inglês.
     */
    private void traduzir(Meal m) {
        mostrarToast(getString(R.string.traduzindo));
        TraducaoService.traduzirMeal(m, new ResultCallback<Meal>() {
            @Override
            public void onSuccess(Meal traduzida) {
                if (!isTelaAtiva() || traduzida != meal) return;
                setTitle(traduzida.getNome());
                tvNome.setText(traduzida.getNome());
                tvIngredientes.setText(traduzida.getIngredientesComoTexto());
                tvInstrucoes.setText(traduzida.getInstrucoes());
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                mostrarToast(getString(R.string.erro_traducao));
            }
        });
    }

    /** Converte a receita da API em uma Receita do app e abre o formulário preenchido. */
    private void importar() {
        if (meal == null) return;
        Receita r = new Receita();
        r.setNome(meal.getNome());
        r.setCategoria(CategoriaMapper.daApi(this, meal.getCategoria()));
        r.setOrigem(CategoriaMapper.origemApiPtBr(this, meal.getArea()));
        r.setIngredientes(meal.getIngredientesComoTexto());
        r.setModoPreparo(meal.getInstrucoes());
        r.setImagemUrl(meal.getImagemUrl());
        r.setDataReceita(DateUtils.hojeBanco());
        r.setIdMealApi(meal.getId());
        // Tempo e porções não existem na API: o usuário completa no formulário

        Intent intent = new Intent(this, ReceitaFormActivity.class);
        intent.putExtra(ReceitaFormActivity.EXTRA_RECEITA_PREENCHIDA, r);
        importarLauncher.launch(intent);
    }

    private void abrirYoutube() {
        if (meal == null || meal.getYoutubeUrl().isEmpty()) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(meal.getYoutubeUrl())));
        } catch (ActivityNotFoundException e) {
            mostrarToast(getString(R.string.erro_abrir_link));
        }
    }
}
