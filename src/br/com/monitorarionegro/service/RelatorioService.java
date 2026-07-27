package br.com.monitorarionegro.service;

import br.com.monitorarionegro.exception.NivelInvalidoException;
import br.com.monitorarionegro.model.EstacaoMonitoramento;
import br.com.monitorarionegro.model.EstadoNivel;
import br.com.monitorarionegro.model.Medicao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RelatorioService {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Path exportarCSV(List<EstacaoMonitoramento> estacoes)
            throws IOException, NivelInvalidoException {

        Path pasta = Paths.get("relatorios");
        Files.createDirectories(pasta);
        Path arquivo = pasta.resolve("historico_monitora_rionegro.csv");

        List<String> linhas = new ArrayList<>();
        linhas.add("Estacao_ID;Estacao;Cidade;Medicao_ID;Data;Nivel_m;Chuva_mm;"
                + "Temperatura_C;Estado;Observacao");

        for (EstacaoMonitoramento estacao : estacoes) {
            for (Medicao medicao : estacao.getMedicoes()) {
                EstadoNivel estado = estacao.getClassificador().classificar(medicao.getNivel());
                linhas.add(String.format(
                        Locale.US,
                        "%d;%s;%s;%d;%s;%.2f;%.2f;%.2f;%s;%s",
                        estacao.getId(),
                        limpar(estacao.getNome()),
                        limpar(estacao.getCidade()),
                        medicao.getId(),
                        medicao.getData().format(FORMATO_DATA),
                        medicao.getNivel(),
                        medicao.getChuva(),
                        medicao.getTemperatura(),
                        limpar(estado.getDescricao()),
                        limpar(medicao.getObservacao())
                ));
            }
        }

        Files.write(
                arquivo,
                linhas,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
        return arquivo.toAbsolutePath();
    }

    private String limpar(String texto) {
        return texto
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
