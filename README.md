# Monitora RioNegro

Sistema em Java para cadastrar estações de monitoramento, registrar medições diárias e acompanhar níveis de cheia e seca do Rio Negro.

> **Aviso:** o projeto é um protótipo educacional. As faixas e os dados demonstrativos não devem ser tratados como informações oficiais.

## Integrantes

- Antônio Augusto
- Chester Amorim
- Gabriela Mota
- José Roberto

## Requisitos atendidos

- Mais de quatro classes próprias com atributos privados e métodos de acesso.
- Encapsulamento nas classes de modelo.
- Interface `ClassificadorNivel` com duas implementações.
- Polimorfismo em tempo de execução por estação.
- Coleções da API Java: `ArrayList`, `LinkedHashMap`, `EnumMap` e `HashSet`.
- Exceções próprias para nível inválido, medição duplicada e registro inexistente.
- CRUD completo de estações e medições.
- Menu em modo texto.
- Código organizado em pacotes.
- Histórico, variação, alertas, comparação entre estações e exportação CSV.

## Estrutura

```text
src/br/com/monitorarionegro/
├── main/
│   └── Main.java
├── model/
│   ├── AlertaHidrologico.java
│   ├── EstacaoMonitoramento.java
│   ├── EstadoNivel.java
│   └── Medicao.java
├── classificacao/
│   ├── ClassificadorNivel.java
│   ├── ClassificadorPadrao.java
│   └── ClassificadorPersonalizado.java
├── service/
│   ├── EstacaoService.java
│   ├── MedicaoService.java
│   └── RelatorioService.java
├── exception/
│   ├── MedicaoDuplicadaException.java
│   ├── NivelInvalidoException.java
│   └── RegistroNaoEncontradoException.java
└── util/
    └── LeitorConsole.java
```

## Funcionalidades

- Cadastrar, listar, atualizar e remover estações.
- Cadastrar, listar, atualizar e remover medições.
- Classificar cada nível como seca, normal, atenção, cheia ou emergência.
- Aplicar faixas padrão ou personalizadas por estação.
- Gerar alertas hidrológicos em estados não normais.
- Mostrar histórico e variação entre medições.
- Comparar a medição mais recente de duas ou mais estações.
- Exibir resumo estatístico.
- Exportar o histórico para CSV.
- Inserir dados fictícios para demonstração.

## Pré-requisito

Instale um JDK, preferencialmente Java 17 ou superior.

Verifique no terminal:

```bash
java -version
javac -version
```

## Execução no Windows PowerShell

Na pasta do projeto:

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
java -cp bin br.com.monitorarionegro.main.Main
```

Também é possível executar:

```powershell
.\run.bat
```

## Execução no Linux ou macOS

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin br.com.monitorarionegro.main.Main
```

Ou:

```bash
chmod +x run.sh
./run.sh
```

## Menu principal

```text
1 - Gerenciar estações
2 - Gerenciar medições
3 - Consultar situação atual
4 - Exibir histórico e variação
5 - Comparar duas ou mais estações
6 - Exibir alertas hidrológicos
7 - Exibir resumo estatístico
8 - Exportar relatório CSV
9 - Adicionar dados demonstrativos
0 - Encerrar
```

## Polimorfismo

A classe `EstacaoMonitoramento` armazena uma referência do tipo `ClassificadorNivel`. Em tempo de execução, essa referência pode apontar para `ClassificadorPadrao` ou `ClassificadorPersonalizado`. O método `classificar` executado depende do objeto associado a cada estação.

A opção **Adicionar dados demonstrativos** cria estações com implementações diferentes para facilitar a apresentação desse requisito.

## Exceções de negócio

- `NivelInvalidoException`: nível negativo.
- `MedicaoDuplicadaException`: duas medições na mesma data para a mesma estação.
- `RegistroNaoEncontradoException`: ID inexistente ou ausência de dados necessários.

## Relatório CSV

A opção 8 cria:

```text
relatorios/historico_monitora_rionegro.csv
```

O arquivo pode ser aberto no Excel ou em outro programa de planilhas.

## GitHub

Sugestão de envio:

```bash
git init
git add .
git commit -m "Projeto Monitora RioNegro"
git branch -M main
git remote add origin URL_DO_REPOSITORIO
git push -u origin main
```
