package br.com.satyacode.satyapass.integracaovalecard.rotinas.item.service;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.satyacode.satyapass.integracaovalecard.rotinas.item.repository.ItemRepository;

import java.math.BigDecimal;

public class ItemService {

    private ItemRepository itemRepository;

    public ItemService() {
        this.itemRepository = new ItemRepository();
    }

    public void atualizarCamposNosItens(DynamicVO itemVO, String campo, Object valor, boolean isTransacaoManual) throws Exception {
        if (isTransacaoManual)
            this.itemRepository.atualizarCamposNosItensTM(itemVO.asBigDecimal("NROUNICO"), itemVO.asBigDecimal("SEQUENCIA"), campo, valor);
        else
            this.itemRepository.atualizarCamposNosItens(itemVO.asBigDecimal("NROUNICO"), itemVO.asBigDecimal("SEQUENCIA"), campo, valor);

    }
}
