package com.example.receitas.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.receitas.R;
import com.example.receitas.data.ReceitaRepository;
import com.example.receitas.data.RepositoryProvider;
import com.example.receitas.model.Receita;
import com.example.receitas.ui.adapter.ReceitaAdapter;
import com.example.receitas.util.CategoriaMapper;
import com.example.receitas.util.ResultCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LISTAGEM das receitas salvas (READ do CRUD).
 * Permite buscar por nome, filtrar por categoria/favoritas, favoritar com um toque,
 * excluir com toque longo e abrir os detalhes com um toque.
 */
public class ReceitaListActivity extends BaseActivity implements ReceitaAdapter.Listener {

    private EditText etBusca;
    private Spinner spCategoriaFiltro;
    private CheckBox cbSomenteFavoritas;
    private TextView tvTotal;
    private TextView tvVazio;
    private ProgressBar pbCarregando;

    private ReceitaAdapter adapter;
    private ReceitaRepository repository;

    /** Lista completa vinda do banco; a filtrada é exibida no adapter. */
    private final List<Receita> todasReceitas = new ArrayList<>();

    // Detalhe e cadastro devolvem uma mensagem (ex.: "Receita excluída") — retorno de dados (R1)
    private final ActivityResultLauncher<Intent> telaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    mostrarMensagemDoResultado(result.getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receita_list);
        habilitarVoltar();

        etBusca = findViewById(R.id.etBusca);
        spCategoriaFiltro = findViewById(R.id.spCategoriaFiltro);
        cbSomenteFavoritas = findViewById(R.id.cbSomenteFavoritas);
        tvTotal = findViewById(R.id.tvTotal);
        tvVazio = findViewById(R.id.tvVazio);
        pbCarregando = findViewById(R.id.pbCarregando);

        RecyclerView rv = findViewById(R.id.rvReceitas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReceitaAdapter(this);
        rv.setAdapter(adapter);

        configurarFiltros();

        findViewById(R.id.fabNova).setOnClickListener(v ->
                telaLauncher.launch(new Intent(this, ReceitaFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega sempre que a tela volta a aparecer (após cadastrar, editar, excluir
        // ou trocar o banco nas Configurações)
        repository = RepositoryProvider.get(this);
        carregarReceitas();
    }

    @Override
    protected void onAtualizar() {
        carregarReceitas();
    }

    private void configurarFiltros() {
        // Spinner: "Todas as categorias" + categorias do strings.xml
        List<String> opcoes = new ArrayList<>();
        opcoes.add(getString(R.string.todas_categorias));
        opcoes.addAll(CategoriaMapper.categoriasDoApp(this));
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opcoes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoriaFiltro.setAdapter(spinnerAdapter);

        spCategoriaFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        cbSomenteFavoritas.setOnCheckedChangeListener((botao, marcado) -> aplicarFiltros());

        etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void carregarReceitas() {
        pbCarregando.setVisibility(View.VISIBLE);
        tvVazio.setVisibility(View.GONE);

        repository.listar(new ResultCallback<List<Receita>>() {
            @Override
            public void onSuccess(List<Receita> receitas) {
                if (!isTelaAtiva()) return;
                pbCarregando.setVisibility(View.GONE);
                todasReceitas.clear();
                todasReceitas.addAll(receitas);
                aplicarFiltros();
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                pbCarregando.setVisibility(View.GONE);
                todasReceitas.clear();
                aplicarFiltros();
                tvVazio.setText(message);
                tvVazio.setVisibility(View.VISIBLE);
                mostrarSnackbar(message);
            }
        });
    }

    /** Filtra a lista em memória por nome, categoria e favoritas. */
    private void aplicarFiltros() {
        String busca = etBusca.getText().toString().trim().toLowerCase(Locale.getDefault());
        int posCategoria = spCategoriaFiltro.getSelectedItemPosition();
        String categoria = posCategoria > 0 ? (String) spCategoriaFiltro.getSelectedItem() : null;
        boolean somenteFavoritas = cbSomenteFavoritas.isChecked();

        List<Receita> filtradas = new ArrayList<>();
        for (Receita r : todasReceitas) {
            boolean nomeOk = busca.isEmpty() || r.getNome().toLowerCase(Locale.getDefault()).contains(busca);
            boolean categoriaOk = categoria == null || categoria.equals(r.getCategoria());
            boolean favoritaOk = !somenteFavoritas || r.isFavorita();
            if (nomeOk && categoriaOk && favoritaOk) {
                filtradas.add(r);
            }
        }

        adapter.setItens(filtradas);
        tvTotal.setText(getString(R.string.total_receitas, filtradas.size()));
        tvVazio.setText(R.string.lista_vazia);
        tvVazio.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
        Log.d(TAG, "Filtro aplicado: " + filtradas.size() + " de " + todasReceitas.size());
    }

    // ---------- Eventos dos itens da lista ----------

    /** Toque no item: abre os detalhes enviando o id pelo Intent (extras — R1). */
    @Override
    public void onClick(Receita receita) {
        Intent intent = new Intent(this, ReceitaDetalheActivity.class);
        intent.putExtra(ReceitaDetalheActivity.EXTRA_RECEITA_ID, receita.getId());
        telaLauncher.launch(intent);
    }

    /** Toque longo: confirma a exclusão com AlertDialog (R4). */
    @Override
    public void onLongClick(Receita receita) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_exclusao_titulo)
                .setMessage(getString(R.string.confirmar_exclusao_msg, receita.getNome()))
                .setPositiveButton(R.string.btn_excluir, (dialog, which) -> excluir(receita))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    /** Toque na estrela: alterna favorita e salva (UPDATE). */
    @Override
    public void onFavoritaClick(Receita receita) {
        receita.setFavorita(!receita.isFavorita());
        repository.atualizar(receita, new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean ok) {
                if (!isTelaAtiva()) return;
                mostrarToast(getString(receita.isFavorita()
                        ? R.string.favorita_adicionada : R.string.favorita_removida));
                aplicarFiltros();
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                receita.setFavorita(!receita.isFavorita()); // desfaz a alteração
                mostrarSnackbar(message);
            }
        });
    }

    private void excluir(Receita receita) {
        repository.excluir(receita.getId(), new ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean ok) {
                if (!isTelaAtiva()) return;
                Log.i(TAG, "Receita excluída: " + receita);
                todasReceitas.remove(receita);
                aplicarFiltros();
                mostrarSnackbar(getString(R.string.receita_excluida));
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                mostrarSnackbar(message);
            }
        });
    }
}
