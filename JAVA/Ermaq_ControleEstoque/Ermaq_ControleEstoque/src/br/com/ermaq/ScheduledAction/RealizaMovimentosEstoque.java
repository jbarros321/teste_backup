package br.com.ermaq.ScheduledAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.JdbcUtils;
import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
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
import br.com.sankhya.modelcore.helper.RotinaFechamentoHelper;
import br.com.sankhya.modelcore.comercial.PrecoCustoHelper;
import java.sql.Timestamp;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;


public class RealizaMovimentosEstoque implements ScheduledAction{


    @Override
    public void onTime(ScheduledActionContext arg0) {
        inserirLog("=== INICIANDO PROCESSAMENTO DE MOVIMENTOS DE ESTOQUE ===", "S");
        ServiceContext ctx = null;

        try {
            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
            auth.makeCurrent();
            ctx = new ServiceContext(null);
            ctx.setAutentication(auth);
            ctx.makeCurrent();
            SPBeanUtils.setupContext(ctx);
            inserirLog("Contexto para ação configurado com sucesso", "S");

            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
            Collection<DynamicVO> cabVOs = cabDAO.find("AD_CONTROLAESTOQUE = 'S' AND STATUSNOTA = 'L'");
            inserirLog("Consulta realizada -" + cabVOs.size() + " nota(s) encontrada(s) para processamento", "S");
            System.out.println("ServiceContext configurado com sucesso");
            
            int notasProcessadas = 0;
            int notasComSucesso = 0;
            int notasComErro = 0;
                 
            for (DynamicVO cabVO : cabVOs) {
                final BigDecimal nuNota = cabVO.asBigDecimal("NUNOTA");
                final String tipMov = cabVO.asString("TIPMOV");

                notasProcessadas++;
                inserirLog("", "S"); // Linha em branco para separar
                inserirLog("=== PROCESSANDO NOTA " + notasProcessadas + "/" + cabVOs.size() + " ===", "S");
                inserirLog("[NOTA: " + nuNota + "] - Tipo de Movimento: " + tipMov, "S");

                JapeSession.SessionHandle hnd = null;
                
                try {
                    hnd = JapeSession.open();     
                    hnd.setCanTimeout(false);
                    System.out.println("Antes do doWithTx linha 74");
                    hnd.execWithTX(new JapeSession.TXBlock() {
                    	
                    	
                        public void doWithTx() throws Exception {
                        	 inserirLog("[NOTA: " + nuNota + "] - Iniciando transação para registros de baixa e entrada", "S");
                             System.out.println("Passou o doWithTx linha 79");

                            try {
                            	
                            	
	
                                if ("C".equals(tipMov)) {
                                    inserirLog("Processando compra - TIPMOV: C para nota: " + nuNota, "S");
                                    BigDecimal nuReqBaixa = geraBaixaCompra(nuNota);
                                    inserirLog("Baixa de compra gerada. Requisição: " + nuReqBaixa, "S");
                                    BigDecimal nuReqEntrada = geraEntradaCompra(nuNota);
                                    inserirLog("Entrada de compra gerada. Requisição: " + nuReqEntrada, "S");
                                    confirmarNota(nuReqBaixa);
                                    inserirLog("Nota de baixa confirmada: " + nuReqBaixa, "S");
                                    confirmarNota(nuReqEntrada);
                                    inserirLog("Nota de entrada confirmada: " + nuReqEntrada, "S");
                                    atualizarStatus(nuNota, nuReqBaixa, nuReqEntrada);
                                    inserirLog("Status da nota atualizado: " + nuNota, "S");
                                }

                                if ("F".equals(tipMov) || "V".equals(tipMov)) {
                                    inserirLog("Processando retalho - TIPMOV: " + tipMov + " para nota: " + nuNota, "S");
                                    BigDecimal nuReqBaixa = geraBaixaRetalho(nuNota);
                                    inserirLog("Baixa de retalho gerada. Requisição: " + nuReqBaixa, "S");
                                    BigDecimal nuReqEntrada = geraEntradaRetalho(nuNota);
                                    inserirLog("Entrada de retalho gerada. Requisição: " + nuReqEntrada, "S");
                                    confirmarNota(nuReqBaixa);
                                    inserirLog("Nota de baixa de retalho confirmada: " + nuReqBaixa, "S");
                                    confirmarNota(nuReqEntrada);
                                    inserirLog("Nota de entrada de retalho confirmada: " + nuReqEntrada, "S");
                                    atualizarStatus(nuNota, nuReqBaixa, nuReqEntrada);
                                    inserirLog("Status da nota de retalho atualizado: " + nuNota, "S");
                                }
                            } catch (Exception e) {
                                // Log final do erro na transação
                                inserirLog("[NOTA: " + nuNota + "] -  ERRO NA TRANSAÇÃO: " + e.getMessage(), "E");
                                inserirLog("[NOTA: " + nuNota + "] -  Executando rollback - nenhuma alteração será gravada", "E");
                                System.out.println("linha 111 Erro na transação para nota " + nuNota + ": " + e.getMessage());
                                
                                throw new JapeSession.CanceledTransactionException();
                            }
                        }
                    });
                    
                    notasComSucesso++;
                 //   inserirLog("[NOTA: " + nuNota + "] - TRANSAÇÃO CONFIRMADA", "S");

                } catch (JapeSession.CanceledTransactionException e) {
                    notasComErro++;
                    inserirLog("[NOTA: " + nuNota + "] -  TRANSAÇÃO CANCELADA - Todas as operações foram desfeitas", "E");
                    logarErroDetalhado("TRANSAÇÃO CANCELADA", nuNota, e);
                    
                } catch (Exception e) {
                    notasComErro++;
                    inserirLog("[NOTA: " + nuNota + "] - ERRO CRÍTICO NO PROCESSAMENTO", "E");
                    logarErroDetalhado("ERRO CRÍTICO", nuNota, e);
                    
                } finally {
                    JapeSession.close(hnd);
                    inserirLog("[NOTA: " + nuNota + "] - Sessão encerrada", "S");
                }
            
            }

            inserirLog("", "S");
            inserirLog("=== RESUMO DO PROCESSAMENTO ===", "S");
            inserirLog("Total de notas analisadas: " + notasProcessadas, "S");
            inserirLog("Notas processadas com sucesso: " + notasComSucesso, "S");
            inserirLog("Notas com erro: " + notasComErro, notasComErro > 0 ? "E" : "S");
            inserirLog("=== PROCESSAMENTO FINALIZADO ===", "S");
            System.out.println("Processamento concluído com sucesso");
            
        } catch (Exception e) {
            logarErroDetalhado("ERRO na execução do ScheduledAction", null, e);
            System.out.println("Erro na execução do ScheduledAction RealizaMovimentosEstoque: " + e.getMessage());
            
        } finally {
            if (ctx != null) ctx.unregistry();
            AuthenticationInfo.unregistry();
        }
    }

