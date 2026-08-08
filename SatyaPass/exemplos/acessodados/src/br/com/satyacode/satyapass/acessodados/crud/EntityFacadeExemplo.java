package br.com.satyacode.satyapass.acessodados.crud;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Collection;

public class EntityFacadeExemplo {

    public void buscarRegistroPelaPK() throws Exception {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO produtoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("Produto", new Object[]{BigDecimal.ZERO});
        System.out.println("Produto: " + produtoVO.asBigDecimal("CODPROD") + " - Nome do Produto: " + produtoVO.asString("DESCRPROD"));

    }

    public void buscarRegistroPelaPKComposta() throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO funcionarioVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("Funcionario", new Object[]{BigDecimal.ONE, new BigDecimal(56)});

    }

    public void buscarRegistroPorUmaCondicao() throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        FinderWrapper finder = new FinderWrapper(DynamicEntityNames.FUNCIONARIO, "this.CODEMP = ? ", new Object[]{BigDecimal.ONE});
        finder.setMaxResults(-1);
        Collection<DynamicVO> funcionariosVO = dwfFacade.findByDynamicFinderAsVO(finder);

        if (!funcionariosVO.isEmpty()) {
            for (DynamicVO funcVO : funcionariosVO) {
                System.out.println("Cód, Empresa: : " + funcVO.asBigDecimal("CODEMP") + " - Funcionario: " + funcVO.asBigDecimal("CODFUNC") +
                        " - Nome Funcionario: " + funcVO.asString("NOMEFUNC"));
            }
        } else {
            System.out.println("O Array estava vazio!");
        }
    }

    public BigDecimal insercaoDeDados(String nome) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO acessoVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_STAACESSO");
        acessoVO.setProperty("NOME", nome);
        PersistentLocalEntity entity = dwfFacade.createEntity("AD_STAACESSO", (EntityVO) acessoVO);
        DynamicVO acessoNovoVO = (DynamicVO) entity.getValueObject();
        return acessoNovoVO.asBigDecimalOrZero("SEQUENCIA");
    }

    public void atualizarDados(BigDecimal sequencia) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        PersistentLocalEntity acessoEntity = dwfFacade.findEntityByPrimaryKey("AD_STAACESSO", new Object[]{sequencia});
        DynamicVO acessoVO = (DynamicVO) acessoEntity.getValueObject();
        acessoVO.setProperty("DATA", TimeUtils.getNow());
        acessoVO.setProperty("VALOR", sequencia);
        acessoEntity.setValueObject((EntityVO) acessoVO);
    }

    public void removerPelaPk(BigDecimal sequencia) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        dwfFacade.removeEntity("AD_STAACESSO", new Object[]{sequencia});
    }

    public void removerPorUmaCondicao(BigDecimal sequencia) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        FinderWrapper finder = new FinderWrapper("AD_STAACESSO", "this.SEQUENCIA = ? ", new Object[]{sequencia});
        dwfFacade.removeByCriteria(finder);
    }

}
