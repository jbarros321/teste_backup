package br.com.sankhya.action.rest;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.Objects;

public class ActApiTgfmet {

    public void doAction(ContextoAcao ctx) throws Exception {
        JSONObject body = new JSONObject(ctx.getParam("body").toString());
        
        BigDecimal codmet = validarCampoObrigatorio(body, "CODMET");
        String descricao = validarCampoObrigatorioString(body, "DESCRICAO");
        BigDecimal valor = body.has("VALOR") && !body.isNull("VALOR") 
            ? body.getBigDecimal("VALOR") 
            : BigDecimal.ZERO;
        String ativo = body.has("ATIVO") && !body.isNull("ATIVO")
            ? body.getString("ATIVO")
            : "S";
        
        if (!ativo.equals("S") && !ativo.equals("N")) {
            ativo = "S";
        }
        
        if (verificarCodmetExistente(codmet)) {
            JSONObject erro = new JSONObject();
            erro.put("status", "ERRO");
            erro.put("mensagem", "Código CODMET " + codmet + " já existe na tabela TGFMET");
            erro.put("codigo", codmet);
            ctx.setReturn(erro.toString());
            return;
        }
        
        JapeWrapper dao = JapeFactory.dao("TGFMET");
        dao.create()
            .set("CODMET", codmet)
            .set("DESCRICAO", descricao.trim())
            .set("VALOR", valor)
            .set("ATIVO", ativo)
            .save();
        
        JSONObject response = new JSONObject();
        response.put("status", "OK");
        response.put("mensagem", "Registro inserido com sucesso na TGFMET");
        response.put("CODMET", codmet);
        response.put("DESCRICAO", descricao);
        response.put("VALOR", valor);
        response.put("ATIVO", ativo);
        
        ctx.setReturn(response.toString());
    }
    
    private BigDecimal validarCampoObrigatorio(JSONObject body, String campo) throws Exception {
        if (!body.has(campo) || body.isNull(campo)) {
            throw new IllegalArgumentException("Campo obrigatório '" + campo + "' não informado");
        }
        return body.getBigDecimal(campo);
    }
    
    private String validarCampoObrigatorioString(JSONObject body, String campo) throws Exception {
        if (!body.has(campo) || body.isNull(campo)) {
            throw new IllegalArgumentException("Campo obrigatório '" + campo + "' não informado");
        }
        String valor = body.getString(campo);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo obrigatório '" + campo + "' não pode ser vazio");
        }
        return valor;
    }
    
    private boolean verificarCodmetExistente(BigDecimal codmet) throws Exception {
        JapeWrapper dao = JapeFactory.dao("TGFMET");
        return dao.findOne("CODMET = ?", codmet) != null;
    }
}



