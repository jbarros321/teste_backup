package br.com.satyacode.satyapass.simulacao.integracao;

import br.com.satyacode.satyapass.log.model.StatusItemEnum;
import br.com.satyacode.satyapass.simulacao.integracao.utils.factory.LogFactory;

public class Controller {

    public void gerenciarIntegracao() {
        LogFactory.incluirItem( "Iniciando integracao", "Chamando este metodo para mostrar como é facil a utilizacao agora da inclusao do log", "INFO", StatusItemEnum.INFO, true);

        LogFactory.incluirItem( "Fim da  integracao", "Chamando este metodo para mostrar como é facil a utilizacao agora da inclusao do log", "INFO", StatusItemEnum.INFO, true);

    }
}
