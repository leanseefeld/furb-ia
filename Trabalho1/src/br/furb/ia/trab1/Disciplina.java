package br.furb.ia.trab1;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {

	private Disciplina preRequisito;
	private String nome;
	private int semestre;
	private List<Aula> aulas;

	public Disciplina(String nome, int semestre) {
		super();
		this.nome = nome;
		this.semestre = semestre;
	}

	public Disciplina(Disciplina preRequisito, String nome, int semestre, List<Aula> aulas) {
		super();
		this.preRequisito = preRequisito;
		this.nome = nome;
		this.semestre = semestre;
		this.aulas = aulas;
	}

	public void addAula(Dia dia, int periodo) {
		aulas.add(new Aula(dia, periodo));
		aulas = new ArrayList<>(2);
	}

	public Disciplina getPreRequisito() {
		return preRequisito;
	}

	public void setPreRequisito(Disciplina preRequisito) {
		this.preRequisito = preRequisito;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getSemestre() {
		return semestre;
	}

	public void setSemestre(int semestre) {
		this.semestre = semestre;
	}

	public List<Aula> getAulas() {
		return aulas;
	}

	public void setAulas(List<Aula> aulas) {
		this.aulas = aulas;
	}

}
