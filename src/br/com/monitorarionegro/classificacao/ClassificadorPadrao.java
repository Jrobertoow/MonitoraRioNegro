package br.com.monitorarionegro.classificacao;

import br.com.monitorarionegro.exception.NivelInvalidoException;
import br.com.monitorarionegro.model.EstadoNivel;

public class ClassificadorPadrao implements ClassificadorNivel {
    private static final double LIMITE_SECA = 15.0;
    private static final double LIMITE_ATENCAO = 27.0;
    private static final double LIMITE_CHEIA = 28.0;
    private static final double LIMITE_EMERGENCIA = 29.0;

    @Override
    public EstadoNivel classificar(double nivel) throws NivelInvalidoException {
        if (nivel < 0) {
            throw new NivelInvalidoException("O nível do rio não pode ser negativo.");
        }
        if (nivel < LIMITE_SECA) {
            return EstadoNivel.SECA;
        }
        if (nivel < LIMITE_ATENCAO) {
            return EstadoNivel.NORMAL;
        }
        if (nivel < LIMITE_CHEIA) {
            return EstadoNivel.ATENCAO;
        }
        if (nivel < LIMITE_EMERGENCIA) {
            return EstadoNivel.CHEIA;
        }
        return EstadoNivel.EMERGENCIA;
    }

    @Override
    public String gerarRecomendacao(EstadoNivel estado) {
        switch (estado) {
            case SECA:
                return "Acompanhar possíveis impactos na navegação e no abastecimento.";
            case ATENCAO:
                return "Aumentar a frequência das medições e acompanhar a tendência.";
            case CHEIA:
                return "Reforçar o monitoramento e preparar medidas preventivas.";
            case EMERGENCIA:
                return "Acionar os responsáveis e alertar áreas potencialmente afetadas.";
            default:
                return "Manter o acompanhamento periódico.";
        }
    }

    @Override
    public String getDescricaoFaixas() {
        return "Padrão: seca < 15,00 m; normal < 27,00 m; atenção < 28,00 m; "
                + "cheia < 29,00 m; emergência >= 29,00 m.";
    }
}
