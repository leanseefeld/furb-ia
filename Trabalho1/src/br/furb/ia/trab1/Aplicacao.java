package br.furb.ia.trab1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import br.furb.ia.trab1.busca.HorarioBusca;
import br.furb.ia.trab1.interpretador.InterpretadorHorario;
import busca.Busca;
import busca.BuscaIterativo;
import busca.BuscaLargura;
import busca.BuscaProfundidade;
import busca.MostraStatusConsole;
import busca.Nodo;

public class Aplicacao {


	public static void main(String[] args) throws FileNotFoundException {
		List<Disciplina> DISCIPLINAS_OFERTADAS,
		DISCIPLINAS_CURSADAS;
		try (Scanner sc = new Scanner(new File("res/Grade01.grd"))) {
			InterpretadorHorario interpretador = new InterpretadorHorario(sc);
			interpretador.interpretar();

			DISCIPLINAS_OFERTADAS = interpretador.getDisciplinas();
			DISCIPLINAS_CURSADAS = interpretador.getCursadas();
		}
		
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
				System.out.println(meta.getProfundidade());
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
