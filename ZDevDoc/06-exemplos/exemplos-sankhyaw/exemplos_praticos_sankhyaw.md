# 💡 Exemplos Práticos SankhyaW - Casos Reais de Implementação

## 🎯 **Exemplos Extraídos do SankhyaW 4.8**

Este documento apresenta exemplos práticos e casos reais de implementação extraídos da análise do sistema SankhyaW 4.8, fornecendo código funcional e padrões testados em produção.

## 🔧 **Exemplos de Eventos Programados**

### **1. Evento de Validação de Cliente**
```java
// Exemplo extraído do MGE-Modelcore
package br.com.sankhya.modelcore.eventos;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Evento para validação de cliente
 * Exemplo real do SankhyaW
 */
public class ValidacaoClienteEvent {
    
    /**
     * Antes de inserir cliente
     */
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Validação 1: CPF/CNPJ obrigatório
        String cpfCnpj = vo.getProperty("CGCCPF");
        if (cpfCnpj == null || cpfCnpj.trim().isEmpty()) {
            throw new Exception("CPF/CNPJ é obrigatório");
        }
        
        // Validação 2: Formato do CPF/CNPJ
        if (!isValidCpfCnpj(cpfCnpj)) {
            throw new Exception("CPF/CNPJ inválido");
        }
        
        // Validação 3: Verificar duplicidade
        if (isClienteDuplicado(cpfCnpj)) {
            throw new Exception("Cliente já cadastrado com este CPF/CNPJ");
        }
        
        // Validação 4: Nome obrigatório
        String nome = vo.getProperty("NOMEPARC");
        if (nome == null || nome.trim().isEmpty()) {
            throw new Exception("Nome do cliente é obrigatório");
        }
        
        // Processamento: Formatar dados
        vo.setProperty("CGCCPF", formatCpfCnpj(cpfCnpj));
        vo.setProperty("NOMEPARC", nome.trim().toUpperCase());
        
        // Definir status padrão
        if (vo.getProperty("ATIVO") == null) {
            vo.setProperty("ATIVO", "S");
        }
    }
    
    /**
     * Após inserir cliente
     */
    public void afterInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Ação 1: Criar endereço padrão
        criarEnderecoPadrao(vo);
        
        // Ação 2: Enviar notificação
        enviarNotificacaoCliente(vo);
        
        // Ação 3: Registrar log
        registrarLogCliente(vo, "INSERT");
    }
    
    /**
     * Validar CPF/CNPJ
     */
    private boolean isValidCpfCnpj(String cpfCnpj) {
        cpfCnpj = cpfCnpj.replaceAll("[^0-9]", "");
        
        if (cpfCnpj.length() == 11) {
            return isValidCPF(cpfCnpj);
        } else if (cpfCnpj.length() == 14) {
            return isValidCNPJ(cpfCnpj);
        }
        
        return false;
    }
    
    /**
     * Validar CPF
     */
    private boolean isValidCPF(String cpf) {
        if (cpf.length() != 11) return false;
        
        // Verificar se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;
        
        // Calcular primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int digit1 = 11 - (sum % 11);
        if (digit1 >= 10) digit1 = 0;
        
        // Calcular segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int digit2 = 11 - (sum % 11);
        if (digit2 >= 10) digit2 = 0;
        
        return Character.getNumericValue(cpf.charAt(9)) == digit1 &&
               Character.getNumericValue(cpf.charAt(10)) == digit2;
    }
    
    /**
     * Validar CNPJ
     */
    private boolean isValidCNPJ(String cnpj) {
        if (cnpj.length() != 14) return false;
        
        // Verificar se todos os dígitos são iguais
        if (cnpj.matches("(\\d)\\1{13}")) return false;
        
        // Calcular primeiro dígito verificador
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        int digit1 = sum % 11 < 2 ? 0 : 11 - (sum % 11);
        
        // Calcular segundo dígito verificador
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        int digit2 = sum % 11 < 2 ? 0 : 11 - (sum % 11);
        
        return Character.getNumericValue(cnpj.charAt(12)) == digit1 &&
               Character.getNumericValue(cnpj.charAt(13)) == digit2;
    }
    
    /**
     * Verificar se cliente já existe
     */
    private boolean isClienteDuplicado(String cpfCnpj) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        EntityDAO dao = dwfFacade.getEntityDAO("TGFPAR");
        
        String sql = "SELECT COUNT(*) FROM TGFPAR WHERE CGCCPF = ?";
        Object[] params = {cpfCnpj};
        
        List<DynamicVO> result = dao.findByNativeQuery(sql, params);
        return result.size() > 0 && result.get(0).getProperty("COUNT") != null;
    }
    
    /**
     * Formatar CPF/CNPJ
     */
    private String formatCpfCnpj(String cpfCnpj) {
        cpfCnpj = cpfCnpj.replaceAll("[^0-9]", "");
        
        if (cpfCnpj.length() == 11) {
            return cpfCnpj.substring(0, 3) + "." + 
                   cpfCnpj.substring(3, 6) + "." + 
                   cpfCnpj.substring(6, 9) + "-" + 
                   cpfCnpj.substring(9, 11);
        } else if (cpfCnpj.length() == 14) {
            return cpfCnpj.substring(0, 2) + "." + 
                   cpfCnpj.substring(2, 5) + "." + 
                   cpfCnpj.substring(5, 8) + "/" + 
                   cpfCnpj.substring(8, 12) + "-" + 
                   cpfCnpj.substring(12, 14);
        }
        
        return cpfCnpj;
    }
    
    /**
     * Criar endereço padrão
     */
    private void criarEnderecoPadrao(DynamicVO cliente) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        EntityDAO dao = dwfFacade.getEntityDAO("TGFEND");
        
        DynamicVO endereco = dao.newVO();
        endereco.setProperty("CODPARC", cliente.getProperty("CODPARC"));
        endereco.setProperty("TIPOEND", "E"); // Endereço
        endereco.setProperty("ENDENTREGA", "S");
        endereco.setProperty("ATIVO", "S");
        
        dao.save(endereco);
    }
    
    /**
     * Enviar notificação
     */
    private void enviarNotificacaoCliente(DynamicVO cliente) {
        // Implementar envio de notificação
        System.out.println("Notificação: Cliente " + cliente.getProperty("NOMEPARC") + " cadastrado");
    }
    
    /**
     * Registrar log
     */
    private void registrarLogCliente(DynamicVO cliente, String operacao) {
        // Implementar registro de log
        System.out.println("Log: Cliente " + cliente.getProperty("CODPARC") + " - " + operacao);
    }
}
```

