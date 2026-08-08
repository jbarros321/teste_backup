# 🍳 Receitas Sankhya Developer - Exemplos Práticos

## 🎯 **Visão Geral das Receitas**

As **Receitas** do Sankhya Developer são exemplos práticos e casos de uso que demonstram como implementar funcionalidades específicas usando as ferramentas e APIs da plataforma Sankhya. São guias passo-a-passo para soluções comuns.

## 🏗️ **Estrutura das Receitas**

### **Categorias Principais**
- **🔧 Personalização**: Adaptação de funcionalidades
- **🛠️ Customização**: Criação de novas funcionalidades
- **🔗 Integração**: Conectividade com sistemas externos
- **📊 Relatórios**: Geração de análises
- **🤖 Automação**: Automatização de processos
- **📱 Mobile**: Aplicações móveis

## 📋 **Receitas Disponíveis**

### **1. Receitas de Personalização**

#### **1.1 Personalização de Formulários**
- **Objetivo**: Adaptar formulários existentes
- **Tecnologias**: Dicionário de Dados, SankhyaJS
- **Aplicação**: Campos personalizados, validações

```javascript
// Exemplo: Validação personalizada de formulário
function validarFormularioPedido() {
    var nunota = document.getElementById('NUNOTA').value;
    var codparc = document.getElementById('CODPARC').value;
    var dtmov = document.getElementById('DTMOV').value;
    
    var erros = [];
    
    if (!nunota) {
        erros.push('Número da nota é obrigatório');
    }
    
    if (!codparc) {
        erros.push('Código do parceiro é obrigatório');
    }
    
    if (!dtmov) {
        erros.push('Data de movimento é obrigatória');
    }
    
    if (erros.length > 0) {
        alert('ERROS ENCONTRADOS:\n' + erros.join('\n'));
        return false;
    }
    
    return true;
}
```

#### **1.2 Personalização de Dashboards**
- **Objetivo**: Criar painéis personalizados
- **Tecnologias**: XML, SankhyaJS
- **Aplicação**: Visualização de dados

```xml
<!-- Exemplo: Dashboard personalizado -->
<gadget>
    <name>DashboardVendas</name>
    <title>Dashboard de Vendas</title>
    <type>chart</type>
    <data-source>
        <query>
            SELECT 
                TO_CHAR(DTMOV, 'MM/YYYY') AS MES,
                SUM(VLRNOTA) AS TOTAL_VENDAS
            FROM TGFCAB
            WHERE STATUSNOTA = 'L'
            AND DTMOV >= ADD_MONTHS(SYSDATE, -12)
            GROUP BY TO_CHAR(DTMOV, 'MM/YYYY')
            ORDER BY MES
        </query>
    </data-source>
    <chart-type>line</chart-type>
    <refresh-interval>300</refresh-interval>
</gadget>
```

#### **1.3 Personalização de Relatórios**
- **Objetivo**: Criar relatórios personalizados
- **Tecnologias**: iReport, SQL
- **Aplicação**: Análises e documentação

```sql
-- Exemplo: Relatório de vendas por vendedor
SELECT 
    V.NOMEVENDEDOR,
    COUNT(C.NUNOTA) AS QTD_PEDIDOS,
    SUM(C.VLRNOTA) AS TOTAL_VENDAS,
    AVG(C.VLRNOTA) AS MEDIA_VENDAS
FROM TGFCAB C
INNER JOIN TGFVEN V ON C.CODVEND = V.CODVEND
WHERE C.STATUSNOTA = 'L'
AND C.DTMOV >= :DATA_INICIO
AND C.DTMOV <= :DATA_FIM
GROUP BY V.NOMEVENDEDOR
ORDER BY TOTAL_VENDAS DESC
```

### **2. Receitas de Customização**

#### **2.1 Criação de Botões de Ação**
- **Objetivo**: Automatizar ações específicas
- **Tecnologias**: XML, SQL, Java
- **Aplicação**: Processamento de dados

