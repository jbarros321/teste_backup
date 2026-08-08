package br.com.sankhya.industria.manutencao.util;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.engine.delegate.DelegateTask;

import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.metadata.EntityField;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgeserv.integracao.OrdemServicoAPI;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.metadata.FieldMetadata;
import br.com.sankhya.modelcore.metadata.FieldOption;
import br.com.sankhya.modelcore.util.DatasetUtils.InformacoesAnexo;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.workflow.model.engine.DefaultProcessEngineService;
import br.com.sankhya.workflow.model.engine.camunda.ProcessEngineCamunda;

import com.google.gson.Gson;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

public class ManutencaoSnkUtil {

	private static final Pattern PADRAO_CAMPO = Pattern.compile("CAMPO\\[([^]]+)\\]", Pattern.DOTALL|Pattern.MULTILINE);

	public static void finalizaItemUsuario(BigDecimal numOS, BigDecimal codUsu, boolean finalizaOS) throws Exception {

		long qtdItensAbertos = NativeSql.getBigDecimal("COUNT(1)", "TCSITE", "HRFINAL IS NULL AND NUMOS =?", new Object[] { numOS }).longValue();

		if (qtdItensAbertos == 1) {
			if (finalizaOS) {
				OrdemServicoAPI.fecharOS(numOS, codUsu);
			}
		} else {
			BigDecimal item = OrdemServicoAPI.getItemAbertoUsuario(numOS, codUsu, false);
			SimpleDateFormat sdf = new SimpleDateFormat("HHmm");

			Map<String, Object> dataIte = new HashMap<String, Object>();
			Timestamp now = new Timestamp(System.currentTimeMillis());
			dataIte.put("INICEXEC", TimeUtils.clearTime(now));
			dataIte.put("HRINICIAL", new BigDecimal(sdf.format(now)));
			dataIte.put("HRFINAL", new BigDecimal(sdf.format(now)));
			dataIte.put("INTERVALO", BigDecimal.ZERO);

			OrdemServicoAPI.alterarItem(numOS, item, codUsu, dataIte);
		}

	}

	public static String getIdTarefa(BigDecimal idInstPrn, BigDecimal idInstTar) throws Exception{
		return NativeSql.getString("IDELEMENTO", "TWFITAR", "IDINSTPRN = ? AND IDINSTTAR = ?", new Object [] {idInstPrn, idInstTar});
	}

	public static void salvaLogAnexoOS(Map<String,Object> linhaSoliMan, BigDecimal codUsu) throws Exception {
		EntityFacade facade = EntityFacadeFactory.getDWFFacade();
		DynamicVO vo = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.ANEXO);
		vo.setProperty("CODATA", linhaSoliMan.get("NUMOS"));
		vo.setProperty("TIPOCONTEUDO", "N");
		vo.setProperty("LINK", "/mge/download.mge?fileName=sab:
		vo.setProperty("CODUSU", codUsu);
		vo.setProperty("ARQUIVO", "server.log");
		vo.setProperty("DESCRICAO", "Log");
		vo.setProperty("TIPO", "W");
		facade.createEntity(DynamicEntityNames.ANEXO, (EntityVO) vo);
	}

	public static void atualizaCamposOs(BigDecimal numOS, Map<String, Object> campos) throws Exception {

		if (numOS != null && !campos.isEmpty()) {
			PersistentLocalEntity ple = EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKey(DynamicEntityNames.ORDEM_SERVICO, new Object[] { numOS });
			DynamicVO osVo = (DynamicVO) ple.getValueObject();

			for(String campo:campos.keySet()){
				osVo.setProperty(campo, campos.get(campo));
			}

			ple.setValueObject((EntityVO) osVo);
		}
	}

	public static BigDecimal getUltimoItemAbertoUsuario(BigDecimal numOS, BigDecimal codUsu) throws Exception {
		return NativeSql.getBigDecimal("NUMITEM", "TCSITE", "NUMOS = ? AND CODUSU = ? AND HRFINAL IS NULL AND ROWNUM = 1 ORDER BY NUMITEM DESC", new Object[] { numOS, codUsu });
	}

