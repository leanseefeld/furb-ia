package br.furb.ia.trab1.interpretador;

class Dependencia {

	private String disciplina;
	private String dependencia;

	public Dependencia(String disciplina, String dependencia) {
		this.disciplina = disciplina;
		this.dependencia = dependencia;
	}

	public String getDisciplina() {
		return disciplina;
	}

	public void setDisciplina(String disciplina) {
		this.disciplina = disciplina;
	}

	public String getDependencia() {
		return dependencia;
	}

	public void setDependencia(String dependencia) {
		this.dependencia = dependencia;
	}

}