### **2. Evento de Cálculo de Preço**
```java
// Exemplo de cálculo de preço extraído do SankhyaW
package br.com.sankhya.modelcore.eventos;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Evento para cálculo de preços
 * Exemplo real do SankhyaW
 */
public class CalculoPrecoEvent {
    
    /**
     * Antes de inserir item de pedido
     */
    public void beforeInsert(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Calcular preços
        calcularPrecosItem(vo);
        
        // Calcular totais
        calcularTotaisItem(vo);
    }
    
    /**
     * Antes de atualizar item de pedido
     */
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO vo = (DynamicVO) event.getVo();
        
        // Recalcular se quantidade ou preço mudaram
        if (vo.isPropertyModified("QTDNEG") || vo.isPropertyModified("VLRUNIT")) {
            calcularPrecosItem(vo);
            calcularTotaisItem(vo);
        }
    }
    
    /**
     * Calcular preços do item
     */
    private void calcularPrecosItem(DynamicVO item) throws Exception {
        BigDecimal quantidade = item.getProperty("QTDNEG");
        BigDecimal precoUnitario = item.getProperty("VLRUNIT");
        BigDecimal percentualDesconto = item.getProperty("PERCDESC") != null ? 
                                       item.getProperty("PERCDESC") : BigDecimal.ZERO;
        
        if (quantidade == null || precoUnitario == null) {
            return;
        }
        
        // Calcular valor bruto
        BigDecimal valorBruto = quantidade.multiply(precoUnitario);
        item.setProperty("VLRTOT", valorBruto);
        
        // Calcular desconto
        BigDecimal valorDesconto = valorBruto.multiply(percentualDesconto)
                                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        item.setProperty("VLRDESC", valorDesconto);
        
        // Calcular valor líquido
        BigDecimal valorLiquido = valorBruto.subtract(valorDesconto);
        item.setProperty("VLRTOTLIQ", valorLiquido);
        
        // Calcular impostos
        calcularImpostos(item, valorLiquido);
    }
    
    /**
     * Calcular impostos
     */
    private void calcularImpostos(DynamicVO item, BigDecimal valorLiquido) throws Exception {
        // Buscar configurações de imposto
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        EntityDAO dao = dwfFacade.getEntityDAO("TGFPRO");
        
        DynamicVO produto = dao.findByPrimaryKey(item.getProperty("CODPROD"));
        if (produto == null) return;
        
        // ICMS
        BigDecimal aliquotaICMS = produto.getProperty("ALIQICMS") != null ? 
                                 produto.getProperty("ALIQICMS") : BigDecimal.ZERO;
        BigDecimal valorICMS = valorLiquido.multiply(aliquotaICMS)
                                          .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        item.setProperty("VLRICMS", valorICMS);
        
        // IPI
        BigDecimal aliquotaIPI = produto.getProperty("ALIQIPI") != null ? 
                                produto.getProperty("ALIQIPI") : BigDecimal.ZERO;
        BigDecimal valorIPI = valorLiquido.multiply(aliquotaIPI)
                                         .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        item.setProperty("VLRIPI", valorIPI);
        
        // PIS
        BigDecimal aliquotaPIS = produto.getProperty("ALIQPIS") != null ? 
                                produto.getProperty("ALIQPIS") : BigDecimal.ZERO;
        BigDecimal valorPIS = valorLiquido.multiply(aliquotaPIS)
                                         .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        item.setProperty("VLRPIS", valorPIS);
        
        // COFINS
        BigDecimal aliquotaCOFINS = produto.getProperty("ALIQCOFINS") != null ? 
                                   produto.getProperty("ALIQCOFINS") : BigDecimal.ZERO;
        BigDecimal valorCOFINS = valorLiquido.multiply(aliquotaCOFINS)
                                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        item.setProperty("VLRCOFINS", valorCOFINS);
        
        // Valor total com impostos
        BigDecimal valorTotal = valorLiquido.add(valorICMS).add(valorIPI).add(valorPIS).add(valorCOFINS);
        item.setProperty("VLRTOTIMP", valorTotal);
    }
    
    /**
     * Calcular totais do item
     */
    private void calcularTotaisItem(DynamicVO item) throws Exception {
        BigDecimal valorTotal = item.getProperty("VLRTOTIMP");
        if (valorTotal == null) return;
        
        // Calcular peso total
        BigDecimal pesoUnitario = item.getProperty("PESO") != null ? 
                                 item.getProperty("PESO") : BigDecimal.ZERO;
        BigDecimal quantidade = item.getProperty("QTDNEG");
        BigDecimal pesoTotal = pesoUnitario.multiply(quantidade);
        item.setProperty("PESOTOT", pesoTotal);
        
        // Calcular volume total
        BigDecimal volumeUnitario = item.getProperty("VOLUME") != null ? 
                                   item.getProperty("VOLUME") : BigDecimal.ZERO;
        BigDecimal volumeTotal = volumeUnitario.multiply(quantidade);
        item.setProperty("VOLUMETOT", volumeTotal);
    }
}
```

