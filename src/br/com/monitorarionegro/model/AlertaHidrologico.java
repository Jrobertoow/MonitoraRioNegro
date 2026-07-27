package br.com.monitorarionegro.model;

import java.time.LocalDateTime;

public class AlertaHidrologico {
    private final int id;
    private final int estacaoId;
    private final String nomeEstacao;
    private final LocalDateTime dataHora;
    private final EstadoNivel estado;
    private final double nivel;
    private final String mensagem;

    public AlertaHidrologico(
            int id,
            int estacaoId,
            String nomeEstacao,
            LocalDateTime dataHora,
            EstadoNivel estado,
            double nivel,
            String mensagem) {
        this.id = id;
        this.estacaoId = estacaoId;
        this.nomeEstacao = nomeEstacao;
        this.dataHora = dataHora;
        this.estado = estado;
        this.nivel = nivel;
        this.mensagem = mensagem;
    }

    public int getId() {
        return id;
    }

    public int getEstacaoId() {
        return estacaoId;
    }

    public String getNomeEstacao() {
        return nomeEstacao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public EstadoNivel getEstado() {
        return estado;
    }

    public double getNivel() {
        return nivel;
    }

    public String getMensagem() {
        return mensagem;
    }
}
