package br.furb.ia.trab1.buscaiterativa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.furb.ia.trab1.Aula;
import br.furb.ia.trab1.Dia;
import br.furb.ia.trab1.Disciplina;
import busca.BuscaIterativo;
import busca.Estado;
import busca.MostraStatusConsole;

public class Busca implements Estado {

	Disciplina[][] horarios = new Disciplina[5][2];
	ArrayList<Disciplina> disciplinasDisponiveis = new ArrayList<Disciplina>();

	private Dia getDia(int index) {
		Dia dia = null;
		switch (index) {
		case 0:
			dia = Dia.SEG;
		case 1:
			dia = Dia.TER;
		case 2:
			dia = Dia.QUA;
		case 3:
			dia = Dia.QUI;
		case 4:
			dia = Dia.SEX;
		}
		return dia;
	}

	private int getIndex(Dia dia) {
		return dia.GetValue();
	}

	public Busca(ArrayList<Disciplina> disciplinasDisponiveis) {
		this.disciplinasDisponiveis = new ArrayList<>(disciplinasDisponiveis);
	}

	@Override
	public Busca clone() {
		Busca novaBusca = new Busca(disciplinasDisponiveis);
		for (int i = 0; i < horarios.length; i++) {
			for (int j = 0; j < horarios.length; j++) {
				novaBusca.horarios[i][j] = this.horarios[i][j];
			}
		}
		return novaBusca;
	}

	@Override
	public int custo() {
		return 1;
	}

	@Override
	public boolean ehMeta() {
		return numeroHorariosVagos() == 0;
	}

	@Override
	public List<Estado> sucessores() {
		/*
		 * Pelo que entendi, soh vai terminar quando todas as aulas forem
		 * preenchidas logo, se para o o horario em questão não tiver disciplina
		 * disponivel, esse será um caminho sem futuro e ja deve ser descartado
		 */
		List<Estado> proximosEstados = new ArrayList<Estado>();

		for (int i = 0; i < horarios.length; i++) {
			for (int j = 0; j < horarios[i].length; j++) {
				if (horarios[i][j] == null) {

					Dia dia = getDia(i);
					Disciplina disciplinaEncontrada = null;
					int[][] posicoes = new int[2][2];
					for (Disciplina disc : disciplinasDisponiveis) {
						// Procura alguma disciplina para este horario
						disciplinaEncontrada = null;
						Aula aulaEncontrada = null;
						for (Aula aula : disc.getAulas()) {
							if (aula.getDia() == dia) {
								if (aula.getHorario() == j + 1) {
									aulaEncontrada = aula;

									posicoes[1] = new int[] { i, j };
									break;
								}
							}
						}
						// se encontrou aula para este dia,
						// verifica se a outra aula tbm da
						if (aulaEncontrada != null) {
							for (Aula aula : disc.getAulas()) {
								if (aula != aulaEncontrada) {
									int auxDia = getIndex(aula.getDia());
									int auxHorario = aula.getHorario() + 1;
									if (horarios[auxDia][auxHorario] == null) {
										disciplinaEncontrada = disc;
										posicoes[2] = new int[] { auxDia,
												auxHorario };
										break;
									}
								}
							}
						}
						/*
						 * Se encontrou uma disciplina que pode encaixar todas
						 * suas aulas então cria um clone desse estado e altera
						 * esse novo estado
						 */
						if (disciplinaEncontrada != null) {
							Busca novaBusca = this.clone();
							novaBusca.horarios[posicoes[1][1]][posicoes[1][2]] = disciplinaEncontrada;
							novaBusca.horarios[posicoes[2][1]][posicoes[2][2]] = disciplinaEncontrada;
							novaBusca.disciplinasDisponiveis
									.remove(disciplinaEncontrada);
							proximosEstados.add(novaBusca);
						}
					}
					return proximosEstados;
				}
			}
		}
		return proximosEstados;
	}

	public int numeroHorariosVagos() {
		int contador = 0;
		for (int i = 0; i < horarios.length; i++) {
			for (int j = 0; j < horarios[i].length; j++) {
				if (horarios[i][j] == null)
					contador++;
			}
		}
		return contador;
	}

	public static void main(String[] args) {
		BuscaIterativo busca = new BuscaIterativo(new MostraStatusConsole());
		busca.busca(new Busca(new ArrayList<Disciplina>()));
	}
}