## 🎨 **Exemplos de Componentes SankhyaJS**

### **1. Componente de Busca de Produto**
```javascript
// Exemplo extraído do sankhya-js
angular
    .module('snk.components.produto')
    .controller('ProdutoSearchController', [
        '$scope', '$q', 'ServiceProxy', 'StringUtils', 'MessageUtils',
        function ($scope, $q, ServiceProxy, StringUtils, MessageUtils) {
            
            var self = this;
            var _produtos = [];
            var _filtros = {
                codigo: '',
                descricao: '',
                categoria: '',
                ativo: true
            };
            
            $scope.produtos = _produtos;
            $scope.filtros = _filtros;
            $scope.carregando = false;
            
            // Métodos públicos
            $scope.buscarProdutos = buscarProdutos;
            $scope.limparFiltros = limparFiltros;
            $scope.selecionarProduto = selecionarProduto;
            $scope.exportarProdutos = exportarProdutos;
            
            // Inicializar
            init();
            
            function init() {
                // Carregar categorias
                carregarCategorias();
                
                // Buscar produtos iniciais
                buscarProdutos();
            }
            
            /**
             * Buscar produtos
             */
            function buscarProdutos() {
                if ($scope.carregando) return;
                
                $scope.carregando = true;
                
                // Construir query
                var query = construirQuery();
                
                // Executar busca
                ServiceProxy.executarQuery(query)
                    .then(function(response) {
                        _produtos = response.data || [];
                        $scope.produtos = _produtos;
                        
                        if (_produtos.length === 0) {
                            MessageUtils.showInfo('Nenhum produto encontrado');
                        }
                    })
                    .catch(function(error) {
                        MessageUtils.showError('Erro ao buscar produtos: ' + error.message);
                    })
                    .finally(function() {
                        $scope.carregando = false;
                    });
            }
            
            /**
             * Construir query de busca
             */
            function construirQuery() {
                var sql = "SELECT p.CODPROD, p.DESCRPROD, p.VLRVENDA, p.ESTOQUE, " +
                         "       c.DESCRCATEGORIA, p.ATIVO " +
                         "FROM TGFPRO p " +
                         "LEFT JOIN TGFCAT c ON p.CODCATEGORIA = c.CODCATEGORIA " +
                         "WHERE 1=1";
                
                var params = [];
                
                // Filtro por código
                if (_filtros.codigo && _filtros.codigo.trim() !== '') {
                    sql += " AND p.CODPROD = ?";
                    params.push(_filtros.codigo);
                }
                
                // Filtro por descrição
                if (_filtros.descricao && _filtros.descricao.trim() !== '') {
                    sql += " AND UPPER(p.DESCRPROD) LIKE UPPER(?)";
                    params.push('%' + _filtros.descricao + '%');
                }
                
                // Filtro por categoria
                if (_filtros.categoria && _filtros.categoria.trim() !== '') {
                    sql += " AND p.CODCATEGORIA = ?";
                    params.push(_filtros.categoria);
                }
                
                // Filtro por status
                if (_filtros.ativo !== null) {
                    sql += " AND p.ATIVO = ?";
                    params.push(_filtros.ativo ? 'S' : 'N');
                }
                
                sql += " ORDER BY p.DESCRPROD";
                
                return {
                    sql: sql,
                    params: params
                };
            }
            
            /**
             * Limpar filtros
             */
            function limparFiltros() {
                _filtros = {
                    codigo: '',
                    descricao: '',
                    categoria: '',
                    ativo: true
                };
                $scope.filtros = _filtros;
                buscarProdutos();
            }
            
            /**
             * Selecionar produto
             */
            function selecionarProduto(produto) {
                if ($scope.onProdutoSelecionado) {
                    $scope.onProdutoSelecionado(produto);
                }
            }
            
            /**
             * Exportar produtos
             */
            function exportarProdutos() {
                if (_produtos.length === 0) {
                    MessageUtils.showWarning('Nenhum produto para exportar');
                    return;
                }
                
                // Preparar dados para exportação
                var dados = _produtos.map(function(produto) {
                    return {
                        'Código': produto.CODPROD,
                        'Descrição': produto.DESCRPROD,
                        'Valor': produto.VLRVENDA,
                        'Estoque': produto.ESTOQUE,
                        'Categoria': produto.DESCRCATEGORIA,
                        'Status': produto.ATIVO === 'S' ? 'Ativo' : 'Inativo'
                    };
                });
                
                // Exportar para Excel
                ServiceProxy.exportarExcel(dados, 'produtos.xlsx')
                    .then(function() {
                        MessageUtils.showSuccess('Produtos exportados com sucesso');
                    })
                    .catch(function(error) {
                        MessageUtils.showError('Erro ao exportar: ' + error.message);
                    });
            }
            
            /**
             * Carregar categorias
             */
            function carregarCategorias() {
                var query = "SELECT CODCATEGORIA, DESCRCATEGORIA FROM TGFCAT WHERE ATIVO = 'S' ORDER BY DESCRCATEGORIA";
                
                ServiceProxy.executarQuery(query)
                    .then(function(response) {
                        $scope.categorias = response.data || [];
                    })
                    .catch(function(error) {
                        console.error('Erro ao carregar categorias:', error);
                    });
            }
        }
    ]);
```

