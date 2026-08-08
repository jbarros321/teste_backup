# Documentação Técnica - Template Sankhya

Esta pasta contém a documentação técnica completa do Template para Personalizações Sankhya.

## 📄 Arquivos Disponíveis

- **DOCUMENTACAO_TECNICA.md** - Versão Markdown (fonte)
- **DOCUMENTACAO_TECNICA.html** - Versão HTML para visualização web (gerado automaticamente)
- **DOCUMENTACAO_TECNICA.pdf** - Versão PDF para impressão (gerado automaticamente)

## 🚀 Como Gerar HTML e PDF

### Método Automático (Recomendado)

Execute o script fornecido:

```bash
cd Template/docs
./gerar-documentacao.sh
```

O script irá:
1. ✅ Gerar `DOCUMENTACAO_TECNICA.html` automaticamente
2. ✅ Tentar gerar `DOCUMENTACAO_TECNICA.pdf` usando ferramentas disponíveis

### Requisitos

#### Para HTML
- **pandoc**: 
  - Ubuntu/Debian: `sudo apt-get install pandoc`
  - macOS: `brew install pandoc`
  - Windows: `choco install pandoc`

#### Para PDF (escolha uma opção)

1. **wkhtmltopdf** (recomendado):
   ```bash
   sudo apt-get install wkhtmltopdf
   ```

2. **Chromium/Chrome** (headless):
   ```bash
   sudo apt-get install chromium chromium-browser
   ```

3. **Pandoc com LaTeX**:
   ```bash
   sudo apt-get install texlive-xetex
   ```

### Método Manual

#### Gerar HTML
```bash
pandoc DOCUMENTACAO_TECNICA.md \
    --from markdown \
    --to html5 \
    --standalone \
    --css=https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.2.0/github-markdown.min.css \
    --metadata title="Documentação Técnica - Template Sankhya" \
    --toc \
    --toc-depth=3 \
    -o DOCUMENTACAO_TECNICA.html
```

#### Gerar PDF via wkhtmltopdf
```bash
wkhtmltopdf \
    --page-size A4 \
    --margin-top 20mm \
    --margin-bottom 20mm \
    --margin-left 15mm \
    --margin-right 15mm \
    --encoding UTF-8 \
    DOCUMENTACAO_TECNICA.html DOCUMENTACAO_TECNICA.pdf
```

#### Gerar PDF via Chromium Headless
```bash
chromium --headless --disable-gpu \
    --print-to-pdf=DOCUMENTACAO_TECNICA.pdf \
    file://$(pwd)/DOCUMENTACAO_TECNICA.html
```

### Conversão Online (Alternativa)

Se não tiver as ferramentas instaladas:

1. **Via Navegador**:
   - Abra o HTML gerado no navegador
   - Use "Imprimir" → "Salvar como PDF"

2. **Via Serviços Online**:
   - https://www.html2pdf.com/
   - https://www.ilovepdf.com/html-to-pdf

## 📋 Conteúdo da Documentação

A documentação técnica inclui:

1. **Visão Geral** - Informações do projeto, objetivo, especificações técnicas
2. **Arquitetura do Projeto** - Padrão arquitetural, stack tecnológico, princípios de design
3. **Componentes Padrão** - Documentação completa de cada componente com código fonte:
   - AbstractRepository (código completo, métodos disponíveis, exemplos)
   - DownloadHelper
   - FileGenerator
   - Formatter
4. **Padrões de Código** - JDK8 máximo, validação defensiva, tratamento de erros, performance
5. **Padrões Sankhya** - EntityFacade, NativeSql, JapeWrapper, filtros obrigatórios
6. **Estrutura de Diretórios** - Estrutura completa do projeto, convenções de nomenclatura
7. **Como Usar o Template** - Passo a passo completo para criar novo projeto
8. **Exemplos Práticos** - Exemplos completos de código
9. **Melhores Práticas** - Código, performance, Sankhya, validação
10. **Troubleshooting** - Erros comuns e soluções

## 🔄 Atualização Automática

Para atualizar HTML e PDF sempre que o Markdown for modificado:

### Usando entr (watcher)
```bash
echo DOCUMENTACAO_TECNICA.md | entr ./gerar-documentacao.sh
```

### Integração Maven
Adicione ao `pom.xml`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>bash</executable>
                <arguments>
                    <argument>docs/gerar-documentacao.sh</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 📝 Observações

- Todos os arquivos estão em codificação UTF-8
- O HTML usa estilo GitHub Markdown para melhor visualização
- O PDF mantém formatação original com índice navegável
- O Markdown é a fonte principal - sempre edite o `.md`, não o `.html` ou `.pdf`

---

**Gerado em**: 2025-12-06  
**Projeto**: Template para Personalizações Sankhya  
**Versão**: 6.0.0
