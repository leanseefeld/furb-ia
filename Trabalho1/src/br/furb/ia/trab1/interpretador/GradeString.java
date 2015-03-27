package br.furb.ia.trab1.interpretador;

import java.util.HashMap;
import java.util.Map;

import br.furb.ia.trab1.Dia;
import br.furb.ia.trab1.Disciplina;

class GradeString {

	private String[][] grade;
	private int semestre;
	private Map<String, Disciplina> disciplinas;

	public GradeString(int semestre, String[][] grade) {
		this.semestre = semestre;
		this.grade = grade;

		disciplinas = new HashMap<>();
		for (int periodo = 0; periodo < grade.length; periodo++) {
			for (int dia = 0; dia < grade[periodo].length; dia++) {
				Disciplina disciplina = getOrCreateDisciplina(grade[periodo][dia]);
				disciplina.addAula(Dia.values()[dia], periodo + 1);
			}
		}
	}

	private Disciplina getOrCreateDisciplina(String disciplinaStr) {
		Disciplina disciplina = disciplinas.get(disciplinaStr);
		if (disciplina == null) {
			disciplina = new Disciplina(disciplinaStr, semestre);
			disciplinas.put(disciplinaStr, disciplina);
		}
		return disciplina;
	}

	public String[][] getGrade() {
		return grade;
	}

	public int getSemestre() {
		return semestre;
	}

	public Map<String, Disciplina> asDisciplinas() {
		return disciplinas;
	}

}