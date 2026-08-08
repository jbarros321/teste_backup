# 🏆 Guia Completo de Personalizações Sankhya

## 🎯 Visão Geral

Este guia abrangente consolida todo o conhecimento sobre personalizações e customizações da plataforma Sankhya, baseado na documentação oficial do [Sankhya Developer](https://developer.sankhya.com.br/docs/tipos_de_personalizacao) e nas melhores práticas de desenvolvimento.

## 📚 Índice de Conteúdo

1. [Tipos de Personalização](#tipos-de-personalização)
2. [Dicionário de Dados](#dicionário-de-dados)
3. [Jape - Persistência de Dados](#jape---persistência-de-dados)
4. [SankhyaJS (HTML5)](#sankhyajs-html5)
5. [Generator Sankhya](#generator-sankhya)
6. [Arquitetura e Padrões](#arquitetura-e-padrões)
7. [Casos de Uso Práticos](#casos-de-uso-práticos)
8. [Boas Práticas](#boas-práticas)
9. [Recursos de Aprendizado](#recursos-de-aprendizado)

## 🛠️ Tipos de Personalização

A plataforma Sankhya oferece **14 tipos principais** de personalização/customização, organizados em diferentes camadas da arquitetura:

### **📱 Camada de Apresentação**
- **Telas Adicionais**: Formulários dinâmicos e relatórios
- **Dashboards**: Visualizações dinâmicas de métricas
- **Relatórios Personalizados**: Relatórios formatados com iReport

### **⚡ Camada de Lógica**
- **Botões de Ação**: Execução de rotinas manuais
- **Ações Agendadas**: Execução automática e periódica
- **Eventos Programados**: Disparados por operações em entidades

### **🗄️ Camada de Dados**
- **Gerenciador de Objetos**: Melhorias em processos de banco
- **Consolidador de Dados**: Tabelas para consolidação
- **Regras de Negócio**: Requisitos específicos para portais

### **🔗 Camada de Integração**
- **API de Integração**: Integração com outros sistemas
- **EDI**: Intercâmbio eletrônico de dados
- **Tarefas de Serviços**: Automação de fluxos de trabalho

### **🔌 Camada de Extensão**
- **Add-ons**: Recursos de diferenciação
- **Metas Gerenciais**: Criação de metas e regras

## 📊 Dicionário de Dados

O Dicionário de Dados é a base para a maioria das personalizações Sankhya:

### **Funcionalidades Principais**
- **Criação de Entidades**: Tabelas personalizadas
- **Gerenciamento de Campos**: Tipos, validações, máscaras
- **Relacionamentos**: 1:1, 1:N, N:N
- **Validações**: Constraints e regras de negócio

### **Recursos Avançados**
- **Campos Calculados**: Derivados de outros campos
- **Campos de Auditoria**: Controle de criação/alteração
- **Campos de Status**: Controle de estados
- **Campos de Integração**: APIs externas

### **Tipos de Dados Suportados**
- **Básicos**: VARCHAR2, NUMBER, DATE, CLOB, BLOB
- **Especiais**: BOOLEAN, CURRENCY, PERCENTAGE, EMAIL, URL
- **Referência**: ENTITY, USER, COMPANY, PRODUCT, PARTNER

## 🗄️ Jape - Persistência de Dados

Framework de persistência que gerencia a comunicação com o banco Oracle:

### **Componentes Principais**
- **EntityManager**: Gerenciamento de entidades
- **QueryBuilder**: Construção de consultas SQL
- **TransactionManager**: Controle de transações
- **CacheManager**: Gerenciamento de cache

### **Operações CRUD**
```java
// Create
EntityManager em = JapeSession.getEntityManager();
TGFCAB cab = new TGFCAB();
em.persist(cab);

// Read
QueryBuilder qb = em.getQueryBuilder();
List<TGFCAB> cabs = qb.select(TGFCAB.class)
    .where("dtmov >= ?", dataInicio)
    .getResultList();

// Update
TGFCAB cab = em.find(TGFCAB.class, nunota);
cab.setStatusnota("A");
em.merge(cab);

// Delete
TGFCAB cab = em.find(TGFCAB.class, nunota);
em.remove(cab);
```

### **Gerenciamento de Transações**
- **Transações Automáticas**: Com anotações @EntityManager
- **Transações Manuais**: Controle explícito
- **Transações Aninhadas**: Suporte a transações internas

## 🎨 SankhyaJS (HTML5)

Framework front-end baseado em AngularJS para desenvolvimento de interfaces:

### **Ferramentas de Desenvolvimento**
- **snkCode**: Extensão do Visual Studio Code
- **Generator Sankhya**: Gerador de código para dynaform
- **Showcase**: Visualização interativa de componentes

### **Taglibs Sankhya**
```jsp
<!-- Carregamento de recursos -->
<snk:load />

<!-- Consultas SQL -->
<snk:query var="dados">
    SELECT campo1, campo2 FROM tabela WHERE condicao = :PARAMETRO
</snk:query>

<!-- Formatação de dados -->
<snk:format value="${valor}" type="currency" />
```

### **Componentes Visuais**
- **Cards Dashboard**: KPIs visuais interativos
- **Gráficos Chart.js**: Pizza, barras, linhas
- **Tabelas Responsivas**: Com formatação condicional
- **Layouts Responsivos**: Mobile-first design

### **Navegação e Interação**
```javascript
// Abrir novo nível
function abrirNivel(parametro) {
    var params = { 'A_PARAMETRO': parametro };
    openLevel('lvl_destino', params);
}

// Atualizar detalhes
function atualizarDetalhes(filtro) {
    const params = { 'A_FILTRO': filtro };
    refreshDetails('svl_detalhes', params);
}
```

## ⚡ Generator Sankhya

Ferramenta de geração de código que trabalha com dynaform:

### **Funcionalidades**
- **Geração de CRUD**: Create, Read, Update, Delete
- **Geração de Interceptors**: Before/After operations
- **Geração de Componentes**: Formulários, grids, validações

### **Templates Gerados**
- **Entidades Java**: Classes JPA com anotações
- **Services**: Lógica de negócio
- **Controllers**: Endpoints REST
- **JSPs**: Formulários dinâmicos
- **Interceptors**: Validações e auditoria

### **Configuração**
```xml
<generator-config>
    <project-name>MeuProjeto</project-name>
    <package-name>br.com.empresa.projeto</package-name>
    <entity-package>br.com.empresa.projeto.entity</entity-package>
    <service-package>br.com.empresa.projeto.service</service-package>
</generator-config>
```

## 🏗️ Arquitetura e Padrões

### **Arquitetura em Camadas**
```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (SankhyaJS)                   │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Components    │ │   Controllers   │ │   Services      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC                         │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Actions       │ │   Interceptors  │ │   Validations   │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    DATA LAYER (Jape)                      │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Entities      │ │   Repositories  │ │   Queries       │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE (Oracle)                      │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   Tables        │ │   Procedures    │ │   Triggers      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### **Padrões de Desenvolvimento**
- **MVC**: Separação de responsabilidades
- **Component-Based**: Reutilização de código
- **Event-Driven**: Triggers e validações
- **Scheduled Jobs**: Automações agendadas
- **Audit Trail**: Logs e monitoramento

## 🎯 Casos de Uso Práticos

### **1. Dashboard de Vendas**
```jsp
<!-- Componente de dashboard -->
<snk:query var="vendas">
    SELECT 
        TO_CHAR(dtmov, 'YYYY-MM') as periodo,
        SUM(vlrnota) as total_vendas,
        COUNT(*) as quantidade_notas
    FROM tgfcab 
    WHERE dtmov BETWEEN :P_PERIODO.INI AND :P_PERIODO.FIN
    GROUP BY TO_CHAR(dtmov, 'YYYY-MM')
    ORDER BY periodo
</snk:query>

<div class="dashboard-container">
    <div class="chart-container">
        <canvas id="vendasChart"></canvas>
    </div>
    <div class="kpi-grid">
        <c:forEach items="${vendas.rows}" var="row">
            <div class="kpi-card">
                <h3>${row.periodo}</h3>
                <div class="value">R$ ${row.total_vendas}</div>
                <div class="subtitle">${row.quantidade_notas} notas</div>
            </div>
        </c:forEach>
    </div>
</div>
```

### **2. Botão de Ação com Validação**
```sql
-- Procedure de botão de ação
CREATE OR REPLACE PROCEDURE STP_VALIDAR_ENTREGA (
    P_CODUSU NUMBER,
    P_IDSESSAO VARCHAR2,
    P_QTDLINHAS NUMBER,
    P_MENSAGEM OUT VARCHAR2
) AS
    FIELD_NUNOTA NUMBER;
    P_ANEXO NUMBER;
BEGIN
    FOR I IN 1..P_QTDLINHAS LOOP
        FIELD_NUNOTA := ACT_INT_FIELD(P_IDSESSAO, I, 'NUNOTA');
        
        -- Verificar anexo obrigatório
        SELECT COUNT(*) INTO P_ANEXO 
        FROM TSIATA 
        WHERE CODATA = FIELD_NUNOTA;
        
        IF P_ANEXO = 0 THEN
            P_MENSAGEM := 'Anexo obrigatório não encontrado!';
            RETURN;
        END IF;
        
        -- Atualizar status
        UPDATE TGFCAB 
        SET PENDENTE = 'N',
            AD_USU_MARC_ENTREG = P_CODUSU,
            AD_DT_MARC_ENTREG = SYSDATE
        WHERE NUNOTA = FIELD_NUNOTA;
    END LOOP;
    
    P_MENSAGEM := 'Operação executada com sucesso!';
END;
```

### **3. Ação Agendada de Limpeza**
```sql
-- Procedure de limpeza automática
CREATE OR REPLACE PROCEDURE STP_LIMPEZA_LOGS AS
    P_COUNT_DELETED NUMBER := 0;
BEGIN
    -- Deletar logs antigos
    DELETE FROM LOG_EXECUCAO 
    WHERE DT_EXECUCAO < SYSDATE - 90;
    
    P_COUNT_DELETED := SQL%ROWCOUNT;
    
    -- Log da execução
    INSERT INTO LOG_EXECUCAO (DT_EXECUCAO, PROCEDURE, REGISTROS_AFETADOS)
    VALUES (SYSDATE, 'STP_LIMPEZA_LOGS', P_COUNT_DELETED);
    
    COMMIT;
END;
```

### **4. Componente HTML5 com Chart.js**
```jsp
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<snk:query var="dados">
    SELECT categoria, COUNT(*) as quantidade
    FROM vendas
    GROUP BY categoria
    ORDER BY quantidade DESC
</snk:query>

<canvas id="categoriaChart"></canvas>

<script>
document.addEventListener('DOMContentLoaded', function () {
    var ctx = document.getElementById('categoriaChart').getContext('2d');
    var labels = [];
    var data = [];

    <c:forEach items="${dados.rows}" var="row">
        labels.push("${row.categoria}");
        data.push(${row.quantidade});
    </c:forEach>

    var myChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56']
            }]
        },
        options: {
            onClick: function(event, elements) {
                if (elements.length > 0) {
                    var categoria = labels[elements[0].index];
                    abrirDetalhes(categoria);
                }
            }
        }
    });
});
</script>
```

## 🛠️ Boas Práticas

### **1. Desenvolvimento**
- **Nomenclatura Consistente**: Use padrões claros
- **Documentação**: Documente código complexo
- **Testes**: Teste em ambiente de desenvolvimento
- **Versionamento**: Controle de versões
- **Performance**: Otimize consultas e componentes

### **2. Segurança**
- **Validação de Entrada**: Sempre valide dados
- **Controle de Acesso**: Implemente permissões
- **Auditoria**: Registre operações importantes
- **Criptografia**: Para dados sensíveis
- **Backup**: Mantenha backups regulares

### **3. Manutenção**
- **Código Limpo**: Mantenha código legível
- **Refatoração**: Melhore código existente
- **Monitoramento**: Acompanhe performance
- **Atualizações**: Mantenha dependências atualizadas
- **Suporte**: Documente procedimentos

### **4. Performance**
- **Índices**: Use índices estratégicos
- **Cache**: Implemente cache quando apropriado
- **Consultas**: Otimize queries SQL
- **Componentes**: Use lazy loading
- **Monitoramento**: Acompanhe métricas

## 📚 Recursos de Aprendizado

### **Documentação Oficial**
- [Sankhya Developer](https://developer.sankhya.com.br/)
- [Tipos de Personalização](https://developer.sankhya.com.br/docs/tipos_de_personalizacao)
- [Dicionário de Dados](https://developer.sankhya.com.br/docs/dicion%C3%A1rio-de-dados)
- [SankhyaJS](https://developer.sankhya.com.br/docs/sankhya-js)
- [Generator Sankhya](https://developer.sankhya.com.br/docs/generator-sankhya)

### **Cursos e Certificações**
- **Universidade Sankhya**: Cursos técnicos
- **Associate Framework**: Certificação back-end e front-end
- **Specialist**: Dashboards, Add-ons, Relatórios
- **Alura**: AngularJS e desenvolvimento web
- **YouTube**: Tutoriais e webinários

### **Ferramentas**
- **Visual Studio Code**: Com extensão snkCode
- **Generator Sankhya**: Geração de código
- **iReport**: Relatórios formatados
- **Oracle SQL Developer**: Desenvolvimento SQL
- **Add-on Studio**: Desenvolvimento de add-ons

### **Comunidade**
- **Sankhya Developer Community**: Fóruns e discussões
- **Portal do Desenvolvedor**: Recursos e suporte
- **Showcase de Componentes**: Exemplos interativos
- **Central de Ajuda**: Documentação detalhada

## 🚀 Próximos Passos

### **Para Desenvolvedores**
1. **Estude os Fundamentos**: Dicionário de Dados e Jape
2. **Pratique SankhyaJS**: Crie componentes simples
3. **Explore Generator**: Automatize desenvolvimento
4. **Implemente Casos Reais**: Use exemplos práticos
5. **Participe da Comunidade**: Compartilhe conhecimento

### **Para Analistas**
1. **Entenda os Tipos**: Conheça as 14 personalizações
2. **Mapeie Necessidades**: Identifique casos de uso
3. **Documente Requisitos**: Especifique funcionalidades
4. **Valide Implementações**: Teste soluções
5. **Evolua Continuamente**: Melhore processos

### **Para Gestores**
1. **Avalie ROI**: Meça benefícios das personalizações
2. **Invista em Treinamento**: Capacite equipes
3. **Estabeleça Padrões**: Defina boas práticas
4. **Monitore Performance**: Acompanhe resultados
5. **Planeje Evolução**: Prepare para o futuro

## 📊 Estatísticas e Métricas

### **Tipos de Personalização Disponíveis**
- **14 Tipos** principais de personalização
- **5 Camadas** arquiteturais
- **3 Níveis** de complexidade (Iniciante, Intermediário, Avançado)

### **Ferramentas e Recursos**
- **6 Ferramentas** principais de desenvolvimento
- **4 Frameworks** de suporte
- **10+ Cursos** de capacitação
- **3 Certificações** especializadas

### **Comunidade e Suporte**
- **Portal Oficial** com documentação completa
- **Comunidade Ativa** de desenvolvedores
- **Showcase Interativo** de componentes
- **Central de Ajuda** detalhada

## ✅ Conclusão

Este guia completo fornece uma visão abrangente das personalizações Sankhya, desde os conceitos fundamentais até implementações práticas. Com este conhecimento, desenvolvedores, analistas e gestores podem:

- **Entender** a arquitetura e tipos de personalização
- **Implementar** soluções eficientes e escaláveis
- **Aproveitar** ferramentas e recursos disponíveis
- **Seguir** boas práticas de desenvolvimento
- **Evoluir** continuamente com a plataforma

A plataforma Sankhya oferece um ecossistema robusto e flexível para personalizações, permitindo criar soluções que atendem às necessidades específicas de cada negócio.

---

**Última atualização**: $(date)  
**Versão**: 1.0  
**Baseado em**: Documentação oficial Sankhya Developer  
**Status**: ✅ Completo e Atualizado

*Este guia representa o conhecimento consolidado sobre personalizações Sankhya, extraído da documentação oficial e melhores práticas de desenvolvimento.*
