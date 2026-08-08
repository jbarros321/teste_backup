package br.com.ermaq.EventoProgramavelJava;

import com.sankhya.util.StringUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

//TGFPRO
public class ValidaControleRetalho implements EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCommit(TransactionContext tranCtx) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		validaControleRetalho(event);
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		validaControleRetalho(event);
		
	}

	private void validaControleRetalho(PersistenceEvent event) throws Exception {
		DynamicVO vo = (DynamicVO) event.getVo();
		
		if(StringUtils.isNotEmpty(vo.asString("AD_CONTROLARETALHO")) && vo.asString("AD_CONTROLARETALHO").equals("S") && StringUtils.isEmpty(vo.asString("AD_TIPRET"))) {
			throw new MGEModelException("Produto está marcado como Controla Retalho e o Tipo de Retalho não foi informado. Favor verificar!");
		}else if(StringUtils.isNotEmpty(vo.asString("AD_CONTROLARETALHO")) && vo.asString("AD_CONTROLARETALHO").equals("S") && StringUtils.isNotEmpty(vo.asString("AD_TIPRET"))) {
			
			String tipRet = vo.asString("AD_TIPRET");
			
			switch (tipRet) {
			case "CH":
				vo.setProperty("TIPCONTEST", "S");
				vo.setProperty("TITCONTEST", "Tipo");
				vo.setProperty("LISCONTEST", "2000x1200Inteiro\r\n"
						+ "2000x1500Inteiro\r\n"
						+ "3000x1200Inteiro\r\n"
						+ "3000x1500Inteiro\r\n"
						+ "0x750Retalho\r\n"
						+ "751x1500Retalho\r\n"
						+ ">1500Retalho");
				vo.setProperty("USALOCAL", "S");
				
				
				
				
				break;
			case "BR":
				vo.setProperty("TIPCONTEST", "S");
				vo.setProperty("TITCONTEST", "Tipo");
				vo.setProperty("LISCONTEST", "6000Inteiro\r\n"
						+ "0x500Retalho\r\n"
						+ "501x1500Retalho\r\n"
						+ "1501x3000Retalho\r\n"
						+ ">3000Retalho");
				vo.setProperty("USALOCAL", "S");
				
				break;
				
			case "BO":
				
				vo.setProperty("TIPCONTEST", "S");
				vo.setProperty("TITCONTEST", "Tipo");
				vo.setProperty("LISCONTEST", "20000x1200Inteiro\r\n"
						+ "20000x1400Inteiro\r\n"
						+ "0x500Retalho\r\n"
						+ "501x1000Retalho\r\n"
						+ "1001x2000Retalho\r\n"
						+ ">2000Retalho");
				vo.setProperty("USALOCAL", "S");
				
				

				break;

			}

		}
			
			
			
			
	}
		
		
		
		
		
	

}
