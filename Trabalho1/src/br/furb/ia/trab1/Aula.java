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

	/**
	 * 1 para primeiro horario 2 para o segundo horario
	 * 
	 * @return
	 */
	public int getHorario() {
		return horario;
	}

	public void setHorario(int horario) {
		this.horario = horario;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dia == null) ? 0 : dia.hashCode());
		result = prime * result + horario;
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
		Aula other = (Aula) obj;
		if (dia != other.dia)
			return false;
		if (horario != other.horario)
			return false;
		return true;
	}

}