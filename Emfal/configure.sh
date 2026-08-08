#!/bin/bash

# Script de configuracao automatica do template
# Uso: ./configure.sh <nome-projeto> <cliente> <descricao>

if [ $# -lt 3 ]; then
    echo "Uso: $0 <nome-projeto> <cliente> <descricao>"
    echo "Exemplo: $0 minha-personalizacao MinhaEmpresa 'Personalizacao para Minha Empresa'"
    exit 1
fi

PROJECT_NAME=$1
CLIENT_NAME=$2
DESCRIPTION=$3

echo "=== CONFIGURANDO TEMPLATE ==="
echo "Projeto: $PROJECT_NAME"
echo "Cliente: $CLIENT_NAME"
echo "Descricao: $DESCRIPTION"
echo ""

# 1. Atualizar pom.xml
echo "1. Atualizando pom.xml..."
sed -i "s/br.com.cliente/$CLIENT_NAME/g" pom.xml
sed -i "s/personalizacao-sankhya/$PROJECT_NAME/g" pom.xml
sed -i "s/Template para personalizacoes no Sankhya/$DESCRIPTION/g" pom.xml
sed -i "s/Personalizacao Sankhya - Template/$CLIENT_NAME - Personalizacao/g" pom.xml

# 2. Atualizar .project
echo "2. Atualizando .project..."
sed -i "s/<name>Template<\/name>/<name>$PROJECT_NAME<\/name>/g" .project

# 3. Criar estrutura de pacote do cliente
echo "3. Criando estrutura de pacote..."
CLIENT_PACKAGE=$(echo $CLIENT_NAME | tr '[:upper:]' '[:lower:]' | sed 's/\.//g')
mkdir -p "src/br/com/$CLIENT_PACKAGE/action/botaoAcao"

# 4. Mover e renomear classe
echo "4. Configurando classe principal..."
CLASS_NAME="${PROJECT_NAME^}"
cp src/br/com/cliente/action/botaoAcao/PersonalizacaoSankhya.java \
   "src/br/com/$CLIENT_PACKAGE/action/botaoAcao/$CLASS_NAME.java"

# 5. Atualizar pacote na classe
echo "5. Atualizando pacote na classe..."
sed -i "s/package br.com.cliente.action.botaoAcao;/package br.com.$CLIENT_PACKAGE.action.botaoAcao;/g" \
   "src/br/com/$CLIENT_PACKAGE/action/botaoAcao/$CLASS_NAME.java"
sed -i "s/class PersonalizacaoSankhya/class $CLASS_NAME/g" \
   "src/br/com/$CLIENT_PACKAGE/action/botaoAcao/$CLASS_NAME.java"

# 6. Remover estrutura antiga
echo "6. Limpando estrutura antiga..."
rm -rf src/br/com/cliente

# 7. Atualizar README
echo "7. Atualizando documentacao..."
sed -i "s/TEMPLATE PERSONALIZACAO SANKHYA/$CLIENT_NAME - PERSONALIZACAO SANKHYA/g" docs/README.md
sed -i "s/Template para criacao de personalizacoes Sankhya/$DESCRIPTION/g" docs/README.md

# 8. Compilar projeto
echo "8. Compilando projeto..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "=== CONFIGURACAO CONCLUIDA COM SUCESSO ==="
    echo "Projeto: $PROJECT_NAME"
    echo "Cliente: $CLIENT_NAME"
    echo "Classe: $CLASS_NAME"
    echo "Pacote: br.com.$CLIENT_PACKAGE.action.botaoAcao"
    echo ""
    echo "Próximos passos:"
    echo "1. Implementar logica na classe $CLASS_NAME"
    echo "2. Testar: mvn clean compile"
    echo "3. Gerar JAR: mvn package"
    echo "4. Atualizar documentacao"
else
    echo ""
    echo "=== ERRO NA COMPILACAO ==="
    echo "Verifique os logs acima para detalhes"
    exit 1
fi
