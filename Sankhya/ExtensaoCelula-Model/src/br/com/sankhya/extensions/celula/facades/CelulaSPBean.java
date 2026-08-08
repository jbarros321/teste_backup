package br.com.sankhya.extensions.celula.facades;

import java.io.CharArrayReader;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import br.com.sankhya.colorpalette.ColorPalette;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sankhya.util.XMLUtils;

public class CelulaSPBean implements SessionBean {

	private SessionContext						sessionContext;
	private boolean 							exibirIndefinido 			= true;

	private static final long					serialVersionUID			= 1L;
	private static final String					ENTIDADE_MEMBROS_CELULA		= "MEMBCELULA";
	private static final String					ENTIDADE_SPRINT_BACKLOG		= "TCSSBL";
	private static final String					ENTIDADE_TAREFAS			= "TCSSBLD";
	private static final String					ENTIDADE_VERSOES			= "TSIVER";
	private static final String					ENTIDADE_CELULA				= "INDCELPROD";
	private static final String					ENTIDADE_TRIAGEM			= "INDTRIAMAN";
	private static final Map<String, String>	STATUS						= getOpcoesStatus();
	private static final TreeSet<String> 		STATUS_DESEJAVEL;
	private static final TreeSet<String>		STATUS_NAO_EXIBIR;
	private static final TreeSet<String>		CORES_NAO_USAR;
	private static final String					COR_TAREFA_OUTRA_CELULA 	= "FF4500";
	private static final String					COR_TAREFA_CONCLUIDA	 	= "CCCCCC";
	private static final String					COR_CORRECAO			 	= "000000";
	private static final String					COR_EVENTO_ADICIONAL 		= "4682B4";
	private static final String					RESOURCEID_FILTROS			= "br.com.sankhya.cronogramacelulas.list";
	private static final Map<String,BigDecimal> TIPO_DE_TASK				= new HashMap<String,BigDecimal>();

	private TreeSet<BigDecimal> 				listUsus					= new TreeSet<BigDecimal>();

	static {
		STATUS_DESEJAVEL = new TreeSet<String>();
		STATUS_DESEJAVEL.add(STATUS.get("PL"));
		STATUS_DESEJAVEL.add(STATUS.get("R"));
		STATUS_DESEJAVEL.add(STATUS.get("P"));
		STATUS_DESEJAVEL.add(STATUS.get("T"));
	}

	static {
		STATUS_NAO_EXIBIR = new TreeSet<String>();
		STATUS_NAO_EXIBIR.add(STATUS.get("C"));
		STATUS_NAO_EXIBIR.add(STATUS.get("RE"));
		STATUS_NAO_EXIBIR.add(STATUS.get("REM"));
	}

	static {
		TIPO_DE_TASK.put("FERIAS", new BigDecimal(-1));
		TIPO_DE_TASK.put("ESTFERIAS", new BigDecimal(-2));
		TIPO_DE_TASK.put("EVENTOSADICIONAIS", new BigDecimal(-3));
		TIPO_DE_TASK.put("EVENTOSADICIONAISC", new BigDecimal(-4));
	}

	static {
		CORES_NAO_USAR = new TreeSet<String>();
		CORES_NAO_USAR.add("E48701");
	}

	public void updateTask(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();

			Element tarefaElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "tarefa");

