# 📋 Campo Adicional - Integração Neogrid Pet Kids

## 📊 Informações do Campo

### Campo Adicional
- **Nome do Campo**: `AD_INTEGRANEOGRID`
- **Tabela**: `TGFPAR` (Parceiros)
- **Tipo**: `CHAR(1)`
- **Valores Aceitos**: `'S'` (Sim) ou `'N'` (Não)
- **Valor Padrão**: `'N'`
- **Obrigatório**: Não

### Localização no Sistema
- **Tela**: Cadastro de Parceiros
- **Aba**: "Geral"
- **Campo**: "Integração Neogrid:"
- **Tipo de Controle**: Toggle/Checkbox (Sim/Não)

## 🎯 Funcionalidade

Este campo controla quais **fornecedores/parceiros** devem ser incluídos na integração com a plataforma Neogrid.

### Quando `AD_INTEGRANEOGRID = 'S'`:

O parceiro/fornecedor será incluído nos seguintes relatórios:

1. **Relatório de Produtos (RELPRO)**
   - Apenas produtos de fornecedores marcados serão incluídos

2. **Relatório de Vendas (VENDAS)**
   - Apenas vendas com produtos de fornecedores marcados serão incluídas

3. **Relatório de Estoque (RELEST)**
   - Apenas estoque de produtos de fornecedores marcados será incluído

### Quando `AD_INTEGRANEOGRID = 'N'` ou NULL:

O parceiro/fornecedor **NÃO** será incluído nos relatórios de integração.

## 📝 Script SQL

O script SQL para criar o campo está disponível em:
```
src/main/sql/AD_INTEGRANEOGRID.sql
```

### Como Executar

1. Conecte-se ao banco de dados do Sankhya
2. Execute o script `AD_INTEGRANEOGRID.sql`
3. Verifique se o campo foi criado corretamente
4. O campo aparecerá automaticamente no cadastro de Parceiros

## 🔧 Como Configurar

1. Acesse o **Cadastro de Parceiros** no sistema Sankhya
2. Abra o parceiro/fornecedor que deseja incluir na integração
3. Acesse a aba **"Geral"**
4. Localize o campo **"Integração Neogrid:"**
5. **Marque a opção** (ativo) para incluir o parceiro na integração
6. Salve o cadastro

## ✅ Validações

O sistema valida automaticamente que apenas parceiros com as seguintes condições serão incluídos:

- ✅ **Parceiro Ativo**: `PAR.ATIVO = 'S'`
- ✅ **Integração Neogrid Marcada**: `PAR.AD_INTEGRANEOGRID = 'S'`
- ✅ **Produto Ativo**: `PRO.ATIVO = 'S'` (para produtos)

## 📌 Observações Importantes

1. **Produtos**: Apenas produtos de fornecedores com `AD_INTEGRANEOGRID = 'S'` serão incluídos no relatório de produtos

2. **Vendas**: Apenas vendas com produtos de fornecedores com `AD_INTEGRANEOGRID = 'S'` serão incluídas

3. **Estoque**: Apenas estoque de produtos de fornecedores com `AD_INTEGRANEOGRID = 'S'` será incluído

4. **Flexibilidade**: Esta configuração permite escolher quais fornecedores serão incluídos na integração com a Neogrid, sem precisar filtrar por CNPJ específico

## 🔍 Verificação

Para verificar se o campo foi criado corretamente, execute:

```sql
SELECT NOMETAB, NOMECAMPO, DESCRCAMPO, TIPO, TAMANHO
FROM AD_CAMPOS
WHERE NOMETAB = 'TGFPAR'
  AND NOMECAMPO = 'AD_INTEGRANEOGRID';
```

## 📚 Referências

- Documentação Técnica: `docs/DOCUMENTACAO_TECNICA.md`
- Script SQL: `src/main/sql/AD_INTEGRANEOGRID.sql`
- Código que utiliza o campo:
  - `src/br/com/petkids/neogrid/repository/ProdutosRepository.java`
  - `src/br/com/petkids/neogrid/repository/VendasRepository.java`
  - `src/br/com/petkids/neogrid/repository/EstoqueRepository.java`
