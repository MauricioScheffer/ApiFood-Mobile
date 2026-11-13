package com.example.receitas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receitas.R;
import com.example.receitas.model.Meal;
import com.example.receitas.network.MealDbService;
import com.example.receitas.ui.adapter.MealAdapter;
import com.example.receitas.util.CategoriaMapper;
import com.example.receitas.util.PrefsManager;
import com.example.receitas.util.ResultCallback;
import com.example.receitas.util.TraducaoService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * EXPLORAR — busca receitas na API pública TheMealDB (R7).
 * Busca por nome (search.php) ou por categoria (filter.php). Requisições em background,
 * com indicador de carregamento e mensagem + "Tentar novamente" em caso de erro.
 */
public class ExplorarActivity extends BaseActivity {

    private TextInputLayout tilBuscaApi;
    private TextInputEditText etBuscaApi;
    private Spinner spCategoriaApi;
    private RecyclerView rvMeals;
    private ProgressBar pbCarregando;
    private TextView tvMensagem;
    private View layoutErro;
    private TextView tvErro;

    private MealAdapter adapter;
    private MealDbService service;
    private PrefsManager prefs;

    private Runnable ultimaAcao;
    /** true quando a lista de categorias da API já foi carregada no Spinner. */
    private boolean categoriasCarregadas = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorar);
        habilitarVoltar();

        service = new MealDbService(this);
        prefs = new PrefsManager(this);

        tilBuscaApi = findViewById(R.id.tilBuscaApi);
        etBuscaApi = findViewById(R.id.etBuscaApi);
        spCategoriaApi = findViewById(R.id.spCategoriaApi);
        rvMeals = findViewById(R.id.rvMeals);
        pbCarregando = findViewById(R.id.pbCarregando);
        tvMensagem = findViewById(R.id.tvMensagem);
        layoutErro = findViewById(R.id.layoutErro);
        tvErro = findViewById(R.id.tvErro);

        // Grade com 2 colunas
        rvMeals.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MealAdapter(this::abrirDetalhes);
        rvMeals.setAdapter(adapter);

        findViewById(R.id.btnBuscar).setOnClickListener(v -> buscarPorNome());
        etBuscaApi.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                buscarPorNome();
                return true;
            }
            return false;
        });
        findViewById(R.id.btnTentarNovamente).setOnClickListener(v -> {
            if (ultimaAcao != null) ultimaAcao.run();
        });

        carregarCategorias();

        // Restaura a última busca salva
        String ultimaBusca = prefs.getUltimaBusca();
        if (!ultimaBusca.isEmpty()) {
            etBuscaApi.setText(ultimaBusca);
            buscarPorNome();
        }
    }

    @Override
    protected void onAtualizar() {
        if (ultimaAcao != null) {
            ultimaAcao.run();
        }
        if (!categoriasCarregadas) {
            carregarCategorias();
        }
    }

    private void carregarCategorias() {
        service.listarCategorias(new ResultCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> categorias) {
                if (!isTelaAtiva()) return;
                List<String> opcoes = new ArrayList<>();
                opcoes.add(getString(R.string.filtrar_categoria_api));
                for (String c : categorias) {
                    opcoes.add(CategoriaMapper.categoriaApiPtBr(ExplorarActivity.this, c));
                }
                ArrayAdapter<String> spAdapter = new ArrayAdapter<>(ExplorarActivity.this,
                        android.R.layout.simple_spinner_item, opcoes);
                spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCategoriaApi.setAdapter(spAdapter);
                spCategoriaApi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position > 0) {
                            // Envia à API o nome original (em inglês), não o rótulo traduzido
                            filtrarPorCategoria(categorias.get(position - 1));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                categoriasCarregadas = true;
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Categorias indisponíveis: " + message);
            }
        });
    }

    private void buscarPorNome() {
        String termo = etBuscaApi.getText() == null ? "" : etBuscaApi.getText().toString().trim();
        if (termo.isEmpty()) {
            tilBuscaApi.setError(getString(R.string.erro_busca_vazia));
            return;
        }
        tilBuscaApi.setError(null);
        esconderTeclado();
        prefs.setUltimaBusca(termo);
        if (spCategoriaApi.getAdapter() != null) spCategoriaApi.setSelection(0);

        ultimaAcao = this::buscarPorNome;
        mostrarCarregando();
        service.buscarPorNome(termo, criarCallback(termo));
    }

    private void filtrarPorCategoria(String categoria) {
        ultimaAcao = () -> filtrarPorCategoria(categoria);
        mostrarCarregando();
        service.filtrarPorCategoria(categoria, criarCallback(categoria));
    }

    /** Callback comum para as duas buscas: mostra a lista, vazio ou erro. */
    private ResultCallback<List<Meal>> criarCallback(String termo) {
        return new ResultCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> meals) {
                if (!isTelaAtiva()) return;
                pbCarregando.setVisibility(View.GONE);
                adapter.setItens(meals);
                traduzirNomes(meals);
                if (meals.isEmpty()) {
                    tvMensagem.setText(getString(R.string.explorar_vazio, termo));
                    tvMensagem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                pbCarregando.setVisibility(View.GONE);
                adapter.setItens(new ArrayList<>());
                tvErro.setText(message);
                layoutErro.setVisibility(View.VISIBLE);
            }
        };
    }

    /** Identifica a busca atual: ignora traduções de buscas antigas. */
    private int geracaoBusca = 0;

    private void mostrarCarregando() {
        geracaoBusca++;
        pbCarregando.setVisibility(View.VISIBLE);
        tvMensagem.setVisibility(View.GONE);
        layoutErro.setVisibility(View.GONE);
    }

    /** Troca os nomes em inglês pelos traduzidos; se falhar, a lista continua em inglês. */
    private void traduzirNomes(List<Meal> meals) {
        final int geracao = geracaoBusca;
        TraducaoService.traduzirNomes(meals, new ResultCallback<List<Meal>>() {
            @Override
            public void onSuccess(List<Meal> traduzidas) {
                if (!isTelaAtiva() || geracao != geracaoBusca) return;
                adapter.setItens(traduzidas);
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "Tradução dos nomes indisponível: " + message);
            }
        });
    }

    private void abrirDetalhes(Meal meal) {
        Intent intent = new Intent(this, MealDetalheActivity.class);
        intent.putExtra(MealDetalheActivity.EXTRA_ID_MEAL, meal.getId());
        intent.putExtra(MealDetalheActivity.EXTRA_NOME_MEAL, meal.getNome());
        startActivity(intent);
    }

    private void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
