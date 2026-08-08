package br.com.ermaq.ScheduledAction;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.JdbcUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.ws.ServiceContext;

public class AjustaSaldoRetalho implements ScheduledAction {

    @Override
    @SuppressWarnings("resource")
    public void onTime(ScheduledActionContext arg0) {
        System.out.println("AjustaSaldoRetalho");
        inserirLog("Iniciando processo AjustaSaldoRetalho", "S");
        
        JapeSession.SessionHandle hnd = null;
        ServiceContext ctx = null;

        try {

            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            auth.makeCurrent();
            ctx = new ServiceContext(null);
            ctx.setAutentication(auth);
            ctx.makeCurrent();
            SPBeanUtils.setupContext(ctx);

            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            inserirLog("Contexto inicializado com sucesso", "S");

            JapeWrapper empDAO = JapeFactory.dao(DynamicEntityNames.EMPRESA);
            Collection<DynamicVO> empVOs = empDAO.find("CODEMP IS NOT NULL");

            inserirLog("Encontradas " + empVOs.size() + " empresas para processar", "S");

            for (DynamicVO empVO : empVOs) {

                BigDecimal codEmp = empVO.asBigDecimal("CODEMP");
                inserirLog("Processando empresa: " + codEmp, "S");

                // validação inicial para não consumir numero unico
                JdbcWrapper jdbc = null;
                NativeSql sql = null;
                ResultSet rs = null;
                boolean realizaBaixa = false;
                int registrosEncontrados = 0;

                try {

                    EntityFacade entity = EntityFacadeFactory.getDWFFacade();
                    jdbc = entity.getJdbcWrapper();
                    jdbc.openSession();

                    sql = new NativeSql(jdbc);
                    sql.appendSql(
                            "SELECT EST.CODEMP,EST.CODLOCAL,EST.CODPROD,EST.CONTROLE,NVL(ESTOQUE-RESERVADO,0) QTD,EST.RESERVADO,EST.ESTOQUE,PRO.CODVOL,PRO.AD_TIPRET ");
                    sql.appendSql("FROM TGFEST EST ");
                    sql.appendSql("INNER JOIN TGFPRO PRO ");
                    sql.appendSql("ON EST.CODPROD = PRO.CODPROD ");
                    sql.appendSql("WHERE PRO.AD_CONTROLARETALHO = 'S' ");
                    sql.appendSql("AND EST.CONTROLE LIKE '%Retalho' ");
                    sql.appendSql("AND NVL(ESTOQUE-RESERVADO,0) > 0 ");
                    sql.appendSql("AND EST.CODEMP = :CODEMP ");
                    sql.setNamedParameter("CODEMP", empVO.asBigDecimal("CODEMP"));

                    rs = sql.executeQuery();

                    while (rs.next()) {
                        registrosEncontrados++;
                        
                        String tipRet = rs.getString("AD_TIPRET");
                        String controle = rs.getString("CONTROLE");
                        BigDecimal qtd = rs.getBigDecimal("QTD");
                        BigDecimal codProd = rs.getBigDecimal("CODPROD");
                        
                        System.out.println("qtd:" + qtd);
                        System.out.println("controle:" + controle);
                        System.out.println("tipRet:" + rs.getString("AD_TIPRET"));
                        System.out.println("CODPROD:" + rs.getBigDecimal("CODPROD"));
                        
                        // REMOVIDO O LOG DETALHADO AQUI - só conta
                        
                        if (tipRet.equals("CH")) {

                            if (controle.equals(">1500Retalho") && qtd.compareTo(new BigDecimal(1.5)) <= 0) {
                                realizaBaixa = true;
                            } else if (controle.equals("751x1500Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(0.751), new BigDecimal(1.5))) {
                                realizaBaixa = true;
                            } else if (controle.equals("0x750Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(0), new BigDecimal(0.75))) {
                                realizaBaixa = true;
                            }

                        }

                        if (tipRet.equals("BR")) {

                            if (controle.equals(">3000Retalho") && qtd.compareTo(new BigDecimal(3)) < 0) {
                                realizaBaixa = true;
                            } else if (controle.equals("1501x3000Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(1.501), new BigDecimal(3))) {
                                realizaBaixa = true;
                            } else if (controle.equals("501x1500Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(0.501), new BigDecimal(1.5))) {
                                realizaBaixa = true;
                            } else if (controle.equals("0x500Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(0), new BigDecimal(0.5))) {
                                realizaBaixa = true;
                            }

                        }

                        if (tipRet.equals("BO")) {

                            if (controle.equals(">2000Retalho") && qtd.compareTo(new BigDecimal(2)) < 0) {
                                realizaBaixa = true;
                            } else if (controle.equals("1001x2000Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(1.001), new BigDecimal(2))) {
                                realizaBaixa = true;
                            } else if (controle.equals("501x1000Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(0.501), new BigDecimal(1))) {
                                realizaBaixa = true;
                            } else if (controle.equals("0x500Retalho")
                                    && !estaNoIntervalo(qtd, new BigDecimal(0), new BigDecimal(0.5))) {
                                realizaBaixa = true;
                            }

                        }

                    }

                    // LOG RESUMO - SEMPRE
                    inserirLog(
                        "Empresa " + codEmp +
                        " - Registros analisados: " + registrosEncontrados +
                        " - Realiza baixa: " + (realizaBaixa ? "Sim" : "Não"),
                        "S"
                    );

                } catch (Exception e) {
                    inserirLog("Erro ao analisar empresa " + codEmp + ": " + e.getMessage(), "E");
                    MGEModelException.throwMe(e);
                } finally {
                    JdbcUtils.closeResultSet(rs);
                    NativeSql.releaseResources(sql);
                    JdbcWrapper.closeSession(jdbc);

                }
                
                System.out.println("realizaBaixa:" + realizaBaixa);
                if (realizaBaixa) {

                    inserirLog("Iniciando transação para empresa " + codEmp, "S");

                    hnd.execWithTX(new JapeSession.TXBlock() {
                        public void doWithTx() throws Exception {
                            System.out.println("Passou doWithTx");
                            inserirLog("Dentro da transação para empresa " + codEmp, "S");
                            
                            try {
                                JdbcWrapper jdbc = null;
                                NativeSql sql = null;
                                ResultSet rs = null;

                                BigDecimal nuReqBaixa = new BigDecimal(0);
                                BigDecimal nuReqEntrada = new BigDecimal(0);

                                EntityFacade entity = EntityFacadeFactory.getDWFFacade();
                                jdbc = entity.getJdbcWrapper();
                                jdbc.openSession();

                                sql = new NativeSql(jdbc);
                                sql.appendSql(
                                        "SELECT EST.CODEMP,EST.CODLOCAL,EST.CODPROD,EST.CONTROLE,NVL(ESTOQUE-RESERVADO,0) QTD,EST.RESERVADO,EST.ESTOQUE,PRO.CODVOL,PRO.AD_TIPRET ");
                                sql.appendSql("FROM TGFEST EST ");
                                sql.appendSql("INNER JOIN TGFPRO PRO ");
                                sql.appendSql("ON EST.CODPROD = PRO.CODPROD ");
                                sql.appendSql("WHERE PRO.AD_CONTROLARETALHO = 'S' ");
                                sql.appendSql("AND EST.CONTROLE LIKE '%Retalho' ");
                                sql.appendSql("AND NVL(ESTOQUE-RESERVADO,0) > 0 ");
                                sql.appendSql("AND EST.CODEMP = :CODEMP ");
                                sql.setNamedParameter("CODEMP", empVO.asBigDecimal("CODEMP"));

                                rs = sql.executeQuery();
                                if (rs.isBeforeFirst()) {

                                    inserirLog("Criando nota de baixa para empresa " + codEmp, "S");

                                    DynamicVO reqBaixaVO = (DynamicVO) entity
                                            .getDefaultValueObjectInstance("CabecalhoNota");

                                    reqBaixaVO.setProperty("CODEMP", empVO.asBigDecimal("CODEMP"));
                                    reqBaixaVO.setProperty("CODTIPOPER",
                                            new BigDecimal(MGECoreParameter.getParameterAsInt("TOPBAIXAAJUSTE")));//1814
                                    reqBaixaVO.setProperty("TIPMOV", "Q");
                                    reqBaixaVO.setProperty("DTNEG", TimeUtils.getNow());
                                    reqBaixaVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("NATREQBAIXARET")));                                                          
                                    reqBaixaVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CRREQBAIXARET"))); 

                                    CACHelper cacHelper = new CACHelper();

                                    JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

                                    PrePersistEntityState cabPreState = PrePersistEntityState.build(entity,
                                            DynamicEntityNames.CABECALHO_NOTA, reqBaixaVO);

                                    BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);

                                    DynamicVO newCabVO = bRegrasCab.getState().getNewVO();

                                    nuReqBaixa = newCabVO.asBigDecimal("NUNOTA");

                                    System.out.println("nuReqBaixa:" + nuReqBaixa);
                                    inserirLog("Nota de baixa criada: " + nuReqBaixa, "S");

                                    Collection<PrePersistEntityState> itensBaixa = new ArrayList<PrePersistEntityState>();
                                    int itensProcessados = 0;

                                    while (rs.next()) {
                                        itensProcessados++;

                                        DynamicVO itemVO = (DynamicVO) entity
                                                .getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
                                        itemVO.setProperty("NUNOTA", nuReqBaixa);
                                        itemVO.setProperty("CODEMP", empVO.asBigDecimal("CODEMP"));
                                        itemVO.setProperty("CODPROD", rs.getBigDecimal("CODPROD"));
                                        itemVO.setProperty("CODVOL", rs.getString("CODVOL"));
                                        itemVO.setProperty("CODLOCALORIG", rs.getBigDecimal("CODLOCAL"));
                                        itemVO.setProperty("CONTROLE", rs.getString("CONTROLE"));

                                        String tipRet = rs.getString("AD_TIPRET");
                                        String controle = rs.getString("CONTROLE");
                                        BigDecimal qtd = rs.getBigDecimal("QTD");
                                        BigDecimal codProd = rs.getBigDecimal("CODPROD");
                                        
                                        System.out.println("qtd:" + qtd);
                                        System.out.println("controle:" + controle);
                                        System.out.println("tipRet:" + rs.getString("AD_TIPRET"));
                                        System.out.println("CODPROD:" + rs.getBigDecimal("CODPROD"));
                                        
                                        // AGORA SIM - LOG DETALHADO APENAS QUANDO PROCESSANDO BAIXA EFETIVA
                                        inserirLog("Processando item baixa " + itensProcessados + " - Produto: " + codProd + " - Qtd: " + qtd, "S");
                                        
                                        if (tipRet.equals("CH")) {

                                            if (controle.equals(">1500Retalho")
                                                    && qtd.compareTo(new BigDecimal(1.5)) <= 0) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("751x1500Retalho") && !estaNoIntervalo(qtd,
                                                    new BigDecimal(0.751), new BigDecimal(1.5))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("0x750Retalho")
                                                    && !estaNoIntervalo(qtd, new BigDecimal(0), new BigDecimal(0.75))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else {
                                                itemVO.setProperty("QTDNEG", null);
                                            }

                                        }

                                        if (tipRet.equals("BR")) {

                                            if (controle.equals(">3000Retalho")
                                                    && qtd.compareTo(new BigDecimal(3)) < 0) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("1501x3000Retalho") && !estaNoIntervalo(qtd,
                                                    new BigDecimal(1.501), new BigDecimal(3))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("501x1500Retalho") && !estaNoIntervalo(qtd,
                                                    new BigDecimal(0.501), new BigDecimal(1.5))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("0x500Retalho")
                                                    && !estaNoIntervalo(qtd, new BigDecimal(0), new BigDecimal(0.5))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else {
                                                itemVO.setProperty("QTDNEG", null);
                                            }

                                        }

                                        if (tipRet.equals("BO")) {

                                            if (controle.equals(">2000Retalho")
                                                    && qtd.compareTo(new BigDecimal(2)) < 0) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("1001x2000Retalho") && !estaNoIntervalo(qtd,
                                                    new BigDecimal(1.001), new BigDecimal(2))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("501x1000Retalho") && !estaNoIntervalo(qtd,
                                                    new BigDecimal(0.501), new BigDecimal(1))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else if (controle.equals("0x500Retalho")
                                                    && !estaNoIntervalo(qtd, new BigDecimal(0), new BigDecimal(0.5))) {
                                                itemVO.setProperty("QTDNEG", qtd);
                                            } else {
                                                itemVO.setProperty("QTDNEG", null);
                                            }

                                        }

                                        System.out.println("QTDNEG:" + itemVO.asBigDecimal("QTDNEG"));
                                        if (itemVO.asBigDecimal("QTDNEG") != null) {
                                            PrePersistEntityState itePreState = PrePersistEntityState.build(entity,
                                                    DynamicEntityNames.ITEM_NOTA, itemVO);
                                            itensBaixa.add(itePreState);
                                            inserirLog("Item adicionado para baixa - Produto: " + codProd + " - Qtd: " + itemVO.asBigDecimal("QTDNEG"), "S");
                                        }

                                    }

                                    System.out.println("itensBaixa.size():" + itensBaixa.size());
                                    inserirLog("Total de itens para baixa: " + itensBaixa.size(), "S");
                                    
                                    if (itensBaixa.size() > 0) {
                                        cacHelper.incluirAlterarItem(nuReqBaixa, auth, itensBaixa, true);
                                        inserirLog("Itens de baixa incluídos na nota " + nuReqBaixa, "S");
                                    } else {
                                        cacHelper.excluirNota(nuReqBaixa);
                                        nuReqBaixa = new BigDecimal(0);
                                        inserirLog("Nota de baixa excluída por não ter itens válidos", "S");
                                    }

                                }

                                if (nuReqBaixa.intValue() > 0) {

                                    inserirLog("Criando nota de entrada para empresa " + codEmp, "S");

                                    DynamicVO reqEntradaVO = (DynamicVO) entity
                                            .getDefaultValueObjectInstance("CabecalhoNota");

                                    reqEntradaVO.setProperty("CODEMP", empVO.asBigDecimal("CODEMP"));
                                    reqEntradaVO.setProperty("CODTIPOPER",
                                            new BigDecimal(MGECoreParameter.getParameterAsInt("TOPENTRAAJUSTE")));//1815
                                    reqEntradaVO.setProperty("TIPMOV", "Q");
                                    reqEntradaVO.setProperty("DTNEG", TimeUtils.getNow());
                                    reqEntradaVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("NATREQENTRET")));
                                    reqEntradaVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CRREQENTRET")));

                                    CACHelper cacHelper = new CACHelper();

                                    JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

                                    PrePersistEntityState cabPreState = PrePersistEntityState.build(entity,
                                            DynamicEntityNames.CABECALHO_NOTA, reqEntradaVO);

                                    BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);

                                    DynamicVO newCabVO = bRegrasCab.getState().getNewVO();

                                    nuReqEntrada = newCabVO.asBigDecimal("NUNOTA");

                                    System.out.println("nuReqEntrada:" + nuReqEntrada);
                                    inserirLog("Nota de entrada criada: " + nuReqEntrada, "S");

                                    Collection<PrePersistEntityState> itensEntrada = new ArrayList<PrePersistEntityState>();

                                    JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
                                    Collection<DynamicVO> itensBaixaVOs = iteDAO.find("nunota = ?", nuReqBaixa);

                                    inserirLog("Processando " + itensBaixaVOs.size() + " itens para entrada", "S");

                                    int itemEntradaCount = 0;
                                    for (DynamicVO itensBaixaVO : itensBaixaVOs) {
                                        itemEntradaCount++;

                                        DynamicVO itemVO = (DynamicVO) entity
                                                .getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);

                                        itemVO.setProperty("NUNOTA", nuReqEntrada);
                                        itemVO.setProperty("CODEMP", itensBaixaVO.asBigDecimal("CODEMP"));
                                        itemVO.setProperty("CODPROD", itensBaixaVO.asBigDecimal("CODPROD"));
                                        itemVO.setProperty("CODVOL", itensBaixaVO.asString("CODVOL"));
                                        itemVO.setProperty("CODLOCALORIG", itensBaixaVO.asBigDecimal("CODLOCALORIG"));
                                        itemVO.setProperty("QTDNEG", itensBaixaVO.asBigDecimal("QTDNEG"));

                                        JapeWrapper prodDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
                                        DynamicVO prodVO = prodDAO.findByPK(itensBaixaVO.getProperty("CODPROD"));

                                        BigDecimal qtdneg = itensBaixaVO.asBigDecimal("QTDNEG");
                                        String tipRet = prodVO.asString("AD_TIPRET");
                                        BigDecimal codProd = itensBaixaVO.asBigDecimal("CODPROD");

                                        inserirLog("Criando item entrada " + itemEntradaCount + " - Produto: " + codProd + " - Qtd: " + qtdneg + " - Tipo: " + tipRet, "S");

                                        if (tipRet.equals("CH")) {

                                            if (estaNoIntervalo(qtdneg, new BigDecimal(0), new BigDecimal(0.75))) {
                                                itemVO.setProperty("CONTROLE", "0x750Retalho");
                                            } else if (estaNoIntervalo(qtdneg, new BigDecimal(0.751),
                                                    new BigDecimal(1.5))) {
                                                itemVO.setProperty("CONTROLE", "751x1500Retalho");
                                            } else {
                                                itemVO.setProperty("CONTROLE", ">1500Retalho");
                                            }

                                        }

                                        if (tipRet.equals("BR")) {

                                            if (estaNoIntervalo(qtdneg, new BigDecimal(0), new BigDecimal(0.5))) {
                                                itemVO.setProperty("CONTROLE", "0x500Retalho");
                                            } else if (estaNoIntervalo(qtdneg, new BigDecimal(0.501),
                                                    new BigDecimal(1.5))) {
                                                itemVO.setProperty("CONTROLE", "501x1500Retalho");
                                            } else if (estaNoIntervalo(qtdneg, new BigDecimal(1.501),
                                                    new BigDecimal(3))) {
                                                itemVO.setProperty("CONTROLE", "1501x3000Retalho");
                                            } else {
                                                itemVO.setProperty("CONTROLE", ">3000Retalho");
                                            }

                                        }

                                        if (tipRet.equals("BO")) {

                                            if (estaNoIntervalo(qtdneg, new BigDecimal(0), new BigDecimal(0.5))) {
                                                itemVO.setProperty("CONTROLE", "0x500Retalho");
                                            } else if (estaNoIntervalo(qtdneg, new BigDecimal(0.501),
                                                    new BigDecimal(1))) {
                                                itemVO.setProperty("CONTROLE", "501x1000Retalho");
                                            } else if (estaNoIntervalo(qtdneg, new BigDecimal(1.001),
                                                    new BigDecimal(2))) {
                                                itemVO.setProperty("CONTROLE", "1001x2000Retalho");
                                            } else {
                                                itemVO.setProperty("CONTROLE", ">2000Retalho");
                                            }

                                        }

                                        inserirLog("Item entrada configurado - Produto: " + codProd + " - Controle: " + itemVO.asString("CONTROLE"), "S");

                                        PrePersistEntityState itePreState = PrePersistEntityState.build(entity,
                                                DynamicEntityNames.ITEM_NOTA, itemVO);
                                        itensEntrada.add(itePreState);

                                    }

                                    cacHelper.incluirAlterarItem(nuReqEntrada, auth, itensEntrada, true);
                                    inserirLog("Itens de entrada incluídos na nota " + nuReqEntrada, "S");

                                    inserirLog("Iniciando confirmação da nota " + nuReqBaixa, "S");
                                    confirmarNota(nuReqBaixa);
                                    inserirLog("Nota " + nuReqBaixa + " confirmada com sucesso", "S");
                                    
                                    inserirLog("Iniciando confirmação da nota " + nuReqEntrada, "S");
                                    confirmarNota(nuReqEntrada);
                                    inserirLog("Nota " + nuReqEntrada + " confirmada com sucesso", "S");

                                    inserirLog("Processo concluído com sucesso para empresa " + codEmp + " - Baixa: " + nuReqBaixa + " - Entrada: " + nuReqEntrada, "S");

                                } else {
                                    inserirLog("Nenhuma nota de entrada criada para empresa " + codEmp, "S");
                                }

                            } catch (Exception e) {
                                String errorMsg = "Erro doWithTx AjustaSaldoRetalho empresa " + empVO.asBigDecimal("CODEMP") + ": " + e.getMessage();
                                System.out.println(errorMsg);
                                inserirLog(errorMsg, "E");
                                e.printStackTrace();
                                throw new JapeSession.CanceledTransactionException();
                            }

                        }

                    });

                } else {
                    inserirLog("Nenhuma baixa necessária para empresa " + codEmp, "S");
                }

            }

            inserirLog("Processo AjustaSaldoRetalho finalizado com sucesso", "S");

        } catch (Exception e) {
            String errorMsg = "Erro ScheduledAction AjustaSaldoRetalho: " + e.getMessage();
            System.out.println(errorMsg);
            inserirLog(errorMsg, "E");
            e.printStackTrace();
        } finally {
            if (ctx != null)
                ctx.unregistry();
            AuthenticationInfo.unregistry();
            JapeSession.close(hnd);
        }

    }

