package br.com.monitorarionegro.model;

public enum EstadoNivel {
    SECA("Seca"),
    NORMAL("Normal"),
    ATENCAO("Atenção"),
    CHEIA("Cheia"),
    EMERGENCIA("Emergência");

    private final String descricao;

    EstadoNivel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
