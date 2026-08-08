# Bibliotecas Sankhya

Este diretório deve conter os seguintes arquivos JAR necessários para compilação:

- `SankhyaW-extensions.jar` - Extensões do SankhyaW
- `jape.jar` - Java Application Platform Engine
- `mge-modelcore.jar` - Model Core do Sankhya
- `sanutil.jar` - Utilitários Sankhya

## Como obter os JARs

Estes arquivos JAR são fornecidos pelo Sankhya e não estão disponíveis no Maven Central. Eles devem ser obtidos:

1. **Do ambiente de desenvolvimento Sankhya instalado localmente** (recomendado)
2. Do diretório de instalação do Sankhya (geralmente em `[INSTALACAO_SANKHYA]/lib/`)
3. Através do suporte técnico Sankhya

### Opção 1: Usar o script automático (Recomendado)

Execute o script helper que tenta localizar e copiar os JARs automaticamente:

```bash
./copy-sankhya-jars.sh
```

O script irá:
- Procurar os JARs em locais comuns de instalação do Sankhya
- Solicitar o caminho se não encontrar automaticamente
- Copiar os JARs encontrados para o diretório `lib/`

### Opção 2: Copiar manualmente

Se você sabe onde estão os JARs, copie-os manualmente:

```bash
# Exemplo (ajuste o caminho conforme sua instalação)
cp /caminho/para/sankhya/lib/SankhyaW-extensions.jar lib/
cp /caminho/para/sankhya/lib/jape.jar lib/
cp /caminho/para/sankhya/lib/mge-modelcore.jar lib/
cp /caminho/para/sankhya/lib/sanutil.jar lib/
```

## Localização típica dos arquivos

Os arquivos geralmente estão localizados em:
- `[SANKHYA_HOME]/lib/SankhyaW-extensions.jar`
- `[SANKHYA_HOME]/lib/jape.jar`
- `[SANKHYA_HOME]/lib/mge-modelcore.jar`
- `[SANKHYA_HOME]/lib/sanutil.jar`

## Nota Importante

⚠️ **Os placeholders vazios permitem que o Maven resolva as dependências, mas a compilação falhará porque as classes Sankhya não estarão disponíveis.**

Para compilar o projeto com sucesso, você **DEVE** substituir os placeholders pelos JARs reais do Sankhya antes de executar `mvn clean package`.

## Placeholders

Se você executou o script `create-lib-placeholders.sh`, arquivos JAR vazios foram criados como placeholders. Estes placeholders:
- ✅ Permitem que o Maven resolva as dependências sem erros
- ❌ NÃO contêm as classes necessárias para compilação
- ⚠️ DEVEM ser substituídos pelos JARs reais antes de compilar