	private void atualizarStatus(BigDecimal nuNota, BigDecimal nuReqBaixa, BigDecimal nuReqEntrada) throws Exception {
		inserirLog("Iniciando atualização de status para nota: " + nuNota, "S"); 
		try {
	            JapeWrapper isDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
	            isDAO.prepareToUpdateByPK(nuNota)
	            .set("AD_CONTROLAESTOQUE", "N")
	            .set("AD_NUREQBAIXA", nuReqBaixa)
	            .set("AD_NUREQENTRADA", nuReqEntrada)
	            .update();
	            inserirLog("Status atualizado com sucesso para nota: " + nuNota, "S");
	            System.out.println("Status atualizado com sucesso para nota: " + nuNota);
	            
		} catch (Exception e) {
		    logarErroDetalhado("Erro ao atualizar status", nuNota, e);
		    MGEModelException.throwMe(e);
		}
	}
	
    private void confirmarNota(BigDecimal nuNota) throws Exception {
        try {
            BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(
                CentralFaturamento.class, 
                "regrasConfirmacaoSilenciosa.xml", 
                AuthenticationInfo.getCurrent()
            );
            barramentoConfirmacao.setValidarSilencioso(true);
            ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
            
        } catch (Exception e) {
            logarErroDetalhado("Erro ao confirmar nota", nuNota, e);
            throw e; 
        }
    }	
    	
	private BigDecimal geraBaixaRetalho(BigDecimal nuNota) throws Exception {
		
		inserirLog("Iniciando geração de baixa de retalho para nota: " + nuNota, "S");
		System.out.println("Iniciando geração de baixa de retalho para nota: " + nuNota);
		
		BigDecimal nuReqBaixa = null;
		
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rs = null;
		SessionHandle hnd = null;
		
		try {
			
			JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
			DynamicVO cabVO = cabDAO.findByPK(nuNota);
			
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			AuthenticationInfo auth = AuthenticationInfo.getCurrent();
			JapeSessionContext.putProperty("usuario_logado", auth.getUserID());
			
			DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
			
			reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
			reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPBAIXAAJUSTE")));
			reqVO.setProperty("TIPMOV", "Q");
			reqVO.setProperty("DTNEG", TimeUtils.getNow());
			reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("NATREQBAIXARET")));    
			reqVO.setProperty("CODCENCUS",new BigDecimal(MGECoreParameter.getParameterAsInt("CRREQBAIXARET"))); 
			