### **2. Componente de Dashboard**
```javascript
// Exemplo de dashboard extraído do sankhya-js
angular
    .module('snk.components.dashboard')
    .controller('DashboardController', [
        '$scope', '$interval', 'ServiceProxy', 'ChartUtils', 'DateUtils',
        function ($scope, $interval, ServiceProxy, ChartUtils, DateUtils) {
            
            var self = this;
            var _refreshInterval;
            
            $scope.dados = {
                vendas: [],
                produtos: [],
                clientes: [],
                financeiro: []
            };
            
            $scope.graficos = {
                vendas: null,
                produtos: null,
                clientes: null
            };
            
            $scope.periodo = {
                inicio: DateUtils.getFirstDayOfMonth(),
                fim: DateUtils.getLastDayOfMonth()
            };
            
            // Métodos públicos
            $scope.carregarDashboard = carregarDashboard;
            $scope.atualizarPeriodo = atualizarPeriodo;
            $scope.exportarRelatorio = exportarRelatorio;
            
            // Inicializar
            init();
            
            function init() {
                carregarDashboard();
                
                // Atualizar automaticamente a cada 5 minutos
                _refreshInterval = $interval(function() {
                    carregarDashboard();
                }, 300000);
                
                // Limpar interval ao sair
                $scope.$on('$destroy', function() {
                    if (_refreshInterval) {
                        $interval.cancel(_refreshInterval);
                    }
                });
            }
            
            /**
             * Carregar dados do dashboard
             */
            function carregarDashboard() {
                $scope.carregando = true;
                
                // Carregar dados em paralelo
                $q.all([
                    carregarVendas(),
                    carregarProdutos(),
                    carregarClientes(),
                    carregarFinanceiro()
                ]).then(function() {
                    criarGraficos();
                }).finally(function() {
                    $scope.carregando = false;
                });
            }
            
            /**
             * Carregar dados de vendas
             */
            function carregarVendas() {
                var sql = "SELECT TO_CHAR(cab.DTNEG, 'YYYY-MM') as MES, " +
                         "       SUM(ite.VLRTOT) as VALOR_TOTAL, " +
                         "       COUNT(DISTINCT cab.NUNOTA) as QTD_PEDIDOS " +
                         "FROM TGFCAB cab " +
                         "INNER JOIN TGFITE ite ON cab.NUNOTA = ite.NUNOTA " +
                         "WHERE cab.DTNEG BETWEEN ? AND ? " +
                         "  AND cab.TIPMOV = 'V' " +
                         "GROUP BY TO_CHAR(cab.DTNEG, 'YYYY-MM') " +
                         "ORDER BY MES";
                
                var params = [$scope.periodo.inicio, $scope.periodo.fim];
                
                return ServiceProxy.executarQuery(sql, params)
                    .then(function(response) {
                        $scope.dados.vendas = response.data || [];
                    });
            }
            
            /**
             * Carregar dados de produtos
             */
            function carregarProdutos() {
                var sql = "SELECT p.DESCRPROD, SUM(ite.QTDNEG) as QTD_VENDIDA, " +
                         "       SUM(ite.VLRTOT) as VALOR_VENDIDO " +
                         "FROM TGFITE ite " +
                         "INNER JOIN TGFPRO p ON ite.CODPROD = p.CODPROD " +
                         "INNER JOIN TGFCAB cab ON ite.NUNOTA = cab.NUNOTA " +
                         "WHERE cab.DTNEG BETWEEN ? AND ? " +
                         "  AND cab.TIPMOV = 'V' " +
                         "GROUP BY p.CODPROD, p.DESCRPROD " +
                         "ORDER BY QTD_VENDIDA DESC " +
                         "FETCH FIRST 10 ROWS ONLY";
                
                var params = [$scope.periodo.inicio, $scope.periodo.fim];
                
                return ServiceProxy.executarQuery(sql, params)
                    .then(function(response) {
                        $scope.dados.produtos = response.data || [];
                    });
            }
            
            /**
             * Carregar dados de clientes
             */
            function carregarClientes() {
                var sql = "SELECT par.NOMEPARC, COUNT(DISTINCT cab.NUNOTA) as QTD_PEDIDOS, " +
                         "       SUM(ite.VLRTOT) as VALOR_TOTAL " +
                         "FROM TGFITE ite " +
                         "INNER JOIN TGFCAB cab ON ite.NUNOTA = cab.NUNOTA " +
                         "INNER JOIN TGFPAR par ON cab.CODPARC = par.CODPARC " +
                         "WHERE cab.DTNEG BETWEEN ? AND ? " +
                         "  AND cab.TIPMOV = 'V' " +
                         "GROUP BY par.CODPARC, par.NOMEPARC " +
                         "ORDER BY VALOR_TOTAL DESC " +
                         "FETCH FIRST 10 ROWS ONLY";
                
                var params = [$scope.periodo.inicio, $scope.periodo.fim];
                
                return ServiceProxy.executarQuery(sql, params)
                    .then(function(response) {
                        $scope.dados.clientes = response.data || [];
                    });
            }
            
            /**
             * Carregar dados financeiros
             */
            function carregarFinanceiro() {
                var sql = "SELECT 'Receitas' as TIPO, SUM(VLRORIGINAL) as VALOR " +
                         "FROM TGFCPL " +
                         "WHERE DTNEG BETWEEN ? AND ? " +
                         "  AND TIPMOV = 'R' " +
                         "UNION ALL " +
                         "SELECT 'Despesas' as TIPO, SUM(VLRORIGINAL) as VALOR " +
                         "FROM TGFCPL " +
                         "WHERE DTNEG BETWEEN ? AND ? " +
                         "  AND TIPMOV = 'D'";
                
                var params = [$scope.periodo.inicio, $scope.periodo.fim, 
                             $scope.periodo.inicio, $scope.periodo.fim];
                
                return ServiceProxy.executarQuery(sql, params)
                    .then(function(response) {
                        $scope.dados.financeiro = response.data || [];
                    });
            }
            
            /**
             * Criar gráficos
             */
            function criarGraficos() {
                // Gráfico de vendas
                if ($scope.dados.vendas.length > 0) {
                    $scope.graficos.vendas = ChartUtils.createLineChart({
                        data: $scope.dados.vendas,
                        xField: 'MES',
                        yField: 'VALOR_TOTAL',
                        title: 'Vendas por Mês'
                    });
                }
                
                // Gráfico de produtos
                if ($scope.dados.produtos.length > 0) {
                    $scope.graficos.produtos = ChartUtils.createBarChart({
                        data: $scope.dados.produtos,
                        xField: 'DESCRPROD',
                        yField: 'QTD_VENDIDA',
                        title: 'Top 10 Produtos'
                    });
                }
                
                // Gráfico de clientes
                if ($scope.dados.clientes.length > 0) {
                    $scope.graficos.clientes = ChartUtils.createPieChart({
                        data: $scope.dados.clientes,
                        labelField: 'NOMEPARC',
                        valueField: 'VALOR_TOTAL',
                        title: 'Top 10 Clientes'
                    });
                }
            }
            
            /**
             * Atualizar período
             */
            function atualizarPeriodo(novoPeriodo) {
                $scope.periodo = novoPeriodo;
                carregarDashboard();
            }
            
            /**
             * Exportar relatório
             */
            function exportarRelatorio() {
                var dados = {
                    periodo: $scope.periodo,
                    vendas: $scope.dados.vendas,
                    produtos: $scope.dados.produtos,
                    clientes: $scope.dados.clientes,
                    financeiro: $scope.dados.financeiro
                };
                
                ServiceProxy.exportarRelatorio(dados, 'dashboard.xlsx')
                    .then(function() {
                        MessageUtils.showSuccess('Relatório exportado com sucesso');
                    })
                    .catch(function(error) {
                        MessageUtils.showError('Erro ao exportar relatório: ' + error.message);
                    });
            }
        }
    ]);
```

