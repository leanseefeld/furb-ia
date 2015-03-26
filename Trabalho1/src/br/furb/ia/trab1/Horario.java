package br.furb.ia.trab1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Horario {

	private Set<Disciplina> disciplinas;

	public Horario(Disciplina[][] grade) {
		disciplinas = new HashSet<>();
		for (int i = 0; i < grade.length; i++) {
			for (int j = 0; j < grade[i].length; j++) {
				Disciplina disciplina = grade[i][j];
				disciplina.addAula(Dia.values()[j], i);
				disciplinas.add(disciplina);
			}
		}
	}

	public List<Disciplina> getDisciplinasList() {
		return new ArrayList<>(disciplinas);
	}

	public Set<Disciplina> getDisciplinas() {
		return disciplinas;
	}

}
