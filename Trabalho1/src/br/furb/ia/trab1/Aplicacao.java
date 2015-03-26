package br.furb.ia.trab1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.furb.ia.trab1.buscaiterativa.HorarioBuscaIterativa;
import busca.BuscaIterativo;
import busca.MostraStatusConsole;
import busca.Nodo;

public class Aplicacao {

	private static final List<Disciplina> DISCIPLINAS_OFERTADAS;
	private static final List<Disciplina> DISCIPLINAS_CURSADAS;

	static {
		int semestre = 1;
		Disciplina A = new Disciplina("A", semestre);
		Disciplina B = new Disciplina("B", semestre);
		Disciplina C = new Disciplina("C", semestre);
		Disciplina D = new Disciplina("D", semestre);
		Disciplina E = new Disciplina("E", semestre);

		semestre = 2;
		Disciplina F = new Disciplina("F", semestre);
		Disciplina G = new Disciplina("G", semestre);
		Disciplina H = new Disciplina("H", semestre);
		Disciplina I = new Disciplina("I", semestre);
		Disciplina J = new Disciplina("J", semestre);

		semestre = 3;
		Disciplina K = new Disciplina("K", semestre);
		Disciplina L = new Disciplina("L", semestre);
		Disciplina M = new Disciplina("M", semestre);
		Disciplina N = new Disciplina("N", semestre);
		Disciplina O = new Disciplina("O", semestre);

		Disciplina[] priPeriodo = { C, A, B, E, B };
		Disciplina[] segPeriodo = { D, C, D, A, E };
		Horario semestre1 = new Horario(new Disciplina[][] { priPeriodo, segPeriodo });

		priPeriodo = new Disciplina[] { J, H, I, H, G };
		segPeriodo = new Disciplina[] { G, F, J, I, F };
		Horario semestre2 = new Horario(new Disciplina[][] { priPeriodo, segPeriodo });

		priPeriodo = new Disciplina[] { K, N, L, O, O };
		segPeriodo = new Disciplina[] { L, M, M, N, K };
		Horario semestre3 = new Horario(new Disciplina[][] { priPeriodo, segPeriodo });

		DISCIPLINAS_OFERTADAS = new ArrayList<>();
		DISCIPLINAS_OFERTADAS.addAll(semestre1.getDisciplinas());
		DISCIPLINAS_OFERTADAS.addAll(semestre2.getDisciplinas());
		DISCIPLINAS_OFERTADAS.addAll(semestre3.getDisciplinas());

		DISCIPLINAS_CURSADAS = Arrays.asList(B, D, E, G, H);
	}

	public static void main(String[] args) {
		List<Disciplina> disciplinasDisponiveis = new ArrayList<>(DISCIPLINAS_OFERTADAS);
		disciplinasDisponiveis.removeAll(DISCIPLINAS_CURSADAS);

		BuscaIterativo busca = new BuscaIterativo(new MostraStatusConsole());
		Nodo meta = busca.busca(new HorarioBuscaIterativa(disciplinasDisponiveis));
		if (meta == null) {
			System.out.println("Problema sem solução");
		} else {
			StringBuilder builder = new StringBuilder();
			printCaminho(meta, builder);
			System.out.println(builder);
		}
	}

	private static void printCaminho(Nodo meta, StringBuilder builder) {
		if (meta != null) {
			printCaminho(meta.getPai(), builder);
			builder.append(meta.getEstado().toString()).append(" -----\n");
		}
	}

}
