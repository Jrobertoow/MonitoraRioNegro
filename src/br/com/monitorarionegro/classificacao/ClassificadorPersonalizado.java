package br.com.monitorarionegro.classificacao;

import br.com.monitorarionegro.exception.NivelInvalidoException;
import br.com.monitorarionegro.model.EstadoNivel;

public class ClassificadorPersonalizado implements ClassificadorNivel {
    private final double limiteSeca;
    private final double limiteAtencao;
    private final double limiteCheia;
    private final double limiteEmergencia;

    public ClassificadorPersonalizado(
            double limiteSeca,
            double limiteAtencao,
            double limiteCheia,
            double limiteEmergencia) {

        if (limiteSeca < 0
                || limiteSeca >= limiteAtencao
                || limiteAtencao >= limiteCheia
                || limiteCheia >= limiteEmergencia) {
            throw new IllegalArgumentException(
                    "As faixas devem ser crescentes: seca < atenção < cheia < emergência."
            );
        }

        this.limiteSeca = limiteSeca;
        this.limiteAtencao = limiteAtencao;
        this.limiteCheia = limiteCheia;
        this.limiteEmergencia = limiteEmergencia;
    }

    @Override
    public EstadoNivel classificar(double nivel) throws NivelInvalidoException {
        if (nivel < 0) {
            throw new NivelInvalidoException("O nível do rio não pode ser negativo.");
        }
        if (nivel < limiteSeca) {
            return EstadoNivel.SECA;
        }
        if (nivel < limiteAtencao) {
            return EstadoNivel.NORMAL;
        }
        if (nivel < limiteCheia) {
            return EstadoNivel.ATENCAO;
        }
        if (nivel < limiteEmergencia) {
            return EstadoNivel.CHEIA;
        }
        return EstadoNivel.EMERGENCIA;
    }

    @Override
    public String gerarRecomendacao(EstadoNivel estado) {
        switch (estado) {
            case SECA:
                return "Verificar os impactos locais da vazante e reforçar o acompanhamento.";
            case ATENCAO:
                return "Realizar novas medições em intervalos menores.";
            case CHEIA:
                return "Preparar ações preventivas conforme o plano local.";
            case EMERGENCIA:
                return "Comunicar imediatamente os responsáveis pelo monitoramento.";
            default:
                return "Continuar o monitoramento da estação.";
        }
    }

    @Override
    public String getDescricaoFaixas() {
        return String.format(
                "Personalizado: seca < %.2f m; normal < %.2f m; atenção < %.2f m; "
                        + "cheia < %.2f m; emergência >= %.2f m.",
                limiteSeca,
                limiteAtencao,
                limiteCheia,
                limiteEmergencia,
                limiteEmergencia
        );
    }

    public double getLimiteSeca() {
        return limiteSeca;
    }

    public double getLimiteAtencao() {
        return limiteAtencao;
    }

    public double getLimiteCheia() {
        return limiteCheia;
    }

    public double getLimiteEmergencia() {
        return limiteEmergencia;
    }
}
