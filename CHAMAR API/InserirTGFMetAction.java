// ========================================
// BOTÃO PARA CHAMAR A API E INSERIR NA TGFMET
// ========================================

package br.com.cliente.action;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.cliente.service.TGFMetService;
import java.math.BigDecimal;

public class InserirTGFMetAction implements AcaoRotinaJava {

    // ========================================
    // MÉTODO QUE EXECUTA QUANDO O BOTÃO É CLICADO
    // ========================================
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        // PASSO 1: Pegar os valores que o usuário digitou
        // CODMET = Código do método (vem do formulário)
        BigDecimal codmet = new BigDecimal(contexto.getParam("CODMET").toString());

        // DESCRICAO = Descrição do método (vem do formulário)
        String descricao = contexto.getParam("DESCRICAO").toString();

        // VALOR = Valor do método (vem do formulário)
        BigDecimal valor = new BigDecimal(contexto.getParam("VALOR").toString());

        // PASSO 2: Chamar o serviço para inserir na tabela
        // new TGFMetService() = Criar o serviço
        // .inserirRegistroTGFMet() = Executar o método de inserir
        new TGFMetService().inserirRegistroTGFMet(codmet, descricao, valor);

        // PASSO 3: Mostrar mensagem para o usuário
        // "Registro inserido com sucesso!" aparece na tela
        contexto.setMensagemRetorno("Registro inserido com sucesso na TGFMET!");
    }
}




