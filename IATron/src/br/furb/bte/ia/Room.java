package br.furb.bte.ia;

// scan in all directions from player position until a wall is found
// for all rows and columns
// the intersection is the definition of a room
// possibly useful for the first move to determine a starting strategy
class Room {

    private GameState gs;

    private Point start;
    private Point topLeft;
    private Point bottomRight;

    int size = -1;

    public Room(GameState gs, Point start) {
	this.gs = gs;
	this.start = start;
    }

    public boolean IsInRoom(Point p) {
	return (p.X > topLeft.X && p.X < bottomRight.X) && (p.Y > topLeft.Y && p.Y < bottomRight.Y);
    }

    private boolean[][] scanX() {
	boolean[][] scan = new boolean[gs.getWidth()][gs.getHeight()];
	int x = start.X;

	for (int y = 0; y < gs.getHeight(); y++) {
	    while (!gs.isWall(++x, y)) {
		scan[x][y] = true;
	    }

	    x = start.X;
	    while (!gs.isWall(--x, y)) {
		scan[x][y] = true;
	    }
	}

	return scan;
    }

    private boolean[][] scanY()
	{
		boolean[][] scan = new boolean[gs.getWidth()][gs.getHeight()];
		int y = start.Y;

		for(int x=0; x < gs.getWidth(); x++) {
			while(!gs.isWall(x,++y)) {
				scan[x][y] = true;
			}

			y = start.Y;
			while(!gs.isWall(x,--y)) {
				scan[x][y] = true;
			}
		}

		return scan;
	}

    private int CalculateSize() {
	int size = 0;

	boolean[][] xscan = scanX();
	boolean[][] yscan = scanY();

	// intersect row and column scans to determine room size
	boolean[][] room = new boolean[gs.getWidth()][gs.getHeight()];
	for (int y = 0; y < gs.getHeight(); y++) {
	    for (int x = 0; x < gs.getWidth(); x++) {
		room[x][y] = xscan[x][y] && yscan[x][y];
		if (room[x][y]) {
		    size++;
		}
	    }
	}

	return size;
    }

    public int GetSize() {
	if (size < 0) {
	    size = CalculateSize();
	}

	return size;
    }
}
