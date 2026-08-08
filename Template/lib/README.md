# 📦 Bibliotecas Sankhya

Esta pasta contém as bibliotecas JAR necessárias para compilar e executar personalizações Sankhya.

## 📋 JARs Necessários

Os seguintes JARs são obrigatórios para o funcionamento do projeto:

1. **SankhyaW-extensions.jar** (52 KB)
   - Extensões do SankhyaW
   - Classes base para personalizações

2. **jape.jar** (759 KB)
   - Framework de persistência JAPE
   - Gerenciamento de entidades e transações

3. **mge-modelcore.jar** (18 MB)
   - Core do modelo Sankhya
   - Classes principais do sistema

4. **sanutil.jar** (390 KB)
   - Utilitários Sankhya
   - Funções auxiliares e helpers

5. **sanws.jar** (90 KB)
   - Web Services Sankhya
   - Integrações e APIs

## 📍 Origem dos JARs

Estes JARs são fornecidos pela instalação do Sankhya e normalmente estão localizados em:

- **Windows**: `C:\Sankhya\SankhyaW\lib\`
- **Linux**: `/opt/sankhya/lib/` ou similar
- **Servidor**: Diretório de instalação do SankhyaW

## ⚠️ Importante

- **Não versionar estes JARs** no controle de versão (adicionar ao `.gitignore`)
- Estes são arquivos proprietários do Sankhya
- Cada ambiente pode ter versões diferentes dos JARs
- Sempre use os JARs da instalação Sankhya do ambiente de destino

## 🔧 Como Obter os JARs

1. **Do servidor Sankhya**:
   - Acesse o servidor onde o Sankhya está instalado
   - Copie os JARs do diretório de instalação

2. **Do projeto Denver**:
   - Os JARs foram copiados do projeto Denver como referência
   - Substitua pelos JARs do seu ambiente quando necessário

3. **Verificação**:
   - Execute `mvn clean package install` para validar
   - O build deve completar com sucesso (`BUILD SUCCESS`)

## 📝 Notas

- Os JARs são marcados como `optional=true` no `pom.xml`
- O escopo é `system` para indicar dependências locais
- O Maven pode mostrar warnings sobre `systemPath`, mas isso é esperado

---

**Última Atualização**: 2025-12-06  
**Versão dos JARs**: Compatível com SankhyaW 4.8+




