package br.furb.bte.ia;

import java.util.LinkedList;
import java.util.Queue;

class Territory {

    private GameState gs;

    private Point me;
    private Point you;

    private int[][] map;
    private int mySize = 0;
    private int opponentSize = 0;

    private static final int BOUNDARY = 9999;

    public Territory(GameState gs) {
	this.gs = gs;
	this.me = new Point(gs.MyX(), gs.MyY());
	this.you = new Point(gs.OpponentX(), gs.OpponentY());
    }

    // modified flood fill to scan outwards from each players' locations
    // and mark their territories
    private void scan() {
	map = new int[gs.getWidth()][gs.getHeight()];

	Queue<Point> q = new LinkedList<Point>();
	q.add(me);
	q.add(you);

	map[me.X][me.Y] = 1;
	map[you.X][you.Y] = -1;

	int player = 1;
	int level = 1;

	while (!q.isEmpty()) {
	    Point node = q.poll();
	    //	    if (node.X < 0 || node.Y < 0 || node.X >= gs.Width() || node.Y >= gs.Height())
	    if (!Map.isValid(node))
		continue;
	    // exclude first nodes from this check
	    if (Math.abs(map[node.X][node.Y]) != 1 && gs.isWall(node.X, node.Y))
		continue;

	    level = map[node.X][node.Y];
	    // already marked as BOUNDARY so skip
	    if (level == BOUNDARY)
		continue;

	    // check player: + us - opponent
	    if (level > 0) {
		player = 1;
	    } else if (level < 0) {
		player = -1;
	    }

	    // bump level
	    level = Math.abs(level) + 1;

	    //Console.Error.WriteLine("x " + node.X + " y " + node.Y + " value " + map[node.X][node.Y]);

	    // process the neighbours
	    Point north = new Point(node.X, node.Y - 1);
	    if (ProcessNeighbour(north, level, player))
		q.add(north);

	    Point east = new Point(node.X + 1, node.Y);
	    if (ProcessNeighbour(east, level, player))
		q.add(east);

	    Point south = new Point(node.X, node.Y + 1);
	    if (ProcessNeighbour(south, level, player))
		q.add(south);

	    Point west = new Point(node.X - 1, node.Y);
	    if (ProcessNeighbour(west, level, player))
		q.add(west);
	}
    }

    @Override
    public String toString() {
	StringBuilder str = new StringBuilder();
	str.append("Território: (OP = Oponente/Player1) \r\n");
	for (int x = 0; x < map.length; x++) {
	    for (int y = 0; y < map[x].length; y++) {
		if (me.isSamePosition(x, y)) {
		    str.append("IA").append("\t");
		} else if (you.isSamePosition(x, y)) {
		    str.append("OP").append("\t");
		} else {
		    str.append(map[x][y]).append("\t");
		}
	    }
	    str.append("\r\n");
	}
	return str.toString();
    }

    private boolean ProcessNeighbour(Point node, int level, int player) {
	if (node.X < 0 || node.Y < 0 || node.X >= gs.getWidth() || node.Y >= gs.getHeight())
	    return false;

	if (gs.isWall(node.X, node.Y))
	    return false;

	int val = map[node.X][node.Y];
	if (Math.abs(val) == 1)
	    return false;

	// already processed by other player and levels are equal so define boundary 
	// of territory and do not process its neighbours
	if (val != 0 && val != BOUNDARY && val * player < 0 && Math.abs(val) == level) {
	    map[node.X][node.Y] = BOUNDARY;
	    // adjust size when area is marked as boundary
	    if (player > 0) {
		opponentSize--;
	    } else {
		mySize--;
	    }
	    return false;
	    // unprocessed and our terrirtory. mark and visit neighbours
	} else if (val == 0 && player > 0) {
	    map[node.X][node.Y] = level;
	    mySize++;
	    return true;
	    // unprocessed and opponent territory. mark and visit neighbours
	} else if (val == 0 && player < 0) {
	    map[node.X][node.Y] = -level;
	    opponentSize++;
	    return true;
	}

	// otherwise, do not visit neighbours
	return false;
    }

    public int GetMySize() {
	return mySize;
    }

    public int GetOpponentSize() {
	return opponentSize;
    }

    public void DetermineTerritories() {
	scan();
    }
}
