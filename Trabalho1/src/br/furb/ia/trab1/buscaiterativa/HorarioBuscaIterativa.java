package br.furb.ia.trab1.buscaiterativa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.furb.ia.trab1.Aula;
import br.furb.ia.trab1.Dia;
import br.furb.ia.trab1.Disciplina;
import busca.Estado;

public class HorarioBuscaIterativa implements Estado {

	private Disciplina[][] horarios = new Disciplina[Dia.values().length][2];
	// Trocar pra um HashSet quando tiver funcionando pra ver se melhora a performance e comentar no relatório
	private List<Disciplina> disciplinasDisponiveis = new ArrayList<Disciplina>();

	public HorarioBuscaIterativa(List<Disciplina> disciplinasDisponiveis) {
		this.disciplinasDisponiveis = new ArrayList<>(disciplinasDisponiveis);
	}

	@Override
	public HorarioBuscaIterativa clone() {
		HorarioBuscaIterativa novaBusca = new HorarioBuscaIterativa(disciplinasDisponiveis);
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
		/*
		 * Pelo que entendi, soh vai terminar quando todas as aulas forem
		 * preenchidas logo, se para o o horario em questão não tiver disciplina
		 * disponivel, esse será um caminho sem futuro e ja deve ser descartado
		 */
		List<Estado> proximosEstados = new ArrayList<Estado>();

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
						}
						/*
						 * Se encontrou uma disciplina que pode encaixar todas
						 * suas aulas então cria um clone desse estado e altera
						 * esse novo estado
						 */
						if (disciplinaEncontrada != null) {
							HorarioBuscaIterativa novaBusca = this.clone();
							novaBusca.horarios[posicoes[0][0]][posicoes[0][1]] = disciplinaEncontrada;
							novaBusca.horarios[posicoes[1][0]][posicoes[1][1]] = disciplinaEncontrada;
							novaBusca.disciplinasDisponiveis.remove(disciplinaEncontrada);
							proximosEstados.add(novaBusca);
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
		int result = 1;
		result = prime * result + ((disciplinasDisponiveis == null) ? 0 : disciplinasDisponiveis.hashCode());
		result = prime * result + Arrays.hashCode(horarios);
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
		HorarioBuscaIterativa other = (HorarioBuscaIterativa) obj;
		if (disciplinasDisponiveis == null) {
			if (other.disciplinasDisponiveis != null)
				return false;
		} else if (disciplinasDisponiveis.size() != other.disciplinasDisponiveis.size() || !disciplinasDisponiveis.containsAll(other.disciplinasDisponiveis))
			return false;
		if (!Arrays.deepEquals(horarios, other.horarios))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder saida = new StringBuilder();
		saida.append("SEGUNDA    TERÇA      QUARTA     QUINTA     SEXTA     \n");
		for (int i = 0; i < horarios[0].length; i++) {
			for (int j = 0; j < horarios.length; j++) {
				saida.append(formataString(horarios[j][i], 10));
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
