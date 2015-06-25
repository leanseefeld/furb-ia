package br.furb.bte.ia;

public class Point {

    public int X, Y;
    public Direction direction;

    public void setDirecao(String name) {
	this.direction = Direction.getDirecaoByName(name);
    }

    public void setDirecao(Direction direcao) {
	this.direction = direcao;
    }

    public Direction getDirecao() {
	return direction;
    }

    public String getDirecaoString() {
	if (direction != null)
	    return direction.name();
	return "Sem direcao";
    }

    public Direction getDirectionFromPoint(int x, int y) {
	if (Y < y)
	    return Direction.North;
	if (Y > y)
	    return Direction.South;
	if (X > x)
	    return Direction.East;
	if (X < x)
	    return Direction.West;
	return null;
    }

    public void moveInDirection(Direction direction) {
	this.direction = direction;
	switch (direction) {
	    case North:
		--Y;
		break;
	    case South:
		++Y;
		break;
	    case West://oeste
		--X;
		break;
	    case East://leste
		++X;
		break;
	}
    }

    public Point() {
	X = 0;
	Y = 0;
    }

    public Point(int x, int y) {
	X = x;
	Y = y;
    }

    @Override
    public String toString() {
	return "X:" + X + " Y:" + Y + " direction: " + this.getDirecaoString();
    }

    public boolean isSamePosition(int x, int y) {
	return this.X == x && this.Y == y;
    }
}
