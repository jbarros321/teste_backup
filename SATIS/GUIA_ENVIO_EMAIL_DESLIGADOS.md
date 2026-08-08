# 📘 Guia Passo a Passo: Acerto e Envio de E-mail (Vendedores Desligados) - Santis

Este documento orienta o fluxo correto para realizar o acerto financeiro e a comunicação com vendedores que saíram da empresa. Siga rigorosamente a ordem abaixo.

---

### 🔍 Pré-requisito: Cadastro do E-mail Particular
Antes de iniciar os passos, confirme se o campo **"E-MAIL PARTICULAR"** no cadastro do vendedor está preenchido. Como o vendedor foi desligado, o sistema enviará o acerto para o e-mail pessoal dele. **Sem isso, o envio falhará.**

---

### 1️⃣ Passo: Preencher os "Dias Trabalhados"
Se o vendedor recebe **Remuneração Fixa**, você deve informar quantos dias ele trabalhou no mês do desligamento.
*   **Onde fazer:** Na tela de fechamento, localize a coluna **"DIAS TRABALHADOS"** e preencha o valor.
*   **Por que:** Esse dado é essencial para o sistema calcular o valor proporcional da Remuneração Fixa. Sem ele, o cálculo ficará errado e as notas não poderão ser geradas.
---
### 2️⃣ Passo: Gerar as Notas (Pedidos de Compra)
Com os dias preenchidos e os valores de comissão conferidos (incluindo a **Comissão Futura** na coluna específica), processe o pagamento oficial.
*   **Como fazer:** Clique no botão de ações (ícone de raio ⚡ ou engrenagem) e selecione a opção **"Gerar Pedidos/Notas de Comissão"**.
*   Este passo cria o documento financeiro oficial que garante o pagamento ao vendedor.
---
### 3️⃣ Passo: Enviar o E-mail de Acerto (Botão de Desligados)
**CUIDADO:** Utilize o botão específico para desligados. Ele é separado do botão de envio padrão de ativos.

1.  Selecione o vendedor na lista de fechamento.
2.  Clique no botão **"Ações"** (ícone de raio ⚡ ou engrenagem).
3.  Escolha a opção: **"Enviar E-mail de Acerto (Vendedor Desligado)"**.
    > **Dica:** Este botão gera a tabela detalhada com todos os valores (Fixo + Comissão + Comissão Futura - Adiantamentos).
4.  Aguarde a mensagem: *"E-mail enviado com sucesso!"*.

---

### 📧 O que o Vendedor Recebe?
O vendedor recebe um e-mail profissional com uma tabela organizada:
*   ✅ **Fixo / Remuneração** (Proporcional aos dias trabalhados)
*   ✅ **Comissões do Mês** (Vendas já baixadas)
*   ✅ **Comissão Futura** (Valores que restavam a receber de vendas parceladas)
*   ✅ **Extras e Reembolsos**
*   ❌ **Desconto de Adiantamentos** (Dedução de valores antecipados)
*   💰 **VALOR LÍQUIDO FINAL**

---

### ⚙️ OBJETOS TÉCNICOS (Para Suporte e TI)
Estes são os componentes que fazem a rotina funcionar:

*   **Gerador do E-mail (SQL)**: `STP_EMAILVENDDESLIGADO_SATIS`
*   **Cálculo do Resumo**: `DB_ATUALIZA_RESUMO_FECH`
*   **Geração de Notas/Pedidos**: `STP_FECHA_COMISSAO_SATIS`
*   **Tabelas Principais**: `AD_DBFECHCOMFIN` e `AD_DBFECHCOMNOTASA`