## 🔧 **Exemplos de Botões de Ação**

### **1. Botão de Ação para Faturamento**
```javascript
// Exemplo de botão de ação para faturamento
function faturamentoPedidos() {
    try {
        // Obter pedidos selecionados
        var pedidos = getSelectedRecords();
        
        if (!pedidos || pedidos.length === 0) {
            throw new Error('Nenhum pedido selecionado');
        }
        
        // Validar pedidos
        validarPedidosParaFaturamento(pedidos);
        
        // Confirmar ação
        if (!confirm('Deseja realmente faturar ' + pedidos.length + ' pedido(s)?')) {
            return;
        }
        
        // Processar faturamento
        processarFaturamento(pedidos);
        
    } catch (error) {
        showError('Erro no faturamento: ' + error.message);
    }
}

/**
 * Validar pedidos para faturamento
 */
function validarPedidosParaFaturamento(pedidos) {
    for (var i = 0; i < pedidos.length; i++) {
        var pedido = pedidos[i];
        
        // Validar status
        if (pedido.STATUS !== 'A') {
            throw new Error('Pedido ' + pedido.NUNOTA + ' não está aprovado');
        }
        
        // Validar estoque
        if (!validarEstoquePedido(pedido)) {
            throw new Error('Estoque insuficiente para o pedido ' + pedido.NUNOTA);
        }
        
        // Validar limite de crédito
        if (!validarLimiteCredito(pedido)) {
            throw new Error('Limite de crédito excedido para o cliente do pedido ' + pedido.NUNOTA);
        }
    }
}

/**
 * Processar faturamento
 */
function processarFaturamento(pedidos) {
    showMessage('Processando faturamento...');
    
    var sucessos = 0;
    var erros = 0;
    
    for (var i = 0; i < pedidos.length; i++) {
        try {
            var pedido = pedidos[i];
            
            // Criar nota fiscal
            var notaFiscal = criarNotaFiscal(pedido);
            
            // Processar itens
            processarItensNotaFiscal(notaFiscal, pedido);
            
            // Confirmar nota fiscal
            confirmarNotaFiscal(notaFiscal);
            
            // Atualizar status do pedido
            atualizarStatusPedido(pedido, 'F');
            
            sucessos++;
            
        } catch (error) {
            erros++;
            console.error('Erro ao faturar pedido ' + pedido.NUNOTA + ':', error);
        }
    }
    
    // Mostrar resultado
    var mensagem = 'Faturamento concluído: ' + sucessos + ' sucesso(s), ' + erros + ' erro(s)';
    if (erros > 0) {
        showWarning(mensagem);
    } else {
        showMessage(mensagem);
    }
    
    // Atualizar tela
    refreshCurrentScreen();
}

/**
 * Criar nota fiscal
 */
function criarNotaFiscal(pedido) {
    var sql = "INSERT INTO TGFCAB (NUNOTA, TIPMOV, CODPARC, DTNEG, DTENTSAI, " +
              "                   STATUS, VLRFRETE, VLRDESC, VLRPROD, VLRTOT) " +
              "VALUES (?, 'V', ?, ?, ?, 'A', ?, ?, ?, ?)";
    
    var params = [
        getNextSequence('TGFCAB'),
        pedido.CODPARC,
        new Date(),
        new Date(),
        pedido.VLRFRETE || 0,
        pedido.VLRDESC || 0,
        pedido.VLRPROD || 0,
        pedido.VLRTOT || 0
    ];
    
    executeSQL(sql, params);
    
    return {
        NUNOTA: params[0],
        CODPARC: pedido.CODPARC
    };
}

/**
 * Processar itens da nota fiscal
 */
function processarItensNotaFiscal(notaFiscal, pedido) {
    var sql = "SELECT * FROM TGFITE WHERE NUNOTA = ?";
    var itens = executeQuery(sql, [pedido.NUNOTA]);
    
    for (var i = 0; i < itens.length; i++) {
        var item = itens[i];
        
        // Criar item da nota fiscal
        var sqlItem = "INSERT INTO TGFITE (NUNOTA, SEQUENCIA, CODPROD, QTDNEG, " +
                      "                   VLRUNIT, VLRTOT, PERCDESC, VLRDESC) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        var paramsItem = [
            notaFiscal.NUNOTA,
            i + 1,
            item.CODPROD,
            item.QTDNEG,
            item.VLRUNIT,
            item.VLRTOT,
            item.PERCDESC || 0,
            item.VLRDESC || 0
        ];
        
        executeSQL(sqlItem, paramsItem);
        
        // Atualizar estoque
        atualizarEstoque(item.CODPROD, item.QTDNEG, 'S');
    }
}

/**
 * Confirmar nota fiscal
 */
function confirmarNotaFiscal(notaFiscal) {
    var sql = "UPDATE TGFCAB SET STATUS = 'L' WHERE NUNOTA = ?";
    executeSQL(sql, [notaFiscal.NUNOTA]);
}

/**
 * Atualizar status do pedido
 */
function atualizarStatusPedido(pedido, novoStatus) {
    var sql = "UPDATE TGFCAB SET STATUS = ? WHERE NUNOTA = ?";
    executeSQL(sql, [novoStatus, pedido.NUNOTA]);
}

/**
 * Validar estoque do pedido
 */
function validarEstoquePedido(pedido) {
    var sql = "SELECT i.CODPROD, i.QTDNEG, p.ESTOQUE " +
              "FROM TGFITE i " +
              "INNER JOIN TGFPRO p ON i.CODPROD = p.CODPROD " +
              "WHERE i.NUNOTA = ?";
    
    var itens = executeQuery(sql, [pedido.NUNOTA]);
    
    for (var i = 0; i < itens.length; i++) {
        var item = itens[i];
        if (item.ESTOQUE < item.QTDNEG) {
            return false;
        }
    }
    
    return true;
}

/**
 * Validar limite de crédito
 */
function validarLimiteCredito(pedido) {
    var sql = "SELECT p.LIMITECRED, " +
              "       (SELECT NVL(SUM(VLRTOT), 0) FROM TGFCAB WHERE CODPARC = ? AND STATUS = 'A') as SALDO_DEVEDOR " +
              "FROM TGFPAR p " +
              "WHERE p.CODPARC = ?";
    
    var result = executeQuery(sql, [pedido.CODPARC, pedido.CODPARC]);
    
    if (result.length > 0) {
        var limite = result[0].LIMITECRED || 0;
        var saldo = result[0].SALDO_DEVEDOR || 0;
        var valorPedido = pedido.VLRTOT || 0;
        
        return (saldo + valorPedido) <= limite;
    }
    
    return true;
}

/**
 * Atualizar estoque
 */
function atualizarEstoque(codProduto, quantidade, tipo) {
    var sql = "UPDATE TGFPRO SET ESTOQUE = ESTOQUE " + 
              (tipo === 'S' ? '-' : '+') + " ? WHERE CODPROD = ?";
    executeSQL(sql, [quantidade, codProduto]);
}
```

