package br.com.monitorarionegro.model;

import br.com.monitorarionegro.classificacao.ClassificadorNivel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstacaoMonitoramento {
    private final int id;
    private String nome;
    private String cidade;
    private String localizacao;
    private ClassificadorNivel classificador;
    private final List<Medicao> medicoes;

    public EstacaoMonitoramento(
            int id,
            String nome,
            String cidade,
            String localizacao,
            ClassificadorNivel classificador) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.localizacao = localizacao;
        this.classificador = classificador;
        this.medicoes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public ClassificadorNivel getClassificador() {
        return classificador;
    }

    public void setClassificador(ClassificadorNivel classificador) {
        this.classificador = classificador;
    }

    public List<Medicao> getMedicoes() {
        return Collections.unmodifiableList(medicoes);
    }

    public void adicionarMedicao(Medicao medicao) {
        medicoes.add(medicao);
    }

    public boolean removerMedicao(Medicao medicao) {
        return medicoes.remove(medicao);
    }
}
