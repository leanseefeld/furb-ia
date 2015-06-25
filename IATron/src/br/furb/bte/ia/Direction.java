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
}
