package br.furb.bte.ia;

import java.io.InputStreamReader;

class Map {

    // Stores the width and height of the Tron map.
    private static int width, height;

    // Stores the actual contents of the Tron map.
    private static boolean[][] walls;

    // Stores the locations of the two players.
    private static Point myLocation, opponentLocation;

    public static boolean[][] GetWall() {
	return walls;
    }

    public static int Width() {
	return width;
    }

    public static int Height() {
	return height;
    }

    // do not count head as wall
    // TODO soooooooo messy
    public static boolean IsNextToWall(int x, int y) {
	int tempX = x;
	int tempY = y;
	tempX = x - 1;
	if (tempX == MyX() && tempY == MyY())
	    return false;

	if (IsWall(tempX, tempY))
	    return true;

	tempX = x - 1;
	if (tempX == MyX() && tempY == MyY())
	    return false;

	if (IsWall(tempX, tempY))
	    return true;

	tempX = x;
	tempY = y - 1;
	if (tempX == MyX() && tempY == MyY())
	    return false;

	if (IsWall(tempX, tempY))
	    return true;

	tempY = y + 1;
	if (tempX == MyX() && tempY == MyY())
	    return false;

	if (IsWall(tempX, tempY))
	    return true;

	return false;
    }

    public static boolean IsWall(int x, int y) {
	if (x < 0 || y < 0 || x >= width || y >= height) {
	    return true;
	} else {
	    return walls[x][y];
	}
    }

    // My X location.
    public static int MyX() {
	return (int) myLocation.X;
    }

    // My Y location.
    public static int MyY() {
	return (int) myLocation.Y;
    }

    // The opponent's X location.
    public static int OpponentX() {
	return (int) opponentLocation.X;
    }

    // The opponent's Y location.
    public static int OpponentY() {
	return (int) opponentLocation.Y;
    }

    public static void MakeMove(Direction direction) {
	switch (direction) {
	    case North:
		MakeMove(1);
		break;
	    case East:
		MakeMove(2);
		break;
	    case South:
		MakeMove(3);
		break;
	    case West:
		MakeMove(4);
		break;
	    default:
		System.out.println("Erro: nenhum movimento definido");
		System.exit(1);
	}
    }

    /**
     * Inicializa o mapa usando uma matriz de inteiros
     * 
     * @param mapa
     *            Representação das informações: <br>
     *            -1 = Obstáculo <br>
     *            1 = moto do player <br>
     *            2 = moto IA
     */
    public static void Initialize(int[][] mapa) {

	final int OBSTACULO = -1;
	final int MOTO_PLAYER = 1;
	final int MOTO_IA = 2;

	width = mapa.length;
	height = mapa[0].length;

	walls = new boolean[width][height];
	for (int x = 0; x < mapa.length; x++) {
	    for (int y = 0; y < mapa[0].length; y++) {
		switch (mapa[x][y]) {
		    case OBSTACULO:
			walls[x][y] = true;
			break;
		    case MOTO_IA:
			walls[x][y] = true;
			myLocation = new Point(x, y);
			break;
		    case MOTO_PLAYER:
			walls[x][y] = true;
			opponentLocation = new Point(x, y);
			break;
		    default:
			walls[x][y] = false;
		}
	    }
	}
    }

