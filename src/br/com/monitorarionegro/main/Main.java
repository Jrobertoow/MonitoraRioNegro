package br.com.monitorarionegro.main;

import br.com.monitorarionegro.classificacao.ClassificadorNivel;
import br.com.monitorarionegro.classificacao.ClassificadorPadrao;
import br.com.monitorarionegro.classificacao.ClassificadorPersonalizado;
import br.com.monitorarionegro.exception.MedicaoDuplicadaException;
import br.com.monitorarionegro.exception.NivelInvalidoException;
import br.com.monitorarionegro.exception.RegistroNaoEncontradoException;
import br.com.monitorarionegro.model.AlertaHidrologico;
import br.com.monitorarionegro.model.EstacaoMonitoramento;
import br.com.monitorarionegro.model.EstadoNivel;
import br.com.monitorarionegro.model.Medicao;
import br.com.monitorarionegro.service.EstacaoService;
import br.com.monitorarionegro.service.MedicaoService;
import br.com.monitorarionegro.service.RelatorioService;
import br.com.monitorarionegro.util.LeitorConsole;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final LeitorConsole leitor;
    private final EstacaoService estacaoService;
    private final MedicaoService medicaoService;
    private final RelatorioService relatorioService;

    public Main() {
        this.leitor = new LeitorConsole();
        this.estacaoService = new EstacaoService();
        this.medicaoService = new MedicaoService(estacaoService);
        this.relatorioService = new RelatorioService();
    }

    public static void main(String[] args) {
        new Main().executar();
    }

    private void executar() {
        int opcao;
        exibirCabecalho();

        do {
            exibirMenuPrincipal();
            opcao = leitor.lerInt("Escolha uma opção: ");

            try {
                switch (opcao) {
                    case 1:
                        menuEstacoes();
                        break;
                    case 2:
                        menuMedicoes();
                        break;
                    case 3:
                        consultarSituacaoAtual();
                        break;
                    case 4:
                        exibirHistoricoVariacao();
                        break;
                    case 5:
                        compararEstacoes();
                        break;
                    case 6:
                        exibirAlertas();
                        break;
                    case 7:
                        exibirResumoEstatistico();
                        break;
                    case 8:
                        exportarCSV();
                        break;
                    case 9:
                        adicionarDadosDemonstrativos();
                        break;
                    case 0:
                        System.out.println("\nSistema encerrado.");
                        break;
                    default:
                        System.out.println("\nOpção inválida.");
                }
            } catch (RegistroNaoEncontradoException
                     | NivelInvalidoException
                     | MedicaoDuplicadaException
                     | IllegalArgumentException
                     | IOException erro) {
                System.out.println("\nERRO: " + erro.getMessage());
            }

            if (opcao != 0) {
                leitor.pausar();
            }
        } while (opcao != 0);

        leitor.fechar();
    }

    private void exibirCabecalho() {
        System.out.println("============================================================");
        System.out.println("                    MONITORA RIONEGRO");
        System.out.println("          Monitoramento de Níveis de Cheia e Seca");
        System.out.println("============================================================");
        System.out.println("Protótipo educacional: limites e dados demonstrativos.\n");
    }

    private void exibirMenuPrincipal() {
        System.out.println("\n---------------------- MENU PRINCIPAL ----------------------");
        System.out.println("1 - Gerenciar estações");
        System.out.println("2 - Gerenciar medições");
        System.out.println("3 - Consultar situação atual");
        System.out.println("4 - Exibir histórico e variação");
        System.out.println("5 - Comparar duas ou mais estações");
        System.out.println("6 - Exibir alertas hidrológicos");
        System.out.println("7 - Exibir resumo estatístico");
        System.out.println("8 - Exportar relatório CSV");
        System.out.println("9 - Adicionar dados demonstrativos");
        System.out.println("0 - Encerrar");
        System.out.println("------------------------------------------------------------");
    }

    private void menuEstacoes() throws RegistroNaoEncontradoException {
        int opcao;
        do {
            System.out.println("\n--- CRUD DE ESTAÇÕES ---");
            System.out.println("1 - Cadastrar estação");
            System.out.println("2 - Listar estações");
            System.out.println("3 - Atualizar estação");
            System.out.println("4 - Remover estação");
            System.out.println("0 - Voltar");
            opcao = leitor.lerInt("Escolha: ");

            switch (opcao) {
                case 1:
                    cadastrarEstacao();
                    break;
                case 2:
                    listarEstacoes();
                    break;
                case 3:
                    atualizarEstacao();
                    break;
                case 4:
                    removerEstacao();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void menuMedicoes()
            throws RegistroNaoEncontradoException,
            NivelInvalidoException,
            MedicaoDuplicadaException {
        int opcao;
        do {
            System.out.println("\n--- CRUD DE MEDIÇÕES ---");
            System.out.println("1 - Registrar medição");
            System.out.println("2 - Listar medições de uma estação");
            System.out.println("3 - Atualizar medição");
            System.out.println("4 - Remover medição");
            System.out.println("0 - Voltar");
            opcao = leitor.lerInt("Escolha: ");

            switch (opcao) {
                case 1:
                    cadastrarMedicao();
                    break;
                case 2:
                    listarMedicoesPorEstacao();
                    break;
                case 3:
                    atualizarMedicao();
                    break;
                case 4:
                    removerMedicao();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private void cadastrarEstacao() {
        System.out.println("\n--- CADASTRO DE ESTAÇÃO ---");
        String nome = leitor.lerTextoObrigatorio("Nome da estação: ");
        String cidade = leitor.lerTextoObrigatorio("Cidade: ");
        String localizacao = leitor.lerTextoOpcional("Localização/descrição: ");
        ClassificadorNivel classificador = criarClassificadorPeloConsole();

        EstacaoMonitoramento estacao = estacaoService.cadastrar(
                nome,
                cidade,
                localizacao,
                classificador
        );
        System.out.println("Estação cadastrada com ID " + estacao.getId() + ".");
    }

    private void listarEstacoes() {
        List<EstacaoMonitoramento> estacoes = estacaoService.listar();
        if (estacoes.isEmpty()) {
            System.out.println("Nenhuma estação cadastrada.");
            return;
        }

        System.out.println("\nID | Estação | Cidade | Localização | Medições");
        for (EstacaoMonitoramento estacao : estacoes) {
            System.out.printf(
                    "%d | %s | %s | %s | %d%n",
                    estacao.getId(),
                    estacao.getNome(),
                    estacao.getCidade(),
                    estacao.getLocalizacao(),
                    estacao.getMedicoes().size()
            );
            System.out.println("   " + estacao.getClassificador().getDescricaoFaixas());
        }
    }

    private void atualizarEstacao() throws RegistroNaoEncontradoException {
        listarEstacoes();
        int id = leitor.lerInt("ID da estação a atualizar: ");
        EstacaoMonitoramento atual = estacaoService.buscarPorId(id);

        String nome = leitor.lerTextoObrigatorio(
                "Novo nome [atual: " + atual.getNome() + "]: "
        );
        String cidade = leitor.lerTextoObrigatorio(
                "Nova cidade [atual: " + atual.getCidade() + "]: "
        );
        String localizacao = leitor.lerTextoOpcional(
                "Nova localização [atual: " + atual.getLocalizacao() + "]: "
        );

        ClassificadorNivel classificador = null;
        if (leitor.lerSimNao("Deseja alterar as faixas de classificação?")) {
            classificador = criarClassificadorPeloConsole();
        }

        estacaoService.atualizar(
                id,
                nome,
                cidade,
                localizacao,
                classificador
        );
        System.out.println("Estação atualizada.");
    }

    private void removerEstacao() throws RegistroNaoEncontradoException {
        listarEstacoes();
        int id = leitor.lerInt("ID da estação a remover: ");
        EstacaoMonitoramento estacao = estacaoService.buscarPorId(id);

        System.out.println(
                "A remoção também apagará " + estacao.getMedicoes().size()
                        + " medição(ões) em memória."
        );
        if (leitor.lerSimNao("Confirmar remoção?")) {
            estacaoService.remover(id);
            System.out.println("Estação removida.");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private ClassificadorNivel criarClassificadorPeloConsole() {
        System.out.println("Tipo de classificação:");
        System.out.println("1 - Padrão demonstrativo");
        System.out.println("2 - Personalizado por estação");
        int tipo = leitor.lerInt("Escolha: ");

        if (tipo != 2) {
            return new ClassificadorPadrao();
        }

        while (true) {
            try {
                double seca = leitor.lerDouble("Limite de seca (abaixo de): ");
                double atencao = leitor.lerDouble("Início da atenção: ");
                double cheia = leitor.lerDouble("Início da cheia: ");
                double emergencia = leitor.lerDouble("Início da emergência: ");
                return new ClassificadorPersonalizado(
                        seca,
                        atencao,
                        cheia,
                        emergencia
                );
            } catch (IllegalArgumentException erro) {
                System.out.println("Faixas inválidas: " + erro.getMessage());
            }
        }
    }

    private void cadastrarMedicao()
            throws RegistroNaoEncontradoException,
            NivelInvalidoException,
            MedicaoDuplicadaException {
        exigirEstacaoCadastrada();
        listarEstacoes();
        int estacaoId = leitor.lerInt("ID da estação: ");
        LocalDate data = leitor.lerData("Data da medição");
        double nivel = leitor.lerDouble("Nível do rio em metros: ");
        double chuva = leitor.lerDouble("Chuva nas últimas 24 h em mm: ");
        double temperatura = leitor.lerDouble("Temperatura em °C: ");
        String observacao = leitor.lerTextoOpcional("Observação: ");

        Medicao medicao = medicaoService.cadastrar(
                estacaoId,
                data,
                nivel,
                chuva,
                temperatura,
                observacao
        );
        System.out.println("Medição cadastrada com ID " + medicao.getId() + ".");
    }

    private void listarMedicoesPorEstacao()
            throws RegistroNaoEncontradoException, NivelInvalidoException {
        exigirEstacaoCadastrada();
        listarEstacoes();
        int estacaoId = leitor.lerInt("ID da estação: ");
        EstacaoMonitoramento estacao = estacaoService.buscarPorId(estacaoId);
        List<Medicao> medicoes = medicaoService.listarPorEstacao(estacaoId);

        if (medicoes.isEmpty()) {
            System.out.println("A estação ainda não possui medições.");
            return;
        }

        System.out.println("\nMedições de " + estacao.getNome());
        System.out.println("ID | Data | Nível | Chuva | Temperatura | Estado | Observação");
        for (Medicao medicao : medicoes) {
            EstadoNivel estado = estacao.getClassificador().classificar(medicao.getNivel());
            System.out.printf(
                    "%d | %s | %.2f m | %.2f mm | %.2f °C | %s | %s%n",
                    medicao.getId(),
                    medicao.getData().format(FORMATO_DATA),
                    medicao.getNivel(),
                    medicao.getChuva(),
                    medicao.getTemperatura(),
                    estado.getDescricao(),
                    medicao.getObservacao()
            );
        }
    }

    private void atualizarMedicao()
            throws RegistroNaoEncontradoException,
            NivelInvalidoException,
            MedicaoDuplicadaException {
        int id = leitor.lerInt("ID da medição a atualizar: ");
        Medicao atual = medicaoService.buscarPorId(id);
        System.out.printf(
                "Registro atual: %s | %.2f m | %.2f mm | %.2f °C%n",
                atual.getData().format(FORMATO_DATA),
                atual.getNivel(),
                atual.getChuva(),
                atual.getTemperatura()
        );

        LocalDate data = leitor.lerData("Nova data");
        double nivel = leitor.lerDouble("Novo nível em metros: ");
        double chuva = leitor.lerDouble("Nova chuva em mm: ");
        double temperatura = leitor.lerDouble("Nova temperatura em °C: ");
        String observacao = leitor.lerTextoOpcional("Nova observação: ");

        medicaoService.atualizar(
                id,
                data,
                nivel,
                chuva,
                temperatura,
                observacao
        );
        System.out.println("Medição atualizada.");
    }

    private void removerMedicao() throws RegistroNaoEncontradoException {
        int id = leitor.lerInt("ID da medição a remover: ");
        Medicao medicao = medicaoService.buscarPorId(id);
        System.out.printf(
                "Medição: %s | %.2f m%n",
                medicao.getData().format(FORMATO_DATA),
                medicao.getNivel()
        );
        if (leitor.lerSimNao("Confirmar remoção?")) {
            medicaoService.remover(id);
            System.out.println("Medição removida.");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    private void consultarSituacaoAtual()
            throws RegistroNaoEncontradoException, NivelInvalidoException {
        exigirEstacaoCadastrada();
        listarEstacoes();
        int estacaoId = leitor.lerInt("ID da estação: ");
        EstacaoMonitoramento estacao = estacaoService.buscarPorId(estacaoId);
        Medicao atual = medicaoService.obterUltimaMedicao(estacaoId);
        EstadoNivel estado = estacao.getClassificador().classificar(atual.getNivel());

        System.out.println("\n--- SITUAÇÃO ATUAL ---");
        System.out.println("Estação: " + estacao.getNome());
        System.out.println("Cidade: " + estacao.getCidade());
        System.out.println("Data: " + atual.getData().format(FORMATO_DATA));
        System.out.printf("Nível: %.2f m%n", atual.getNivel());
        System.out.println("Estado: " + estado.getDescricao());
        System.out.println(
                "Recomendação: "
                        + estacao.getClassificador().gerarRecomendacao(estado)
        );

        try {
            double variacao = medicaoService.calcularVariacaoMaisRecente(estacaoId);
            System.out.printf("Variação mais recente: %+.2f m (%s)%n",
                    variacao,
                    descreverTendencia(variacao));
        } catch (RegistroNaoEncontradoException erro) {
            System.out.println("Variação: ainda não há duas medições.");
        }
    }

    private void exibirHistoricoVariacao()
            throws RegistroNaoEncontradoException, NivelInvalidoException {
        exigirEstacaoCadastrada();
        listarEstacoes();
        int estacaoId = leitor.lerInt("ID da estação: ");
        EstacaoMonitoramento estacao = estacaoService.buscarPorId(estacaoId);
        List<Medicao> medicoes = medicaoService.listarPorEstacao(estacaoId);

        if (medicoes.isEmpty()) {
            System.out.println("A estação ainda não possui medições.");
            return;
        }

        System.out.println("\nData | Nível | Variação | Tendência | Estado");
        Medicao anterior = null;
        for (Medicao atual : medicoes) {
            String variacaoTexto = "Sem anterior";
            String tendencia = "-";
            if (anterior != null) {
                double variacao = atual.getNivel() - anterior.getNivel();
                variacaoTexto = String.format("%+.2f m", variacao);
                tendencia = descreverTendencia(variacao);
            }
            EstadoNivel estado = estacao.getClassificador().classificar(atual.getNivel());
            System.out.printf(
                    "%s | %.2f m | %s | %s | %s%n",
                    atual.getData().format(FORMATO_DATA),
                    atual.getNivel(),
                    variacaoTexto,
                    tendencia,
                    estado.getDescricao()
            );
            anterior = atual;
        }
    }

    private void compararEstacoes()
            throws RegistroNaoEncontradoException, NivelInvalidoException {
        if (estacaoService.quantidade() < 2) {
            throw new RegistroNaoEncontradoException(
                    "Cadastre pelo menos duas estações para realizar a comparação."
            );
        }

        listarEstacoes();
        int quantidade = leitor.lerIntMinimo("Quantidade de estações para comparar: ", 2);
        Set<Integer> ids = new HashSet<>();
        while (ids.size() < quantidade) {
            int id = leitor.lerInt("ID da estação " + (ids.size() + 1) + ": ");
            estacaoService.buscarPorId(id);
            if (!ids.add(id)) {
                System.out.println("Essa estação já foi selecionada.");
            }
        }

        List<ComparacaoEstacao> comparacoes = new ArrayList<>();
        for (int id : ids) {
            EstacaoMonitoramento estacao = estacaoService.buscarPorId(id);
            Medicao medicao = medicaoService.obterUltimaMedicao(id);
            EstadoNivel estado = estacao.getClassificador().classificar(medicao.getNivel());
            comparacoes.add(new ComparacaoEstacao(estacao, medicao, estado));
        }
        comparacoes.sort(
                Comparator.comparingDouble((ComparacaoEstacao item) -> item.medicao.getNivel())
                        .reversed()
        );

        System.out.println("\nEstação | Data | Nível | Estado");
        for (ComparacaoEstacao item : comparacoes) {
            System.out.printf(
                    "%s | %s | %.2f m | %s%n",
                    item.estacao.getNome(),
                    item.medicao.getData().format(FORMATO_DATA),
                    item.medicao.getNivel(),
                    item.estado.getDescricao()
            );
        }

        ComparacaoEstacao maior = comparacoes.get(0);
        ComparacaoEstacao menor = comparacoes.get(comparacoes.size() - 1);
        System.out.printf(
                "Maior nível: %s (%.2f m)%n",
                maior.estacao.getNome(),
                maior.medicao.getNivel()
        );
        System.out.printf(
                "Menor nível: %s (%.2f m)%n",
                menor.estacao.getNome(),
                menor.medicao.getNivel()
        );
        System.out.printf(
                "Diferença entre maior e menor: %.2f m%n",
                maior.medicao.getNivel() - menor.medicao.getNivel()
        );
    }

    private void exibirAlertas() {
        List<AlertaHidrologico> alertas = medicaoService.listarAlertas();
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta foi gerado.");
            return;
        }

        System.out.println("\nID | Data/hora | Estação | Nível | Estado | Mensagem");
        for (AlertaHidrologico alerta : alertas) {
            System.out.printf(
                    "%d | %s | %s | %.2f m | %s | %s%n",
                    alerta.getId(),
                    alerta.getDataHora().format(FORMATO_DATA_HORA),
                    alerta.getNomeEstacao(),
                    alerta.getNivel(),
                    alerta.getEstado().getDescricao(),
                    alerta.getMensagem()
            );
        }
    }

    private void exibirResumoEstatistico() throws NivelInvalidoException {
        List<EstacaoMonitoramento> estacoes = estacaoService.listar();
        if (estacoes.isEmpty()) {
            System.out.println("Nenhuma estação cadastrada.");
            return;
        }

        int totalMedicoes = medicaoService.quantidadeTotalMedicoes();
        if (totalMedicoes == 0) {
            System.out.println("Ainda não existem medições.");
            return;
        }

        double somaNiveis = 0;
        double somaChuvas = 0;
        double somaTemperaturas = 0;
        double maiorNivel = Double.NEGATIVE_INFINITY;
        double menorNivel = Double.POSITIVE_INFINITY;
        Map<EstadoNivel, Integer> totaisPorEstado = new EnumMap<>(EstadoNivel.class);
        for (EstadoNivel estado : EstadoNivel.values()) {
            totaisPorEstado.put(estado, 0);
        }

        for (EstacaoMonitoramento estacao : estacoes) {
            for (Medicao medicao : estacao.getMedicoes()) {
                somaNiveis += medicao.getNivel();
                somaChuvas += medicao.getChuva();
                somaTemperaturas += medicao.getTemperatura();
                maiorNivel = Math.max(maiorNivel, medicao.getNivel());
                menorNivel = Math.min(menorNivel, medicao.getNivel());
                EstadoNivel estado = estacao.getClassificador().classificar(medicao.getNivel());
                totaisPorEstado.put(estado, totaisPorEstado.get(estado) + 1);
            }
        }

        System.out.println("\n--- RESUMO ESTATÍSTICO ---");
        System.out.println("Estações: " + estacoes.size());
        System.out.println("Medições: " + totalMedicoes);
        System.out.printf("Nível médio: %.2f m%n", somaNiveis / totalMedicoes);
        System.out.printf("Maior nível: %.2f m%n", maiorNivel);
        System.out.printf("Menor nível: %.2f m%n", menorNivel);
        System.out.printf("Chuva total: %.2f mm%n", somaChuvas);
        System.out.printf("Temperatura média: %.2f °C%n", somaTemperaturas / totalMedicoes);
        for (EstadoNivel estado : EstadoNivel.values()) {
            System.out.printf(
                    "Registros em %s: %d%n",
                    estado.getDescricao(),
                    totaisPorEstado.get(estado)
            );
        }
    }

    private void exportarCSV() throws IOException, NivelInvalidoException {
        if (medicaoService.quantidadeTotalMedicoes() == 0) {
            System.out.println("Não existem medições para exportar.");
            return;
        }
        Path arquivo = relatorioService.exportarCSV(estacaoService.listar());
        System.out.println("Arquivo criado em: " + arquivo);
    }

    private void adicionarDadosDemonstrativos()
            throws RegistroNaoEncontradoException,
            NivelInvalidoException,
            MedicaoDuplicadaException {
        if (estacaoService.quantidade() > 0
                && !leitor.lerSimNao("Já existem dados. Deseja adicionar os exemplos mesmo assim?")) {
            System.out.println("Operação cancelada.");
            return;
        }

        EstacaoMonitoramento manaus = estacaoService.cadastrar(
                "Porto de Manaus",
                "Manaus",
                "Orla central",
                new ClassificadorPadrao()
        );
        EstacaoMonitoramento novoAirao = estacaoService.cadastrar(
                "Estação Novo Airão",
                "Novo Airão",
                "Área urbana próxima ao rio",
                new ClassificadorPadrao()
        );
        EstacaoMonitoramento barcelos = estacaoService.cadastrar(
                "Estação Barcelos",
                "Barcelos",
                "Margem do Rio Negro",
                new ClassificadorPersonalizado(14.0, 25.5, 27.0, 28.5)
        );

        LocalDate hoje = LocalDate.now();
        double[] niveisManaus = {24.50, 25.20, 26.40, 27.10, 28.20};
        double[] niveisNovoAirao = {22.80, 23.30, 24.10, 24.70, 25.40};
        double[] niveisBarcelos = {23.40, 24.20, 25.60, 26.80, 27.30};

        for (int i = 0; i < 5; i++) {
            LocalDate data = hoje.minusDays(4L - i);
            medicaoService.cadastrar(
                    manaus.getId(), data, niveisManaus[i], 20 + i * 8,
                    30 - i * 0.3, "Dado fictício para demonstração"
            );
            medicaoService.cadastrar(
                    novoAirao.getId(), data, niveisNovoAirao[i], 15 + i * 5,
                    29.5 - i * 0.2, "Dado fictício para demonstração"
            );
            medicaoService.cadastrar(
                    barcelos.getId(), data, niveisBarcelos[i], 18 + i * 6,
                    29.8 - i * 0.25, "Dado fictício para demonstração"
            );
        }

        System.out.println("Dados demonstrativos adicionados: 3 estações e 15 medições.");
        System.out.println(
                "Demonstração de polimorfismo: Manaus/Novo Airão usam ClassificadorPadrao; "
                        + "Barcelos usa ClassificadorPersonalizado."
        );
    }

    private void exigirEstacaoCadastrada() throws RegistroNaoEncontradoException {
        if (estacaoService.quantidade() == 0) {
            throw new RegistroNaoEncontradoException(
                    "Cadastre pelo menos uma estação primeiro."
            );
        }
    }

    private String descreverTendencia(double variacao) {
        if (variacao > 0) {
            return "subindo";
        }
        if (variacao < 0) {
            return "baixando";
        }
        return "estável";
    }

    private static class ComparacaoEstacao {
        private final EstacaoMonitoramento estacao;
        private final Medicao medicao;
        private final EstadoNivel estado;

        private ComparacaoEstacao(
                EstacaoMonitoramento estacao,
                Medicao medicao,
                EstadoNivel estado) {
            this.estacao = estacao;
            this.medicao = medicao;
            this.estado = estado;
        }
    }
}
