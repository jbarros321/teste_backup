package br.com.sankhya.maha2sw;

class MahaRequestResult {
	Empresa	empresa;
	int		responseCode;
	String	message;

	public Empresa getEmpresa() {
		return this.empresa;
	}

	public int getResponseCode() {
		return this.responseCode;
	}

	public void setResponseCode(int code) {
		this.responseCode = code;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String m) {
		this.message = m;
	}
}