    // Reads the map from standard input (from the console).
    public static void Initialize() {
	InputStreamReader readr = new InputStreamReader(System.in);
	String firstLine = "";
	try {
	    int c;
	    while ((c = readr.read()) >= 0) {
		if (c == '\n') {
		    break;
		}
		firstLine += (char) c;
	    }
	} catch (Exception e) {
	    System.out.println("Could not read from stdin.");
	    System.exit(1);
	}
	firstLine = firstLine.trim();
	if (firstLine.equals("") || firstLine.equals("exit")) {
	    System.exit(1);
	    System.out.println("Fim de jogo");
	}
	String[] tokens = firstLine.split(" ");
	if (tokens.length != 2) {
	    System.out.println("FATAL ERROR: the first line of input should "
		    + "be two integers separated by a space. " + "Instead, got: " + firstLine);
	    System.exit(1);
	}
	try {

	    width = Integer.valueOf(tokens[0]);
	    height = Integer.valueOf(tokens[1]);
	} catch (Exception e) {
	    System.out.println("FATAL ERROR: invalid map dimensions: " + firstLine);
	    System.exit(1);
	}
	walls = new boolean[width][height];
	boolean foundMyLocation = false;
	boolean foundHisLocation = false;
	int numSpacesRead = 0;
	int x = 0, y = 0;
	while (y < height) {
	    int c = 0;
	    try {
		c = readr.read();
	    } catch (Exception e) {
		System.out.println("FATAL ERROR: exception while reading " + "from stdin.");
		System.exit(1);
	    }
	    if (c < 0) {
		break;
	    }
	    switch (c) {
		case '\n':
		    if (x != width) {
			System.out.println("Invalid line length: " + x + "(line " + y + ")");
			System.exit(1);
		    }
		    ++y;
		    x = 0;
		    continue;
		case '\r':
		    continue;
		case ' ':
		    walls[x][y] = false;
		    break;
		case '#':
		    walls[x][y] = true;
		    break;
		case '1':
		    if (foundMyLocation) {
			System.out.println("FATAL ERROR: found two locations " + "for player "
				+ "1 in the map! First location is (" + myLocation.X + "," + myLocation.Y
				+ "), second location is (" + x + "," + y + ").");
			System.exit(1);
		    }
		    walls[x][y] = true;
		    myLocation = new Point(x, y);
		    foundMyLocation = true;
		    break;
		case '2':
		    if (foundHisLocation) {
			System.out.println("FATAL ERROR: found two locations for player "
				+ "2 in the map! First location is (" + opponentLocation.X + "," + opponentLocation.Y
				+ "), second location " + "is (" + x + "," + y + ").");
			System.exit(1);
		    }
		    walls[x][y] = true;
		    opponentLocation = new Point(x, y);
		    foundHisLocation = true;
		    break;
		default:
		    System.out.println("FATAL ERROR: invalid character received. " + "ASCII value = " + c);
		    System.exit(1);
		    break;
	    }
	    ++x;
	    ++numSpacesRead;
	}
	if (numSpacesRead != width * height) {
	    System.out.println("FATAL ERROR: wrong number of spaces in the map. " + "Should be " + (width * height)
		    + ", but only found " + numSpacesRead + " spaces before end of stream.");
	    System.exit(1);
	}
	if (!foundMyLocation) {
	    System.out.println("FATAL ERROR: did not find a location for player 1!");
	    System.exit(1);
	}
	if (!foundHisLocation) {
	    System.out.println("FATAL ERROR: did not find a location for player 2!");
	    System.exit(1);
	}
    }

    // Writes the given integer (direction code) to stdout.
    //   1 -- North
    //   2 -- East
    //   3 -- South
    //   4 -- West
    private static void MakeMove(int direction) {
	System.out.println(direction);
    }

    public static String wallsToString() {
	StringBuilder str = new StringBuilder();
	str.append("IA Map: (1=IA, 2=player)\r\n");
	for (int x = 0; x < walls.length; x++) {
	    for (int y = 0; y < walls[x].length; y++) {

		if (myLocation.X == x && myLocation.Y == y) {
		    str.append('1');
		} else if (opponentLocation.X == x && opponentLocation.Y == y) {
		    str.append('2');
		} else if (walls[x][y])
		    str.append('#');
		else
		    str.append(' ');

	    }
	    str.append(" X:" + x + "\r\n");
	}
	return str.toString();
    }

    public static boolean isValid(Point ponto) {
	return //
	/*    */ponto.X >= 0 && //
		ponto.X < Map.width && //
		ponto.Y >= 0 && //
		ponto.X < Map.height;
    }

}
