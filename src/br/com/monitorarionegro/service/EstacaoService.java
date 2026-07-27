package br.com.monitorarionegro.service;

import br.com.monitorarionegro.classificacao.ClassificadorNivel;
import br.com.monitorarionegro.exception.RegistroNaoEncontradoException;
import br.com.monitorarionegro.model.EstacaoMonitoramento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EstacaoService {
    private final Map<Integer, EstacaoMonitoramento> estacoes;
    private int proximoId;

    public EstacaoService() {
        this.estacoes = new LinkedHashMap<>();
        this.proximoId = 1;
    }

    public EstacaoMonitoramento cadastrar(
            String nome,
            String cidade,
            String localizacao,
            ClassificadorNivel classificador) {

        validarTexto(nome, "O nome da estação é obrigatório.");
        validarTexto(cidade, "A cidade é obrigatória.");

        EstacaoMonitoramento estacao = new EstacaoMonitoramento(
                proximoId++,
                nome.trim(),
                cidade.trim(),
                localizacao == null || localizacao.trim().isEmpty()
                        ? "Não informada"
                        : localizacao.trim(),
                classificador
        );

        estacoes.put(estacao.getId(), estacao);
        return estacao;
    }

    public List<EstacaoMonitoramento> listar() {
        return new ArrayList<>(estacoes.values());
    }

    public EstacaoMonitoramento buscarPorId(int id)
            throws RegistroNaoEncontradoException {
        EstacaoMonitoramento estacao = estacoes.get(id);
        if (estacao == null) {
            throw new RegistroNaoEncontradoException(
                    "Estação de ID " + id + " não encontrada."
            );
        }
        return estacao;
    }

    public EstacaoMonitoramento atualizar(
            int id,
            String nome,
            String cidade,
            String localizacao,
            ClassificadorNivel classificador)
            throws RegistroNaoEncontradoException {

        EstacaoMonitoramento estacao = buscarPorId(id);
        validarTexto(nome, "O nome da estação é obrigatório.");
        validarTexto(cidade, "A cidade é obrigatória.");

        estacao.setNome(nome.trim());
        estacao.setCidade(cidade.trim());
        estacao.setLocalizacao(
                localizacao == null || localizacao.trim().isEmpty()
                        ? "Não informada"
                        : localizacao.trim()
        );
        if (classificador != null) {
            estacao.setClassificador(classificador);
        }
        return estacao;
    }

    public EstacaoMonitoramento remover(int id)
            throws RegistroNaoEncontradoException {
        EstacaoMonitoramento removida = estacoes.remove(id);
        if (removida == null) {
            throw new RegistroNaoEncontradoException(
                    "Estação de ID " + id + " não encontrada."
            );
        }
        return removida;
    }

    public int quantidade() {
        return estacoes.size();
    }

    private void validarTexto(String valor, String mensagem) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
