package br.furb.bte.ia;

public enum Direction {
    North,
    South,
    East,
    West;//oeste

    public static Direction getDirecaoByName(String name) {
	char letraInicial = name.toUpperCase().charAt(0);
	switch (letraInicial) {
	    case 'N':
		return North;
	    case 'S':
		return South;
	    case 'W':
		return West;
	    case 'E':
		return East;
	}
	return null;
    }

    public String getNameInPT() {
	switch (this) {
	    case East:
		return "Leste";
	    case North:
		return "Norte";
	    case South:
		return "Sul";
	    case West:
		return "Oeste";
	    default:
		return "Outro Lado";
	}
    }
}
