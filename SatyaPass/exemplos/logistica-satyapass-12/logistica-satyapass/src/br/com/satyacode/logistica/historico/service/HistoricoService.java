package br.com.satyacode.logistica.historico.service;

import br.com.sankhya.modelcore.MGEModelException;
import br.com.satyacode.logistica.historico.model.Historico;
import br.com.satyacode.logistica.historico.repository.HistoricoRepository;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class HistoricoService {

    private HistoricoRepository historicoRepository;

    public HistoricoService(){
        this.historicoRepository = new HistoricoRepository();
    }

    public void incluirHistorico(Historico historico) throws Exception {
        historico.setData(TimeUtils.getNow());
        if(StringUtils.isEmpty(historico.getOcorrencia())){
            throw new Exception("O campo de Ocorrencia não pode ser vazio!");
        }
        try{
            historicoRepository.incluirHistorico(historico);
        }catch (Exception e){
            throw new Exception("Erro ao incluir historico: " + ExceptionUtils.getMessage(e));
        }
    }

}
