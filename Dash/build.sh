#!/bin/bash

echo "=========================================="
echo "Build Dashboard Financeiro - CICRANO"
echo "=========================================="
echo ""

if [ ! -d "lib" ]; then
    echo "❌ ERRO: Pasta lib/ não encontrada!"
    echo ""
    echo "Por favor, crie a pasta lib/ e adicione os JARs do Sankhya:"
    echo "  - SankhyaW-extensions.jar"
    echo "  - jape.jar"
    echo "  - mge-modelcore.jar"
    echo "  - sanutil.jar"
    echo "  - sanws.jar"
    echo ""
    echo "Consulte lib/README.md para mais informações."
    exit 1
fi

if [ ! -f "lib/SankhyaW-extensions.jar" ]; then
    echo "❌ ERRO: JARs do Sankhya não encontrados na pasta lib/"
    echo ""
    echo "Por favor, adicione os JARs necessários. Consulte lib/README.md"
    exit 1
fi

echo "✅ JARs encontrados"
echo ""

if command -v mvn &> /dev/null; then
    echo "Compilando com Maven..."
    mvn clean package install
    if [ $? -eq 0 ]; then
        echo ""
        echo "=========================================="
        echo "✅ BUILD SUCCESS"
        echo "=========================================="
        echo ""
        echo "JAR gerado em: target/personalizacao-dash-1.0.0.jar"
        echo ""
        echo "Próximos passos:"
        echo "1. Copie o JAR para a pasta de extensões do Sankhya"
        echo "2. Configure o servlet no web.xml do Sankhya"
        echo "3. Copie web/dashboard.jsp para a pasta web do Sankhya"
        echo "4. Acesse: http://seu-servidor/dash/dashboard.jsp"
    else
        echo ""
        echo "=========================================="
        echo "❌ BUILD FAILED"
        echo "=========================================="
        exit 1
    fi
else
    echo "⚠️  Maven não encontrado no PATH"
    echo ""
    echo "Por favor, instale o Maven ou adicione ao PATH"
    echo ""
    echo "Alternativamente, você pode compilar manualmente:"
    echo "  javac -cp \"lib/*\" -d target/classes src/**/*.java"
    echo ""
    exit 1
fi











