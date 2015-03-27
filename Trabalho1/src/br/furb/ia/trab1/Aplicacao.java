package br.furb.ia.trab1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import br.furb.ia.trab1.busca.HorarioBusca;
import busca.Busca;
import busca.BuscaIterativo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
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
		
		L.setPreRequisito(H);
		H.setPreRequisito(B);
		I.setPreRequisito(A);
		
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
		try {
			List<Disciplina> disciplinasDisponiveis = new ArrayList<>(DISCIPLINAS_OFERTADAS);
			disciplinasDisponiveis.removeAll(DISCIPLINAS_CURSADAS);

			Busca busca = null;
			String tipoBusca = "";

			System.out.println("Busca em Largura (L), busca com Aprofundamento Iterativa (I) ou Busca em Profundidade (P) ?");

			try (Scanner in = new Scanner(System.in)) {
				do {
					MostraStatusConsole console = new MostraStatusConsole();
					tipoBusca = in.next();
					if (tipoBusca.equalsIgnoreCase("L")) {
						busca = new BuscaLargura(console);
					} else if (tipoBusca.equalsIgnoreCase("P")) {
						busca = new BuscaProfundidade(console);
					} else if (tipoBusca.equalsIgnoreCase("I")) {
						busca = new BuscaIterativo(console);
						((BuscaIterativo)busca).setProfMax(10);
					} else {
						System.out.println("Entrada Inválida!\r\nInforme L ou I");
					}
				} while (busca == null);
			}

			removeDependencias(disciplinasDisponiveis, DISCIPLINAS_CURSADAS);
			HorarioBusca horarioBusca = new HorarioBusca(disciplinasDisponiveis);
			Nodo meta = busca.busca(horarioBusca);
			if (meta == null) {
				System.out.println("Problema sem solução");
			} else {
				StringBuilder builder = new StringBuilder();
				printCaminho(meta, builder);
				System.out.println(builder);
			}
			
			//System.out.print("Fim da busca " + (tipoBusca == "L" ? "em Largura" : (tipoBusca == "I" ? "com Aprofundamento Iterativo": " em Profundidade")));
			if(tipoBusca.equalsIgnoreCase("L")){
				System.out.println("Fim da Busca em Largura");
			}else if(tipoBusca.equalsIgnoreCase("I")){
				System.out.println("Fim da Busca com Aprofundamento Iterativo");
			}else if(tipoBusca.equalsIgnoreCase("P")){
				System.out.println("Fim da Busca em Profundidade");
			}
			
		} catch (Exception ex) {
			System.out.println("Erro\r\n" + ex.getMessage());
		}
	}

	private static void printCaminho(Nodo meta, StringBuilder builder) {
		if (meta != null) {
			printCaminho(meta.getPai(), builder);
			builder.append(meta.getEstado().toString()).append(" -----\n");
		}
	}

	private static void removeDependencias(List<Disciplina> disciplinasSemDep, List<Disciplina> cursadas) {
		List<Disciplina> removidos = new LinkedList<>();
		for (Disciplina disc : disciplinasSemDep) {
			if (disc.getPreRequisito() != null && !cursadas.contains(disc.getPreRequisito())) {
				removidos.add(disc);
			}
		}
		disciplinasSemDep.removeAll(removidos);
	}

}
