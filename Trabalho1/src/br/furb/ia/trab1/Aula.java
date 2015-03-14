package br.furb.ia.trab1;

public class Aula {

	private Dia dia;
	private int horario;

	public Aula(Dia dia, int horario) {
		super();
		this.dia = dia;
		this.horario = horario;
	}

	public Dia getDia() {
		return dia;
	}

	public void setDia(Dia dia) {
		this.dia = dia;
	}

	public int getHorario() {
		return horario;
	}

	public void setHorario(int horario) {
		this.horario = horario;
	}

}