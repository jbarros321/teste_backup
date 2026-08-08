package br.com.sankhya.maha2sw;

import javax.naming.OperationNotSupportedException;

public class ModoExecucao {

	public static final String RESP_SIM 		= "Sim";
	public static final String RESP_NAO 		= "No";
	public static final String RESP_MAHA_SIM 	= "Maha = Sim";
	public static final String RESP_MAHA_NAO 	= "Maha = No";

	private String 	respostaMaha;
	private String 	respostaSimples;
	private int 	modo;

	public ModoExecucao(int modo) throws OperationNotSupportedException {
		this.modo = modo;
		setupModo();
	}

	private void setupModo() throws OperationNotSupportedException {
		switch (modo) {
			case 0:
				respostaMaha = RESP_MAHA_SIM;
				respostaSimples = RESP_SIM;

				break;

			case 1:
				respostaMaha = RESP_MAHA_SIM;
				respostaSimples = RESP_SIM;

				break;

			case 2:
				respostaMaha = RESP_MAHA_SIM;
				respostaSimples = RESP_SIM;

				break;

			case 3:
				respostaMaha = RESP_MAHA_SIM;
				respostaSimples = RESP_SIM;

				break;

			case 4:
				respostaMaha = RESP_MAHA_NAO;
				respostaSimples = RESP_NAO;

				break;

			default:
				throw new OperationNotSupportedException("Modo de execuo Maha no suportado na importao.");
		}
	}

	public String getRespostaMaha() {
		return respostaMaha;
	}

	public String getRespostaSimples() {
		return respostaSimples;
	}
}
