package br.furb.bte.ia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class MyTronBot {

    private static final int TIME_LIMIT = 1000;
    private static final int TIME_THRESHOLD = 150;
    private static final int MAX_MAP_SIZE = 2500;

    private static Instant lastTime;

    // making these global to reduce garbage collection
    private static final Stack<GameState> toVisitStack = new Stack<GameState>();
    private static final Queue<GameState> toVisit = new LinkedList<GameState>();
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

	//	if (gs.IsDraw()) {
	//	    //		return 0
	//	}

	Territory room = new Territory(gs);
	room.DetermineTerritories();

	//	int mySize = room.GetMySize();
	//	int opponentSize = room.GetOpponentSize();
	int size = room.GetMySize() - room.GetOpponentSize();
	//Console.Error.WriteLine(String.Format("my room:{0} other room:{1}",mySize,opponentSize));	

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
	for (Point child : gs.PossibleMoves(p.X, p.Y, true)) {
	    if (isMax) {
		newState = gs.ApplyMoveToMeAndCreate(child.GetDirectionFromPoint(p.X, p.Y));
	    } else {
		newState = gs.ApplyMoveToOpponentAndCreate(child.GetDirectionFromPoint(p.X, p.Y));
	    }

	    alpha = Math.max(alpha, -AlphaBeta(newState, depth - 1, -beta, -alpha, !isMax));
	    if (beta <= alpha) {
		break;
	    }
	}

	return alpha;
    }

    private static int ScoreStraightPath(String direction) {
	return ScoreStraightPath(direction, new Point(Map.MyX(), Map.MyY()));
    }

    private static int ScoreStraightPath(String direction, Point p) {
	int score = 0;
	p.MoveInDirection(direction);

	while (!Map.IsWall(p.X, p.Y)) {
	    score++;
	    p.MoveInDirection(direction);
	}

	return score;
    }

    private static int BreadthFirst(GameState gs) {
	return BreadthFirst(gs, true);
    }

    private static int BreadthFirst(GameState gs, boolean me) {
	toVisit.clear();
	visited.clear();
	toVisit.add(gs);

	while (!toVisit.isEmpty()) {
	    GameState v = toVisit.poll();
	    if (!visited.contains(v)) {
		visited.add(v);
		if (me) {
		    for (Point n : gs.PossibleMoves(v.MyX(), v.MyY())) {
			toVisit.add(v.ApplyMoveToMeAndCreate(n.GetDirectionFromPoint(v.MyX(), v.MyY())));
		    }
		} else {
		    for (Point n : gs.PossibleMoves(v.OpponentX(), v.OpponentY())) {
			toVisit.add(v.ApplyMoveToOpponentAndCreate(n.GetDirectionFromPoint(v.OpponentX(), v.OpponentY())));
		    }
		}
	    }
	}

	return visited.size();
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
	    if (n.X < 0 || n.Y < 0 || n.X >= gs.Width() || n.Y >= gs.Height())
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
			toVisitStack.push(v.ApplyMoveToMeAndCreate(n.GetDirectionFromPoint(v.MyX(), v.MyY())));
		    }
		} else {
		    score += FloodFill(v, v.OpponentX(), v.OpponentY());
		    for (Point n : gs.PossibleMoves(v.OpponentX(), v.OpponentY())) {
			toVisitStack.push(v.ApplyMoveToOpponentAndCreate(n.GetDirectionFromPoint(v.OpponentX(),
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
		String direction = new Point(current.MyX(), current.MyY()).GetDirectionFromPoint(Map.MyX(), Map.MyY());
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

		for (Point n : gs.PossibleMoves(v.MyX(), v.MyY(), true)) {

		    // goal found
		    if (goal != null && n.X == goal.X && n.Y == goal.Y) {
			//Console.Error.WriteLine("Found");
			GameState found = v.ApplyMoveToMeAndCreate(n.GetDirectionFromPoint(v.MyX(), v.MyY()));
			found.SetParent(v);
			return GetPath(found);

			// add neighbours to queue
		    } else if (!v.IsWall(n.X, n.Y)) {
			GameState next = v.ApplyMoveToMeAndCreate(n.GetDirectionFromPoint(v.MyX(), v.MyY()));

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
    private static String PerformSurvivalMove() {
	float score = 0;
	float bestScore = 0;

	GameState gs = new GameState();
	Point p = new Point();
	String bestMove = Map.MOVES[0];

	List<String> ties = new ArrayList<String>();

	for (int i = 0; i < Map.MOVES.length; i++) {
	    p.X = Map.MyX();
	    p.Y = Map.MyY();
	    p.MoveInDirection(Map.MOVES[i]);
	    if (!Map.IsWall(p.X, p.Y)) {
		//score = FloodFill(gs.ApplyMoveToMeAndCreate(Map.MOVES[i]), p.X, p.Y);
		score = FloodFillDepthFirst(gs.ApplyMoveToMeAndCreate(Map.MOVES[i]));
	    } else {
		score = 0;
	    }
	    //Console.Error.WriteLine("Far:" + Map.MOVES[i] + ":" + score);
	    if (score > bestScore) {
		bestMove = Map.MOVES[i];
		bestScore = score;
		ties.clear();
		ties.add(bestMove);
	    } else if (score == bestScore) {
		ties.add(Map.MOVES[i]);
	    }
	}

	// break ties
	// hug closest wall
	if (!ties.isEmpty()) {
	    bestScore = Integer.MAX_VALUE;
	    for (String move : ties) {
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.MoveInDirection(move);
		// use shortest distance to closest wall
		score = Integer.MAX_VALUE;
		int tmp;
		for (String direction : Map.MOVES) {
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

    private static String PerformFarMove() {
	return PerformFarMove(null);
    }

    // Determine which direction has the most available spaces 
    // NOT USED
    private static String PerformFarMove(Path shortestPath) {
	float score = 0;
	float bestScore = 0;

	GameState gs = new GameState();
	Point p = new Point();
	String bestMove = Map.MOVES[0];

	List<String> ties = new ArrayList<String>();

	for (int i = 0; i < Map.MOVES.length; i++) {
	    p.X = Map.MyX();
	    p.Y = Map.MyY();
	    p.MoveInDirection(Map.MOVES[i]);
	    if (!Map.IsWall(p.X, p.Y)) {
		score = BreadthFirst(gs.ApplyMoveToMeAndCreate(Map.MOVES[i]));
	    } else {
		score = 0;
	    }
	    //Console.Error.WriteLine("Far:" + Map.MOVES[i] + ":" + score + " bestscore:" + bestScore);
	    if (score > bestScore) {
		bestMove = Map.MOVES[i];
		bestScore = score;
		ties.clear();
		ties.add(bestMove);
	    } else if (score == bestScore) {
		ties.add(Map.MOVES[i]);
	    }
	}

	// break ties
	if (ties.size() > 1) {
	    if (shortestPath != null) {
		return shortestPath.direction;
	    }
	    bestScore = Integer.MAX_VALUE;
	    for (String move : ties) {
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.MoveInDirection(move);
		if (shortestPath == null) {
		    // use shortest distance to closest wall
		    score = Integer.MAX_VALUE;
		    int tmp;
		    for (String direction : Map.MOVES) {
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

    // alpha beta with iterative deepening
    private static String PerformNearMove(Path shortestPath) {
	int depth = 0;
	float time = 0;
	float score, bestScore;
	GameState gs = new GameState();
	Point p = new Point();
	// default to something that won't kill us - server sometimes
	// runs out of time WAY early resulting in no time to perform alpha beta
	// iterations
	String bestMove = Map.MOVES[0]; //PerformFoolishRandomMove();
	Date lastAlphaBeta;

	List<String> ties = new ArrayList<String>();

	String[] moves = new String[4];
	float[] scores = new float[4]; // 0 north, 1 south, 2 east, 3 west
	scores[0] = 3;
	scores[1] = 2;
	scores[2] = 1;
	scores[3] = 0;

	// used to adjust time estimate for next depth so we don't go over time limit
	float timebase = ((float) Map.Width() * (float) Map.Height()) / (15f * 15f);

	while (Duration() + time < (TIME_LIMIT - TIME_THRESHOLD) && depth <= 12) {
	    score = Integer.MIN_VALUE;
	    bestScore = Integer.MIN_VALUE;
	    depth++;

	    // order moves by previous iterations scores
	    // TODO this really does nothing. Cache of game states is needed
	    // for quick eval retrival and move ordering
	    int length = scores.length;
	    boolean swapped = true;

	    moves[0] = "North";
	    moves[1] = "South";
	    moves[2] = "East";
	    moves[3] = "West";

	    //Est� ordenando os movimentos pela melhor pontua��o
	    while (swapped) {
		swapped = false;
		for (int b = 0; b < length - 1; b++) {
		    if (scores[b] < scores[b + 1]) {
			String tmp = moves[b];
			float ftmp = scores[b];

			moves[b] = moves[b + 1];
			scores[b] = scores[b + 1];

			moves[b + 1] = tmp;
			scores[b + 1] = ftmp;

			swapped = true;
		    }
		}
		length -= 1;

		//Console.Error.WriteLine("best:" + best + " score:" + scores[best]);
	    }

	    for (int i = 0; i < moves.length; i++) {
		String move = moves[i];
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.MoveInDirection(move);
		if (!Map.IsWall(p.X, p.Y)) {
		    Instant instI = Instant.now();
		    // negate since starting with opponents moves
		    //                    lastAlphaBeta = Date.Now;
		    score = -AlphaBeta(gs.ApplyMoveToMeAndCreate(move), depth, -Integer.MAX_VALUE, Integer.MAX_VALUE,
			    false);

		    // estimate time for next depth
		    //                    TimeSpan ts = DateTime.Now - lastAlphaBeta;
		    //                    time = (float)ts.Milliseconds * (depth * timebase);
		    Instant instF = Instant.now();
		    long duration = java.time.Duration.between(instI, instF).toMillis();
		    time = duration * (depth * timebase);
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
		char firstChar = move.toUpperCase().charAt(0);
		switch (firstChar) {
		    case 'N':
			scores[0] = score;
			break;
		    case 'S':
			scores[1] = score;
			break;
		    case 'E':
			scores[2] = score;
			break;
		    case 'W':
			scores[3] = score;
			break;
		}
	    }
	    depth++;
	}

	List<String> secondaryTies = new ArrayList<String>();
	// break ties
	if (ties.size() > 1) {
	    bestScore = Integer.MIN_VALUE;
	    for (String move : ties) {
		//Console.Error.WriteLine("alpha tie break:" + move);
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.MoveInDirection(move);
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
	    for (String move : ties) {
		if (shortestPath != null) {
		    if (move.equals(shortestPath.direction)) {
			bestMove = shortestPath.direction;
			break;
		    }
		}
		p.X = Map.MyX();
		p.Y = Map.MyY();
		p.MoveInDirection(move);
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

    private static String MakeMove() {
	String move = null;
	//	int width = Map.Width();
	//	int height = Map.Height();
	Path path = PerformChaseMove();

	// means our enemy is attainable
	if (path != null) {
	    move = path.direction;
	    //if (path.length < (width + height)) {
	    move = PerformNearMove(path);
	    //} else {
	    //move = PerformFarMove(path);
	    //}
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

    public static String processMove(int[][] mapa) {
	Map.Initialize(mapa);
	lastTime = Instant.now();
	String move = MakeMove();
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
}