	public static BigDecimal getUltimoItemUsuario(BigDecimal numOS, BigDecimal codUsu) throws Exception{
		return NativeSql.getBigDecimal("NUMITEM", "TCSITE", "NUMOS = ? AND CODUSU = ? AND ROWNUM = 1 ORDER BY NUMITEM DESC", new Object [] {numOS, codUsu});
	}

	private static String normalizeField(String entity, String field, Registro r) throws Exception{
		Object value = r.getCampo(field);
		String strValue = null;

		if(value != null){
			FieldMetadata fm = DataDictionaryUtils.getFieldMetadata(entity, field);

			if(fm != null){
				if (!fm.getOptions().isEmpty()) {
					FieldOption opt = fm.getOption(value.toString());
					if(opt != null){
						strValue = opt.getDescription();
					}
				} else {
					switch (fm.getEntityType()) {
						case EntityField.TIMESTAMP:
							strValue = StringUtils.formatTimestamp((Timestamp) value, "H".equals(fm.getUserType()) ? "dd/MM/yyyy HH:mm" : "dd/MM/yyyy");
							break;
						case EntityField.NUMBER:
							strValue = StringUtils.formatNumeric("I".equals(fm.getUserType()) ? "###0;-###0" : "#,##0.00;-#,##0.00", BigDecimalUtil.getBigDecimal(value));
							break;
					}
				}
			}
		}

		if(strValue == null){
			strValue = StringUtils.getNullAsEmpty(value);
		}

		return strValue;
	}

	public static void updateItemOS(BigDecimal numOS, BigDecimal codUsuLogado, String entity, Registro registro, String[] campos) throws Exception {

		BigDecimal numItem = getUltimoItemUsuario(numOS, codUsuLogado);
		NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());

		StringBuffer comando = new StringBuffer();
		comando.append("UPDATE TCSITE SET ");
		for(String campo:campos){
			Object valor = null;
			String campoOrigem = campo;
			String campoDestino = null;
			if(campo.startsWith("__PADRAO__:")){
				campoOrigem = campo.substring(11);
				int posicao = campoOrigem.indexOf(':');
				String padrao = campoOrigem.substring(posicao+1);
				campoOrigem = campoOrigem.substring(0, posicao);
				StringBuffer processado = new StringBuffer();
				Matcher m = PADRAO_CAMPO.matcher(padrao);
				while(m.find()){

					m.appendReplacement(processado, Matcher.quoteReplacement(normalizeField(entity, m.group(1), registro)));
				}
				m.appendTail(processado);
				valor = processado.toString();
			} else {
				String [] parts = campo.split("->");
				if(parts.length == 2){
					campoOrigem = parts[0];
					campoDestino = parts[1];
				}
				valor = registro.getCampo(campoOrigem);
			}
			if(campoDestino == null){
				campoDestino = campoOrigem;
			}
			comando.append(campoDestino).append(" = :").append(campoOrigem);
			query.setNamedParameter(campoOrigem, valor);
		}
		comando.append(" WHERE NUMOS = :NUMOS AND NUMITEM = :NUMITEM ");

