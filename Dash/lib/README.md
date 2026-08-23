# Dependências Sankhya - Dash

## 📦 JARs Necessários

Esta pasta deve conter os seguintes JARs do Sankhya:

### Obrigatórios

1. **SankhyaW-extensions.jar**
   - Extensões do Sankhya
   - Localização no SDK: `api_sankhya/SankhyaW-extensions.jar`

2. **jape.jar**
   - Java Persistence API do Sankhya
   - Localização no SDK: `api_sankhya/jape.jar`

3. **mge-modelcore.jar**
   - Core do modelo Sankhya
   - Localização no SDK: `api_sankhya/mge-modelcore.jar`

4. **sanutil.jar**
   - Utilitários Sankhya
   - Localização no SDK: `api_sankhya/sanutil.jar`

5. **sanws.jar**
   - ServiceContext e funcionalidades web
   - Localização no SDK: `api_sankhya/sanws.jar`

## 📥 Como Obter

### Opção 1: SDK Sankhya
1. Baixe o SDK do Sankhya
2. Navegue até a pasta `api_sankhya/`
3. Copie os JARs listados acima para esta pasta `lib/`

### Opção 2: Copiar de Outro Projeto
Se você já tem outro projeto com os JARs:
```bash
cp /caminho/outro/projeto/lib/*.jar /caminho/Dash/lib/
```

### Opção 3: Servidor Sankhya
Os JARs geralmente estão na pasta de instalação do Sankhya:
- Windows: `C:\Sankhya\...`
- Linux: `/opt/sankhya/...`

## ⚠️ Importante

- **NÃO** commite os JARs no repositório Git (devem estar no .gitignore)
- Os JARs são específicos da versão do Sankhya
- Sem estes JARs, o projeto **NÃO compilará**

## ✅ Verificação

Após copiar os JARs, verifique se todos estão presentes:
```bash
ls -la lib/
```

Você deve ver os 5 JARs listados acima.