```xml
<!-- Exemplo: Botão de ação para validar pedidos -->
<action-button>
    <name>ValidarPedidos</name>
    <label>Validar Pedidos</label>
    <type>database-routine</type>
    <routine>STP_VALIDAR_PEDIDOS</routine>
    <parameters>
        <parameter name="P_CODUSU" source="user" field="CODUSU"/>
        <parameter name="P_IDSESSAO" source="session" field="IDSESSAO"/>
        <parameter name="P_QTDLINHAS" source="selected-rows" field="count"/>
    </parameters>
    <validation>
        <required-fields>NUNOTA</required-fields>
        <business-rules>VALIDAR_STATUS_PEDIDO</business-rules>
    </validation>
</action-button>
```

```sql
-- Procedure para validar pedidos
CREATE OR REPLACE PROCEDURE STP_VALIDAR_PEDIDOS (
    P_CODUSU NUMBER,
    P_IDSESSAO VARCHAR2,
    P_QTDLINHAS NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    FIELD_NUNOTA NUMBER;
    P_STATUS_ATUAL VARCHAR2(1);
BEGIN
    FOR I IN 1..P_QTDLINHAS LOOP
        FIELD_NUNOTA := ACT_INT_FIELD(P_IDSESSAO, I, 'NUNOTA');
        
        SELECT STATUSNOTA INTO P_STATUS_ATUAL
        FROM TGFCAB
        WHERE NUNOTA = FIELD_NUNOTA;
        
        IF P_STATUS_ATUAL = 'L' THEN
            P_MENSAGEM := 'Pedido já confirmado!';
            RETURN;
        END IF;
        
        UPDATE TGFCAB
        SET STATUSNOTA = 'L',
            DTALTER = SYSDATE,
            USUALTER = P_CODUSU
        WHERE NUNOTA = FIELD_NUNOTA;
        
    END LOOP;
    
    P_MENSAGEM := 'Pedidos validados com sucesso!';
    COMMIT;
    
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        P_MENSAGEM := 'Erro ao validar pedidos: ' || SQLERRM;
END;
```

#### **2.2 Criação de Eventos Programados**
- **Objetivo**: Automatizar validações e processos
- **Tecnologias**: Java, Jape
- **Aplicação**: Regras de negócio

```java
// Exemplo: Evento programado para validar pedidos
package br.com.empresa.sankhya.event;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.dwfdata.vo.TgfcabVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.DynamicEntityManager;

public class ValidarPedidoEvent {
    
    private DynamicEntityManager dem;
    
    public ValidarPedidoEvent() {
        this.dem = EntityFacadeFactory.getDWFFacade().getDynamicEntityManager();
    }
    
    /**
     * Evento executado antes de inserir pedido
     */
    public void beforeInsert(DynamicVO pedido) throws Exception {
        // Validar campos obrigatórios
        if (pedido.asString("CODPARC") == null) {
            throw new Exception("Código do parceiro é obrigatório!");
        }
        
        if (pedido.asBigDecimal("VLRNOTA").compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Valor da nota deve ser maior que zero!");
        }
        
        // Validar crédito do cliente
        Integer codparc = pedido.asInteger("CODPARC");
        DynamicVO cliente = dem.findByPrimaryKey("TGFPAR", new Object[]{codparc});
        
        if (cliente != null) {
            BigDecimal credito = cliente.asBigDecimal("CREDITO");
            BigDecimal vlrnota = pedido.asBigDecimal("VLRNOTA");
            
            if (vlrnota.compareTo(credito) > 0) {
                throw new Exception("Valor excede crédito disponível do cliente!");
            }
        }
    }
    
    /**
     * Evento executado após inserir pedido
     */
    public void afterInsert(DynamicVO pedido) throws Exception {
        // Atualizar crédito do cliente
        Integer codparc = pedido.asInteger("CODPARC");
        BigDecimal vlrnota = pedido.asBigDecimal("VLRNOTA");
        
        DynamicVO cliente = dem.findByPrimaryKey("TGFPAR", new Object[]{codparc});
        if (cliente != null) {
            BigDecimal novoCredito = cliente.asBigDecimal("CREDITO").subtract(vlrnota);
            cliente.setProperty("CREDITO", novoCredito);
            dem.update(cliente);
        }
        
        // Enviar notificação
        enviarNotificacaoPedido(pedido);
    }
    
    private void enviarNotificacaoPedido(DynamicVO pedido) {
        // Implementar envio de notificação
        System.out.println("Pedido " + pedido.asInteger("NUNOTA") + " criado com sucesso!");
    }
}
```