		query.setNamedParameter("NUMOS", numOS);
		query.setNamedParameter("NUMITEM", numItem);
		query.executeUpdate(comando.toString());
	}

	public static void updateSoliman(Registro auxiliar, Registro soliman, String [] campos) throws Exception{
		Map<String, Map<String, String>> anexos = new HashMap<String, Map<String,String>>();
		for(String campo:campos){
			String [] parts = campo.split("->");
			String campoOrigem = parts[0];
			String campoDestino = parts.length == 2 ? parts[1] : null;
			if(campoOrigem.startsWith("A:")){

				parts = campoOrigem.split(":");

				if(parts.length != 3){
					throw new IllegalArgumentException("Formato invlido para campo anexo. Use A:NOME_TABELA:NOME_CAMPO. " + campoOrigem);
				}

				String tableName = parts[1];
				campoOrigem = parts[2];
				if(campoDestino == null){
					campoDestino = campoOrigem;
				}
				Map<String, String> c = anexos.get(tableName);

				if(c == null){
					c = new HashMap<String, String>();
					anexos.put(tableName, c);
				}
				c.put(campoOrigem, campoDestino);
			} else {
				if(campoDestino == null){
					campoDestino = campoOrigem;
				}
				soliman.setCampo(campoDestino, auxiliar.getCampo(campoOrigem));
			}
		}

		soliman.save();

		if(!anexos.isEmpty()){
			NativeSql query = new NativeSql(EntityFacadeFactory.getDWFFacade().getJdbcWrapper());
			for(String tableName:anexos.keySet()){
				Map<String, String> camposAnexo = anexos.get(tableName);
				StringBuffer comando = new StringBuffer();
				comando.append(" MERGE INTO AD_TWFSOLIMAN S USING ");
				comando.append("      ( ");
				comando.append("		SELECT IDINSTPRN");
				for(String campoAnexo:camposAnexo.keySet()){
					comando.append(",").append(campoAnexo);
				}
				comando.append("        FROM ").append(tableName);
				comando.append("        WHERE IDINSTPRN = :IDINSTPRN AND IDINSTTAR = :IDINSTTAR AND CODREGISTRO = :CODREGISTRO ");
				comando.append("       ) T ON (S.IDINSTPRN = T.IDINSTPRN) ");
				comando.append(" WHEN MATCHED THEN ");
				comando.append(" UPDATE SET ");
				boolean first = true;
				for(String c:camposAnexo.keySet()){
					if(!first){
						comando.append(",");
					}
					comando.append("S.").append(camposAnexo.get(c)).append(" = T.").append(c);
					first = false;
				}
				query.setNamedParameter("IDINSTPRN", auxiliar.getCampo("IDINSTPRN"));
				query.setNamedParameter("IDINSTTAR", auxiliar.getCampo("IDINSTTAR"));
				query.setNamedParameter("CODREGISTRO", auxiliar.getCampo("CODREGISTRO"));
				query.executeUpdate(comando.toString());
			}
		}
	}

	public static void copiaAnexosOSProcesso(BigDecimal idInstPrn, BigDecimal OS) throws Exception {
		EntityFacade facade = EntityFacadeFactory.getDWFFacade();

		Collection<DynamicVO> anexos = facade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ANEXO, "this.CODATA = ? AND TIPO = 'W'", new Object [] {OS}));
		String pkProcesso = idInstPrn + "_InstanciaProcesso";

		for (DynamicVO anexoVO:anexos) {
			String arquivo = anexoVO.asString("ARQUIVO");

			boolean ehAnexoLink = true;
			if(arquivo.indexOf("server.log") > -1){
				byte [] data = anexoVO.asBlob("CONTEUDO");
				if(data != null) {

					ehAnexoLink = false;

					InformacoesAnexo inf = new InformacoesAnexo();
					inf.name = arquivo;
					inf.size = data.length;
					inf.type = "application/zip";
					inf.lastModifiedDate = anexoVO.asTimestamp("DTALTER");

					JdbcWrapper jdbc = facade.getJdbcWrapper();
					try{
						NativeSql query = new NativeSql(jdbc);
						StringBuffer anexo = new StringBuffer("__start_fileinformation__" + new Gson().toJson(inf) + "__end_fileinformation__");
						anexo.append(new String(data));
						query.setNamedParameter("LOG", anexo.toString().getBytes());
						query.setNamedParameter("OS", OS);
						query.executeUpdate("UPDATE AD_TWFSOLIMAN SET LOG = :LOG WHERE NUMOS = :OS AND LOG IS NULL");
					}finally{
						JdbcWrapper.closeSession(jdbc);
					}
				}
			}

			if(ehAnexoLink){
				StringBuffer buf = new StringBuffer();
				String link = anexoVO.asString("LINK");

				if (link == null) {
					buf.append("/mge/download.mge?fileName=sab:
					buf.append(arquivo);
					buf.append("&pkValues={");
					buf.append("CODATA:").append(anexoVO.asBigDecimal("CODATA"));
					buf.append(",TIPO:'").append(anexoVO.asString("TIPO")).append("'");
					buf.append(",DESCRICAO:'").append(anexoVO.asString("DESCRICAO")).append("'");
					buf.append(",SEQUENCIA:").append(anexoVO.asBigDecimal("SEQUENCIA"));
					buf.append(",SEQUENCIAPR:").append(anexoVO.asBigDecimal("SEQUENCIAPR"));
					buf.append("}");
				} else {
					buf.append(link);
				}

				DynamicVO anexoSistemaVO = (DynamicVO) facade.getDefaultValueObjectInstance(DynamicEntityNames.ANEXO_SISTEMA);
				anexoSistemaVO.setProperty("PKREGISTRO", pkProcesso);
				anexoSistemaVO.setProperty("DESCRICAO", anexoVO.asString("DESCRICAO"));
				anexoSistemaVO.setProperty("DHALTER", new Timestamp(TimeUtils.getToday()));
				anexoSistemaVO.setProperty("NOMEARQUIVO", link == null ? anexoVO.asString("ARQUIVO") : " ");
				anexoSistemaVO.setProperty("NOMEINSTANCIA", "InstanciaProcesso");
				anexoSistemaVO.setProperty("TIPOACESSO", "ALL");
				anexoSistemaVO.setProperty("TIPOAPRES", "LOC");
				anexoSistemaVO.setProperty("RESOURCEID", "br.com.sankhya.workflow.listatarefa");
				anexoSistemaVO.setProperty("LINK", buf.toString());

				facade.createEntity(DynamicEntityNames.ANEXO_SISTEMA, (EntityVO) anexoSistemaVO);
			}
		}
	}

	public static BigDecimal getUsuarioUltimoItemOS(BigDecimal numOS) throws Exception {
		return getUsuarioUltimoItemOS(numOS, "CODUSU");
	}

	public static BigDecimal getUsuarioUltimoItemOS(BigDecimal numOS, String infoRetorno) throws Exception {
		return NativeSql.getBigDecimal(infoRetorno, "TCSITE", "NUMOS = ? AND TERMEXEC IS NULL AND ROWNUM = 1 ORDER BY NUMITEM DESC", new Object [] { numOS });
	}

	public static BigDecimal getExecutanteAnteriorFila(BigDecimal numOS, BigDecimal fila) throws Exception {
		JdbcWrapper jdbc = null;

		try {
			jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();

			NativeSql query = new NativeSql(jdbc);
			query.appendSql(" SELECT ");
			query.appendSql(" 	I.CODUSU ");
			query.appendSql(" FROM ");
			query.appendSql(" 	TCSITE I ");
			query.appendSql(" INNER JOIN TCSRUS R ON ");
			query.appendSql(" 	R.CODUSUREL = I.CODUSU ");
			query.appendSql(" WHERE ");
			query.appendSql(" 	I.NUMOS = :NUMOS ");
			query.appendSql(" 	AND I.CODUSU NOT IN ( ");
			query.appendSql(" 	SELECT ");
			query.appendSql(" 		RUS.CODUSU ");
			query.appendSql(" 	FROM ");
			query.appendSql(" 		TCSRUS RUS ");
			query.appendSql(" 	WHERE ");
			query.appendSql(" 		RUS.CODUSUREL = I.CODUSU ");
			query.appendSql(" 		AND RUS.TIPO = 'F') ");
			query.appendSql(" 	AND R.CODUSU = :FILA ");
			query.appendSql(" 	AND R.TIPO = 'F' ");
			query.appendSql(" 	AND ROWNUM = 1 ");
			query.appendSql(" ORDER BY ");
			query.appendSql(" 	I.NUMITEM DESC ");

			query.setNamedParameter("NUMOS", numOS);
			query.setNamedParameter("FILA", fila);

			ResultSet rs = query.executeQuery();

			if (rs.next()) {
				return rs.getBigDecimal("CODUSU");
			}

			return null;
		} finally {
			JdbcWrapper.closeSession(jdbc);
		}
	}

	public static void inserirDonoAtividade(BigDecimal idInstTarefa, String donoAtividade) throws Exception {

		DelegateTask task = (DelegateTask) JapeSession.getCurrentSession().getProperty("EXECUTING_TASK");

		if (task != null) {
			ProcessEngineCamunda engine = (ProcessEngineCamunda) DefaultProcessEngineService.getInstance();

			if (!engine.userIsCreated(donoAtividade)) {
				engine.createUser(donoAtividade, null);
			}

			task.setAssignee(donoAtividade);
		} else {
			DefaultProcessEngineService.getInstance().setAssigneeTask(idInstTarefa.toString(), donoAtividade);
		}
	}
}
