package com.example.receitas.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.receitas.R;
import com.example.receitas.data.ReceitaRepository;
import com.example.receitas.data.RepositoryProvider;
import com.example.receitas.model.Receita;
import com.example.receitas.util.CategoriaMapper;
import com.example.receitas.util.DateUtils;
import com.example.receitas.util.ResultCallback;
import com.example.receitas.util.Validador;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * CADASTRO e EDIÇÃO de receitas (CREATE e UPDATE do CRUD) com validação completa (R3).
 *
 * Modos de abertura (definidos pelos extras do Intent):
 *  - sem extras ............................ nova receita em branco;
 *  - EXTRA_RECEITA_ID ...................... edita a receita com esse id;
 *  - EXTRA_RECEITA_PREENCHIDA (Receita) .... nova receita já preenchida (importada da API).
 *
 * Ao salvar, devolve RESULT_OK com EXTRA_MENSAGEM para a tela anterior.
 */
public class ReceitaFormActivity extends BaseActivity {

    public static final String EXTRA_RECEITA_ID = "extra_receita_id";
    public static final String EXTRA_RECEITA_PREENCHIDA = "extra_receita_preenchida";
    private static final String ESTADO_RECEITA_BASE = "estado_receita_base";

    private ImageView ivPreview;
    private TextInputLayout tilNome, tilOrigem, tilTempo, tilPorcoes, tilData,
            tilIngredientes, tilModoPreparo, tilImagemUrl;
    private TextInputEditText etNome, etOrigem, etTempo, etPorcoes, etData,
            etIngredientes, etModoPreparo, etImagemUrl;
    private Spinner spCategoria;
    private TextView tvErroCategoria;
    private RadioGroup rgDificuldade;
    private CheckBox cbFavorita;
    private Button btnSalvar;

    private ReceitaRepository repository;

