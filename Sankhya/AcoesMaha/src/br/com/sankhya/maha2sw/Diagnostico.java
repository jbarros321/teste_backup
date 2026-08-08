package br.com.sankhya.maha2sw;

import java.util.Iterator;
import java.util.List;

class Diagnostico {
	private int				numero;
	private List<Processo>	processos;

	public int getNumero() {
		return this.numero;
	}

	public List<Processo> getProcessos() {
		return this.processos;
	}

	public void traverseProcedimento(ProcedimentoCallBack callBack) throws Exception {

		callBack.onStart();

		for (Iterator <Processo>iterator = processos.iterator(); iterator.hasNext();) {
			Processo p = iterator.next();
			callBack.onProcesso(p);

			for (Iterator <Atividade>iterator1 = p.getAtividades().iterator(); iterator1.hasNext();) {
				Atividade a = iterator1.next();
				callBack.onAtividade(a, p);

				Procedimento pr;
				for (Iterator <Procedimento>iterator2 = a.getProcedimentos().iterator(); iterator2.hasNext(); callBack.onProcedimento(pr, a, p))
					pr = iterator2.next();
			}
		}

		callBack.onFinish();

	}

	public static abstract interface ProcedimentoCallBack {

		public abstract void onStart() throws Exception;

		public abstract void onFinish() throws Exception;

		public abstract void onProcesso(Processo paramProcesso) throws Exception;

		public abstract void onAtividade(Atividade paramAtividade, Processo paramProcesso) throws Exception;

		public abstract void onProcedimento(Procedimento paramProcedimento, Atividade paramAtividade, Processo paramProcesso) throws Exception;
	}
}
