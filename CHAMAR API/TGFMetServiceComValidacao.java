// ========================================
// VERSÃO COM VALIDAÇÕES - MAIS SEGURO
// ========================================

package br.com.cliente.service;

import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import java.math.BigDecimal;
import java.util.Objects;

public class TGFMetServiceComValidacao {

    // ========================================
    // MÉTODO COM VALIDAÇÕES PARA SER MAIS SEGURO
    // ========================================
    public void inserirRegistroTGFMet(BigDecimal codmet, String descricao, BigDecimal valor) throws Exception {

        // VALIDAÇÃO 1: Verificar se CODMET foi informado
        if (codmet == null) {
            throw new IllegalArgumentException("Código do método (CODMET) é obrigatório!");
        }

        // VALIDAÇÃO 2: Verificar se DESCRICAO foi informada
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória!");
        }

        // VALIDAÇÃO 3: Verificar se descrição não é muito longa
        if (descricao.length() > 100) {
            throw new IllegalArgumentException("Descrição não pode ter mais de 100 caracteres!");
        }

        // VALIDAÇÃO 4: Verificar se VALOR é positivo (se foi informado)
        if (valor != null && valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor não pode ser negativo!");
        }

        // VALIDAÇÃO 5: Verificar se CODMET já existe
        if (verificarSeCodmetExiste(codmet)) {
            throw new IllegalArgumentException("Código do método " + codmet + " já existe!");
        }

        // Se passou todas as validações, fazer o insert
        executarInsert(codmet, descricao, valor);
    }

    // ========================================
    // MÉTODO PRIVADO PARA VERIFICAR SE CÓDIGO JÁ EXISTE
    // ========================================
    private boolean verificarSeCodmetExiste(BigDecimal codmet) throws Exception {
        JapeWrapper dao = JapeFactory.dao("TGFMET");
        // findOne retorna null se não encontrar, ou o registro se encontrar
        return dao.findOne("CODMET = ?", codmet) != null;
    }

    // ========================================
    // MÉTODO PRIVADO PARA EXECUTAR O INSERT
    // ========================================
    private void executarInsert(BigDecimal codmet, String descricao, BigDecimal valor) throws Exception {
        JapeWrapper dao = JapeFactory.dao("TGFMET");

        dao.create()
            .set("CODMET", codmet)
            .set("DESCRICAO", descricao.trim()) // Remove espaços extras
            .set("VALOR", valor)
            .set("ATIVO", "S") // Define como ativo por padrão
            .save();
    }
}




