package br.com.monitorarionegro.service;

import br.com.monitorarionegro.exception.MedicaoDuplicadaException;
import br.com.monitorarionegro.exception.NivelInvalidoException;
import br.com.monitorarionegro.exception.RegistroNaoEncontradoException;
import br.com.monitorarionegro.model.AlertaHidrologico;
import br.com.monitorarionegro.model.EstacaoMonitoramento;
import br.com.monitorarionegro.model.EstadoNivel;
import br.com.monitorarionegro.model.Medicao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MedicaoService {
    private final EstacaoService estacaoService;
    private final List<AlertaHidrologico> alertas;
    private int proximoIdMedicao;
    private int proximoIdAlerta;

    public MedicaoService(EstacaoService estacaoService) {
        this.estacaoService = estacaoService;
        this.alertas = new ArrayList<>();
        this.proximoIdMedicao = 1;
        this.proximoIdAlerta = 1;
    }

    public Medicao cadastrar(
            int estacaoId,
            LocalDate data,
            double nivel,
            double chuva,
            double temperatura,
            String observacao)
            throws RegistroNaoEncontradoException,
            NivelInvalidoException,
            MedicaoDuplicadaException {

        EstacaoMonitoramento estacao = estacaoService.buscarPorId(estacaoId);
        validarDados(data, nivel, chuva, temperatura);
        validarDataDuplicada(estacao, data, -1);

        Medicao medicao = new Medicao(
                proximoIdMedicao++,
                data,
                nivel,
                chuva,
                temperatura,
                normalizarObservacao(observacao)
        );

        estacao.adicionarMedicao(medicao);
        gerarAlertaSeNecessario(estacao, medicao);
        return medicao;
    }

    public List<Medicao> listarPorEstacao(int estacaoId)
            throws RegistroNaoEncontradoException {
        EstacaoMonitoramento estacao = estacaoService.buscarPorId(estacaoId);
        List<Medicao> resultado = new ArrayList<>(estacao.getMedicoes());
        resultado.sort(Comparator.comparing(Medicao::getData));
        return resultado;
    }

    public Medicao atualizar(
            int medicaoId,
            LocalDate data,
            double nivel,
            double chuva,
            double temperatura,
            String observacao)
            throws RegistroNaoEncontradoException,
            NivelInvalidoException,
            MedicaoDuplicadaException {

        LocalizacaoMedicao localizacao = localizarMedicao(medicaoId);
        validarDados(data, nivel, chuva, temperatura);
        validarDataDuplicada(localizacao.estacao, data, medicaoId);

        Medicao medicao = localizacao.medicao;
        medicao.setData(data);
        medicao.setNivel(nivel);
        medicao.setChuva(chuva);
        medicao.setTemperatura(temperatura);
        medicao.setObservacao(normalizarObservacao(observacao));

        gerarAlertaSeNecessario(localizacao.estacao, medicao);
        return medicao;
    }

    public Medicao remover(int medicaoId)
            throws RegistroNaoEncontradoException {
        LocalizacaoMedicao localizacao = localizarMedicao(medicaoId);
        localizacao.estacao.removerMedicao(localizacao.medicao);
        return localizacao.medicao;
    }

    public Medicao buscarPorId(int medicaoId)
            throws RegistroNaoEncontradoException {
        return localizarMedicao(medicaoId).medicao;
    }

    public Medicao obterUltimaMedicao(int estacaoId)
            throws RegistroNaoEncontradoException {
        List<Medicao> medicoes = listarPorEstacao(estacaoId);
        if (medicoes.isEmpty()) {
            throw new RegistroNaoEncontradoException(
                    "A estação selecionada ainda não possui medições."
            );
        }
        return medicoes.get(medicoes.size() - 1);
    }

    public double calcularVariacaoMaisRecente(int estacaoId)
            throws RegistroNaoEncontradoException {
        List<Medicao> medicoes = listarPorEstacao(estacaoId);
        if (medicoes.size() < 2) {
            throw new RegistroNaoEncontradoException(
                    "São necessárias pelo menos duas medições para calcular a variação."
            );
        }
        Medicao atual = medicoes.get(medicoes.size() - 1);
        Medicao anterior = medicoes.get(medicoes.size() - 2);
        return atual.getNivel() - anterior.getNivel();
    }

    public List<AlertaHidrologico> listarAlertas() {
        return new ArrayList<>(alertas);
    }

    public int quantidadeTotalMedicoes() {
        int total = 0;
        for (EstacaoMonitoramento estacao : estacaoService.listar()) {
            total += estacao.getMedicoes().size();
        }
        return total;
    }

    private void validarDados(
            LocalDate data,
            double nivel,
            double chuva,
            double temperatura) throws NivelInvalidoException {
        if (data == null) {
            throw new IllegalArgumentException("A data da medição é obrigatória.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data não pode estar no futuro.");
        }
        if (nivel < 0) {
            throw new NivelInvalidoException("O nível do rio não pode ser negativo.");
        }
        if (chuva < 0) {
            throw new IllegalArgumentException("A quantidade de chuva não pode ser negativa.");
        }
        if (temperatura < -20 || temperatura > 60) {
            throw new IllegalArgumentException(
                    "A temperatura deve estar entre -20 °C e 60 °C."
            );
        }
    }

    private void validarDataDuplicada(
            EstacaoMonitoramento estacao,
            LocalDate data,
            int idIgnorado) throws MedicaoDuplicadaException {
        for (Medicao medicao : estacao.getMedicoes()) {
            if (medicao.getId() != idIgnorado && medicao.getData().equals(data)) {
                throw new MedicaoDuplicadaException(
                        "Já existe uma medição nessa estação para a data informada."
                );
            }
        }
    }

    private void gerarAlertaSeNecessario(
            EstacaoMonitoramento estacao,
            Medicao medicao) throws NivelInvalidoException {
        EstadoNivel estado = estacao.getClassificador().classificar(medicao.getNivel());
        if (estado == EstadoNivel.NORMAL) {
            return;
        }

        String mensagem = estacao.getClassificador().gerarRecomendacao(estado);
        alertas.add(new AlertaHidrologico(
                proximoIdAlerta++,
                estacao.getId(),
                estacao.getNome(),
                LocalDateTime.now(),
                estado,
                medicao.getNivel(),
                mensagem
        ));
    }

    private String normalizarObservacao(String observacao) {
        if (observacao == null || observacao.trim().isEmpty()) {
            return "Sem observações";
        }
        return observacao.trim();
    }

    private LocalizacaoMedicao localizarMedicao(int medicaoId)
            throws RegistroNaoEncontradoException {
        for (EstacaoMonitoramento estacao : estacaoService.listar()) {
            for (Medicao medicao : estacao.getMedicoes()) {
                if (medicao.getId() == medicaoId) {
                    return new LocalizacaoMedicao(estacao, medicao);
                }
            }
        }
        throw new RegistroNaoEncontradoException(
                "Medição de ID " + medicaoId + " não encontrada."
        );
    }

    private static class LocalizacaoMedicao {
        private final EstacaoMonitoramento estacao;
        private final Medicao medicao;

        private LocalizacaoMedicao(
                EstacaoMonitoramento estacao,
                Medicao medicao) {
            this.estacao = estacao;
            this.medicao = medicao;
        }
    }
}
