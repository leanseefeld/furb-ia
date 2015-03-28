package br.furb.ia.trab1.busca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.furb.ia.trab1.Aula;
import br.furb.ia.trab1.Dia;
import br.furb.ia.trab1.Disciplina;
import busca.Estado;

public class HorarioBusca implements Estado {

	private Disciplina[][] horarios = new Disciplina[Dia.values().length][2];
	private List<Disciplina> disciplinasDisponiveis = new ArrayList<Disciplina>();

	public HorarioBusca(List<Disciplina> disciplinasDisponiveis) {
		this.disciplinasDisponiveis = new ArrayList<>(disciplinasDisponiveis);
	}

	@Override
	public HorarioBusca clone() {
		HorarioBusca novaBusca = new HorarioBusca(disciplinasDisponiveis);
		for (int i = 0; i < horarios.length; i++) {
			for (int j = 0; j < horarios[i].length; j++) {
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
		List<Estado> proximosEstados = new ArrayList<>();

		lacoDias: for (int iDia = 0; iDia < horarios.length; iDia++) {
			for (int iPeriodo = 0; iPeriodo < horarios[iDia].length; iPeriodo++) {
				if (horarios[iDia][iPeriodo] == null) {

					Dia dia = Dia.values()[iDia];
					Disciplina disciplinaEncontrada = null;
					int[][] posicoes = new int[2][2];
					for (Disciplina disc : disciplinasDisponiveis) {
						// Procura alguma disciplina para este horario
						disciplinaEncontrada = null;
						Aula aulaEncontrada = null;
						for (Aula aula : disc.getAulas()) {
							if (aula.getDia() == dia) {
								if (aula.getHorario() == iPeriodo + 1) {
									aulaEncontrada = aula;

									posicoes[0] = new int[] { iDia, iPeriodo };
									break;
								}
							}
						}
						// se encontrou aula para este dia,
						// verifica se a outra aula tbm da
						if (aulaEncontrada != null) {
							for (Aula aula : disc.getAulas()) {
								if (aula != aulaEncontrada) {
									int auxDia = aula.getDia().ordinal();
									int auxHorario = aula.getHorario() - 1;
									if (horarios[auxDia][auxHorario] == null) {
										disciplinaEncontrada = disc;
										posicoes[1] = new int[] { auxDia, auxHorario };
										break;
									}
								}
							}
							/*
							 * Se encontrou uma disciplina que pode encaixar todas
							 * suas aulas então cria um clone desse estado e altera
							 * esse novo estado
							 */
							if (disciplinaEncontrada != null) {
								HorarioBusca novaBusca = this.clone();
								novaBusca.horarios[posicoes[0][0]][posicoes[0][1]] = disciplinaEncontrada;
								novaBusca.horarios[posicoes[1][0]][posicoes[1][1]] = disciplinaEncontrada;
								novaBusca.disciplinasDisponiveis.remove(disciplinaEncontrada);
								proximosEstados.add(novaBusca);
							}
						}
					}
					break lacoDias;
				}
			}
		}
		return proximosEstados;
	}

	public int numeroHorariosVagos() {
		int contador = 0;
		for (int i = 0; i < horarios.length; i++) {
			for (int j = 0; j < horarios[i].length; j++) {
				if (horarios[i][j] == null) {
					contador++;
				}
			}
		}
		return contador;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		/*int result = 1;
		result = prime * result + Arrays.hashCode(horarios);
		return result;*/
		return prime * this.toString().hashCode(); 
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HorarioBusca other = (HorarioBusca) obj;
		if (!Arrays.deepEquals(horarios, other.horarios))
			return false;
		if (!this.ComparaGrade(other.horarios))
			return false;
		return true;
	}

	private boolean ComparaGrade(Disciplina[][] horarios2) {
		for (int i = 0; i < horarios2.length; i++) {
			for (int j = 0; j < horarios2[i].length; j++) {
				if (!horarios2[i][j].equals(this.horarios[i][j])) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder saida = new StringBuilder();
		saida.append("SEGUNDA    TERÇA      QUARTA     QUINTA     SEXTA     \n");
		for (int i = 0; i < horarios[0].length; i++) {
			for (int j = 0; j < horarios.length; j++) {
				saida.append(formataString(horarios[j][i], 10) + " ");
			}
			saida.append('\n');
		}
		return saida.toString();
	}

	public String formataString(Disciplina d, int tamanhoMaximo) {
		String nome = d == null ? " -" : d.getNome();
		if (nome.length() > tamanhoMaximo) {
			return nome.substring(0, tamanhoMaximo);
		}
		char[] emptyChars = new char[tamanhoMaximo - nome.length()];
		Arrays.fill(emptyChars, ' ');
		return nome.concat(new String(emptyChars));
	}
}
