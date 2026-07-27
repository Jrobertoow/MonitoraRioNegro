package br.com.monitorarionegro.model;

import java.time.LocalDate;

public class Medicao {
    private final int id;
    private LocalDate data;
    private double nivel;
    private double chuva;
    private double temperatura;
    private String observacao;

    public Medicao(
            int id,
            LocalDate data,
            double nivel,
            double chuva,
            double temperatura,
            String observacao) {
        this.id = id;
        this.data = data;
        this.nivel = nivel;
        this.chuva = chuva;
        this.temperatura = temperatura;
        this.observacao = observacao;
    }

    public int getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public double getNivel() {
        return nivel;
    }

    public void setNivel(double nivel) {
        this.nivel = nivel;
    }

    public double getChuva() {
        return chuva;
    }

    public void setChuva(double chuva) {
        this.chuva = chuva;
    }

    public double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(double temperatura) {
        this.temperatura = temperatura;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