    public static boolean estaNoIntervalo(BigDecimal valor, BigDecimal min, BigDecimal max) {
        return valor.compareTo(min) >= 0 && valor.compareTo(max) <= 0;
    }
    
    // Método para inserir log na tabela AD_DETACAOLOG com campo RESULTADO

    private void inserirLog(String mensagem, String resultado) {
        JdbcWrapper jdbc = null;
        NativeSql sqlInsert = null;
        NativeSql sqlSeq = null;
        ResultSet rsSeq = null;
        
        try {
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();
            
            // Primeiro, buscar a próxima sequência
            sqlSeq = new NativeSql(jdbc);
            sqlSeq.appendSql("SELECT NVL(MAX(SEQUENCIA), 0) + 1 AS PROXIMA_SEQ FROM AD_DETACAOLOG WHERE NUACAO = 3");
            rsSeq = sqlSeq.executeQuery();
            BigDecimal proximaSequencia = new BigDecimal(1);
            if (rsSeq.next()) {
                proximaSequencia = rsSeq.getBigDecimal("PROXIMA_SEQ");
            }
            
            // Inserir o registro na tabela AD_DETACAOLOG
            sqlInsert = new NativeSql(jdbc);
            sqlInsert.appendSql("INSERT INTO AD_DETACAOLOG (SEQUENCIA, NUACAO, DTINCLUSAO, STATUS, RESULTADO) ");
            sqlInsert.appendSql("VALUES (:SEQUENCIA, :NUACAO, SYSDATE, :STATUS, :RESULTADO)");        
            sqlInsert.setNamedParameter("SEQUENCIA", proximaSequencia);
            sqlInsert.setNamedParameter("NUACAO", new BigDecimal(3));
            sqlInsert.setNamedParameter("STATUS", mensagem);
            sqlInsert.setNamedParameter("RESULTADO", resultado);
            sqlInsert.executeUpdate();
            
            System.out.println("LOG: " + mensagem + " - Resultado: " + resultado);
            
        } catch (Exception e) {
            System.out.println("Erro ao inserir log: " + e.getMessage());
            // Não propagar o erro para não interromper o processo principal
        } finally {
            JdbcUtils.closeResultSet(rsSeq);
            if (sqlSeq != null) {
                NativeSql.releaseResources(sqlSeq);
            }
            if (sqlInsert != null) {
                NativeSql.releaseResources(sqlInsert);
            }
            JdbcWrapper.closeSession(jdbc);
            
        }
    }
        
    // Método confirmarNota modificado para incluir o log com resultado

    private void confirmarNota(BigDecimal nuNota) throws Exception {
        System.out.println("confirmarNota");
        
        JdbcWrapper jdbc = null;
        
        try {
            // Confirmar a nota
            BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class,
                    "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
            barramentoConfirmacao.setValidarSilencioso(true);
            ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
            
            // Inserir log na tabela AD_DETACAOLOG
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();
         
            
        } catch (Exception e) {
            String errorMsg = "Erro ao confirmar nota " + nuNota + ": " + e.getMessage();
            System.out.println(errorMsg);
            inserirLog(errorMsg, "E");
            throw e;
        } finally {
            JdbcWrapper.closeSession(jdbc);
        }
    }

}