			CACHelper cacHelper = new CACHelper();

			JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

			PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
					DynamicEntityNames.CABECALHO_NOTA, reqVO);

			BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);

			DynamicVO newCabVO = bRegrasCab.getState().getNewVO();

			nuReqBaixa = newCabVO.asBigDecimal("NUNOTA");
			
			inserirLog("Cabeçalho de baixa de retalho criado. Requisição: " + nuReqBaixa, "S");
			System.out.println("Cabeçalho de baixa de retalho criado. Requisição: " + nuReqBaixa);
			
			Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
			
			try {

				hnd = JapeSession.open();
				EntityFacade entity = EntityFacadeFactory.getDWFFacade();
				jdbc = entity.getJdbcWrapper();
				jdbc.openSession();

				sql = new NativeSql(jdbc);
				sql.appendSql("SELECT ITE.CODEMP, ITE.CODPROD,ITE.QTDNEG,ITE.CODVOL,ITE.CODLOCALORIG,VOA.QUANTIDADE,PRO.AD_TIPRET,ITE.CONTROLE ");
				sql.appendSql("FROM TGFITE ITE ");
				sql.appendSql("INNER JOIN TGFPRO PRO ");
				sql.appendSql("ON ITE.CODPROD = PRO.CODPROD ");
				sql.appendSql("INNER JOIN TGFVOA VOA ");
				sql.appendSql("ON VOA.CODPROD = ITE.CODPROD AND VOA.CODVOL = 'UN' AND ITE.CONTROLE = VOA.CONTROLE ");
	//			sql.appendSql("ON VOA.CODPROD = ITE.CODPROD AND VOA.CODVOL = ITE.CODVOL AND ITE.CONTROLE = VOA.CONTROLE ");
				sql.appendSql("WHERE PRO.AD_CONTROLARETALHO = 'S' ");
				sql.appendSql("AND ITE.CONTROLE LIKE '%Inteiro' ");
				sql.appendSql("AND MOD(ITE.QTDNEG,VOA.QUANTIDADE) <> 0 ");
				sql.appendSql("AND  ITE.NUNOTA = :NUNOTA ");
				sql.setNamedParameter("NUNOTA", nuNota);
				rs = sql.executeQuery();		
				
				while (rs.next()) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);		
					itemVO.setProperty("NUNOTA", nuReqBaixa);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", rs.getBigDecimal("CODPROD"));
					itemVO.setProperty("CODVOL", rs.getString("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", rs.getBigDecimal("CODLOCALORIG"));
					BigDecimal medidaPadrao = rs.getBigDecimal("QUANTIDADE");
					BigDecimal qtdneg = rs.getBigDecimal("QTDNEG");
					BigDecimal consumo = qtdneg.remainder(medidaPadrao);
					BigDecimal retalho = medidaPadrao.subtract(consumo);
					
					itemVO.setProperty("QTDNEG", retalho);
					itemVO.setProperty("CONTROLE", rs.getString("CONTROLE"));
									
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					
				}
				
				inserirLog("Itens de baixa de retalho preparados","S");	
				System.out.println("Itens de baixa de retalho preparados");
				
				cacHelper.incluirAlterarItem(nuReqBaixa, auth, itensNota, true);
				
				inserirLog("Itens de baixa de retalho incluídos com sucesso", "S");
				System.out.println("Itens de baixa de retalho incluídos com sucesso");

			} catch (Exception e) {
			    logarErroDetalhado("Erro ao processar itens de baixa de retalho", nuNota, e);
			    MGEModelException.throwMe(e);
			} finally {
				JdbcUtils.closeResultSet(rs);
				NativeSql.releaseResources(sql);
				JdbcWrapper.closeSession(jdbc);
				JapeSession.close(hnd);

			}
				
		} catch (Exception e) {
		    logarErroDetalhado("Erro na geração de baixa de retalho", nuNota, e);
		    MGEModelException.throwMe(e);
		}
			
		return nuReqBaixa;
	}
	
	private BigDecimal geraEntradaRetalho(BigDecimal nuNota) throws Exception {
		
		inserirLog("Iniciando geração de entrada de retalho para nota: " + nuNota, "S");
		System.out.println("Iniciando geração de entrada de retalho para nota: " + nuNota);
		
		BigDecimal nuReqEntrada = null;
		
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rs = null;
		SessionHandle hnd = null;
		
		try {
			
			JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
			DynamicVO cabVO = cabDAO.findByPK(nuNota);
			
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			AuthenticationInfo auth = AuthenticationInfo.getCurrent();
			JapeSessionContext.putProperty("usuario_logado", auth.getUserID());
			
			DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
			
			reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
			reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPENTRAAJUSTE")));
			reqVO.setProperty("TIPMOV", "Q");
			reqVO.setProperty("DTNEG", TimeUtils.getNow());
			reqVO.setProperty("CODNAT",new BigDecimal(MGECoreParameter.getParameterAsInt("NATREQENTRET")));
			reqVO.setProperty("CODCENCUS",new BigDecimal(MGECoreParameter.getParameterAsInt("CRREQENTRET")));
			
			CACHelper cacHelper = new CACHelper();

			JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

			PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
			DynamicEntityNames.CABECALHO_NOTA, reqVO);

			BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);

			DynamicVO newCabVO = bRegrasCab.getState().getNewVO();

			nuReqEntrada = newCabVO.asBigDecimal("NUNOTA");
			
			inserirLog("Cabeçalho de entrada de retalho criado. Requisição: " + nuReqEntrada, "S");
			System.out.println("Cabeçalho de entrada de retalho criado. Requisição: " + nuReqEntrada);
			
			Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
			
			try {

				hnd = JapeSession.open();
				EntityFacade entity = EntityFacadeFactory.getDWFFacade();
				jdbc = entity.getJdbcWrapper();
				jdbc.openSession();

				sql = new NativeSql(jdbc);
				sql.appendSql("SELECT ITE.CODEMP, ITE.CODPROD,ITE.QTDNEG,ITE.CODVOL,ITE.CODLOCALORIG,VOA.QUANTIDADE,PRO.AD_TIPRET ");
				sql.appendSql("FROM TGFITE ITE ");
				sql.appendSql("INNER JOIN TGFPRO PRO ");
				sql.appendSql("ON ITE.CODPROD = PRO.CODPROD ");
				sql.appendSql("INNER JOIN TGFVOA VOA ");
				sql.appendSql("ON VOA.CODPROD = ITE.CODPROD AND VOA.CODVOL = 'UN' AND ITE.CONTROLE = VOA.CONTROLE ");
				sql.appendSql("WHERE PRO.AD_CONTROLARETALHO = 'S' ");
				sql.appendSql("AND ITE.CONTROLE LIKE '%Inteiro' ");
				sql.appendSql("AND MOD(ITE.QTDNEG,VOA.QUANTIDADE) <> 0 ");
				sql.appendSql("AND  ITE.NUNOTA = :NUNOTA ");
				sql.setNamedParameter("NUNOTA", nuNota);

				rs = sql.executeQuery();
				while (rs.next()) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", rs.getBigDecimal("CODPROD"));
					itemVO.setProperty("CODVOL", rs.getString("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", rs.getBigDecimal("CODLOCALORIG"));
					
					String tipRet = rs.getString("AD_TIPRET");
					BigDecimal medidaPadrao = rs.getBigDecimal("QUANTIDADE");
					BigDecimal qtdneg = rs.getBigDecimal("QTDNEG");
					
					BigDecimal consumo = qtdneg.remainder(medidaPadrao);
					BigDecimal retalho = medidaPadrao.subtract(consumo);
					
					itemVO.setProperty("QTDNEG", retalho);
					
					if(tipRet.equals("CH")) {
						
						if(estaNoIntervalo(retalho, new BigDecimal(0), new BigDecimal(0.75))) {
							itemVO.setProperty("CONTROLE", "0x750Retalho");
						}else if(estaNoIntervalo(retalho, new BigDecimal(0.751), new BigDecimal(1.5))) {
							itemVO.setProperty("CONTROLE", "751x1500Retalho");
						}else {
							itemVO.setProperty("CONTROLE", ">1500Retalho");
						}

					}
					
					if(tipRet.equals("BR")) {
						
						if(estaNoIntervalo(retalho, new BigDecimal(0), new BigDecimal(0.5))) {
							itemVO.setProperty("CONTROLE", "0x500Retalho");
						}else if(estaNoIntervalo(retalho, new BigDecimal(0.501), new BigDecimal(1.5))) {
							itemVO.setProperty("CONTROLE", "501x1500Retalho");
						}else if(estaNoIntervalo(retalho, new BigDecimal(1.501), new BigDecimal(3))) {
							itemVO.setProperty("CONTROLE", "1501x3000Retalho");
						}else {
							itemVO.setProperty("CONTROLE", ">3000Retalho");
						}

					}
					
					if(tipRet.equals("BO")) {
						
						if(estaNoIntervalo(retalho, new BigDecimal(0), new BigDecimal(0.5))) {
							itemVO.setProperty("CONTROLE", "0x500Retalho");
						}else if(estaNoIntervalo(retalho, new BigDecimal(0.501), new BigDecimal(1))) {
							itemVO.setProperty("CONTROLE", "501x1000Retalho");
						}else if(estaNoIntervalo(retalho, new BigDecimal(1.001), new BigDecimal(2))) {
							itemVO.setProperty("CONTROLE", "1001x2000Retalho");
						}else {
							itemVO.setProperty("CONTROLE", ">2000Retalho");
						}

					}
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
				
				}
				
				inserirLog("Itens de entrada de retalho preparados.", "S");
				System.out.println("Itens de entrada de retalho preparados.");
				
				cacHelper.incluirAlterarItem(nuReqEntrada, auth, itensNota, true);
				
				inserirLog("Itens de entrada de retalho incluídos com sucesso", "S");
				System.out.println("Itens de entrada de retalho incluídos com sucesso");

			} catch (Exception e) {
			    logarErroDetalhado("Erro ao processar itens de entrada de retalho", nuNota, e);
			    MGEModelException.throwMe(e);
			} finally {
				JdbcUtils.closeResultSet(rs);
				NativeSql.releaseResources(sql);
				JdbcWrapper.closeSession(jdbc);
				JapeSession.close(hnd);

			}

		} catch (Exception e) {
		    logarErroDetalhado("Erro na geração de entrada de retalho", nuNota, e);
		    System.out.println("Erro geraEntradaRetalho: " + e.getMessage());
		    MGEModelException.throwMe(e);
		}
		
		
		return nuReqEntrada;
	}
	

	private BigDecimal geraEntradaCompra(BigDecimal nuNota) throws Exception {
		inserirLog("Iniciando geração de entrada de compra para nota: " + nuNota, "S");
		BigDecimal nuReqEntrada = null;
		
		JdbcWrapper jdbc = null;
		NativeSql sql = null;
		ResultSet rs = null;
		SessionHandle hnd = null;
		
		hnd = JapeSession.open();
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		jdbc = entity.getJdbcWrapper();
		jdbc.openSession();

		sql = new NativeSql(jdbc);
		sql.appendSql("SELECT VLRDESTAQUE FROM TGFCAB WHERE NUNOTA = :NUNOTA");
		sql.setNamedParameter("NUNOTA", nuNota);
		rs = sql.executeQuery();
		
		BigDecimal valorDestaque = BigDecimal.ZERO;
		
		if (rs.next()) {
		    valorDestaque = rs.getBigDecimal("VLRDESTAQUE");
		}
		
		try {
			
			JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
			DynamicVO cabVO = cabDAO.findByPK(nuNota);
			
			EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
			AuthenticationInfo auth = AuthenticationInfo.getCurrent();
			JapeSessionContext.putProperty("usuario_logado", auth.getUserID());
			
			DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
			
			reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
			reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPENTRACONTEST")));
			reqVO.setProperty("TIPMOV", "Q");
			reqVO.setProperty("DTNEG", TimeUtils.getNow());
			reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("CODNATENTCOMP")));
			reqVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CODCRENTCOMP")));
			reqVO.setProperty("VLRDESTAQUE", valorDestaque);
			
			
			CACHelper cacHelper = new CACHelper();

			JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

			PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade,
					DynamicEntityNames.CABECALHO_NOTA, reqVO);

			BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);

			DynamicVO newCabVO = bRegrasCab.getState().getNewVO();

			nuReqEntrada = newCabVO.asBigDecimal("NUNOTA");
			
			inserirLog("Cabeçalho de entrada de compra criado. Requisição: " + nuReqEntrada, "S");
			System.out.println("Cabeçalho de entrada de compra criado. Requisição: " + nuReqEntrada);
			
			JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
			Collection<DynamicVO> itens = iteDAO.find("NUNOTA = ? AND CONTROLE LIKE '%Inteiro' AND (AD_BO20000X1200 IS NOT NULL OR AD_BO20000X1400 IS NOT NULL OR AD_BORETALHO IS NOT NULL OR AD_BR6000 IS NOT NULL OR AD_BRRETALHO IS NOT NULL OR AD_CH2000X1200 IS NOT NULL OR AD_CH2000X1500 IS NOT NULL OR AD_CH3000X1200 IS NOT NULL OR AD_CH3000X1500 IS NOT NULL OR AD_CHRETALHO IS NOT NULL)",nuNota);
			
			inserirLog("Itens encontrados para entrada de compra. Total: " + itens.size(), "S");
			System.out.println("Itens encontrados para entrada de compra. Total: " + itens.size());
			
			Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();
			
			int totalItensProcessados = 0;
			
			for(DynamicVO ite: itens) {
				
				JapeWrapper prodDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
				DynamicVO prodVO = prodDAO.findByPK(ite.getProperty("CODPROD"));
				
				
				if(ite.asBigDecimalOrZero("AD_BO20000X1200").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_BO20000X1200"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "20000x1200Inteiro");
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_BO20000X1200"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_BO20000X1400").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_BO20000X1400"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "20000x1400Inteiro");
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_BO20000X1400"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				
				if(ite.asBigDecimalOrZero("AD_BR6000").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_BR6000"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "6000Inteiro");
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_BR6000"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_CH2000X1200").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_CH2000X1200"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "2000x1200Inteiro");
					//NOVO
					itemVO.setProperty( "VLRUNIT", ite.asBigDecimal("VLRTOT").divide( ite.asBigDecimal("AD_CH2000X1200"),6, RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_CH2000X1500").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_CH2000X1500"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "2000x1500Inteiro");
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_CH2000X1500"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_CH3000X1200").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_CH3000X1200"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "3000x1200Inteiro");
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_CH3000X1200"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_CH3000X1500").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_CH3000X1500"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					itemVO.setProperty("CONTROLE", "3000x1500Inteiro");
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_CH3000X1500"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_CHRETALHO").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_CHRETALHO"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_CHRETALHO"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_CHRETALHO"), new BigDecimal(0), new BigDecimal(0.75))) {
						itemVO.setProperty("CONTROLE", "0x750Retalho");
					}else if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_CHRETALHO"), new BigDecimal(0.751), new BigDecimal(1.5))) {
						itemVO.setProperty("CONTROLE", "751x1500Retalho");
					}else {
						itemVO.setProperty("CONTROLE", ">1500Retalho");
					}
					
					
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_BRRETALHO").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_BRRETALHO"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_BRRETALHO"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_CHRETALHO"), new BigDecimal(0), new BigDecimal(0.5))) {
						itemVO.setProperty("CONTROLE", "0x500Retalho");
					}else if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_CHRETALHO"), new BigDecimal(0.501), new BigDecimal(1.5))) {
						itemVO.setProperty("CONTROLE", "501x1500Retalho");
					}else if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_CHRETALHO"), new BigDecimal(1.501), new BigDecimal(3))) {
						itemVO.setProperty("CONTROLE", "1501x3000Retalho");
					}else {
						itemVO.setProperty("CONTROLE", ">3000Retalho");
					}
										
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
				
				if(ite.asBigDecimalOrZero("AD_BORETALHO").intValue() > 0) {
					
					DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
					
					itemVO.setProperty("NUNOTA", nuReqEntrada);
					itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
					itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
					itemVO.setProperty("QTDNEG", ite.getProperty("AD_BORETALHO"));
					itemVO.setProperty("CODVOL", prodVO.getProperty("CODVOL"));
					itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
					
					//NOVO
					itemVO.setProperty("VLRUNIT", ite.asBigDecimal("VLRTOT").divide(ite.asBigDecimal("AD_BORETALHO"),2,RoundingMode.HALF_UP));
					itemVO.setProperty("VLRDESC", new BigDecimal(0));
					itemVO.setProperty("PERCDESC", new BigDecimal(0));
					
					if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_BORETALHO"), new BigDecimal(0), new BigDecimal(0.5))) {
						itemVO.setProperty("CONTROLE", "0x500Retalho");
					}else if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_BORETALHO"), new BigDecimal(0.501), new BigDecimal(1))) {
						itemVO.setProperty("CONTROLE", "501x1000Retalho");
					}else if(estaNoIntervalo(ite.asBigDecimalOrZero("AD_BORETALHO"), new BigDecimal(1.001), new BigDecimal(2))) {
						itemVO.setProperty("CONTROLE", "1001x2000Retalho");
					}else {
						itemVO.setProperty("CONTROLE", ">2000Retalho");
					}
										
					PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade,
							DynamicEntityNames.ITEM_NOTA, itemVO);
					itensNota.add(itePreState);
					totalItensProcessados++;
				}
						
			

			}
			
			inserirLog("Itens de entrada de compra preparados. Total processado: " + totalItensProcessados, "S");
			System.out.println("Itens de entrada de compra preparados. Total processado: " + totalItensProcessados);
			
			cacHelper.incluirAlterarItem(nuReqEntrada, auth, itensNota, true);
			
			inserirLog("Itens de entrada de compra incluídos com sucesso", "S");
			System.out.println("Itens de entrada de compra incluídos com sucesso");
			
		} catch (Exception e) {
		    logarErroDetalhado("Erro na geração de entrada de compra", nuNota, e);
		    System.out.println("Erro geraEntradaCompra: " + e.getMessage());
		    MGEModelException.throwMe(e);
		} 
		
		return nuReqEntrada;
	}
	
	
	 public static boolean estaNoIntervalo(BigDecimal valor, BigDecimal min, BigDecimal max) {
	        return valor.compareTo(min) >= 0 && valor.compareTo(max) <= 0;
	 }
	    // Método para inserir log na tabela AD_DETACAOLOG com campo RESULTADO

	 /**
	  * Método para inserir log na tabela AD_DETACAOLOG com campo RESULTADO
	  * EXECUTA EM THREAD SEPARADA para garantir que os logs não sejam perdidos
	  * quando ocorrer CanceledTransactionException
	  */
	 /**
	  * Método para inserir log na tabela AD_DETACAOLOG com campo RESULTADO
	  * EXECUTA EM THREAD SEPARADA para garantir que os logs não sejam perdidos
	  */
	 private void inserirLog(String mensagem, String resultado) {
	     // Executar em thread separada para garantir transação independente
	     new Thread(() -> {
	         inserirLogInterno(mensagem, resultado);
	     }).start();
	 }

	 /**
	  * Método interno que executa o insert do log em thread separada
	  */
	 private void inserirLogInterno(String mensagem, String resultado) {
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
	         sqlSeq.appendSql("SELECT NVL(MAX(SEQUENCIA), 0) + 1 AS PROXIMA_SEQ FROM AD_DETACAOLOG WHERE NUACAO = 2");
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
	         sqlInsert.setNamedParameter("NUACAO", new BigDecimal(2));
	         sqlInsert.setNamedParameter("STATUS", mensagem);
	         sqlInsert.setNamedParameter("RESULTADO", resultado);
	         sqlInsert.executeUpdate();
	         
	         // Forçar commit fechando a sessão
	         JdbcWrapper.closeSession(jdbc);
	         jdbc = null;
	         
	     } catch (Exception e) {
	         System.err.println("ERRO AO INSERIR LOG: " + e.getMessage());
	         e.printStackTrace();
	     } finally {
	         JdbcUtils.closeResultSet(rsSeq);
	         if (sqlSeq != null) {
	             NativeSql.releaseResources(sqlSeq);
	         }
	         if (sqlInsert != null) {
	             NativeSql.releaseResources(sqlInsert);
	         }
	         if (jdbc != null) {
	             JdbcWrapper.closeSession(jdbc);
	         }
	     }
	 }

	 /**
	  * Método auxiliar para logar erros com detalhes
	  */
	    private void logarErroDetalhado(String operacao, BigDecimal nuNota, Exception e) {
	        String notaInfo = (nuNota != null) ? "[NOTA: " + nuNota + "] - " : "";
	        String tipoErro = e.getClass().getSimpleName();
	        String mensagem = e.getMessage() != null ? e.getMessage() : "Erro sem mensagem específica";

	        inserirLog("", "E"); // Linha em branco
	        inserirLog(" === DETALHES DO ERRO ===", "E");
	        inserirLog(notaInfo + " OPERAÇÃO: " + operacao, "E");
	        inserirLog(notaInfo + " TIPO: " + tipoErro, "E");
	        inserirLog(notaInfo + " MENSAGEM: " + mensagem, "E");

	        // Capturar causa raiz
	        Throwable causa = e.getCause();
	        if (causa != null) {
	            inserirLog(notaInfo + " CAUSA RAIZ: " + causa.getMessage(), "E");
	        }

	        // Stack trace do método principal
	        StackTraceElement[] stack = e.getStackTrace();
	        if (stack.length > 0) {
	            inserirLog(notaInfo + " LOCAL: " + stack[0].getMethodName() + " (Linha " + stack[0].getLineNumber() + ")", "E");
	        }

	        // Informações específicas do Sankhya
	        if (e instanceof MGEModelException) {
	            inserirLog(notaInfo + " ERRO SANKHYA: " + e.getMessage(), "E");
	        }

	        inserirLog("=== FIM DOS DETALHES ===", "E");
	        inserirLog("", "E"); // Linha em branco
	    }

		
	    private BigDecimal geraBaixaCompra(BigDecimal nuNota) throws Exception {
	        inserirLog("[NOTA: " + nuNota + "] - Iniciando geração de baixa de compra", "S");
	        
	        BigDecimal nuReqBaixa = null;
	        
	        try {
	            JapeWrapper cabDAO = JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA);
	            DynamicVO cabVO = cabDAO.findByPK(nuNota);
	            
	            if (cabVO == null) {
	                throw new Exception("Cabeçalho de nota não encontrado para NUNOTA=" + nuNota);
	            }

	            inserirLog("[NOTA: " + nuNota + "] - Preparando dados do cabeçalho de baixa...", "S");
	            
	            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
	            AuthenticationInfo auth = AuthenticationInfo.getCurrent();
	            JapeSessionContext.putProperty("usuario_logado", auth.getUserID());

	            DynamicVO reqVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("CabecalhoNota");
	            reqVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
	            reqVO.setProperty("CODTIPOPER", new BigDecimal(MGECoreParameter.getParameterAsInt("TOPBAIXACONTEST")));
	            reqVO.setProperty("TIPMOV", "Q");
	            reqVO.setProperty("DTNEG", TimeUtils.getNow());
	            reqVO.setProperty("CODNAT", new BigDecimal(MGECoreParameter.getParameterAsInt("CODNATBAIXACOMP")));    
	            reqVO.setProperty("CODCENCUS", new BigDecimal(MGECoreParameter.getParameterAsInt("CODCRBAIXACOMP")));

	            CACHelper cacHelper = new CACHelper();
	            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);

	            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfEntityFacade, DynamicEntityNames.CABECALHO_NOTA, reqVO);
	            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(auth, cabPreState);
	            DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
	            nuReqBaixa = newCabVO.asBigDecimal("NUNOTA");

	            inserirLog("[NOTA: " + nuNota + "] - Cabeçalho de baixa criado: " + nuReqBaixa, "S");

	            // Buscar e processar itens
	            JapeWrapper iteDAO = JapeFactory.dao(DynamicEntityNames.ITEM_NOTA);
	            Collection<DynamicVO> itens = iteDAO.find("NUNOTA = ? AND CONTROLE LIKE '%Inteiro' AND (AD_BO20000X1200 IS NOT NULL OR AD_BO20000X1400 IS NOT NULL OR AD_BORETALHO IS NOT NULL OR AD_BR6000 IS NOT NULL OR AD_BRRETALHO IS NOT NULL OR AD_CH2000X1200 IS NOT NULL OR AD_CH2000X1500 IS NOT NULL OR AD_CH3000X1200 IS NOT NULL OR AD_CH3000X1500 IS NOT NULL OR AD_CHRETALHO IS NOT NULL)", nuNota);

	            inserirLog("[NOTA: " + nuNota + "] - " + itens.size() + " item(ns) encontrado(s) para baixa", "S");

	            Collection<PrePersistEntityState> itensNota = new ArrayList<PrePersistEntityState>();

	            for(DynamicVO ite: itens) {
	                DynamicVO itemVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA);
	                itemVO.setProperty("NUNOTA", nuReqBaixa);
	                itemVO.setProperty("CODEMP", cabVO.asBigDecimal("CODEMP"));
	                itemVO.setProperty("CODPROD", ite.getProperty("CODPROD"));
	                itemVO.setProperty("QTDNEG", ite.getProperty("QTDNEG"));
	                itemVO.setProperty("CODVOL", ite.getProperty("CODVOL"));
	                itemVO.setProperty("CODLOCALORIG", ite.getProperty("CODLOCALORIG"));
	                itemVO.setProperty("CONTROLE", ite.getProperty("CONTROLE"));
	                itemVO.setProperty("VLRUNIT", ite.getProperty("VLRUNIT"));
	                itemVO.setProperty("VLRDESC", new BigDecimal(0));
	                itemVO.setProperty("PERCDESC", new BigDecimal(0));

	                PrePersistEntityState itePreState = PrePersistEntityState.build(dwfEntityFacade, DynamicEntityNames.ITEM_NOTA, itemVO);
	                itensNota.add(itePreState);
	            }

	            inserirLog("[NOTA: " + nuNota + "] - Incluindo " + itensNota.size() + " item(ns) na baixa...", "S");
	            cacHelper.incluirAlterarItem(nuReqBaixa, auth, itensNota, true);
	            inserirLog("[NOTA: " + nuNota + "] - ✓ Itens incluídos na baixa com sucesso", "S");

	        } catch (Exception e) {
	            logarErroDetalhado("Erro na geração de baixa de compra", nuNota, e);
	            MGEModelException.throwMe(e);
	        }

	        return nuReqBaixa;
	    }

}