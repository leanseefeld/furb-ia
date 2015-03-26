package br.furb.ia.trab1;

import java.util.ArrayList;
import java.util.List;

public class Disciplina {

	private Disciplina preRequisito;
	private String nome;
	private int semestre;
	private final List<Aula> aulas;

	public Disciplina(String nome, int semestre) {
		this(null, nome, semestre, new ArrayList<Aula>(2));
	}

	public Disciplina(Disciplina preRequisito, String nome, int semestre, List<Aula> aulas) {
		this.preRequisito = preRequisito;
		this.nome = nome;
		this.semestre = semestre;
		this.aulas = aulas;
	}

	public void addAula(Dia dia, int periodo) {
		aulas.add(new Aula(dia, periodo));
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
		this.aulas.clear();
		this.aulas.addAll(aulas);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result + ((preRequisito == null) ? 0 : preRequisito.hashCode());
		result = prime * result + semestre;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Disciplina other = (Disciplina) obj;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (preRequisito == null) {
			if (other.preRequisito != null)
				return false;
		} else if (!preRequisito.equals(other.preRequisito))
			return false;
		if (semestre != other.semestre)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getNome();
	}

}
