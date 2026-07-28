# Monitora RioNegro — Interface Web

Interface web responsiva para o projeto **Monitora RioNegro — Monitoramento de Níveis de Cheia e Seca**.

## Integrantes

- Antônio Augusto
- Chester Amorim
- Gabriela Mota
- José Roberto

## Tecnologias

- HTML5
- CSS3
- JavaScript puro
- LocalStorage do navegador
- Canvas para os gráficos

## Funcionalidades

- Painel geral com estatísticas;
- CRUD de estações de monitoramento;
- CRUD de medições diárias;
- Classificação em seca, normal, atenção, cheia e emergência;
- Classificador padrão e faixas personalizadas por estação;
- Validação de medição duplicada na mesma data;
- Histórico e cálculo de variação;
- Comparação entre duas estações;
- Alertas hidrológicos automáticos;
- Gráficos sem bibliotecas externas;
- Exportação do histórico para CSV;
- Dados demonstrativos;
- Layout responsivo para computador e celular.

## Observação importante

Esta versão funciona apenas no navegador e utiliza **LocalStorage**. Ela não acessa diretamente o projeto Java nem um banco de dados SQL.

Para integrar a interface ao Java, será necessário criar uma API Java, por exemplo com Spring Boot, e substituir as operações de LocalStorage por requisições `fetch()`.

## Como executar no VS Code

### Opção 1 — extensão Live Server

1. Abra a pasta `MonitoraRioNegro_Web` no VS Code.
2. Instale a extensão **Live Server**.
3. Clique com o botão direito em `index.html`.
4. Escolha **Open with Live Server**.

### Opção 2 — abrir diretamente

Abra o arquivo `index.html` no navegador. A maioria das funções funcionará normalmente sem servidor.

### Opção 3 — servidor local do Python

No terminal, dentro da pasta do projeto:

```bash
python -m http.server 5500
```

Depois abra:

```text
http://localhost:5500
```

## Estrutura

```text
MonitoraRioNegro_Web/
├── index.html
├── styles.css
├── app.js
├── README.md
└── assets/
    └── logo.svg
```

## Dados demonstrativos

Use o botão **Carregar demonstração** no topo da página. Ele cria três estações, medições fictícias e alertas para facilitar a apresentação do projeto.

## Limites demonstrativos padrão

- Abaixo de 15 m: seca;
- De 15 m até menos de 27 m: normal;
- De 27 m até menos de 28 m: atenção;
- De 28 m até menos de 29 m: cheia;
- A partir de 29 m: emergência.

Esses limites são educacionais e não representam faixas oficiais de uma estação hidrológica.

## Atualização visual amazônica

- Títulos e cabeçalhos em caixa alta.
- Paisagem vetorial do Rio Negro com floresta, pôr do sol, rio e aves.
- Paleta inspirada na floresta amazônica, no Rio Negro, na areia e no pôr do sol.
- Texturas discretas de folhas amazônicas nos fundos e painéis.