    /**
     * Receita que está sendo editada (id > 0) ou a receita importada da API (id = 0).
     * Guarda campos que não aparecem no formulário, como o id e o idMealApi.
     */
    private Receita receitaBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receita_form);
        habilitarVoltar();

        repository = RepositoryProvider.get(this);
        vincularViews();
        configurarSpinnerCategoria();
        configurarCampoData();
        configurarLimpezaDeErros();

        etImagemUrl.setOnFocusChangeListener((v, temFoco) -> {
            if (!temFoco) carregarPreview();
        });
        btnSalvar.setOnClickListener(v -> salvar());

        Intent intent = getIntent();
        long idEdicao = intent.getLongExtra(EXTRA_RECEITA_ID, 0);

        if (savedInstanceState != null) {
            // Tela recriada (ex.: girou o celular): os campos já são restaurados pelo Android
            receitaBase = (Receita) savedInstanceState.getSerializable(ESTADO_RECEITA_BASE);
            if (receitaBase == null && idEdicao > 0) {
                carregarParaEdicao(idEdicao); // girou antes de terminar de carregar
            }
            atualizarTitulo();
            carregarPreview();
            return;
        }

        if (idEdicao > 0) {
            carregarParaEdicao(idEdicao);
        } else if (intent.hasExtra(EXTRA_RECEITA_PREENCHIDA)) {
            receitaBase = (Receita) intent.getSerializableExtra(EXTRA_RECEITA_PREENCHIDA);
            preencherCampos(receitaBase);
            mostrarSnackbar(getString(R.string.importar_complete));
        } else {
            etData.setText(DateUtils.hojeTela());
        }
        atualizarTitulo();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ESTADO_RECEITA_BASE, receitaBase);
    }

    @Override
    protected boolean podeAtualizar() {
        return false;
    }

    private boolean isEdicao() {
        return receitaBase != null && !receitaBase.isNova();
    }

    private void atualizarTitulo() {
        setTitle(isEdicao() ? R.string.titulo_editar_receita : R.string.titulo_nova_receita);
    }

    private void vincularViews() {
        ivPreview = findViewById(R.id.ivPreview);
        tilNome = findViewById(R.id.tilNome);
        tilOrigem = findViewById(R.id.tilOrigem);
        tilTempo = findViewById(R.id.tilTempo);
        tilPorcoes = findViewById(R.id.tilPorcoes);
        tilData = findViewById(R.id.tilData);
        tilIngredientes = findViewById(R.id.tilIngredientes);
        tilModoPreparo = findViewById(R.id.tilModoPreparo);
        tilImagemUrl = findViewById(R.id.tilImagemUrl);
        etNome = findViewById(R.id.etNome);
        etOrigem = findViewById(R.id.etOrigem);
        etTempo = findViewById(R.id.etTempo);
        etPorcoes = findViewById(R.id.etPorcoes);
        etData = findViewById(R.id.etData);
        etIngredientes = findViewById(R.id.etIngredientes);
        etModoPreparo = findViewById(R.id.etModoPreparo);
        etImagemUrl = findViewById(R.id.etImagemUrl);
        spCategoria = findViewById(R.id.spCategoria);
        tvErroCategoria = findViewById(R.id.tvErroCategoria);
        rgDificuldade = findViewById(R.id.rgDificuldade);
        cbFavorita = findViewById(R.id.cbFavorita);
        btnSalvar = findViewById(R.id.btnSalvar);
    }

    /** Spinner com "Selecione uma categoria" na posição 0 + categorias do strings.xml. */
    private void configurarSpinnerCategoria() {
        List<String> opcoes = new ArrayList<>();
        opcoes.add(getString(R.string.selecione_categoria));
        opcoes.addAll(CategoriaMapper.categoriasDoApp(this));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcoes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);

        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) tvErroCategoria.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /** Ícone de calendário no campo de data abre um DatePickerDialog. */
    private void configurarCampoData() {
        tilData.setEndIconOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            Date atual = DateUtils.parseTela(textoDe(etData));
            if (atual != null) cal.setTime(atual);

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (picker, ano, mes, dia) -> etData.setText(DateUtils.formatarTela(ano, mes, dia)),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // sem datas futuras
            dialog.show();
        });
    }

    /** Remove a mensagem de erro de um campo assim que o usuário volta a digitar nele. */
    private void configurarLimpezaDeErros() {
        TextInputLayout[] campos = {tilNome, tilOrigem, tilTempo, tilPorcoes, tilData,
                tilIngredientes, tilModoPreparo, tilImagemUrl};
        for (TextInputLayout til : campos) {
            if (til.getEditText() == null) continue;
            til.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    til.setError(null);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    // ------------------------------------------------------------------
    // Carregar e preencher
    // ------------------------------------------------------------------

    private void carregarParaEdicao(long id) {
        btnSalvar.setEnabled(false);
        repository.buscarPorId(id, new ResultCallback<Receita>() {
            @Override
            public void onSuccess(Receita receita) {
                if (!isTelaAtiva()) return;
                receitaBase = receita;
                preencherCampos(receita);
                atualizarTitulo();
                btnSalvar.setEnabled(true);
            }

            @Override
            public void onError(String message) {
                if (!isTelaAtiva()) return;
                mostrarToast(message);
                finish();
            }
        });
    }

    private void preencherCampos(Receita r) {
        etNome.setText(r.getNome());
        etOrigem.setText(r.getOrigem());
        // 0 = não informado (acontece nas receitas importadas da API)
        etTempo.setText(r.getTempoPreparo() > 0 ? String.valueOf(r.getTempoPreparo()) : "");
        etPorcoes.setText(r.getPorcoes() > 0 ? String.valueOf(r.getPorcoes()) : "");
        etData.setText(r.getDataReceita() != null ? DateUtils.bancoParaTela(r.getDataReceita()) : DateUtils.hojeTela());
        etIngredientes.setText(r.getIngredientes());
        etModoPreparo.setText(r.getModoPreparo());
        etImagemUrl.setText(r.getImagemUrl());
        cbFavorita.setChecked(r.isFavorita());

        // +1 porque a posição 0 do Spinner é "Selecione uma categoria"
        int indice = CategoriaMapper.categoriasDoApp(this).indexOf(r.getCategoria());
        spCategoria.setSelection(indice >= 0 ? indice + 1 : 0);

        if (Receita.DIFICULDADE_DIFICIL.equals(r.getDificuldade())) {
            rgDificuldade.check(R.id.rbDificil);
        } else if (Receita.DIFICULDADE_MEDIO.equals(r.getDificuldade())) {
            rgDificuldade.check(R.id.rbMedio);
        } else {
            rgDificuldade.check(R.id.rbFacil);
        }
        carregarPreview();
    }

    private void carregarPreview() {
        String url = textoDe(etImagemUrl);
        if (Validador.urlOpcional(this, url) != null || url.isEmpty()) {
            ivPreview.setImageResource(R.drawable.ic_receita_placeholder);
            return;
        }
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_receita_placeholder)
                .error(R.drawable.ic_receita_placeholder)
                .centerCrop()
                .into(ivPreview);
    }

    // ------------------------------------------------------------------
    // Validação (R3) e salvamento
    // ------------------------------------------------------------------

    /**
     * Valida todos os campos, mostra o erro em cada campo inválido e
     * devolve true só se TUDO estiver correto. Dados inválidos nunca chegam ao banco.
     */
    private boolean validar() {
        View primeiroInvalido = null;

        primeiroInvalido = marcar(tilNome, Validador.textoObrigatorio(this, textoDe(etNome), 3, 80), primeiroInvalido);

        boolean categoriaOk = spCategoria.getSelectedItemPosition() > 0;
        tvErroCategoria.setVisibility(categoriaOk ? View.GONE : View.VISIBLE);
        if (!categoriaOk && primeiroInvalido == null) primeiroInvalido = spCategoria;

        primeiroInvalido = marcar(tilOrigem, Validador.textoOpcional(this, textoDe(etOrigem), 40), primeiroInvalido);
        primeiroInvalido = marcar(tilTempo, Validador.inteiroNaFaixa(this, textoDe(etTempo), 1, 1440), primeiroInvalido);
        primeiroInvalido = marcar(tilPorcoes, Validador.inteiroNaFaixa(this, textoDe(etPorcoes), 1, 50), primeiroInvalido);
        primeiroInvalido = marcar(tilData, Validador.dataNaoFutura(this, textoDe(etData)), primeiroInvalido);
        primeiroInvalido = marcar(tilIngredientes, Validador.textoObrigatorio(this, textoDe(etIngredientes), 3, 5000), primeiroInvalido);
        primeiroInvalido = marcar(tilModoPreparo, Validador.textoObrigatorio(this, textoDe(etModoPreparo), 10, 10000), primeiroInvalido);
        primeiroInvalido = marcar(tilImagemUrl, Validador.urlOpcional(this, textoDe(etImagemUrl)), primeiroInvalido);

        if (rgDificuldade.getCheckedRadioButtonId() == -1) {
            mostrarToast(getString(R.string.erro_dificuldade));
            if (primeiroInvalido == null) primeiroInvalido = rgDificuldade;
        }

        if (primeiroInvalido != null) {
            primeiroInvalido.requestFocus();
            Log.d(TAG, "Formulário com erros de validação");
            return false;
        }
        return true;
    }

    /** Aplica a mensagem de erro no campo e devolve o primeiro campo inválido encontrado. */
    private View marcar(TextInputLayout til, String erro, View primeiroInvalido) {
        til.setError(erro);
        if (erro != null && primeiroInvalido == null) {
            return til;
        }
        return primeiroInvalido;
    }

    /** Monta o objeto Receita com os valores (já validados) do formulário. */
    private Receita lerFormulario() {
        Receita r = new Receita();
        if (receitaBase != null) {
            r.setId(receitaBase.getId());
            r.setIdMealApi(receitaBase.getIdMealApi());
        }
        r.setNome(textoDe(etNome));
        r.setCategoria((String) spCategoria.getSelectedItem());
        r.setOrigem(textoDe(etOrigem));
        r.setTempoPreparo(Integer.parseInt(textoDe(etTempo)));
        r.setPorcoes(Integer.parseInt(textoDe(etPorcoes)));
        r.setDataReceita(DateUtils.telaParaBanco(textoDe(etData)));
        r.setIngredientes(textoDe(etIngredientes));
        r.setModoPreparo(textoDe(etModoPreparo));
        r.setImagemUrl(textoDe(etImagemUrl));
        r.setFavorita(cbFavorita.isChecked());

        int marcado = rgDificuldade.getCheckedRadioButtonId();
        if (marcado == R.id.rbDificil) {
            r.setDificuldade(Receita.DIFICULDADE_DIFICIL);
        } else if (marcado == R.id.rbMedio) {
            r.setDificuldade(Receita.DIFICULDADE_MEDIO);
        } else {
            r.setDificuldade(Receita.DIFICULDADE_FACIL);
        }
        return r;
    }

    private void salvar() {
        if (!validar()) {
            mostrarSnackbar(getString(R.string.form_com_erros));
            return;
        }
        Receita receita = lerFormulario();
        Log.i(TAG, (receita.isNova() ? "Inserindo " : "Atualizando ") + receita);

        btnSalvar.setEnabled(false);
        btnSalvar.setText(R.string.salvando);

        if (receita.isNova()) {
            repository.inserir(receita, new ResultCallback<Long>() {
                @Override
                public void onSuccess(Long id) {
                    if (!isTelaAtiva()) return;
                    finalizarComSucesso(getString(R.string.receita_salva), id);
                }

                @Override
                public void onError(String message) {
                    falhaAoSalvar(message);
                }
            });
        } else {
            repository.atualizar(receita, new ResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean ok) {
                    if (!isTelaAtiva()) return;
                    finalizarComSucesso(getString(R.string.receita_atualizada), receita.getId());
                }

                @Override
                public void onError(String message) {
                    falhaAoSalvar(message);
                }
            });
        }
    }

    /** Devolve o resultado para a tela que abriu o formulário e fecha. */
    private void finalizarComSucesso(String mensagem, long id) {
        Intent resultado = new Intent();
        resultado.putExtra(EXTRA_MENSAGEM, mensagem);
        resultado.putExtra(EXTRA_RECEITA_ID, id);
        setResult(RESULT_OK, resultado);
        finish();
    }

    private void falhaAoSalvar(String mensagem) {
        if (!isTelaAtiva()) return;
        btnSalvar.setEnabled(true);
        btnSalvar.setText(R.string.btn_salvar);
        mostrarSnackbar(mensagem);
    }

    private static String textoDe(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
