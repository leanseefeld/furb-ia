package br.furb.bte.ia;

import java.util.ArrayList;
import java.util.List;

class GameState {

    public boolean[][] map;
    // Stores the locations of the two players.
    private Point myLocation, opponentLocation;

    private int width, height;
    private boolean myWin = false;
    private boolean opponentWin = false;

    private GameState parent;
    private float score;

    public GameState() {
	this(Map.GetWall(), Map.Width(), Map.Height(), new Point(Map.MyX(), Map.MyY()), new Point(Map.OpponentX(),
		Map.OpponentY()));
    }

    public GameState(GameState gs) {
	this(gs.map, gs.Width(), gs.Height(), new Point(gs.MyX(), gs.MyY()), new Point(gs.OpponentX(), gs.OpponentY()));
	this.map = (boolean[][]) gs.map.clone();
    }

    private GameState(boolean[][] map, int width, int height, Point me, Point opponent) {
	this.map = map;
	this.width = width;
	this.height = height;
	myLocation = me;
	opponentLocation = opponent;
    }

    // TODO this is really not good enough 
    // should be including walls too
    // 
    @Override
    public boolean equals(Object obj) {
	if (obj != null && obj instanceof GameState) {
	    GameState key = (GameState) obj;

	    return key.MyX() == MyX() && key.MyY() == MyY() && key.OpponentX() == OpponentX()
		    && key.OpponentY() == OpponentY();
	}
	return false;
    }

    @Override
    public int hashCode() {
	return MyX() + MyY() + OpponentX() + OpponentY();
	//	return MyX().GetHashCode() + MyY().GetHashCode() + OpponentX().GetHashCode() + OpponentY().GetHashCode();
    }

    public float GetScore() {
	return score;
    }

    public void SetScore(float score) {
	this.score = score;
    }

    public void SetParent(GameState gs) {
	parent = gs;
    }

    public GameState GetParent() {
	return parent;
    }

    public int Width() {
	return width;
    }

    public int Height() {
	return height;
    }

    public boolean IsWall(int x, int y) {
	if (x < 0 || y < 0 || x >= width || y >= height) {
	    return true;
	} else {
	    return map[x][y];
	}
    }

    // My X location.
    public int MyX() {
	return (int) myLocation.X;
    }

    // My Y location.
    public int MyY() {
	return (int) myLocation.Y;
    }

    // The opponent's X location.
    public int OpponentX() {
	return (int) opponentLocation.X;
    }

    // The opponent's Y location.
    public int OpponentY() {
	return (int) opponentLocation.Y;
    }

    public boolean IsDraw() {
	return MyX() == OpponentX() && MyY() == OpponentY();
    }

    public boolean IsOpponentWin() {
	return !IsDraw() && opponentWin;
    }

    public boolean IsMyWin() {
	return !IsDraw() && myWin;
    }

    public boolean IsEndGame() {
	return IsDraw() || IsMyWin() || IsOpponentWin();
    }

    public void ApplyMoveToMe(String direction) {
	//	Console.Error.WriteLine("Applying  move to me:" + direction);
	//Console.Error.WriteLine(String.Format("Before X:{0} Y:{1}, MapX:{2} MapY:{3}",MyX(), MyY(),Map.MyX(), Map.MyY()));
	myLocation.MoveInDirection(direction);
	if (map[myLocation.X][myLocation.Y]) {
	    opponentWin = true;
	} else {
	    map[myLocation.X][myLocation.Y] = true;
	}
	//Console.Error.WriteLine(String.Format("After X:{0} Y:{1}, MapX:{2} MapY:{3}",MyX(), MyY(),Map.MyX(), Map.MyY()));
    }

    public void ApplyMoveToOpponent(String direction) {
	//	Console.Error.WriteLine("Applying  move to opponent:" + direction);
	opponentLocation.MoveInDirection(direction);
	if (map[opponentLocation.X][opponentLocation.Y]) {
	    myWin = true;
	} else {
	    map[opponentLocation.X][opponentLocation.Y] = true;
	}
    }

    public GameState ApplyMoveToMeAndCreate(String direction) {
	GameState gs = new GameState((boolean[][]) map.clone(), width, height, new Point(myLocation.X, myLocation.Y),
		new Point(opponentLocation.X, opponentLocation.Y));
	gs.ApplyMoveToMe(direction);
	return gs;
    }

    public GameState ApplyMoveToOpponentAndCreate(String direction) {
	GameState gs = new GameState((boolean[][]) map.clone(), width, height, new Point(myLocation.X, myLocation.Y),
		new Point(opponentLocation.X, opponentLocation.Y));
	gs.ApplyMoveToOpponent(direction);
	return gs;
    }

    public Iterable<Point> PossibleMoves(int x, int y) {
	return PossibleMoves(x, y, false);
    }

    public Iterable<Point> PossibleMoves(int x, int y, boolean ignoreWalls) {
	List<Point> pontos = new ArrayList<Point>();
	for (int i = 0; i < Map.MOVES.length; i++) {
	    Point move = new Point(x, y);
	    move.MoveInDirection(Map.MOVES[i]);
	    if (!ignoreWalls && !IsWall(move.X, move.Y)) {
		pontos.add(move);
	    } else if (ignoreWalls) {
		pontos.add(move);
	    }
	}
	return pontos;
    }
}
