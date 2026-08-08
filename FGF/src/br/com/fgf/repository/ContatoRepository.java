package br.com.fgf.repository;

import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ContatoRepository {

    public BigDecimal obterProximoCodContato() throws Exception {
        EntityFacadeFactory.getDWFFacade();
        JapeWrapper dao = JapeFactory.dao("TGFNUM");
        DynamicVO tgfnum = dao.findOne("ARQUIVO = ?", br.com.fgf.util.Constants.ARQUIVO_TGFCTT);
        
        if (tgfnum == null) {
            throw new IllegalStateException("Não foi possível obter registro de sequencial para TGFCTT na tabela TGFNUM");
        }
        
        BigDecimal ultcod = tgfnum.asBigDecimalOrZero("ULTCOD");
        BigDecimal proximoCod = ultcod.add(BigDecimal.ONE);
        
        dao.prepareToUpdate(tgfnum)
            .set("ULTCOD", proximoCod)
            .update();
        
        return proximoCod;
    }

    public Set<String> buscarEmailsExistentes(BigDecimal codparc) throws Exception {
        Set<String> emails = new LinkedHashSet<>(10);
        FinderWrapper finder = new FinderWrapper(DynamicEntityNames.CONTATO, "this.CODPARC = ? AND this.EMAIL IS NOT NULL", new Object[]{codparc});
        Collection<DynamicVO> contatos = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(finder);
        
        contatos.stream()
            .map(vo -> vo.asString("EMAIL"))
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toUpperCase)
            .forEach(emails::add);
        
        return emails;
    }

    public String buscarEmailsParceiro(BigDecimal codparc) throws Exception {
        DynamicVO parceiro = (DynamicVO) EntityFacadeFactory.getDWFFacade()
            .findEntityByPrimaryKeyAsVO(DynamicEntityNames.PARCEIRO, new Object[]{codparc});
        
        if (parceiro == null) {
            return "";
        }
        
        return Optional.ofNullable(parceiro.asString("AD_EMAILS"))
            .map(String::trim)
            .orElse("");
    }

    public void criarContato(BigDecimal codcontato, BigDecimal codparc, String email, String nomecontato) throws Exception {
        JapeWrapper dao = JapeFactory.dao("Contato");
        dao.create()
            .set("CODCONTATO", codcontato)
            .set("CODPARC", codparc)
            .set("EMAIL", email.trim())
            .set("NOMECONTATO", nomecontato.trim())
            .set("APELIDO", nomecontato.trim())
            .set("RECEBEBOLETOEMAIL", br.com.fgf.util.Constants.SIM)
            .set("ATIVO", br.com.fgf.util.Constants.SIM)
            .save();
    }
}