#### **2.3 Criação de Componentes HTML5**
- **Objetivo**: Interfaces web modernas
- **Tecnologias**: SankhyaJS, HTML5, CSS, JavaScript
- **Aplicação**: Dashboards e formulários

```jsp
<!-- Exemplo: Componente HTML5 para dashboard de vendas -->
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<snk:query var="vendas">
    SELECT 
        TO_CHAR(DTMOV, 'MM/YYYY') AS MES,
        SUM(VLRNOTA) AS TOTAL_VENDAS,
        COUNT(*) AS QTD_PEDIDOS
    FROM TGFCAB
    WHERE STATUSNOTA = 'L'
    AND DTMOV >= ADD_MONTHS(SYSDATE, -12)
    GROUP BY TO_CHAR(DTMOV, 'MM/YYYY')
    ORDER BY MES
</snk:query>

<div class="dashboard-vendas">
    <h2>Dashboard de Vendas</h2>
    
    <div class="filtros">
        <label>Período:</label>
        <input type="date" id="dataInicio" />
        <input type="date" id="dataFim" />
        <button onclick="atualizarDashboard()">Atualizar</button>
    </div>
    
    <div class="grafico">
        <canvas id="graficoVendas" width="800" height="400"></canvas>
    </div>
    
    <div class="tabela">
        <table>
            <thead>
                <tr>
                    <th>Mês</th>
                    <th>Total de Vendas</th>
                    <th>Quantidade de Pedidos</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${vendas.rows}" var="row">
                    <tr>
                        <td>${row.MES}</td>
                        <td>R$ ${row.TOTAL_VENDAS}</td>
                        <td>${row.QTD_PEDIDOS}</td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

<script>
// Gráfico de vendas usando Chart.js
function criarGraficoVendas() {
    var ctx = document.getElementById('graficoVendas').getContext('2d');
    var dados = [
        <c:forEach items="${vendas.rows}" var="row" varStatus="status">
        {
            x: '${row.MES}',
            y: ${row.TOTAL_VENDAS}
        }${!status.last ? ',' : ''}
        </c:forEach>
    ];
    
    new Chart(ctx, {
        type: 'line',
        data: {
            datasets: [{
                label: 'Vendas',
                data: dados,
                borderColor: 'rgb(75, 192, 192)',
                tension: 0.1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

function atualizarDashboard() {
    var dataInicio = document.getElementById('dataInicio').value;
    var dataFim = document.getElementById('dataFim').value;
    
    // Atualizar dados via AJAX
    fetch('/sankhya/dashboard/vendas', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            dataInicio: dataInicio,
            dataFim: dataFim
        })
    })
    .then(response => response.json())
    .then(data => {
        // Atualizar gráfico e tabela
        atualizarGrafico(data);
        atualizarTabela(data);
    })
    .catch(error => {
        console.error('Erro ao atualizar dashboard:', error);
    });
}

// Inicializar gráfico quando a página carregar
document.addEventListener('DOMContentLoaded', function() {
    criarGraficoVendas();
});
</script>

<style>
.dashboard-vendas {
    padding: 20px;
    font-family: Arial, sans-serif;
}

.filtros {
    margin-bottom: 20px;
    padding: 10px;
    background-color: #f5f5f5;
    border-radius: 5px;
}

.filtros label {
    font-weight: bold;
    margin-right: 10px;
}

.filtros input, .filtros button {
    margin-right: 10px;
    padding: 5px;
}

.grafico {
    margin-bottom: 20px;
    text-align: center;
}

.tabela {
    overflow-x: auto;
}

.tabela table {
    width: 100%;
    border-collapse: collapse;
}

.tabela th, .tabela td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
}

.tabela th {
    background-color: #f2f2f2;
    font-weight: bold;
}
</style>
```

