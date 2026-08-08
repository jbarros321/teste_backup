package br.com.sankhya.portfolio.venda.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.UsuarioVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.ws.ServiceContext;

public class ExemploContextualizacaoHelper {

	private SessionHandle		hnd;
	private EntityFacade		dwfFacade;
	private AuthenticationInfo	auth;

	public ExemploContextualizacaoHelper(SessionHandle hnd) throws Exception {
		this.hnd = hnd;

		dwfFacade = EntityFacadeFactory.getDWFFacade();

		auth = new AuthenticationInfo("SUP", BigDecimal.ZERO, BigDecimal.ZERO, 0);
		auth.makeCurrent();

		ServiceContext ctx = new ServiceContext(null);
		ctx.setAutentication(auth);
		ctx.makeCurrent();

		setupContext();
	}

	private void setupContext() throws Exception {
		UsuarioVO usuVO = (UsuarioVO) ((DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO, new Object[] { auth.getUserID() })).wrapInterface(UsuarioVO.class);

		JapeSessionContext.putProperty("usuario_logado", auth.getUserID());
		JapeSessionContext.putProperty("emp_usu_logado", usuVO.getCODEMP());
		JapeSessionContext.putProperty("dh_atual", new Timestamp(System.currentTimeMillis()));
		JapeSessionContext.putProperty("d_atual", new Timestamp(TimeUtils.getToday()));
		JapeSessionContext.putProperty("usuarioVO", usuVO);
		JapeSessionContext.putProperty("authInfo", auth);
		JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
		JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
		JapeSession.putProperty(AtributosRegras.INC_UPD_ITEM_CENTRAL, Boolean.TRUE);
	}

	public void incluirPedido(DynamicVO pedidoVendaVO, DynamicVO topReservaVO) throws Exception {

		DynamicVO newCabVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.CABECALHO_NOTA);
		PrePersistEntityState cabState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, newCabVO);

		CACHelper cacHelper = new CACHelper();
		cacHelper.incluirAlterarCabecalho(auth, cabState);
	}
}
