package br.com.sankhya.industria.manutencao.util;

import java.math.BigDecimal;

public interface ManutencaoConstants {

	public static final BigDecimal	FILA_TESTE_ENTRADA							= new BigDecimal(46);
	public static final BigDecimal	FILA_TESTE_SAIDA							= new BigDecimal(2060);
	public static final BigDecimal	FILA_SOFTWARE								= new BigDecimal(186);
	public static final BigDecimal	FILA_SOFTWARE_PDV							= new BigDecimal(1736);
	public static final BigDecimal	FILA_SOFTWARE_RH							= new BigDecimal(3190);
	public static final BigDecimal	FILA_SOFTWARE_PESSOAL_PLUS					= new BigDecimal(5148);
	public static final BigDecimal	FILA_COMPILACAO_WEB							= new BigDecimal(1721);
	public static final BigDecimal	FILA_OSRELEASE								= new BigDecimal(160);
	public static final BigDecimal	FILA_CLOUD									= new BigDecimal(4528);
	public static final BigDecimal	FILA_SD										= new BigDecimal(125);
	public static final BigDecimal	FILA_MANUTENCAO								= new BigDecimal(176);
	public static final BigDecimal	FILA_IMPLATACAO								= new BigDecimal(5723);

	public static final BigDecimal	SERV_IND_CORRECAO_ERRO						= new BigDecimal(50601);
	public static final BigDecimal	SERV_IND_TESTE								= new BigDecimal(50605);
	public static final BigDecimal	SERV_IND_COMPILACAO							= new BigDecimal(50603);
	public static final BigDecimal	SERV_GER_COMUNICACAO_ADMINISTRATIVA			= new BigDecimal(50313);
	public static final BigDecimal	SERV_IND_ANALISE_DE_ERROS					= new BigDecimal(50506);

	public static final BigDecimal	GRU_LINHA_G_SK								= new BigDecimal(1002);
	public static final BigDecimal	GRU_LINHA_W_SK								= new BigDecimal(1003);
	public static final BigDecimal	GRU_LINHA_G_JV								= new BigDecimal(1005);
	public static final BigDecimal	GRU_LINHA_W_JV								= new BigDecimal(1006);

	public static final BigDecimal	PROD_JW										= new BigDecimal(20459);

	public static final BigDecimal	CR_DIRETORIA_DE_TECNOLOGIA					= new BigDecimal(10001400);
	public static final BigDecimal	CR_DIRETORIA_DE_DESENVOLVIMENTO_DE_NEGOCIOS	= new BigDecimal(10001500);
	public static final BigDecimal	CR_TI										= new BigDecimal(10001505);

	public static final String		MERGE										= "MG";
	public static final String		CORRIGIR_ERRO								= "CE";
	public static final String		TESTE_ENTRADA								= "TE";
	public static final String		COMPILACAO									= "CP";
	public static final String		TESTE_SAIDA									= "TS";
	public static final String		SOFTWARE									= "SF";

	public static final String		PENDENTE									= "P";
	public static final String		CONCLUIDO									= "C";

	public static final String		SITPROD_ATIVO								= "A";
	public static final String		SITPROD_BONIFICADO							= "B";
	public static final String		SITPROD_CANCELADO							= "C";
	public static final String		SITPROD_SUSPENSO							= "S";

	public static final BigDecimal	CODUSUCOMPILADOR							= new BigDecimal(160);

}