### **3. Receitas de Integração**

#### **3.1 Integração com E-commerce**
- **Objetivo**: Sincronizar dados de vendas
- **Tecnologias**: API REST, Webhooks
- **Aplicação**: Automação de processos

```javascript
// Exemplo: Integração com e-commerce
class EcommerceIntegration {
    constructor(apiUrl, token) {
        this.apiUrl = apiUrl;
        this.token = token;
    }
    
    // Sincronizar pedidos do e-commerce
    async sincronizarPedidos() {
        try {
            // Buscar pedidos pendentes no e-commerce
            const pedidosEcommerce = await this.buscarPedidosEcommerce();
            
            for (const pedido of pedidosEcommerce) {
                // Criar pedido no Sankhya
                const pedidoSankhya = await this.criarPedidoSankhya(pedido);
                
                // Atualizar status no e-commerce
                await this.atualizarStatusEcommerce(pedido.id, 'PROCESSADO');
                
                console.log(`Pedido ${pedido.id} sincronizado com sucesso`);
            }
            
        } catch (error) {
            console.error('Erro na sincronização:', error);
        }
    }
    
    async buscarPedidosEcommerce() {
        const response = await fetch(`${this.apiUrl}/pedidos?status=PENDENTE`, {
            headers: {
                'Authorization': `Bearer ${this.token}`,
                'Content-Type': 'application/json'
            }
        });
        
        return await response.json();
    }
    
    async criarPedidoSankhya(pedidoEcommerce) {
        const pedidoSankhya = {
            cliente: await this.buscarOuCriarCliente(pedidoEcommerce.cliente),
            dataPedido: pedidoEcommerce.dataPedido,
            observacoes: `Pedido do e-commerce: ${pedidoEcommerce.id}`,
            itens: await this.converterItens(pedidoEcommerce.itens)
        };
        
        const response = await fetch('/sankhya/api/pedidos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(pedidoSankhya)
        });
        
        return await response.json();
    }
    
    async buscarOuCriarCliente(clienteEcommerce) {
        // Buscar cliente existente
        let cliente = await this.buscarClienteSankhya(clienteEcommerce.email);
        
        if (!cliente) {
            // Criar novo cliente
            cliente = await this.criarClienteSankhya(clienteEcommerce);
        }
        
        return cliente.id;
    }
    
    async converterItens(itensEcommerce) {
        const itensSankhya = [];
        
        for (const item of itensEcommerce) {
            const produto = await this.buscarProdutoSankhya(item.sku);
            
            if (produto) {
                itensSankhya.push({
                    produto: produto.id,
                    quantidade: item.quantidade,
                    preco: item.preco,
                    desconto: item.desconto || 0
                });
            }
        }
        
        return itensSankhya;
    }
}

// Uso da integração
const integracao = new EcommerceIntegration('https://api.ecommerce.com', 'TOKEN_AQUI');

// Sincronizar pedidos a cada 5 minutos
setInterval(() => {
    integracao.sincronizarPedidos();
}, 5 * 60 * 1000);
```

#### **3.2 Integração com Sistema de Pagamento**
- **Objetivo**: Processar pagamentos automaticamente
- **Tecnologias**: API REST, Webhooks
- **Aplicação**: Automação financeira

