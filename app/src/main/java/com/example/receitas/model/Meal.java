package com.example.receitas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 Receita vinda da API pública TheMealDB (leitura).
 Nas buscas por categoria a API devolve o id, nome e imagem;
 */
public class Meal implements Serializable {

    private String id;
    private String nome;
    private String categoria;
    private String area;
    private String instrucoes;
    private String imagemUrl;
    private String youtubeUrl;
    private final List<String> ingredientes = new ArrayList<>(); // "medida - ingrediente"

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getInstrucoes() { return instrucoes; }
    public void setInstrucoes(String instrucoes) { this.instrucoes = instrucoes; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

    public List<String> getIngredientes() { return ingredientes; }

    /** Ingredientes em texto, um por linha (formato usado no campo da Receita). */
    public String getIngredientesComoTexto() {
        StringBuilder sb = new StringBuilder();
        for (String item : ingredientes) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(item);
        }
        return sb.toString();
    }
}
