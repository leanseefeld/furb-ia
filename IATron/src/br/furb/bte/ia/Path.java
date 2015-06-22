package br.furb.bte.ia;

class Path {

    public String direction;
    /**
     * Distancia do destino
     */
    public int length;
    public float cost;

    public Path(String direction, int length) {
	this(direction, length, 0);
    }

    public Path(String direction, int length, float cost) {
	this.direction = direction;
	this.length = length;
	this.cost = cost;
    }
}