```python
# Exemplo: Integração com sistema de pagamento
import requests
import json
from datetime import datetime

class PagamentoIntegration:
    def __init__(self, api_url, token):
        self.api_url = api_url
        self.token = token
        self.headers = {
            'Authorization': f'Bearer {token}',
            'Content-Type': 'application/json'
        }
    
    def processar_pagamento(self, pedido_id, valor, dados_pagamento):
        """Processar pagamento de um pedido"""
        try:
            # Criar transação no sistema de pagamento
            transacao = self.criar_transacao(pedido_id, valor, dados_pagamento)
            
            # Processar pagamento
            resultado = self.processar_transacao(transacao['id'])
            
            if resultado['status'] == 'APROVADO':
                # Atualizar status do pedido no Sankhya
                self.atualizar_status_pedido(pedido_id, 'PAGO')
                
                # Gerar nota fiscal
                self.gerar_nota_fiscal(pedido_id)
                
                return {'sucesso': True, 'transacao': transacao['id']}
            else:
                # Atualizar status do pedido
                self.atualizar_status_pedido(pedido_id, 'PAGAMENTO_NEGADO')
                
                return {'sucesso': False, 'erro': resultado['mensagem']}
                
        except Exception as e:
            return {'sucesso': False, 'erro': str(e)}
    
    def criar_transacao(self, pedido_id, valor, dados_pagamento):
        """Criar transação no sistema de pagamento"""
        dados = {
            'pedido_id': pedido_id,
            'valor': valor,
            'cartao': dados_pagamento['cartao'],
            'cvv': dados_pagamento['cvv'],
            'validade': dados_pagamento['validade'],
            'nome_portador': dados_pagamento['nome_portador']
        }
        
        response = requests.post(
            f'{self.api_url}/transacoes',
            headers=self.headers,
            json=dados
        )
        
        response.raise_for_status()
        return response.json()
    
    def processar_transacao(self, transacao_id):
        """Processar transação"""
        response = requests.post(
            f'{self.api_url}/transacoes/{transacao_id}/processar',
            headers=self.headers
        )
        
        response.raise_for_status()
        return response.json()
    
    def atualizar_status_pedido(self, pedido_id, status):
        """Atualizar status do pedido no Sankhya"""
        dados = {'status': status}
        
        response = requests.put(
            f'/sankhya/api/pedidos/{pedido_id}',
            headers=self.headers,
            json=dados
        )
        
        response.raise_for_status()
        return response.json()
    
    def gerar_nota_fiscal(self, pedido_id):
        """Gerar nota fiscal para pedido pago"""
        response = requests.post(
            f'/sankhya/api/pedidos/{pedido_id}/gerar-nota-fiscal',
            headers=self.headers
        )
        
        response.raise_for_status()
        return response.json()

# Uso da integração
integracao = PagamentoIntegration('https://api.pagamento.com', 'TOKEN_AQUI')

# Processar pagamento
resultado = integracao.processar_pagamento(
    pedido_id=12345,
    valor=150.00,
    dados_pagamento={
        'cartao': '4111111111111111',
        'cvv': '123',
        'validade': '12/25',
        'nome_portador': 'João Silva'
    }
)

if resultado['sucesso']:
    print(f"Pagamento processado com sucesso! Transação: {resultado['transacao']}")
else:
    print(f"Erro no pagamento: {resultado['erro']}")
```

### **4. Receitas de Automação**

#### **4.1 Automação de Relatórios**
- **Objetivo**: Gerar relatórios automaticamente
- **Tecnologias**: iReport, SQL, Java
- **Aplicação**: Análises periódicas

