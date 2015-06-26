package br.furb.bte.ia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class MyTronBot {

    private static final int TIME_LIMIT = 1000;
    private static final int TIME_THRESHOLD = 150;
    private static final int MAX_MAP_SIZE = 2500;
    private static final int MAX_DEPTH = 2;

    private static Instant lastTime;

    // making these global to reduce garbage collection
    private static final Stack<GameState> toVisitStack = new Stack<GameState>();
    private static final List<GameState> visited = new ArrayList<GameState>();

    private static float GetEuclideanOpponentDistance(int x, int y) {
	return GetEuclideanOpponentDistance(new Point(x, y), new Point(Map.OpponentX(), Map.OpponentY()));
    }

    private static float GetEuclideanOpponentDistance(Point me, Point opponent) {
	return (float) Math.abs(Math.sqrt(Math.pow((float) me.X - (float) opponent.X, 2)
		+ Math.pow((float) me.Y - (float) opponent.Y, 2)));
    }

    //    // This is junk from start package
    //    private static String PerformFoolishRandomMove()
    //    {
    //        int x = Map.MyX();
    //        int y = Map.MyY();
    //        List<String> validMoves = new ArrayList<String>();
    //
    //        if (!Map.IsWall(x, y - 1))
    //        {
    //            validMoves.add("North");
    //        }
    //        if (!Map.IsWall(x + 1, y))
    //        {
    //            validMoves.add("East");
    //        }
    //        if (!Map.IsWall(x, y + 1))
    //        {
    //            validMoves.add("South");
    //        }
    //        if (!Map.IsWall(x - 1, y))
    //        {
    //            validMoves.add("West");
    //        }
    //        if (validMoves.isEmpty())
    //        {
    //            return "North"; // Hopeless. Might as well go North!
    //        }
    //        else
    //        {
    //            Random rand = new Random();
    //            int whichMove = rand.Next(validMoves.Count);
    //            return validMoves[whichMove];
    //        }
    //    }

    // Evaluation function for alpha-beta pruning / minimax
    private static float EvaluateMove(GameState gs) {
	if (gs.IsMyWin()) {
	    return MAX_MAP_SIZE;
	}

	if (gs.IsOpponentWin()) {
	    return -MAX_MAP_SIZE;
	}

	Territory room = new Territory(gs);
	room.DetermineTerritories();
	int size = room.GetMySize() - room.GetOpponentSize();
	//	System.out.println("IA:" + room.GetMySize() + " OP:" + room.GetOpponentSize());
	System.out.println(room.toString());

	return (float) size;
    }

    private static float AlphaBeta(GameState gs, int depth, float alpha, float beta, boolean isMax) {
	if (depth == 0 || gs.IsEndGame()) {
	    float val = EvaluateMove(gs);
	    return isMax ? val : -val;
	}

	Point p = null;
	if (isMax) {
	    p = new Point(gs.MyX(), gs.MyY());
	} else {
	    p = new Point(gs.OpponentX(), gs.OpponentY());
	}

	GameState newState = null;
	for (Point child : gs.possibleMoves(p.X, p.Y, true)) {
	    Direction newDirection = child.getDirectionFromPoint(p.X, p.Y);
	    if (isMax) {
		newState = gs.ApplyMoveToMeAndCreate(newDirection);
	    } else {
		newState = gs.ApplyMoveToOpponentAndCreate(newDirection);
	    }

	    float alphaAux = -AlphaBeta(newState, depth - 1, -beta, -alpha, !isMax);
	    System.out.println("Depth: " + depth + " " + (isMax ? "MAX" : "MIN") + " Direction:" + newDirection
		    + " alpha:" + alphaAux);
	    alpha = Math.max(alpha, alphaAux);
	    if (beta <= alpha) {
		break;
	    }
	}

	//	Point locationPlayerOrigem;
	//	if (isMax)
	//	    locationPlayerOrigem = gs.getOpponent();
	//	else
	//	    locationPlayerOrigem = gs.getMe();
	//
	//	System.out.println("Depth: " + depth + " " + (isMax ? "MAX" : "MIN") + " alpha:" + alpha + " Best Direction:"
	//		+ locationPlayerOrigem.getDirecaoString());
	return alpha;
    }

    private static int ScoreStraightPath(Direction direction, Point p) {
	int score = 0;
	p.moveInDirection(direction);

	while (!Map.IsWall(p.X, p.Y)) {
	    score++;
	    p.moveInDirection(direction);
	}

	return score;
    }

    private static int FloodFill(GameState gs, int x, int y) {
	Queue<Point> q = new LinkedList<Point>();

	int total = 0;

	// shallow copy array
	boolean[][] map = (boolean[][]) gs.map.clone();

	q.add(new Point(x, y));
	map[x][y] = false;
	//Console.Error.WriteLine(x + " " + y);

	while (!q.isEmpty()) {
	    Point n = q.poll();
	    if (n.X < 0 || n.Y < 0 || n.X >= gs.getWidth() || n.Y >= gs.getHeight())
		continue;

	    // process neighbours, mark as visited and increment count
	    if (!map[n.X][n.Y]) {
		q.add(new Point(n.X + 1, n.Y));
		q.add(new Point(n.X - 1, n.Y));
		q.add(new Point(n.X, n.Y - 1));
		q.add(new Point(n.X, n.Y + 1));
		map[n.X][n.Y] = true;
		total++;
	    }

	}

	return total;
    }

    private static int FloodFillDepthFirst(GameState gs) {
	return FloodFillDepthFirst(gs, true);
    }

    // do flood fill from all reachable nodes in graph and return running count
    private static int FloodFillDepthFirst(GameState gs, boolean me) {
	toVisitStack.clear();
	visited.clear();
	toVisitStack.push(gs);

	int score = 0;

	while (!toVisitStack.isEmpty()) {
	    GameState v = toVisitStack.pop();
	    if (!visited.contains(v)) {
		visited.add(v);

		if (me) {
		    score += FloodFill(v, v.MyX(), v.MyY());
		    for (Point n : gs.PossibleMoves(v.MyX(), v.MyY())) {
			toVisitStack.push(v.ApplyMoveToMeAndCreate(n.getDirectionFromPoint(v.MyX(), v.MyY())));
		    }
		} else {
		    score += FloodFill(v, v.OpponentX(), v.OpponentY());
		    for (Point n : gs.PossibleMoves(v.OpponentX(), v.OpponentY())) {
			toVisitStack.push(v.ApplyMoveToOpponentAndCreate(n.getDirectionFromPoint(v.OpponentX(),
				v.OpponentY())));
		    }

		}

	    }
	}

	return score;
    }

    // used with A*
    private static Path GetPath(GameState gs) {
	GameState parent;
	GameState current = gs;

	int length = 0;
	float cost = current.GetScore();

	while ((parent = current.GetParent()) != null) {
	    length++;
	    cost += parent.GetScore();
	    if (parent.MyX() == Map.MyX() && parent.MyY() == Map.MyY()) {
		//Console.Error.WriteLine("Got move");
		Direction direction = new Point(current.MyX(), current.MyY()).getDirectionFromPoint(Map.MyX(),
			Map.MyY());
		return new Path(direction, length, cost);
	    } else {
		current = parent;
	    }
	}

	return null;
    }

    // Modified A* search
    // TODO clean up this is barely legible
    private static Path MoveByShortestPath(GameState gs, Point goal) {
	List<GameState> toVisit = new ArrayList<GameState>();
	List<GameState> visited = new ArrayList<GameState>();
	toVisit.add(gs);

	while (!toVisit.isEmpty()) {
	    // determine which node in queue is closet to the goal
	    if (toVisit.size() > 1) {
		float best = GetEuclideanOpponentDistance(toVisit.get(0).MyX(), toVisit.get(0).MyY());
		float tmp;
		int bestIndex = 0;
		for (int i = 1; i < toVisit.size(); i++) {
		    tmp = GetEuclideanOpponentDistance(toVisit.get(0).MyX(), toVisit.get(0).MyY());
		    if (tmp < best) {
			bestIndex = i;
			best = tmp;
		    }
		}
		if (bestIndex > 0) {
		    GameState removed = toVisit.get(bestIndex);
		    toVisit.remove(bestIndex);
		    toVisit.add(0, removed);
		}
	    }

	    GameState v = toVisit.remove(0);

	    if (!visited.contains(v)) {
		visited.add(v);

		for (Point n : gs.possibleMoves(v.MyX(), v.MyY(), true)) {

		    // goal found
		    if (goal != null && n.X == goal.X && n.Y == goal.Y) {
			//Console.Error.WriteLine("Found");
			GameState found = v.ApplyMoveToMeAndCreate(n.getDirectionFromPoint(v.MyX(), v.MyY()));
			found.SetParent(v);
			return GetPath(found);

			// add neighbours to queue
		    } else if (!v.isWall(n.X, n.Y)) {
			GameState next = v.ApplyMoveToMeAndCreate(n.getDirectionFromPoint(v.MyX(), v.MyY()));

			if (toVisit.contains(next)) {
			    GameState parent = toVisit.get(toVisit.indexOf(next)).GetParent();
			    // path back to start node is shorter from node being processed
			    if (GetEuclideanOpponentDistance(new Point(v.MyX(), v.MyY()),
				    new Point(Map.MyX(), Map.MyY())) < parent.GetScore()) {
				//Console.Error.WriteLine("betta");							
				toVisit.get(toVisit.indexOf(next)).SetParent(v);
				continue;
			    }
			}

			v.SetScore(GetEuclideanOpponentDistance(new Point(v.MyX(), v.MyY()),
				new Point(Map.MyX(), Map.MyY())));
			next.SetParent(v);
			toVisit.add(next);
		    }
		}
	    }
	}

	return null;
    }

    private static Path PerformChaseMove() {
	GameState gs = new GameState();

	Path p = null;
	p = MoveByShortestPath(gs, new Point(Map.OpponentX(), Map.OpponentY()));

	return p;
    }

    // Determine which direction has the most available spaces and fill
    // as efficiently as possible
    private static Direction PerformSurvivalMove() {
	float score = 0;
	float bestScore = 0;

	GameState gs = new GameState();
	Point p = new Point();
	Direction bestMove = Direction.values()[0];

	List<Direction> ties = new ArrayList<Direction>();

	System.out.println(Map.wallsToString());
	
	for (Direction direction : Direction.values()) {
	    p.X = Map.MyX();
	    p.Y = Map.MyY();
	    p.moveInDirection(direction);
	    if (!Map.IsWall(p.X, p.Y)) {
		//score = FloodFill(gs.ApplyMoveToMeAndCreate(Map.MOVES[i]), p.X, p.Y);
		score = FloodFillDepthFirst(gs.ApplyMoveToMeAndCreate(direction));
	    } else {
		score = 0;
	    }
	    //Console.Error.WriteLine("Far:" + Map.MOVES[i] + ":" + score);
	    if (score > bestScore) {
		bestMove = direction;
		bestScore = score;
		ties.clear();
		ties.add(bestMove);
	    } else if (score == bestScore) {
		ties.add(direction);
	    }
	}

	// break ties
	// hug closest wall
	if (!ties.isEmpty()) {
	    bestScore = Integer.MAX_VALUE;
	    for (Direction move : ties) {
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.moveInDirection(move);
		// use shortest distance to closest wall
		score = Integer.MAX_VALUE;
		int tmp;
		for (Direction direction : Direction.values()) {
		    Point q = new Point(p.X, p.Y);
		    q.moveInDirection(direction);
		    if (q.isSamePosition(Map.MyX(), Map.MyY())) {
			//		    if (q.X == Map.MyX() && q.Y == Map.MyY()) {
			continue;
		    }
		    q.X = p.X;
		    q.Y = p.Y;
		    tmp = ScoreStraightPath(direction, q);
		    if (tmp < score) {
			score = tmp;
		    }
		}
		//Console.Error.WriteLine("Far tie break:" + move + ":" + score);
		if (score < bestScore) {
		    bestScore = score;
		    bestMove = move;
		}
	    }
	}

	return bestMove;
    }

    // Determine which direction has the most available spaces and fill
    // as efficiently as possible
    // TODO come back to this maybe...it's TOO SLOW!
    /*
       private static String PerformSurvivalMove()
       {
        float score = 0;
        float bestScore = 0;
        int depth = 0;


        GameState gs = new GameState();	
        int breadth = BreadthFirst(gs);
        int maxDepth = (int)((double)breadth / Math.Pow(2, (double)breadth / 20));
        Console.Error.WriteLine("breadth " + breadth + " " + maxDepth);

        Point p = new Point();
        String bestMove = Map.MOVES[0];

        List<String> ties = new List<String>();

        Queue<GameState> previous = new Queue<GameState>();
        List<GameState> paths = new List<GameState>();
        previous.Enqueue(gs);

        int lastLength = 0;

        while(previous.Count > 0 && (depth < 10 && depth < maxDepth)) {
            GameState basis = previous.Dequeue();

            for(int i=0; i<Map.MOVES.Length; i++) {
                p.X = basis.MyX(); p.Y = basis.MyY();
                p.MoveInDirection(Map.MOVES[i]);
                if (!Map.IsWall(p.X, p.Y)) {
                    GameState move = basis.ApplyMoveToMeAndCreate(Map.MOVES[i]);
                    score = FloodFillDepthFirst(move);
                    move.SetScore(score);
                    move.SetParent(basis);
                    previous.Enqueue(move);
                    paths.Add(move);
                //Console.Error.WriteLine("Far:" + Map.MOVES[i] + ":" + score);
                }
            }

            Path path = null;
            foreach (GameState move in paths) {
                path = GetPath(move);
                //Console.Error.WriteLine("Survive:" + path.direction + ":" + path.cost + " length:" + path.length);
                if (path.cost > bestScore) {
                    bestScore = path.cost;
                    bestMove = path.direction;
                }
            }

            if (path != null && path.length > lastLength) {
                lastLength = path.length;
                paths.Clear();
                bestScore = 0;
                depth++;
            }
        }

        // break ties
        // hug closest wall
        if (ties.Count > 0) {
            bestScore = int.MaxValue;
            foreach(String move in ties) {
                p.X = Map.MyX(); p.Y = Map.MyY();
                p.MoveInDirection(move);
                // use shortest distance to closest wall
                score = int.MaxValue;
                int tmp;
                foreach (String direction in Map.MOVES) {
                    Point q = new Point(p.X, p.Y);
                    q.MoveInDirection(direction);
                    if (q.X == Map.MyX() && q.Y == Map.MyY()) {
                        continue;
                    }
                    q.X = p.X; 
                    q.Y = p.Y;
                    tmp = ScoreStraightPath(direction, q);
                    if (tmp < score) {
                        score = tmp;
                    }
                }
                //Console.Error.WriteLine("Far tie break:" + move + ":" + score);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
            }
        }
    	
        return bestMove;
       }
    */

    // alpha beta with iterative deepening
    private static Direction PerformNearMove(Path shortestPath) {
	int depth = 0;
	float time = 0;
	float score, bestScore;
	GameState gs = new GameState();
	Point p = new Point();
	// default to something that won't kill us - server sometimes
	// runs out of time WAY early resulting in no time to perform alpha beta
	// iterations
	Direction bestMove = Direction.values()[0]; //PerformFoolishRandomMove();

	List<Direction> ties = new ArrayList<Direction>();

	Direction[] moves = new Direction[4];
	float[] scores = new float[4]; // 0 north, 1 south, 2 east, 3 west
	scores[0] = 3;
	scores[1] = 2;
	scores[2] = 1;
	scores[3] = 0;

	// used to adjust time estimate for next depth so we don't go over time limit
	float timebase = ((float) Map.Width() * (float) Map.Height()) / (15f * 15f);

	while (Duration() + time < (TIME_LIMIT - TIME_THRESHOLD) && depth <= MAX_DEPTH) {
	    score = Integer.MIN_VALUE;
	    bestScore = Integer.MIN_VALUE;
	    depth++;

	    // order moves by previous iterations scores
	    // TODO this really does nothing. Cache of game states is needed
	    // for quick eval retrival and move ordering
	    int length = scores.length;
	    boolean swapped = true;

	    moves[0] = Direction.North;
	    moves[1] = Direction.South;
	    moves[2] = Direction.East;
	    moves[3] = Direction.West;

	    //Está ordenando os movimentos pela melhor pontuação
	    while (swapped) {
		swapped = false;
		for (int b = 0; b < length - 1; b++) {
		    if (scores[b] < scores[b + 1]) {
			Direction tmp = moves[b];
			float ftmp = scores[b];

			moves[b] = moves[b + 1];
			scores[b] = scores[b + 1];

			moves[b + 1] = tmp;
			scores[b + 1] = ftmp;

			swapped = true;
		    }
		}
		length -= 1;
	    }

	    System.out.println("Melhores Scores");
	    for (int i = 0; i < scores.length; i++) {
		System.out.println(" Score: " + scores[i] + "\t " + moves[i]);
	    }

	    for (int i = 0; i < moves.length; i++) {
		Direction move = moves[i];
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.moveInDirection(move);
		if (!Map.IsWall(p.X, p.Y)) {
		    Instant instI = Instant.now();
		    // negate since starting with opponents moves
		    score = -AlphaBeta(gs.ApplyMoveToMeAndCreate(move), depth, -Integer.MAX_VALUE, Integer.MAX_VALUE,
			    false);

		    // estimate time for next depth
		    Instant instF = Instant.now();
		    long duration = java.time.Duration.between(instI, instF).toMillis();
		    time = duration * (depth * timebase);

		    System.out.println("AlphaBeta: Depth:" + depth + " Duration:" + duration + " BestScore:" + score
			    + " Direction:" + move.name());
		} else {
		    score = Integer.MIN_VALUE;
		}
		//Console.Error.WriteLine("alphabeta:" + move + ":" + score + " depth:" + depth);
		if (score > bestScore) {
		    bestMove = move;
		    bestScore = score;
		    ties.clear();
		    ties.add(bestMove);
		} else if (score == bestScore) {
		    ties.add(move);
		}

		// track score
		//                String temp = move.Substring(0, 1).ToUpper();
		//                int firstChar = (int)temp[0];
		switch (move) {
		    case North:
			scores[0] = score;
			break;
		    case South:
			scores[1] = score;
			break;
		    case East:
			scores[2] = score;
			break;
		    case West:
			scores[3] = score;
			break;
		}
	    }
	    System.out.println("Fim Depth: " + depth + " " + (false ? "MAX" : "MIN") + " alpha:" + score);

	    depth++;
	}

	System.out.println("Melhores direcoes");
	for (int i = 0; i < ties.size(); i++) {
	    System.out.println(" * " + ties.get(i));
	}

	List<Direction> secondaryTies = new ArrayList<Direction>();
	// break ties
	if (ties.size() > 1) {
	    bestScore = Integer.MIN_VALUE;
	    for (Direction move : ties) {
		//Console.Error.WriteLine("alpha tie break:" + move);
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.moveInDirection(move);
		if (Map.IsWall(p.X, p.Y)) {
		    continue;
		}

		Territory room = new Territory(gs.ApplyMoveToMeAndCreate(move));
		room.DetermineTerritories();
		score = (float) room.GetMySize() - (float) room.GetOpponentSize();

		//Console.Error.WriteLine("alpha tie break:" + move + ":" + score);
		if (score > bestScore) {
		    bestScore = score;
		    bestMove = move;
		    secondaryTies.clear();
		    secondaryTies.add(move);
		} else if (score == bestScore) {
		    secondaryTies.add(move);
		}
	    }
	}

	// kinda lame, but need another tie breaker...quick and dirty
	if (secondaryTies.size() > 1) {
	    bestScore = Integer.MIN_VALUE;
	    for (Direction move : ties) {
		if (shortestPath != null) {
		    if (move.equals(shortestPath.direction)) {
			bestMove = shortestPath.direction;
			break;
		    }
		}
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.moveInDirection(move);
		if (Map.IsWall(p.X, p.Y)) {
		    continue;
		}
		score = -GetEuclideanOpponentDistance(p.X, p.Y);
		//Console.Error.WriteLine("alpha tie break:" + move + ":" + score);
		if (score > bestScore) {
		    bestScore = score;
		    bestMove = move;
		}
	    }
	}

	return bestMove;
    }

    private static Direction MakeMove() {
	Direction move = null;
	Instant inst = Instant.now();
	Path path = PerformChaseMove();
	//	Path path = new Path("Norte", 0);
	System.out.println("Duração de PerformChaseMove():"
		+ java.time.Duration.between(inst, Instant.now()).toMillis());

	//Significa que seu inimigo é alcançável
	if (path != null) {
	    //	    move = path.direction;
	    move = PerformNearMove(path);
	} else if (path == null) {
	    move = PerformSurvivalMove();
	}

	return move;
    }

    private static long Duration() {
	long tempo = java.time.Duration.between(lastTime, Instant.now()).toMillis();
	return tempo;
	//	TimeSpan ts = DateTime.Now - lastTime;
	//	return ts.Milliseconds;
    }

    public static Direction processMove(int[][] mapa) {
	Map.Initialize(mapa);
	lastTime = Instant.now();
	Direction move = MakeMove();
	System.out.println("Duração do loop: " + Duration() + " ms");
	return move;
    }

    public static void Main() {
	while (true) {
	    Map.Initialize();
	    lastTime = Instant.now();
	    //	    lastTime = DateTime.Now;
	    Map.MakeMove(MakeMove());
	    System.out.println("Duração do loop: " + Duration() + " ms");
	}
    }

    public static void imprimirMapa() {
	System.out.println(Map.wallsToString());
    }

    public static void imprimirMapaGameState() {
	GameState gs = new GameState();
	StringBuilder str = new StringBuilder();
	str.append("IA Map: (1=IA, 2=player)\r\n");
	for (int x = 0; x < gs.map.length; x++) {
	    for (int y = 0; y < gs.map[x].length; y++) {

		if (gs.MyX() == x && gs.MyY() == y) {
		    str.append('1');
		} else if (gs.OpponentX() == x && gs.OpponentY() == y) {
		    str.append('2');
		} else if (gs.map[x][y])
		    str.append('#');
		else
		    str.append(' ');

	    }
	    str.append(" X:" + x + "\r\n");
	}
	System.out.println(str.toString());
    }
}
