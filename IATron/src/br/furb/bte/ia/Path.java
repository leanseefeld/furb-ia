package br.furb.bte.ia;

class Path {

    public Direction direction;
    /**
     * Distancia do destino
     */
    public int length;
    public float cost;

    public Path(Direction direction, int length) {
	this(direction, length, 0);
    }

    public Path(Direction direction, int length, float cost) {
	this.direction = direction;
	this.length = length;
	this.cost = cost;
    }
}