```java
// Exemplo: Automação de relatórios
package br.com.empresa.sankhya.automation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RelatorioAutomation {
    
    private Connection connection;
    
    public RelatorioAutomation(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Gerar relatório de vendas mensal
     */
    public void gerarRelatorioVendasMensal() {
        try {
            // Buscar dados de vendas
            Map<String, Object> dados = buscarDadosVendas();
            
            // Gerar relatório
            String arquivo = gerarRelatorio(dados, "relatorio_vendas_mensal");
            
            // Enviar por email
            enviarRelatorioPorEmail(arquivo, "Relatório de Vendas Mensal");
            
            System.out.println("Relatório de vendas mensal gerado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }
    
    private Map<String, Object> buscarDadosVendas() {
        Map<String, Object> dados = new HashMap<>();
        
        try {
            String sql = """
                SELECT 
                    V.NOMEVENDEDOR,
                    COUNT(C.NUNOTA) AS QTD_PEDIDOS,
                    SUM(C.VLRNOTA) AS TOTAL_VENDAS,
                    AVG(C.VLRNOTA) AS MEDIA_VENDAS
                FROM TGFCAB C
                INNER JOIN TGFVEN V ON C.CODVEND = V.CODVEND
                WHERE C.STATUSNOTA = 'L'
                AND C.DTMOV >= TRUNC(SYSDATE, 'MM')
                AND C.DTMOV < ADD_MONTHS(TRUNC(SYSDATE, 'MM'), 1)
                GROUP BY V.NOMEVENDEDOR
                ORDER BY TOTAL_VENDAS DESC
            """;
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String vendedor = rs.getString("NOMEVENDEDOR");
                int qtdPedidos = rs.getInt("QTD_PEDIDOS");
                double totalVendas = rs.getDouble("TOTAL_VENDAS");
                double mediaVendas = rs.getDouble("MEDIA_VENDAS");
                
                dados.put(vendedor, Map.of(
                    "qtdPedidos", qtdPedidos,
                    "totalVendas", totalVendas,
                    "mediaVendas", mediaVendas
                ));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar dados de vendas", e);
        }
        
        return dados;
    }
    
    private String gerarRelatorio(Map<String, Object> dados, String nomeRelatorio) {
        try {
            // Criar arquivo de relatório
            String nomeArquivo = nomeRelatorio + "_" + new Date().getTime() + ".pdf";
            File arquivo = new File("/relatorios/" + nomeArquivo);
            
            // Gerar PDF usando iReport
            // (Implementação específica do iReport)
            
            return arquivo.getAbsolutePath();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório", e);
        }
    }
    
    private void enviarRelatorioPorEmail(String arquivo, String assunto) {
        try {
            // Implementar envio de email
            System.out.println("Enviando relatório por email: " + arquivo);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar email", e);
        }
    }
}
```

#### **4.2 Automação de Importação de Dados**
- **Objetivo**: Importar dados de sistemas externos
- **Tecnologias**: Java, SQL, CSV/Excel
- **Aplicação**: Sincronização de dados

