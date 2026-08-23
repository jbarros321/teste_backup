// ========================================
// SERVIÇO PARA INSERIR DADOS NA TABELA TGFMET
// ========================================

package br.com.cliente.service;

import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import java.math.BigDecimal;

public class TGFMetService {

    // ========================================
    // MÉTODO PRINCIPAL PARA INSERIR REGISTRO
    // ========================================
    public void inserirRegistroTGFMet(BigDecimal codmet, String descricao, BigDecimal valor) throws Exception {

        // PASSO 1: Conectar com a tabela TGFMET
        // JapeWrapper é como um "controlador" da tabela
        JapeWrapper dao = JapeFactory.dao("TGFMET");

        // PASSO 2: Criar um novo registro vazio
        // .create() cria uma linha nova na tabela
        dao.create()

            // PASSO 3: Preencher os campos da tabela
            // CODMET = Código do método (número)
            .set("CODMET", codmet)

            // DESCRICAO = Descrição do método (texto)
            .set("DESCRICAO", descricao)

            // VALOR = Valor do método (número decimal)
            .set("VALOR", valor)

            // PASSO 4: Salvar na tabela
            // .save() é como clicar em "Salvar" no banco de dados
            .save();
    }
}




