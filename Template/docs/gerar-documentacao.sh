#!/bin/bash

# Script para gerar HTML e PDF a partir do Markdown
# Requer: pandoc, wkhtmltopdf ou chromium/chrome headless

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCS_DIR="$SCRIPT_DIR"
MD_FILE="$DOCS_DIR/DOCUMENTACAO_TECNICA.md"
HTML_FILE="$DOCS_DIR/DOCUMENTACAO_TECNICA.html"
PDF_FILE="$DOCS_DIR/DOCUMENTACAO_TECNICA.pdf"

if [ ! -f "$MD_FILE" ]; then
    echo "Erro: Arquivo Markdown não encontrado: $MD_FILE"
    exit 1
fi

echo "Gerando HTML e PDF a partir de: $MD_FILE"

# Verificar se pandoc está instalado
if ! command -v pandoc &> /dev/null; then
    echo "AVISO: pandoc não encontrado. Instalando dependências..."
    echo "Por favor, instale o pandoc manualmente:"
    echo "  Ubuntu/Debian: sudo apt-get install pandoc"
    echo "  macOS: brew install pandoc"
    echo "  Windows: choco install pandoc"
    exit 1
fi

# Gerar HTML com estilo GitHub
echo "Gerando HTML..."
pandoc "$MD_FILE" \
    --from markdown \
    --to html5 \
    --standalone \
    --css=https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.2.0/github-markdown.min.css \
    --metadata title="Documentação Técnica - Template Sankhya" \
    --metadata lang=pt-BR \
    --toc \
    --toc-depth=3 \
    --highlight-style=github \
    -o "$HTML_FILE"

if [ $? -eq 0 ]; then
    echo "✅ HTML gerado com sucesso: $HTML_FILE"
else
    echo "❌ Erro ao gerar HTML"
    exit 1
fi

# Gerar PDF
echo "Gerando PDF..."

# Tentar usar wkhtmltopdf primeiro
if command -v wkhtmltopdf &> /dev/null; then
    echo "Usando wkhtmltopdf..."
    wkhtmltopdf \
        --page-size A4 \
        --margin-top 20mm \
        --margin-bottom 20mm \
        --margin-left 15mm \
        --margin-right 15mm \
        --encoding UTF-8 \
        --enable-local-file-access \
        --print-media-type \
        --no-outline \
        "$HTML_FILE" "$PDF_FILE"
    
    if [ $? -eq 0 ]; then
        echo "✅ PDF gerado com sucesso: $PDF_FILE"
        exit 0
    fi
fi

# Tentar usar chromium/chrome headless
if command -v chromium &> /dev/null || command -v chromium-browser &> /dev/null || command -v google-chrome &> /dev/null; then
    CHROME_CMD=""
    if command -v chromium &> /dev/null; then
        CHROME_CMD="chromium"
    elif command -v chromium-browser &> /dev/null; then
        CHROME_CMD="chromium-browser"
    elif command -v google-chrome &> /dev/null; then
        CHROME_CMD="google-chrome"
    fi
    
    if [ -n "$CHROME_CMD" ]; then
        echo "Usando $CHROME_CMD headless..."
        "$CHROME_CMD" \
            --headless \
            --disable-gpu \
            --print-to-pdf="$PDF_FILE" \
            --print-to-pdf-no-header \
            --no-pdf-header-footer \
            "file://$HTML_FILE"
        
        if [ $? -eq 0 ]; then
            echo "✅ PDF gerado com sucesso: $PDF_FILE"
            exit 0
        fi
    fi
fi

# Tentar usar pandoc para PDF (requer LaTeX)
echo "Tentando gerar PDF com pandoc (requer LaTeX)..."
pandoc "$MD_FILE" \
    --from markdown \
    --to pdf \
    --standalone \
    --toc \
    --toc-depth=3 \
    --highlight-style=github \
    --pdf-engine=xelatex \
    -V geometry:margin=2cm \
    -V lang=pt-BR \
    -o "$PDF_FILE" 2>/dev/null

if [ $? -eq 0 ] && [ -f "$PDF_FILE" ]; then
    echo "✅ PDF gerado com sucesso: $PDF_FILE"
    exit 0
else
    echo "⚠️  Não foi possível gerar PDF automaticamente."
    echo "Opções disponíveis:"
    echo "  1. Instalar wkhtmltopdf: sudo apt-get install wkhtmltopdf"
    echo "  2. Instalar chromium: sudo apt-get install chromium"
    echo "  3. Usar pandoc com LaTeX: sudo apt-get install texlive-xetex"
    echo ""
    echo "HTML gerado com sucesso: $HTML_FILE"
    echo "Você pode converter manualmente o HTML para PDF usando:"
    echo "  - Navegador: Abrir HTML e imprimir como PDF"
    echo "  - Online: Usar serviços como html2pdf.com"
    exit 0
fi