```java
// Exemplo: Automação de importação de produtos
package br.com.empresa.sankhya.automation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ImportacaoProdutos {
    
    private Connection connection;
    
    public ImportacaoProdutos(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Importar produtos de arquivo CSV
     */
    public void importarProdutosCSV(String arquivoCSV) {
        try {
            List<Produto> produtos = lerArquivoCSV(arquivoCSV);
            
            for (Produto produto : produtos) {
                if (produtoExiste(produto.getCodigo())) {
                    atualizarProduto(produto);
                } else {
                    inserirProduto(produto);
                }
            }
            
            System.out.println("Importação de produtos concluída com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro na importação: " + e.getMessage());
        }
    }
    
    private List<Produto> lerArquivoCSV(String arquivo) {
        List<Produto> produtos = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            boolean primeiraLinha = true;
            
            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue; // Pular cabeçalho
                }
                
                String[] campos = linha.split(",");
                
                Produto produto = new Produto();
                produto.setCodigo(campos[0]);
                produto.setNome(campos[1]);
                produto.setDescricao(campos[2]);
                produto.setPreco(Double.parseDouble(campos[3]));
                produto.setAtivo("S".equals(campos[4]));
                
                produtos.add(produto);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler arquivo CSV", e);
        }
        
        return produtos;
    }
    
    private boolean produtoExiste(String codigo) {
        try {
            String sql = "SELECT COUNT(*) FROM TGFPRO WHERE CODPROD = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, codigo);
            
            return stmt.executeQuery().getInt(1) > 0;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar produto", e);
        }
    }
    
    private void inserirProduto(Produto produto) {
        try {
            String sql = """
                INSERT INTO TGFPRO (
                    CODPROD, DESCRPROD, DESCRPRODCOMPL, 
                    VLRVENDA, ATIVO, DTALTER, USUALTER
                ) VALUES (?, ?, ?, ?, ?, SYSDATE, 1)
            """;
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, produto.getCodigo());
            stmt.setString(2, produto.getNome());
            stmt.setString(3, produto.getDescricao());
            stmt.setDouble(4, produto.getPreco());
            stmt.setString(5, produto.isAtivo() ? "S" : "N");
            
            stmt.executeUpdate();
            
            System.out.println("Produto inserido: " + produto.getCodigo());
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }
    
    private void atualizarProduto(Produto produto) {
        try {
            String sql = """
                UPDATE TGFPRO SET
                    DESCRPROD = ?,
                    DESCRPRODCOMPL = ?,
                    VLRVENDA = ?,
                    ATIVO = ?,
                    DTALTER = SYSDATE,
                    USUALTER = 1
                WHERE CODPROD = ?
            """;
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setDouble(3, produto.getPreco());
            stmt.setString(4, produto.isAtivo() ? "S" : "N");
            stmt.setString(5, produto.getCodigo());
            
            stmt.executeUpdate();
            
            System.out.println("Produto atualizado: " + produto.getCodigo());
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }
    
    // Classe para representar produto
    public static class Produto {
        private String codigo;
        private String nome;
        private String descricao;
        private double preco;
        private boolean ativo;
        
        // Getters e setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        
        public double getPreco() { return preco; }
        public void setPreco(double preco) { this.preco = preco; }
        
        public boolean isAtivo() { return ativo; }
        public void setAtivo(boolean ativo) { this.ativo = ativo; }
    }
}
```

## 🎯 **Casos de Uso por Categoria**

### **Personalização**
- **Formulários**: Adaptação de campos e validações
- **Dashboards**: Criação de painéis personalizados
- **Relatórios**: Geração de análises customizadas
- **Workflows**: Automação de processos

### **Customização**
- **Botões de Ação**: Automação de ações específicas
- **Eventos Programados**: Validações e triggers
- **Componentes HTML5**: Interfaces modernas
- **APIs**: Integração com sistemas externos

### **Integração**
- **E-commerce**: Sincronização de vendas
- **Pagamentos**: Processamento automático
- **ERP**: Integração de sistemas
- **CRM**: Sincronização de clientes

### **Automação**
- **Relatórios**: Geração periódica
- **Importação**: Dados em lote
- **Notificações**: Alertas automáticos
- **Backup**: Cópia de segurança

## 🚀 **Próximos Passos**

### **Exploração Detalhada**
1. **Análise Individual**: Cada receita em detalhes
2. **Exemplos Práticos**: Código funcional
3. **Casos de Uso**: Aplicações reais
4. **Boas Práticas**: Padrões e convenções
5. **Troubleshooting**: Solução de problemas

### **Consolidação**
- **Documentação Unificada**: Guia consolidado
- **Exemplos Completos**: Código funcional
- **Casos de Uso**: Aplicações práticas
- **Referência Rápida**: Índice de funcionalidades

---

*Este documento representa a estrutura completa das Receitas disponíveis no Sankhya Developer, baseado na análise sistemática da documentação oficial.*
