package br.com.satyapass.abastecimento.abs_avulso.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.PrePersistEntityState;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ListenerParameters;
import br.com.sankhya.modelcore.util.MGECoreParameter;
import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyapass.abastecimento.abs_avulso.repository.AbastecimentoRepository;
import br.com.satyapass.abastecimento.log.AbastecimentoLogFactory;
import com.sankhya.util.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.math.BigDecimal;

public class AbastecimentoService {

    private EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

    private AbastecimentoRepository abastecimentoRepository;

    public AbastecimentoService(){
        this.abastecimentoRepository = new AbastecimentoRepository();
    }

    public void gerenciarAbastecimento(BigDecimal codAbastecimento) throws Exception {
        AbastecimentoLogFactory.incluirItem("Inicio - Método: gerenciarAbastecimento: " + codAbastecimento, "", StatusItemEnum.INFO, true);

        DynamicVO abastecimentoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_ABTAVU", new Object[]{codAbastecimento});
        if (StringUtils.isNotEmpty(abastecimentoVO.asBigDecimal("NUNOTA"))){
            throw  new Exception("A requisição já foi gerada. Nro Unico: "+ abastecimentoVO.asBigDecimal("NUNOTA"));
        }else {
            BigDecimal nroUnicoDaNota = incluirCabecalho(abastecimentoVO);
            abastecimentoRepository.atualizarNunotaDaRequisicao(codAbastecimento, nroUnicoDaNota);
        }

    }

    private BigDecimal retornarCodigoDaTop() throws Exception {
        try{
            return new BigDecimal(MGECoreParameter.getParameterAsInt("TOP_ABASTECIMEN"));
        }catch (Exception e){
            throw new Exception("Erro ao encontrar o parametro 'TOP_ABASTECIMEN'. Acionar o departamento de TI");
        }
    }

    private BigDecimal incluirCabecalho(DynamicVO abastecimentoVO) throws Exception {
        BigDecimal nunotaGerado = BigDecimal.ZERO;
        try{
            DynamicVO cabVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
            BigDecimal codTipOperacao = retornarCodigoDaTop();
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(codTipOperacao);
            cabVO.setProperty("CODTIPOPER", topVO.asBigDecimal("CODTIPOPER"));
            cabVO.setProperty("DHTIPOPER", topVO.asTimestamp("DHALTER"));
            cabVO.setProperty("TIPMOV", topVO.asString("TIPMOV"));
            cabVO.setProperty("CODPARC", abastecimentoVO.asBigDecimalOrZero("CODPARC"));
            cabVO.setProperty("CODEMP", abastecimentoVO.asBigDecimalOrZero("CODEMP"));
            cabVO.setProperty("DTNEG", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("DTFATUR", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("DTENTSAI", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("DTMOV", abastecimentoVO.asTimestamp("DATA"));
            cabVO.setProperty("CODNAT", BigDecimal.ZERO);
            cabVO.setProperty("CODCENCUS", BigDecimal.ZERO);
            cabVO.setProperty("CODPROJ", abastecimentoVO.asBigDecimalOrZero("CODPROJ"));
            cabVO.setProperty("CIF_FOB", "S");
            cabVO.setProperty("CODTIPVENDA", BigDecimal.ZERO);
            cabVO.setProperty("CODVEND", BigDecimal.ZERO);
            cabVO.setProperty("RATEADO", "N");
            cabVO.setProperty("CODVEICULO", abastecimentoVO.asBigDecimalOrZero("CODVEICULO"));
            cabVO.setProperty("CODUSU", AuthenticationInfo.getCurrent().getUserID());
            cabVO.setProperty("OBSERVACAO", "Gerado automaticamente pelo abastecimento avulso: " +abastecimentoVO.asBigDecimal("CODABAST") );

            CACHelper cacHelper = new CACHelper();
            JapeSessionContext.putProperty(ListenerParameters.CENTRAIS, Boolean.TRUE);
            PrePersistEntityState cabPreState = PrePersistEntityState.build(dwfFacade, DynamicEntityNames.CABECALHO_NOTA, cabVO);
            BarramentoRegra bRegrasCab = cacHelper.incluirAlterarCabecalho(AuthenticationInfo.getCurrent(), cabPreState);
            DynamicVO newCabVO = bRegrasCab.getState().getNewVO();
            nunotaGerado = newCabVO.asBigDecimal("NUNOTA");
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("Erro ao gerar o cabeçalho: " + ExceptionUtils.getMessage(e));
        }
        return nunotaGerado;
    }

}