			BigDecimal numOS = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "NUMOS");
			BigDecimal numSprint = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "NUMSPRINT");
			BigDecimal codUsu = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "CODUSU");
			BigDecimal codUsuOld = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "CODUSUOLD");

			boolean ehTarefaQuebrada = "S".equals(XMLUtils.getRequiredAttributeAsString(tarefaElem, "TAREFAQUEBRADA")) ? true : false;
			String ciclo = XMLUtils.getRequiredAttributeAsString(tarefaElem, "CICLO");

			int diffDtIni = TimeUtils.getDifference(XMLUtils.getRequiredAttributeAsTimestamp(tarefaElem, "DTINI"), XMLUtils.getRequiredAttributeAsTimestamp(tarefaElem, "DTINIOLD"), false);
			int diffDtFim = TimeUtils.getDifference(XMLUtils.getRequiredAttributeAsTimestamp(tarefaElem, "DTFIM"), XMLUtils.getRequiredAttributeAsTimestamp(tarefaElem, "DTFIMOLD"), false);

			if (!ehTarefaQuebrada) {

				JapeWrapper tarefaDAO = JapeFactory.dao(ENTIDADE_TAREFAS);
				Collection<DynamicVO> tarefasVO = tarefaDAO.find("NUMOS = ? AND NUMSPRINT = ? AND CODUSU = ?", new Object[] { numOS, numSprint, codUsuOld });

				FluidUpdateVO fluid = null;

				for (DynamicVO tarefaVO : tarefasVO) {
					tarefaDAO.prepareToUpdate(tarefaVO)
								.set("CODUSU", codUsu)
								.set("DTINICIO", new Timestamp(TimeUtils.add(tarefaVO.asTimestamp("DTINICIO").getTime(), diffDtIni, Calendar.DAY_OF_MONTH)))
								.set("DTPREV", new Timestamp(TimeUtils.add(tarefaVO.asTimestamp("DTPREV").getTime(), diffDtFim, Calendar.DAY_OF_MONTH)))
								.update();
				}

				JapeWrapper sprintBackDAO = JapeFactory.dao(ENTIDADE_SPRINT_BACKLOG);
				DynamicVO sprintVO = sprintBackDAO.findByPK(new Object[] {numOS, numSprint, ciclo});
				FluidUpdateVO fluidSprintVO = sprintBackDAO.prepareToUpdate(sprintVO);

				if (sprintVO.asBigDecimalOrZero("DONOTESTE").compareTo(codUsuOld) == 0) {
					fluidSprintVO.set("DONOTESTE", codUsu);
				} else {
					fluidSprintVO.set("DONO", codUsu);
				}

				if (tarefasVO.isEmpty()) {
					fluidSprintVO.set("DTINI", new Timestamp(TimeUtils.add(sprintVO.asTimestamp("DTINI").getTime(), diffDtIni, Calendar.DAY_OF_MONTH)));
					fluidSprintVO.set("DTFIM", new Timestamp(TimeUtils.add(sprintVO.asTimestamp("DTFIM").getTime(), diffDtFim, Calendar.DAY_OF_MONTH)));
				}

				fluidSprintVO.update();
			} else {
				JapeWrapper tarefaDAO = JapeFactory.dao(ENTIDADE_TAREFAS);
				BigDecimal numTarefa = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "NUMTAREFA");
				Collection<DynamicVO> tarefasVO = tarefaDAO.find("NUMOS = ? AND NUMSPRINT = ? AND NUMTAREFA = ?", new Object[] { numOS, numSprint, numTarefa });

				for (DynamicVO tarefaVO : tarefasVO) {
					tarefaDAO.prepareToUpdate(tarefaVO).set("CODUSU", codUsu)
					.set("DTINICIO", new Timestamp(TimeUtils.add(tarefaVO.asTimestamp("DTINICIO").getTime(), diffDtIni, Calendar.DAY_OF_MONTH)))
					.set("DTPREV", new Timestamp(TimeUtils.add(tarefaVO.asTimestamp("DTPREV").getTime(), diffDtFim, Calendar.DAY_OF_MONTH)))
					.update();
				}

			}
		} catch (Exception e) {
			throwsMGEExceptionRollingBack(e);
		} finally {
			JapeSession.close(hnd);
		}

	}

	public void updateErrorTask(ServiceContext ctx) throws MGEModelException {
		SessionHandle hnd = null;

		try {
			hnd = JapeSession.open();

			Element tarefaElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "tarefa");

			BigDecimal numOS = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "NUMOS");
			BigDecimal codUsu = XMLUtils.getRequiredAttributeAsBigDecimal(tarefaElem, "CODUSU");

			Timestamp dtIni = XMLUtils.getRequiredAttributeAsTimestamp(tarefaElem, "DTINI");
			Timestamp dtFim = XMLUtils.getRequiredAttributeAsTimestamp(tarefaElem, "DTFIM");

			boolean ehTarefaDesenvolvimento = Boolean.valueOf(XMLUtils.getRequiredAttributeAsString(tarefaElem, "DEVTASK"));

			JapeWrapper triagemDAO = JapeFactory.dao(ENTIDADE_TRIAGEM);
			FluidUpdateVO fluidTriagemVO = triagemDAO.prepareToUpdateByPK(new Object[] {numOS});

			if (ehTarefaDesenvolvimento) {
				fluidTriagemVO.set("CODUSUDEV", codUsu);
			} else {
				fluidTriagemVO.set("CODUSUTESTE", codUsu);
			}

			fluidTriagemVO.set("DTINIPREV", dtIni);
			fluidTriagemVO.set("DTFIMPREV", dtFim);

			fluidTriagemVO.update();
		} catch (Exception e) {
			throwsMGEExceptionRollingBack(e);
		} finally {
			JapeSession.close(hnd);
		}
	}

	public void loadScheduleDiagram(ServiceContext ctx) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			Element cronogramaElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "cronograma");
			BigDecimal codUsu = AuthenticationInfo.getCurrent().getUserID();

			JapeWrapper configDAO = JapeFactory.dao(DynamicEntityNames.CONFIGURACAO_RECURSO);
			DynamicVO configCelulasVO = configDAO.findByPK(new Object[] { RESOURCEID_FILTROS + "." + ENTIDADE_CELULA, "T", codUsu });

			String filterCelula = getConfigFilterList(configCelulasVO, false);

			ScheduleTimeLine timeline = new ScheduleTimeLine();

			listUsus = new TreeSet<BigDecimal>();

			exibirIndefinido = cronogramaElem.getChild("indefinido").getAttributeValue("VALOR").equals("S");

			geraTarefasProjeto(jdbc, configDAO, timeline, codUsu, filterCelula);
			geraTarefasCorrecao(jdbc, configDAO, timeline, codUsu, filterCelula);
			geraFerias(jdbc, timeline, codUsu);
			geraEstFerias(jdbc, timeline, codUsu);
			geraTarefasQuebradas(jdbc,configDAO,timeline, codUsu);

			JapeWrapper empresaDAO = JapeFactory.dao(ENTIDADE_MEMBROS_CELULA);
			DynamicVO membroVO = empresaDAO.findOne("PAPEL = 1 AND CODUSU = ?", codUsu);

			boolean podeAlterarCronograma = false;
			if (membroVO != null) {
				podeAlterarCronograma = true;
			}

			Element source = new Element("cronogramaCelula");
			source.setAttribute("podeAlterar", String.valueOf(podeAlterarCronograma));
			source.addContent(timeline.buildXML());

			ctx.getBodyElement().addContent(source);
		} catch (Exception e) {
			throwsMGEExceptionRollingBack(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	private String getSQLFiltroUsuarios(Map<Integer,String> ArrayUsuarios) {

		String FilterUsu = "";

		for (Integer codUsuInt : ArrayUsuarios.keySet()) {
			if (!StringUtils.isEmpty(FilterUsu)) {
				FilterUsu += ", ";
			}
			FilterUsu += codUsuInt.toString();
		}

		return FilterUsu;

	}

	private Map<Integer,String> getArrayUsuarios(List<Element> Executantes) {
		Map<Integer,String> ArrayUsuarios = new HashMap<Integer,String>();
		for (Element Usu : Executantes) {
			ArrayUsuarios.put(Integer.parseInt(Usu.getAttributeValue("ID")),Usu.getAttributeValue("NOME"));
		}
		return ArrayUsuarios;
	}

	public void loadSpecificScheduleDiagram(ServiceContext ctx) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;

		try {
			hnd = JapeSession.open();
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			Element cronogramaElem = XMLUtils.getRequiredChild(ctx.getRequestBody(), "cronograma");
			BigDecimal codUsu = AuthenticationInfo.getCurrent().getUserID();

			JapeWrapper configDAO = JapeFactory.dao(DynamicEntityNames.CONFIGURACAO_RECURSO);

			ScheduleTimeLine timeline = new ScheduleTimeLine();

			List<Element> Executantes = cronogramaElem.getChildren("executante");

			Map<Integer,String> FilterUsu = getArrayUsuarios(Executantes);

			exibirIndefinido = cronogramaElem.getChild("indefinido").getAttributeValue("VALOR").equals("S");

			DynamicVO configCelulasVO = configDAO.findByPK(new Object[] { RESOURCEID_FILTROS + "." + ENTIDADE_CELULA, "T", codUsu });
			String filterCelula = getConfigFilterList(configCelulasVO, false);

			listUsus = new TreeSet<BigDecimal>();

			geraTarefasUsuarios(jdbc, timeline, codUsu, FilterUsu, configDAO, filterCelula);
			geraTarefasCorrecaoUsuarios(jdbc, timeline, codUsu, FilterUsu, configDAO, filterCelula);
			geraFerias(jdbc, timeline, codUsu);
			geraEstFerias(jdbc, timeline, codUsu);
			geraTarefasQuebradas(jdbc, configDAO, timeline, codUsu);

			JapeWrapper empresaDAO = JapeFactory.dao(ENTIDADE_MEMBROS_CELULA);
			DynamicVO membroVO = empresaDAO.findOne("PAPEL = 1 AND CODUSU = ?", codUsu);

			boolean podeAlterarCronograma = false;
			if (membroVO != null) {
				podeAlterarCronograma = true;
			}

			Element source = new Element("cronogramaCelula");
			source.setAttribute("podeAlterar", String.valueOf(podeAlterarCronograma));
			source.addContent(timeline.buildXML());

			ctx.getBodyElement().addContent(source);
		} catch (Exception e) {
			throwsMGEExceptionRollingBack(e);
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	private String returnSQLString() {
		String sqlUsus = "";
		for (BigDecimal usu : listUsus) {
			if (sqlUsus == "") {
				sqlUsus = usu.toString();
			} else {
				sqlUsus += ", " + usu.toString();
			}
		}
		return sqlUsus;
	}

	private void geraTarefasQuebradas(JdbcWrapper jdbc, JapeWrapper configDAO, ScheduleTimeLine timeline, BigDecimal codUsu) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasScrum.sql");

		if (listUsus.size() > 0) {
			String sqlUsus = returnSQLString();

			DynamicVO configVersaoVO = configDAO.findByPK(new Object[] { RESOURCEID_FILTROS + "." + ENTIDADE_VERSOES, "T", codUsu });

			String filterVersion = getConfigFilterList(configVersaoVO, true);

			if (StringUtils.isEmpty(filterVersion)) {
				query.removeSQLComment("VERSAO_ATUAL");
			} else {
				query.removeSQLComment("VERSAO_VARIAVEL");
				query.replaceSQLComment("${VERSOES}", filterVersion);
			}

			query.replaceSQLComment("${USUARIOS}", sqlUsus);

			ResultSet rs = query.executeQuery();

			while (rs.next()) {
				BigDecimal codUsuTarefa = rs.getBigDecimal("CODUSU");
				String nomeUsuTarefa = rs.getString("NOMEUSU");

				BigDecimal numOS = rs.getBigDecimal("NUMOS");
				BigDecimal numTarefa = rs.getBigDecimal("NUMTAREFA");
				BigDecimal numSprint = rs.getBigDecimal("NUMSPRINT");
				int celula = rs.getInt("CELULA");

				String ciclo = rs.getString("CICLO");
				String descricao = rs.getString("DESCRICAO");
				String corTarefa = rs.getString("COR");

				String status = rs.getString("STATUS");
				if ("D".equals(status)) {
					corTarefa = COR_TAREFA_CONCLUIDA;
				}

				Timestamp dtFim = rs.getTimestamp("DTFIM");
				Timestamp dtIni = rs.getTimestamp("DTINI") != null ? rs.getTimestamp("DTINI") : dtFim;

				if (!dtFim.before(dtIni) && codUsuTarefa != null) {
					if (!"D".equals(status) && dtIni.before(timeline.dataInicio)) {
						timeline.dataInicio = dtIni;
					}
					timeline.addTask(celula, numOS, numSprint, ciclo, descricao, codUsuTarefa, nomeUsuTarefa, dtIni, dtFim, "", nomeUsuTarefa, "", corTarefa, "S", false, true, numTarefa);
				}

			}
		}

	}

	private void geraFerias(JdbcWrapper jdbc, ScheduleTimeLine timeline, BigDecimal codUsu) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queFeriasCronograma.sql");

		if (listUsus.size() > 0) {
			String sqlUsus = returnSQLString();

			query.removeSQLComment("FILTRO_USUARIOS");
			query.replaceSQLComment("${USUARIOS}", sqlUsus);

			ResultSet rs = query.executeQuery();

			while (rs.next()) {

				BigDecimal codFer = rs.getBigDecimal("SOLIC");
				String nomeUsu = rs.getString("NOMEUSU");
				BigDecimal codUsuFerias = rs.getBigDecimal("CODUSU");
				BigDecimal qtdDesc = rs.getBigDecimal("QTDDESC") != null ? rs.getBigDecimal("QTDDESC") : new BigDecimal(30);
				String infoFuncionarioEEmpresa = rs.getString("CODFUNC") + "|" + rs.getString("CODEMP");
				String descricao = "Frias de " + nomeUsu;

				Timestamp dtInicioFerias = rs.getTimestamp("DTFERIAS");
				if (dtInicioFerias != null && qtdDesc != null) {
					Timestamp dtFimFerias = new Timestamp(TimeUtils.add(dtInicioFerias.getTime(), qtdDesc.intValue(), Calendar.DAY_OF_MONTH));

					if (codUsuFerias != null) {
						timeline.addTaskFerias(codFer, descricao, codUsuFerias, nomeUsu, dtInicioFerias, dtFimFerias, infoFuncionarioEEmpresa);
					}
				}

			}
		}
	}
	private void geraEstFerias(JdbcWrapper jdbc, ScheduleTimeLine timeline, BigDecimal codUsu) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queEstFeriasCronograma.sql");

		if (listUsus.size() > 0) {
			String sqlUsus = returnSQLString();

			query.removeSQLComment("FILTRO_USUARIOS");
			query.replaceSQLComment("${USUARIOS}", sqlUsus);

			ResultSet rs = query.executeQuery();

			while (rs.next()) {

				BigDecimal codEstFer = rs.getBigDecimal("CODEST");
				String nomeUsu = rs.getString("NOMEUSU");
				BigDecimal codUsuFerias = rs.getBigDecimal("CODUSU");
				BigDecimal qtdDesc = rs.getBigDecimal("QTDDIASDESCANSO") != null ? rs.getBigDecimal("QTDDIASDESCANSO") : new BigDecimal(30);
				String infoFuncionarioEEmpresa = rs.getString("CODFUNC") + "|" + rs.getString("CODEMP");

				String descricao = "Estimativa Frias de " + nomeUsu;

				Timestamp dtInicioEst = rs.getTimestamp("DTINIEST");
				if (dtInicioEst != null && qtdDesc != null) {
					Timestamp dtFimEst = new Timestamp(TimeUtils.add(dtInicioEst.getTime(), qtdDesc.intValue(), Calendar.DAY_OF_MONTH));

					if (codUsuFerias != null) {
						timeline.addTaskEstFerias(codEstFer, descricao, codUsuFerias, nomeUsu, dtInicioEst, dtFimEst, infoFuncionarioEEmpresa);
					}
				}
			}
		}
	}

	private void geraAdicionais(JdbcWrapper jdbc, ScheduleTimeLine timeline, BigDecimal codUsu, String filterCelula) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queAdicionaisCronograma.sql");

		if (listUsus.size() > 0) {

			String sqlUsus = returnSQLString();

			if (!StringUtils.isEmpty(filterCelula)) {
				query.removeSQLComment("FILTRO_CELULA");
				query.replaceSQLComment("${CELULAS}", filterCelula);
			} else {
				query.removeSQLComment("FILTRO_USUARIOS");
				query.replaceSQLComment("${USUARIOS}", sqlUsus);
			}

			ResultSet rs = query.executeQuery();

			Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

			while (rs.next()) {

				String nomeUsu = rs.getString("NOMEUSU");
				BigDecimal codUsuEv = rs.getBigDecimal("CODUSU");

				String descricao = rs.getString("DESCRICAO");
				String status = rs.getString("STATUS") == null ? "Em Aberto" : rs.getString("STATUS");
				BigDecimal codEvento = rs.getBigDecimal("CODEVENTO");

				Timestamp dtInicio = rs.getTimestamp("DTINICIO");
				Timestamp dtFim = rs.getTimestamp("DTFIM");
				if (codUsuEv != null) {
					boolean concluido = false;
					if (dtInicio.before(firstDate) && "Em Aberto".equals(status)) {
						firstDate = dtInicio;
					} else if (!"Em Aberto".equals(status)) {
						concluido = true;
					}
					timeline.addTaskEventosAdicionais(codEvento, descricao, codUsuEv, nomeUsu, dtInicio, dtFim, concluido);
				}

			}
			if (firstDate != null && firstDate.before(timeline.dataInicio)) {
				timeline.dataInicio = firstDate;
			}
		}
	}

	private void geraTarefasProjeto(JdbcWrapper jdbc, JapeWrapper configDAO, ScheduleTimeLine timeline, BigDecimal codUsu, String filterCelula) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasCronograma.sql");

		DynamicVO configVersaoVO = configDAO.findByPK(new Object[] { RESOURCEID_FILTROS + "." + ENTIDADE_VERSOES, "T", codUsu });

		String filterVersion = getConfigFilterList(configVersaoVO, true);

		if (StringUtils.isEmpty(filterVersion)) {
			query.removeSQLComment("VERSAO_ATUAL");
		} else {
			query.removeSQLComment("VERSAO_VARIAVEL");
			query.replaceSQLComment("${VERSOES}", filterVersion);
		}

		boolean usaFiltroCelula = false;

		if (!StringUtils.isEmpty(filterCelula)) {
			query.removeSQLComment("FILTRO_CELULA");
			query.replaceSQLComment("${CELULAS}", filterCelula);
			usaFiltroCelula = true;
			if (exibirIndefinido) {
				geraTarefasSemExecutante(jdbc, configDAO, timeline, codUsu, filterCelula);
			}
		} else {
			query.removeSQLComment("FILTRA_USUARIO");
			query.replaceSQLComment("${USUARIOS}", AuthenticationInfo.getCurrent().getUserID().toString());
			listUsus.add(AuthenticationInfo.getCurrent().getUserID());
		}

		ResultSet rs = query.executeQuery();

		Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

		while (rs.next()) {
			Integer celula = rs.getInt("CELULA");
			BigDecimal numOS = rs.getBigDecimal("NUMOS");
			BigDecimal numSprint = rs.getBigDecimal("NUMSPRINT");
			BigDecimal codUsuDev = rs.getBigDecimal("EXECDEV");
			BigDecimal codUsuTeste = rs.getBigDecimal("EXECTESTE");

			int tarefas = rs.getInt("TAREFAS");

			TreeSet<String> celulasDevList = returnListByString(rs.getString("CELULADEV"), ", ");
			TreeSet<String> celulasTesterList = returnListByString(rs.getString("CELULATESTER"), ", ");
			TreeSet<String> celulasFiltro = returnListByString(filterCelula, ", ");

			String nomeUsuDev = rs.getString("NOMEEXECDEV");
			String nomeUsuTeste = rs.getString("NOMEEXECTESTE");
			String corTarefa;
			String status = StringUtils.getEmptyAsNull(rs.getString("STATUS"));
			status = status == null ? "No identificado" : STATUS.get(status);

			if ((StringUtils.isEmpty(filterCelula) || celulasFiltro.contains(celula.toString())) && STATUS_DESEJAVEL.contains(status)) {
				corTarefa = rs.getString("COR") != null && rs.getString("COR").contains("#") ? rs.getString("COR").replace("#","") : rs.getString("COR");
			} else if (!STATUS_DESEJAVEL.contains(status)) {
				corTarefa = COR_TAREFA_CONCLUIDA;
			} else {
				corTarefa = COR_TAREFA_OUTRA_CELULA;
			}

			if (STATUS_NAO_EXIBIR.contains(status)) {
				continue;
			}

			String descricao = StringUtils.getEmptyAsNull(rs.getString("TITULO"));
			String emExecucao = rs.getString("EMEXECUCAO");
			String ciclo = rs.getString("CICLO");

			Timestamp dtInicioDev = rs.getTimestamp("DTINI");
			Timestamp dtFimDev = rs.getTimestamp("DTFIM");
			Timestamp dtInicioTeste = rs.getTimestamp("DTINITESTE");
			Timestamp dtFimTeste = rs.getTimestamp("DTFIMTESTE");

			boolean ehPairProg = "S".equals(StringUtils.getEmptyAsNull(rs.getString("PAIRPROG"))) ? true : false;
			boolean ehTesteUnit = "U".equals(StringUtils.getEmptyAsNull(rs.getString("TESTEAUTO"))) ? true : false;

			String toolTip = getToolTipTask(numOS, rs.getString("NOMEPARC"), descricao, nomeUsuDev, nomeUsuTeste, status, ehPairProg, ehTesteUnit, false);

			boolean incluirDev = ((usaFiltroCelula && !Collections.disjoint(celulasFiltro,celulasDevList)) || (!usaFiltroCelula && codUsu.equals(codUsuDev))) && codUsuDev != null;
			boolean incluirTester = ((usaFiltroCelula && !Collections.disjoint(celulasFiltro,celulasTesterList)) || (!usaFiltroCelula && codUsu.equals(codUsuTeste)))&& codUsuTeste != null;

			if (incluirDev && (TimeUtils.getDifference(dtFimDev, dtInicioDev, false) >= 0)) {
				if (!listUsus.contains(codUsuDev)) {
					listUsus.add(codUsuDev);
				}
				if (tarefas == 0) {
					timeline.addTask(celula, numOS, numSprint, ciclo, descricao, codUsuDev, nomeUsuDev, dtInicioDev, dtFimDev, toolTip, nomeUsuDev, nomeUsuTeste, corTarefa, emExecucao, true);
					if (STATUS_DESEJAVEL.contains(status) && dtInicioDev.before(firstDate)) {
						firstDate = dtInicioDev;
					}
				}
				if (usaFiltroCelula && !belongsToCell(celulasDevList, celulasFiltro) && listUsus.contains(codUsuDev)) {
					timeline.lanes.get(codUsuDev + " - " + nomeUsuDev).addProperty("FOREIGNUSU", "S");
				}
			}

			if (incluirTester && (TimeUtils.getDifference(dtFimTeste, dtInicioTeste, false) >= 0)) {
				if (!listUsus.contains(codUsuTeste)) {
					listUsus.add(codUsuTeste);
				}
				if (tarefas == 0) {
					timeline.addTask(celula, numOS, numSprint, ciclo, descricao, codUsuTeste, nomeUsuTeste, dtInicioTeste, dtFimTeste, toolTip, nomeUsuDev, nomeUsuTeste, corTarefa, emExecucao, false);
				}
				if (usaFiltroCelula && !belongsToCell(celulasTesterList, celulasFiltro) && celulasFiltro.contains(celula.toString())  && listUsus.contains(codUsuTeste)) {
					timeline.lanes.get(codUsuTeste + " - " + nomeUsuTeste).addProperty("FOREIGNUSU", "S");
				}
			}

		}
		if (firstDate != null && firstDate.before(timeline.dataInicio)) {
			timeline.dataInicio = firstDate;
		}
	}

	private void geraTarefasSemExecutante(JdbcWrapper jdbc, JapeWrapper configDAO, ScheduleTimeLine timeline, BigDecimal codUsu, String filterCelula) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasSemExecutante.sql");

		DynamicVO configVersaoVO = configDAO.findByPK(new Object[] { RESOURCEID_FILTROS + "." + ENTIDADE_VERSOES, "T", codUsu });

		String filterVersion = getConfigFilterList(configVersaoVO, true);

		if (StringUtils.isEmpty(filterVersion)) {
			query.removeSQLComment("VERSAO_ATUAL");
		} else {
			query.removeSQLComment("VERSAO_VARIAVEL");
			query.replaceSQLComment("${VERSOES}", filterVersion);
		}

		if (!StringUtils.isEmpty(filterCelula)) {
			query.removeSQLComment("FILTRO_CELULA");
			query.replaceSQLComment("${CELULAS}", filterCelula);
		}

		ResultSet rs = query.executeQuery();

		Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

		while (rs.next()) {
			Integer celula = rs.getInt("CELULA");
			BigDecimal numOS = rs.getBigDecimal("NUMOS");
			BigDecimal numSprint = rs.getBigDecimal("NUMSPRINT");
			BigDecimal codUsuDev = new BigDecimal(0);

			String nomeUsuDev = "~ Indefinido";
			String nomeUsuTeste = "";
			String corTarefa = rs.getString("COR") != null && rs.getString("COR").contains("#") ? rs.getString("COR").replace("#","") : rs.getString("COR");;
			String status = StringUtils.getEmptyAsNull(rs.getString("STATUS"));
			status = status == null ? "No identificado" : STATUS.get(status);

			if (!STATUS_DESEJAVEL.contains(status)) {
				corTarefa = COR_TAREFA_CONCLUIDA;
			}

			if (STATUS_NAO_EXIBIR.contains(status)) {
				continue;
			}

			String descricao = StringUtils.getEmptyAsNull(rs.getString("TITULO"));
			String emExecucao = rs.getString("EMEXECUCAO");
			String ciclo = rs.getString("CICLO");

			Timestamp dtInicioDev = rs.getTimestamp("DTINI");
			Timestamp dtFimDev = rs.getTimestamp("DTFIM");
			Timestamp dtInicioTeste = rs.getTimestamp("DTINITESTE");
			Timestamp dtFimTeste = rs.getTimestamp("DTFIMTESTE");

			boolean ehPairProg = "S".equals(StringUtils.getEmptyAsNull(rs.getString("PAIRPROG"))) ? true : false;
			boolean ehTesteUnit = "U".equals(StringUtils.getEmptyAsNull(rs.getString("TESTEAUTO"))) ? true : false;

			String toolTip = getToolTipTask(numOS, rs.getString("NOMEPARC"), descricao, nomeUsuDev, nomeUsuTeste, status, ehPairProg, ehTesteUnit, false);

			if (TimeUtils.getDifference(dtFimDev, dtInicioDev, false) >= 0) {
				if (STATUS_DESEJAVEL.contains(status) && dtInicioDev.before(firstDate)) {
					firstDate = dtInicioDev;
				}
				timeline.addTask(celula, numOS, numSprint, ciclo, descricao, codUsuDev, nomeUsuDev, dtInicioDev, dtFimDev, toolTip, nomeUsuDev, nomeUsuTeste, corTarefa, emExecucao, true);
			}

		}
		if (firstDate != null && firstDate.before(timeline.dataInicio)) {
			timeline.dataInicio = firstDate;
		}
		geraTarefasCorrecaoSemExecutante(jdbc, configDAO, timeline, codUsu, filterCelula);
	}

	private void geraTarefasCorrecaoSemExecutante(JdbcWrapper jdbc, JapeWrapper configDAO, ScheduleTimeLine timeline, BigDecimal codUsu, String filterCelula) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasCorrecaoSemExecutante.sql");

		if (!StringUtils.isEmpty(filterCelula)) {
			query.removeSQLComment("FILTRO_CELULA");
			query.replaceSQLComment("${CELULAS}", filterCelula);
		}

		ResultSet rs = query.executeQuery();

		Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

		while (rs.next()) {
			Integer celula = rs.getInt("CELULA");
			BigDecimal numOS = rs.getBigDecimal("NUMOS");
			BigDecimal numSprint = new BigDecimal(-1);
			BigDecimal codUsuDev = new BigDecimal(0);

			TreeSet<String> celulasFiltro = returnListByString(filterCelula, ", ");

			String nomeUsuDev = "~ Indefinido";
			String nomeUsuTeste = "~ Indefinido";
			String titulo = StringUtils.getEmptyAsNull(rs.getString("TITULO"));
			String descricao = (titulo == null ? numOS.toString() : numOS.toString() + " - " + titulo);

			Timestamp dtIniPrev = rs.getTimestamp("DTINIPREV");
			Timestamp dtFimPrev = rs.getTimestamp("DTFIMPREV");

			Integer lastItemOpen = rs.getInt("LASTITEMOPEN");

			boolean pertenceCelula = true;

			if (!StringUtils.isEmpty(filterCelula) && !celulasFiltro.contains(celula.toString())) {
				pertenceCelula = false;
			}

			if (TimeUtils.getDifference(dtFimPrev, dtIniPrev, false) >= 0) {
				String toolTip = getToolTipTask(numOS, rs.getString("NOMEPARC"), titulo, nomeUsuDev, nomeUsuTeste);

				if (lastItemOpen != 0 && dtIniPrev.before(firstDate)) {
					firstDate = dtIniPrev;
				}

				String corTarefa = "";
				if ((lastItemOpen != 0) && pertenceCelula) {
					corTarefa = COR_CORRECAO;
				} else if (lastItemOpen == 0) {
					corTarefa = COR_TAREFA_CONCLUIDA;
				} else {
					corTarefa = COR_TAREFA_OUTRA_CELULA;
				}

				timeline.addTaskCorrecao(celula, numOS, numSprint, descricao, codUsuDev, nomeUsuDev, dtIniPrev, dtFimPrev, toolTip, nomeUsuDev, nomeUsuTeste, true, corTarefa);

			}
		}
		if (firstDate != null && firstDate.before(timeline.dataInicio)) {
			timeline.dataInicio = firstDate;
		}

	}

	private boolean belongsToCell(TreeSet<String> celulasUsu, TreeSet<String> celulasSelecionadas) {
		for (String celulaSelecionada : celulasSelecionadas) {
			if (celulasUsu.contains(celulaSelecionada)) return true;
		}
		return false;
	}

	private TreeSet<String> returnListByString(String str, String delimiter) {
		TreeSet<String> list = new TreeSet<String>();
		if (str.indexOf(delimiter) == -1 && str.length() > 0) {
			list.add(str);
		} else {
			for (String umaCelula : str.split(delimiter)) {
				list.add(umaCelula);
			}
		}
		return list;
	}

	private void geraTarefasUsuarios(JdbcWrapper jdbc, ScheduleTimeLine timeline, BigDecimal codUsu, Map<Integer,String> FilterUsu, JapeWrapper configDAO, String filterCelula) throws Exception {
		geraTarefasProjeto(jdbc, configDAO, timeline, codUsu, filterCelula);

		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasCronograma.sql");

		List<String> listUsu = null;
		String Filtro = getSQLFiltroUsuarios(FilterUsu);

		DynamicVO configVersaoVO = configDAO.findByPK(new Object[] { RESOURCEID_FILTROS + "." + ENTIDADE_VERSOES, "T", codUsu });

		String filterVersion = getConfigFilterList(configVersaoVO, true);
		if (StringUtils.isEmpty(filterVersion)) {
			query.removeSQLComment("VERSAO_ATUAL");
		} else {
			query.removeSQLComment("VERSAO_VARIAVEL");
			query.replaceSQLComment("${VERSOES}", filterVersion);
		}

		if (!StringUtils.isEmpty(Filtro)) {
			query.removeSQLComment("FILTRA_USUARIO");
			query.replaceSQLComment("${USUARIOS}", Filtro);
			listUsu = Arrays.asList(Filtro.split(", "));
		}

		ResultSet rs = query.executeQuery();

		Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

		BigDecimal codUsuAtivo = null;

		while (rs.next()) {
			Integer celula = rs.getInt("CELULA");
			BigDecimal numOS = rs.getBigDecimal("NUMOS");
			BigDecimal numSprint = rs.getBigDecimal("NUMSPRINT");
			BigDecimal codUsuDev = rs.getBigDecimal("EXECDEV");
			BigDecimal codUsuTeste = rs.getBigDecimal("EXECTESTE");

			TreeSet<String> celulasDevList = returnListByString(rs.getString("CELULADEV"), ", ");
			TreeSet<String> celulasTesterList = returnListByString(rs.getString("CELULATESTER"), ", ");
			TreeSet<String> celulasFiltro = returnListByString(filterCelula, ", ");

			String nomeUsuDev = rs.getString("NOMEEXECDEV");
			String nomeUsuTeste = rs.getString("NOMEEXECTESTE");

			String laneDescriptionDev = codUsuDev + " - " + nomeUsuDev;
			ScheduleLane laneDev = timeline.lanes.get(laneDescriptionDev);
			if (laneDev == null || codUsuDev.equals(codUsuAtivo)) {
				codUsuAtivo = codUsuDev;
				String corTarefa;
				String status = StringUtils.getEmptyAsNull(rs.getString("STATUS"));
				status = status == null ? "No identificado" : STATUS.get(status);
				if ((StringUtils.isEmpty(filterCelula) || celulasFiltro.contains(celula.toString())) && STATUS_DESEJAVEL.contains(status)) {
					corTarefa = rs.getString("COR") != null && rs.getString("COR").contains("#") ? rs.getString("COR").replace("#","") : rs.getString("COR");
				} else if (!STATUS_DESEJAVEL.contains(status)) {
					corTarefa = COR_TAREFA_CONCLUIDA;
				} else {
					corTarefa = COR_TAREFA_OUTRA_CELULA;
				}

				if (STATUS_NAO_EXIBIR.contains(status)) {
					continue;
				}

				int tarefas = rs.getInt("TAREFAS");

				String descricao = StringUtils.getEmptyAsNull(rs.getString("TITULO"));
				String emExecucao = rs.getString("EMEXECUCAO");
				String ciclo = rs.getString("CICLO");

				Timestamp dtInicioDev = rs.getTimestamp("DTINI");
				Timestamp dtFimDev = rs.getTimestamp("DTFIM");
				Timestamp dtInicioTeste = rs.getTimestamp("DTINITESTE");
				Timestamp dtFimTeste = rs.getTimestamp("DTFIMTESTE");

				boolean ehPairProg = "S".equals(StringUtils.getEmptyAsNull(rs.getString("PAIRPROG"))) ? true : false;
				boolean ehTesteUnit = "U".equals(StringUtils.getEmptyAsNull(rs.getString("TESTEAUTO"))) ? true : false;

				if (STATUS_NAO_EXIBIR.contains(status)) {
					continue;
				}

				String toolTip = getToolTipTask(numOS, rs.getString("NOMEPARC"), descricao, nomeUsuDev, nomeUsuTeste, status, ehPairProg, ehTesteUnit, false);

				Boolean fazParte = codUsuDev != null ? listUsu.contains(codUsuDev.toString()) : false;
				if (fazParte && codUsuDev != null && (TimeUtils.getDifference(dtFimDev, dtInicioDev, false) >= 0)) {
					if (!listUsus.contains(codUsuDev)) {
						listUsus.add(codUsuDev);
					}
					if (tarefas == 0) {
						timeline.addTask(celula, numOS, numSprint, ciclo, descricao, codUsuDev, nomeUsuDev, dtInicioDev, dtFimDev, toolTip, nomeUsuDev, nomeUsuTeste, corTarefa, emExecucao, true);
						if (STATUS_DESEJAVEL.contains(status) && dtInicioDev.before(firstDate)) {
							firstDate = dtInicioDev;
						}
					}
				}
				Boolean fazParteTester = codUsuTeste != null ? listUsu.contains(codUsuTeste.toString()) : false;
				if (fazParteTester && codUsuTeste != null && (TimeUtils.getDifference(dtFimTeste, dtInicioTeste, false) >= 0)) {
					if (!listUsus.contains(codUsuTeste)) {
						listUsus.add(codUsuTeste);
					}
					if (tarefas == 0) {
						timeline.addTask(celula, numOS, numSprint, ciclo, descricao, codUsuTeste, nomeUsuTeste, dtInicioTeste, dtFimTeste, toolTip, nomeUsuDev, nomeUsuTeste, corTarefa, emExecucao, false);
					}
				}
				if (!StringUtils.isEmpty(filterCelula)) {

					if (!belongsToCell(celulasDevList, celulasFiltro)) {
						timeline.lanes.get(codUsuDev + " - " + nomeUsuDev).addProperty("FOREIGNUSU", "S");
					}
					if (fazParteTester && !belongsToCell(celulasTesterList, celulasFiltro)) {
						timeline.lanes.get(codUsuTeste + " - " + nomeUsuTeste).addProperty("FOREIGNUSU", "S");
					}
				}
			}

		}

		if (firstDate != null && firstDate.before(timeline.dataInicio)) {
			timeline.dataInicio = firstDate;
		}

	}

	private void completaLanes(ScheduleTimeLine timeline, Map<Integer,String> FilterUsu) {
		for (String description : FilterUsu.values()) {
			ScheduleLane lane = timeline.lanes.get(description);
			if (lane == null && description.indexOf("Indefinido") == -1) {
				lane = new ScheduleLane(description);
				String[] partsDescription = description.split(" - ");
				lane.addProperty("CODUSU",partsDescription[0]);
				lane.addProperty("NOMEUSU",partsDescription[1]);
				lane.addProperty("NOTASKS","S");
				timeline.lanes.put(description,lane);
				listUsus.add(BigDecimal.valueOf(Long.parseLong(partsDescription[0])));
			}
		}
	}

	private void geraTarefasCorrecao(JdbcWrapper jdbc, JapeWrapper configDAO, ScheduleTimeLine timeline, BigDecimal codUsu, String filterCelula) throws Exception {
		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasCorrecao.sql");

		boolean usaFiltroCelula = false;

		if (!StringUtils.isEmpty(filterCelula)) {
			query.removeSQLComment("FILTRO_CELULA");
			query.replaceSQLComment("${CELULAS}", filterCelula);
			usaFiltroCelula = true;
		} else {
			query.removeSQLComment("FILTRA_USUARIO");
			query.replaceSQLComment("${USUARIOS}", AuthenticationInfo.getCurrent().getUserID().toString());
		}

		ResultSet rs = query.executeQuery();

		Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

		while (rs.next()) {
			Integer celula = rs.getInt("CELULA");
			BigDecimal numOS = rs.getBigDecimal("NUMOS");
			BigDecimal numSprint = new BigDecimal(-1);
			BigDecimal codUsuDev = rs.getBigDecimal("EXECDEV");
			BigDecimal codUsuTeste = rs.getBigDecimal("EXECTESTE");

			TreeSet<String> celulasDevList = returnListByString(rs.getString("CELULADEV"), ", ");
			TreeSet<String> celulasTesterList = returnListByString(rs.getString("CELULATESTER"), ", ");
			TreeSet<String> celulasFiltro = returnListByString(filterCelula, ", ");

			String nomeUsuDev = rs.getString("NOMEEXECDEV");
			String nomeUsuTeste = rs.getString("NOMEEXECTESTE");
			String titulo = StringUtils.getEmptyAsNull(rs.getString("TITULO"));
			String descricao = (titulo == null ? numOS.toString() : numOS.toString() + " - " + titulo);

			Timestamp dtIniPrev = rs.getTimestamp("DTINIPREV");
			Timestamp dtFimPrev = rs.getTimestamp("DTFIMPREV");

			Integer lastItemOpen = rs.getInt("LASTITEMOPEN");

			boolean pertenceCelula = true;

			if (!StringUtils.isEmpty(filterCelula) && !celulasFiltro.contains(celula.toString())) {
				pertenceCelula = false;
			}

			if (TimeUtils.getDifference(dtFimPrev, dtIniPrev, false) >= 0) {
				String toolTip = getToolTipTask(numOS, rs.getString("NOMEPARC"), titulo, nomeUsuDev, nomeUsuTeste);

				boolean incluirDev = ((usaFiltroCelula && !Collections.disjoint(celulasFiltro,celulasDevList)) || (!usaFiltroCelula && codUsu.equals(codUsuDev))) && codUsuDev != null;
				boolean incluirTester = ((usaFiltroCelula && !Collections.disjoint(celulasFiltro,celulasTesterList)) || (!usaFiltroCelula && codUsu.equals(codUsuTeste)))&& codUsuTeste != null;

				if (lastItemOpen != 0 && dtIniPrev.before(firstDate)) {
					firstDate = dtIniPrev;
				}

				String corTarefa = "";
				if ((lastItemOpen != 0) && pertenceCelula) {
					corTarefa = COR_CORRECAO;
				} else if (lastItemOpen == 0) {
					corTarefa = COR_TAREFA_CONCLUIDA;
				} else {
					corTarefa = COR_TAREFA_OUTRA_CELULA;
				}

				if (incluirDev) {
					timeline.addTaskCorrecao(celula, numOS, numSprint, descricao, codUsuDev, nomeUsuDev, dtIniPrev, dtFimPrev, toolTip, nomeUsuDev, nomeUsuTeste, true, corTarefa);
					if (!listUsus.contains(codUsuDev)) {
						listUsus.add(codUsuDev);
					}
					if (usaFiltroCelula && !belongsToCell(celulasDevList, celulasFiltro) && listUsus.contains(codUsuDev)) {
						timeline.lanes.get(codUsuDev + " - " + nomeUsuDev).addProperty("FOREIGNUSU", "S");
					}
				}

				if (incluirTester) {
					timeline.addTaskCorrecao(celula, numOS, numSprint, descricao, codUsuTeste, nomeUsuTeste, dtIniPrev, dtFimPrev, toolTip, nomeUsuDev, nomeUsuTeste, false, corTarefa);
					if (!listUsus.contains(codUsuTeste)) {
						listUsus.add(codUsuTeste);
					}
					if (usaFiltroCelula && !belongsToCell(celulasTesterList, celulasFiltro) && celulasFiltro.contains(celula.toString()) && listUsus.contains(codUsuTeste)) {
						timeline.lanes.get(codUsuTeste + " - " + nomeUsuTeste).addProperty("FOREIGNUSU", "S");
					}
				}

			}
		}
		if (firstDate != null && firstDate.before(timeline.dataInicio)) {
			timeline.dataInicio = firstDate;
		}

		geraAdicionais(jdbc, timeline, codUsu, filterCelula);

		if (usaFiltroCelula) {
			NativeSql queryCelula = new NativeSql(jdbc);

			queryCelula.loadSql(getClass(), "queMembrosCelulas.sql");
			queryCelula.replaceSQLComment("${CELULAS}", filterCelula);

			ResultSet rsCelula = queryCelula.executeQuery();

			Map<Integer,String> ususCelula = new HashMap<Integer, String>();
			while (rsCelula.next()) {
				ususCelula.put(rsCelula.getBigDecimal(1).intValue(),rsCelula.getBigDecimal(1).toString() + " - " + rsCelula.getString(2));
			}

			completaLanes(timeline, ususCelula);
		}

	}

	private void geraTarefasCorrecaoUsuarios(JdbcWrapper jdbc, ScheduleTimeLine timeline, BigDecimal codUsu, Map<Integer,String> FilterUsu, JapeWrapper configDAO, String filterCelula) throws Exception {
		geraTarefasCorrecao(jdbc, configDAO, timeline, codUsu, filterCelula);

		NativeSql query = new NativeSql(jdbc);
		query.loadSql(getClass(), "queTarefasCorrecao.sql");

		List<String> listUsu = null;
		String Filtro = getSQLFiltroUsuarios(FilterUsu);

		if (!StringUtils.isEmpty(Filtro)) {
			query.removeSQLComment("FILTRA_USUARIO");
			query.replaceSQLComment("${USUARIOS}", Filtro);
			listUsu = Arrays.asList(Filtro.split(","));
		}

		ResultSet rs = query.executeQuery();

		Date firstDate = new Date(TimeUtils.clearTime(System.currentTimeMillis()));

		while (rs.next()) {
			Integer celula = rs.getInt("CELULA");
			BigDecimal numOS = rs.getBigDecimal("NUMOS");
			BigDecimal numSprint = new BigDecimal(-1);
			BigDecimal codUsuDev = rs.getBigDecimal("EXECDEV");
			BigDecimal codUsuTeste = rs.getBigDecimal("EXECTESTE");

			TreeSet<String> celulasDevList = returnListByString(rs.getString("CELULADEV"), ", ");
			TreeSet<String> celulasTesterList = returnListByString(rs.getString("CELULATESTER"), ", ");
			TreeSet<String> celulasFiltro = returnListByString(filterCelula, ", ");

			String nomeUsuDev = rs.getString("NOMEEXECDEV");
			String nomeUsuTeste = rs.getString("NOMEEXECTESTE");

			String laneDescriptionDev = codUsuDev + " - " + nomeUsuDev;
			ScheduleLane laneDev = timeline.lanes.get(laneDescriptionDev);

			Integer lastItemOpen = rs.getInt("LASTITEMOPEN");

			boolean pertenceCelula = true;

			if (!StringUtils.isEmpty(filterCelula) && !celulasFiltro.contains(celula.toString())) {
				pertenceCelula = false;
			}

			if (laneDev == null) {

				String titulo = StringUtils.getEmptyAsNull(rs.getString("TITULO"));
				String descricao = (titulo == null ? numOS.toString() : numOS.toString() + " - " + titulo);

				Timestamp dtIniPrev = rs.getTimestamp("DTINIPREV");
				Timestamp dtFimPrev = rs.getTimestamp("DTFIMPREV");

				if (TimeUtils.getDifference(dtFimPrev, dtIniPrev, false) >= 0) {
					String toolTip = getToolTipTask(numOS, rs.getString("NOMEPARC"), titulo, nomeUsuDev, nomeUsuTeste);

					if (lastItemOpen != 0 && dtIniPrev.before(firstDate)) {
						firstDate = dtIniPrev;
					}

					String corTarefa = "";
					if ((lastItemOpen != 0) && pertenceCelula) {
						corTarefa = COR_CORRECAO;
					} else if (lastItemOpen == 0) {
						corTarefa = COR_TAREFA_CONCLUIDA;
					} else {
						corTarefa = COR_TAREFA_OUTRA_CELULA;
					}

					Boolean fazParte = codUsuDev != null ? listUsu.contains(codUsuDev.toString()) : false;
					if (fazParte && codUsuDev != null) {
						timeline.addTaskCorrecao(celula, numOS, numSprint, descricao, codUsuDev, nomeUsuDev, dtIniPrev, dtFimPrev, toolTip, nomeUsuDev, nomeUsuTeste, true, corTarefa);
						if (lastItemOpen != 0 && dtIniPrev.before(firstDate)) {
							firstDate = dtIniPrev;
						}
					}
					Boolean fazParteTester = codUsuTeste != null ? listUsu.contains(codUsuTeste.toString()) : false;
					String laneDescriptionTester = codUsuDev + " - " + nomeUsuDev;
					ScheduleLane laneTester = timeline.lanes.get(laneDescriptionTester);
					if (fazParteTester && codUsuTeste != null && laneTester == null) {
						timeline.addTaskCorrecao(celula, numOS, numSprint, descricao, codUsuTeste, nomeUsuTeste, dtIniPrev, dtFimPrev, toolTip, nomeUsuDev, nomeUsuTeste, false, corTarefa);
					}

					if (!StringUtils.isEmpty(filterCelula)) {

						if (fazParte && (celulasDevList.size() > 0 && !celulasDevList.first().equals(" ")) && !belongsToCell(celulasDevList, celulasFiltro)) {
							timeline.lanes.get(codUsuDev + " - " + nomeUsuDev).addProperty("FOREIGNUSU", "S");
						}
						if (fazParteTester && (celulasTesterList.size() > 0 && !celulasTesterList.first().equals(" ")) && fazParteTester && !belongsToCell(celulasTesterList, celulasFiltro)) {
							timeline.lanes.get(codUsuTeste + " - " + nomeUsuTeste).addProperty("FOREIGNUSU", "S");
						}
					}
				}
			}
		}

		if (firstDate != null && firstDate.before(timeline.dataInicio)) {
			timeline.dataInicio = firstDate;
		}

		completaLanes(timeline,FilterUsu);
	}

	private static Map<String, String> getOpcoesStatus() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("PL", "Planned");
		map.put("R", "Ready");
		map.put("P", "In-Progress");
		map.put("T", "In-Test");
		map.put("D", "Done");
		map.put("RE", "Refused");
		map.put("C", "Canceled");
		map.put("REM", "Removed");

		return map;
	}

	private String getToolTipTask(BigDecimal numOS, String parceiro, String titulo, String nomeDesenv, String nomeTeste) {
		return getToolTipTask(numOS, parceiro, titulo, nomeDesenv, nomeTeste, null, false, false, true);
	}

	private String getToolTipTask(BigDecimal numOS, String parceiro, String titulo, String nomeDesenv, String nomeTeste, String status, boolean ehPairProg, boolean ehTesteUnit, boolean ehTarefaCorrecao) {
		StringBuffer toolTipBuf = new StringBuffer();

		toolTipBuf.append("<font color=\"#0000FF\" size=\"12\">");
		toolTipBuf.append("<b>OS " + numOS + "</b>");
		toolTipBuf.append("</font><br />");
		toolTipBuf.append("<b>Parceiro:</b> <i>" + parceiro + "</i><br />");

		if (titulo != null) {
			toolTipBuf.append("<b>Ttulo:</b> <i>" + titulo + "</i><br />");
		}

		if (nomeTeste != null && nomeDesenv != null) {
			toolTipBuf.append("<b>Dev/Teste:</b> <i>${NOME_DEV}/${NOME_TESTE}</i><br />");
		} else if (nomeDesenv != null) {
			toolTipBuf.append("<b>Desenvolvedor:</b> <i>${NOME_DEV}</i><br />");
		} else if (nomeTeste != null) {
			toolTipBuf.append("<b>Teste:</b> <i>${NOME_TESTE}</i><br />");
		}

		if (!ehTarefaCorrecao) {
			String pair = ehPairProg ? "Sim" : "No";
			String teste = ehTesteUnit ? "Sim" : "No";
			toolTipBuf.append("<b>Pair/Teste unitrio:</b> <i>" + pair + "/" + teste + "</i><br />");
		}

		toolTipBuf.append("<b>Previso:</b> ${DT_INICIO} a ${DT_FIM}<br />");

		if (!ehTarefaCorrecao) {
			toolTipBuf.append("<b>Status:</b> <i>" + status + "</i>");
		}

		return toolTipBuf.toString();
	}

	private static class ProjetoInfo {
		private String	cor;
		private Date	inicio;
		private Date	fim;
	}

	private static class ScheduleTimeLine {
		private Map<String, ScheduleLane>		lanes;
		private Map<BigDecimal, ProjetoInfo>	projetos;
		private Date							dataInicio;
		private SimpleDateFormat				dateFormat;
		private ColorPalette					colors;
		private Map<Integer, String>			colorsUsedByCelula	= new HashMap<Integer, String>();

		public ScheduleTimeLine() throws Exception {
			colors = new ColorPalette();
			lanes = new LinkedHashMap<String, ScheduleLane>();
			projetos = new HashMap<BigDecimal, ProjetoInfo>();
			dataInicio = new Date(TimeUtils.clearTime(System.currentTimeMillis()));
			dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		}

		public String formatDate(Date d) {
			return dateFormat.format(d);
		}

		public String getCorProjeto(BigDecimal projeto) {
			return projetos.get(projeto).cor;
		}

		public void addTaskCorrecao(int celula, BigDecimal numOS, BigDecimal numSprint, String descricao, BigDecimal codUsu, String nomeUsu, Timestamp dtInicio, Timestamp dtFim, String toolTip, String nomeDev, String nomeTeste, boolean ehDesenvolvedor, String corTarefa) throws Exception {
			addTask(celula, numOS, numSprint, "Sem Ciclo", descricao, codUsu, nomeUsu, dtInicio, dtFim, toolTip, nomeDev, nomeTeste, corTarefa, "N", ehDesenvolvedor);
		}

		public void addTaskFerias(BigDecimal codFerias, String descricao, BigDecimal codUsuFerias, String nomeUsu, Timestamp dtInicioFerias, Timestamp dtFimFerias, String infoFuncionarioEEmpresa) throws Exception {
			addTask(0, TIPO_DE_TASK.get("FERIAS"), codFerias, "Sem Ciclo", descricao, codUsuFerias, nomeUsu, dtInicioFerias, dtFimFerias, "Frias", nomeUsu, "", "DC143C", infoFuncionarioEEmpresa, false);
		}

		public void addTaskEstFerias(BigDecimal codEstFer, String descricao, BigDecimal codUsuFerias, String nomeUsu, Timestamp dtInicioEst, Timestamp dtFimEst, String infoFuncionarioEEmpresa) throws Exception {
			addTask(0, TIPO_DE_TASK.get("ESTFERIAS"), codEstFer, "Sem Ciclo", descricao, codUsuFerias, nomeUsu, dtInicioEst, dtFimEst, "Estimativa de Frias", nomeUsu, "", "DC143C", infoFuncionarioEEmpresa, false);
		}

		public void addTaskEventosAdicionais(BigDecimal codEvento, String descricao, BigDecimal codUsuEv, String nomeUsu, Timestamp dtInicio, Timestamp dtFim, boolean concluido) throws Exception {
			String corTarefa = COR_EVENTO_ADICIONAL;
			BigDecimal tipoDeTask = TIPO_DE_TASK.get("EVENTOSADICIONAIS");
			if (concluido) {
				corTarefa = COR_TAREFA_CONCLUIDA;
				tipoDeTask = TIPO_DE_TASK.get("EVENTOSADICIONAISC");
			}
			addTask(0, tipoDeTask, codEvento, "Sem Ciclo", descricao, codUsuEv, nomeUsu, dtInicio, dtFim, "Evento Adicional - #" + codEvento.toString(), nomeUsu, "", corTarefa, "N", false);
		}

		public void addTask(int celula, BigDecimal numOS, BigDecimal numSprint, String ciclo, String descricao, BigDecimal codUsu, String nomeUsu, Timestamp dtInicio, Timestamp dtFim, String toolTip, String nomeDev, String nomeTeste, String corTarefa, String emExecucao, boolean ehDesenvolvedor) throws Exception {
			addTask(celula, numOS, numSprint, ciclo, descricao, codUsu, nomeUsu, dtInicio, dtFim, toolTip, nomeDev, nomeTeste, corTarefa, emExecucao, ehDesenvolvedor, false, new BigDecimal(0));
		}

		public void addTask(int celula, BigDecimal numOS, BigDecimal numSprint, String ciclo, String descricao, BigDecimal codUsu, String nomeUsu, Timestamp dtInicio, Timestamp dtFim, String toolTip, String nomeDev, String nomeTeste, String corTarefa, String emExecucao, boolean ehDesenvolvedor, boolean ehTarefaQuebrada, BigDecimal numTarefa) throws Exception {
			ScheduleTask t = new ScheduleTask(this, numOS, descricao);
			t.inicio = dtInicio;
			t.fim = dtFim;

			t.addProperty("NUMOS", numOS.toString());
			t.addProperty("CODUSU", codUsu.toString());
			t.addProperty("NUMSPRINT", numSprint.toString());
			t.addProperty("DTINI", TimeUtils.formataDDMMYYYY(dtInicio));
			t.addProperty("DTFIM", TimeUtils.formataDDMMYYYY(dtFim));
			t.addProperty("ISDEV", String.valueOf(ehDesenvolvedor));
			t.addProperty("NOMEDEV", nomeDev);
			t.addProperty("NOMETESTE", nomeTeste);
			t.addProperty("EMEXECUCAO", emExecucao);
			t.addProperty("CICLO", ciclo);
			t.addProperty("TAREFAQUEBRADA", ehTarefaQuebrada ? "S" : "N");
			t.addProperty("NUMTAREFA", numTarefa.toString());
			if (ehTarefaQuebrada && COR_TAREFA_CONCLUIDA.equals(corTarefa)) {
				t.addProperty("COR", corTarefa);
			}

			if (toolTip != null) {
				t.textoToolTip = toolTip;
			}
			ProjetoInfo projeto = projetos.get(numOS);

			if (!projetos.containsKey(numOS)) {
				projeto = new ProjetoInfo();

				if (StringUtils.getEmptyAsNull(corTarefa) == null || (ehTarefaQuebrada && COR_TAREFA_CONCLUIDA.equals(corTarefa))) {
					do {
						corTarefa = colors.getNextColor();
					} while (isUsedColor(celula, corTarefa) || CORES_NAO_USAR.contains(corTarefa));

					projeto.cor = corTarefa;

					JapeWrapper backlogDAO = JapeFactory.dao(ENTIDADE_SPRINT_BACKLOG);
					backlogDAO.prepareToUpdateByPK(new Object [] {numOS,numSprint,ciclo}).set("COR", corTarefa).update();
				} else {
					projeto.cor = corTarefa.trim();
				}

				colorsUsedByCelula.put(celula, corTarefa);
				projetos.put(numOS, projeto);
			}

			String descricaoLane = codUsu + " - " + nomeUsu;

			ScheduleLane lane = lanes.get(descricaoLane);

			if (lane == null) {
				lane = new ScheduleLane(descricaoLane);

				if (codUsu != null) {
					lane.addProperty("CELULA", String.valueOf(celula));
					lane.addProperty("CODUSU", codUsu.toString());
					lane.addProperty("NOMEUSU", ehDesenvolvedor ? nomeDev : nomeTeste);
					lane.addProperty("ISDEV", String.valueOf(ehDesenvolvedor));
					lane.addProperty("FOREIGNUSU", "N");
					lane.addProperty("NOTASKS", "N");
				}

				lanes.put(descricaoLane, lane);
			}

			if (t.inicio != null) {
				if (projeto.inicio == null || (projeto.inicio.getTime() > t.inicio.getTime())) {
					projeto.inicio = t.inicio;
				}

			}

			lane.addTask(t);
		}

		public Element buildXML() throws Exception {
			Element timelineElem = new Element("timeline");

			timelineElem.setAttribute("firstDate", formatDate(dataInicio));
			timelineElem.setAttribute("viewPortSize", "10");

			for (ScheduleLane l : lanes.values()) {
				timelineElem.addContent(l.buildXML());
			}

			return timelineElem;
		}

		private boolean isUsedColor(int celula, String color) {
			if (color.equals(colorsUsedByCelula.get(celula))) {
				return true;
			}

			return false;
		}
	}

	private static class ScheduleLane {
		private Collection<ScheduleTask>	tasks;
		private String						description;
		private Map<String, String>			properties;

		public ScheduleLane(String description) {
			this.description = description;
			tasks = new ArrayList<ScheduleTask>();
		}

		public void addTask(ScheduleTask t) {
			tasks.add(t);
		}

		public void addProperty(String key, String value) {
			if (properties == null) {
				properties = new HashMap<String, String>();
			}
			properties.put(key, value);
		}

		public Element buildXML() throws Exception {

			Element laneElem = new Element("lane");
			laneElem.setAttribute("description", description);

			for (ScheduleTask t : tasks) {
				laneElem.addContent(t.buildXML());
			}

			if (properties != null) {
				Element propsElement = new Element("properties");
				for (String key : properties.keySet()) {
					Element propElem = new Element(key);
					propElem.addContent(new CDATA(properties.get(key)));
					propsElement.addContent(propElem);
				}
				laneElem.addContent(propsElement);
			}

			return laneElem;
		}
	}

	private static class ScheduleTask {
		private ScheduleTimeLine	timeline;
		private BigDecimal			projeto;
		private Date				inicio;
		private Date				fim;
		private String				descricao;
		private String				textoToolTip;
		private Map<String, String>	properties;

		public ScheduleTask(ScheduleTimeLine timeline, BigDecimal projeto, String descricao) {
			this.timeline = timeline;
			this.projeto = projeto;
			this.descricao = descricao;
		}

		public void addProperty(String key, String value) {
			if (properties == null) {
				properties = new HashMap<String, String>();
			}

			properties.put(key, value);
		}

		public Element buildXML() throws Exception {
			Element taskElement = new Element("task");

			Date dtIni = inicio;
			Date dtFim = fim;

			taskElement.setAttribute("start", timeline.formatDate(dtIni));
			taskElement.setAttribute("end", timeline.formatDate(dtFim));
			taskElement.setAttribute("allDay", "true");

			XMLUtils.addContentElement(taskElement, "description", descricao);
			if (properties != null && properties.get("COR") != null) {
				XMLUtils.addContentElement(taskElement, "color", "0x" + properties.get("COR"));
			} else {
				XMLUtils.addContentElement(taskElement, "color", "0x" + timeline.getCorProjeto(projeto));
			}

			taskElement.addContent(new Element("tooltiptext").addContent(new CDATA(textoToolTip)));

			if (properties != null) {
				Element propsElement = new Element("properties");

				for (String key : properties.keySet()) {
					Element propElem = new Element(key);
					propElem.addContent(new CDATA(properties.get(key)));
					propsElement.addContent(propElem);
				}

				taskElement.addContent(propsElement);
			}

			return taskElement;
		}
	}

	private String getConfigFilterList(DynamicVO vo, boolean isString) throws Exception {
		SAXBuilder sax = new SAXBuilder();
		String strFilter = "";

		if (vo != null) {
			char[] charConfig = (char[]) vo.getProperty("CONFIG");

			if (charConfig != null) {
				InputSource input = new InputSource(new CharArrayReader(charConfig));
				Element config = sax.build(input).getRootElement();
				List<Element> filters = config.getChildren("item");

				String selected;

				for (Element filter : filters) {
					selected = XMLUtils.getRequiredAttributeAsString(filter, "selected");

					if ("true".equals(selected)) {
						if (!StringUtils.isEmpty(strFilter)) {
							strFilter += ", ";
						}

						if (isString) {
							strFilter += "\'" + XMLUtils.getRequiredAttributeAsString(filter, "data") + "\'";
						} else {
							strFilter += XMLUtils.getRequiredAttributeAsString(filter, "data");
						}
					}
				}
			}
		}

		return strFilter;
	}

	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		sessionContext = ctx;
	}

	private void throwsMGEExceptionRollingBack(Throwable e) throws MGEModelException {
		sessionContext.setRollbackOnly();
		MGEModelException.throwMe(e);
	}
}
