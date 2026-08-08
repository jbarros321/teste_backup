package br.com.sankhya.maha2sw;

class Empresa {
	private String		email;
	private Diagnostico	diagnostico;

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Diagnostico getDiagnostico() {
		return this.diagnostico;
	}

	public void setDiagnostico(Diagnostico diagnostico) {
		this.diagnostico = diagnostico;
	}
}
