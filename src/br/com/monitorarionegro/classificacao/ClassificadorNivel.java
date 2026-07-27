package br.com.monitorarionegro.classificacao;

import br.com.monitorarionegro.exception.NivelInvalidoException;
import br.com.monitorarionegro.model.EstadoNivel;

public interface ClassificadorNivel {
    EstadoNivel classificar(double nivel) throws NivelInvalidoException;

    String gerarRecomendacao(EstadoNivel estado);

    String getDescricaoFaixas();
}