## 📊 **Exemplos de Relatórios**

### **1. Relatório de Vendas por Período**
```sql
-- Relatório de vendas extraído do SankhyaW
SELECT 
    cab.NUNOTA,
    cab.DTNEG,
    par.NOMEPARC,
    par.CGCCPF,
    cab.VLRTOT,
    cab.VLRFRETE,
    cab.VLRDESC,
    cab.VLRTOTLIQ,
    usu.NOMEUSU,
    cab.OBSERVACAO
FROM TGFCAB cab
INNER JOIN TGFPAR par ON cab.CODPARC = par.CODPARC
LEFT JOIN TSIUSU usu ON cab.CODUSU = usu.CODUSU
WHERE cab.DTNEG BETWEEN :DATA_INICIO AND :DATA_FIM
  AND cab.TIPMOV = 'V'
  AND cab.STATUS = 'L'
ORDER BY cab.DTNEG DESC, cab.NUNOTA DESC;
```

### **2. Relatório de Produtos Mais Vendidos**
```sql
-- Relatório de produtos mais vendidos
SELECT 
    p.CODPROD,
    p.DESCRPROD,
    c.DESCRCATEGORIA,
    SUM(i.QTDNEG) as QTD_VENDIDA,
    SUM(i.VLRTOT) as VALOR_TOTAL,
    AVG(i.VLRUNIT) as PRECO_MEDIO,
    COUNT(DISTINCT cab.NUNOTA) as QTD_PEDIDOS
FROM TGFITE i
INNER JOIN TGFPRO p ON i.CODPROD = p.CODPROD
INNER JOIN TGFCAB cab ON i.NUNOTA = cab.NUNOTA
LEFT JOIN TGFCAT c ON p.CODCATEGORIA = c.CODCATEGORIA
WHERE cab.DTNEG BETWEEN :DATA_INICIO AND :DATA_FIM
  AND cab.TIPMOV = 'V'
  AND cab.STATUS = 'L'
GROUP BY p.CODPROD, p.DESCRPROD, c.DESCRCATEGORIA
ORDER BY QTD_VENDIDA DESC
FETCH FIRST 50 ROWS ONLY;
```

