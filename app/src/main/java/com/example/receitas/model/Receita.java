package com.example.receitas.model;

import java.io.Serializable;

/**
 * Entidade principal do app: uma receita cadastrada pelo usuário.
 * É a mesma estrutura salva no SQLite (tabela "receitas") e no MySQL.
 * Implementa Serializable para poder ser enviada entre Activities via Intent (extras).
 */
public class Receita implements Serializable {

    // Códigos de dificuldade salvos no banco (o texto exibido vem do strings.xml)
    public static final String DIFICULDADE_FACIL = "FACIL";
    public static final String DIFICULDADE_MEDIO = "MEDIO";
    public static final String DIFICULDADE_DIFICIL = "DIFICIL";

    private long id;                // 0 = ainda não salva
    private String nome;
    private String categoria;
    private String origem;          // país/região de origem (opcional)
    private int tempoPreparo;       // em minutos
    private int porcoes;
    private String dificuldade = DIFICULDADE_FACIL;
    private String ingredientes;    // um ingrediente por linha
    private String modoPreparo;
    private String imagemUrl;       // opcional
    private boolean favorita;
    private String dataReceita;     // formato ISO yyyy-MM-dd (veja DateUtils)
    private String idMealApi;       // id da TheMealDB quando importada (opcional)

    public Receita() {
    }

    public boolean isNova() {
        return id <= 0;
    }

    // ---------- Getters e setters ----------

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public int getTempoPreparo() { return tempoPreparo; }
    public void setTempoPreparo(int tempoPreparo) { this.tempoPreparo = tempoPreparo; }

    public int getPorcoes() { return porcoes; }
    public void setPorcoes(int porcoes) { this.porcoes = porcoes; }

    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }

    public String getIngredientes() { return ingredientes; }
    public void setIngredientes(String ingredientes) { this.ingredientes = ingredientes; }

    public String getModoPreparo() { return modoPreparo; }
    public void setModoPreparo(String modoPreparo) { this.modoPreparo = modoPreparo; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public boolean isFavorita() { return favorita; }
    public void setFavorita(boolean favorita) { this.favorita = favorita; }

    public String getDataReceita() { return dataReceita; }
    public void setDataReceita(String dataReceita) { this.dataReceita = dataReceita; }

    public String getIdMealApi() { return idMealApi; }
    public void setIdMealApi(String idMealApi) { this.idMealApi = idMealApi; }

    @Override
    public String toString() {
        return "Receita{id=" + id + ", nome='" + nome + "', categoria='" + categoria + "'}";
    }
}
