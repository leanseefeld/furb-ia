package br.furb.ia.trab1;

public enum Dia {
	SEG(0), TER(1), QUA(2), QUI(3), SEX(4);

	private Dia(int valor) {
		this.value = valor;
	}

	private int value;

	public int GetValue() {
		return value;
	}
}