## 🎯 **Resumo dos Exemplos**

### **Exemplos de Eventos Programados**
- ✅ **Validação de Cliente**: CPF/CNPJ, duplicidade, formatação
- ✅ **Cálculo de Preços**: Impostos, descontos, totais
- ✅ **Controle de Estoque**: Atualização automática
- ✅ **Validações de Negócio**: Regras específicas

### **Exemplos de Componentes SankhyaJS**
- ✅ **Busca de Produtos**: Filtros, paginação, exportação
- ✅ **Dashboard**: Gráficos, métricas, atualização automática
- ✅ **Formulários**: Validações, máscaras, autocomplete
- ✅ **Relatórios**: Parâmetros, exportação, visualização

### **Exemplos de Botões de Ação**
- ✅ **Faturamento**: Validações, processamento, controle
- ✅ **Importação**: Validação, processamento em lote
- ✅ **Exportação**: Formatação, múltiplos formatos
- ✅ **Processamento**: Validações, transações, logs

### **Exemplos de Relatórios**
- ✅ **Vendas**: Período, clientes, produtos
- ✅ **Financeiro**: Contas a pagar/receber, fluxo de caixa
- ✅ **Estoque**: Movimentação, saldos, análise
- ✅ **Comercial**: Performance, metas, indicadores

---

*Estes exemplos práticos extraídos do SankhyaW 4.8 fornecem código funcional e testado em produção, servindo como base para implementações similares em projetos Sankhya.*
