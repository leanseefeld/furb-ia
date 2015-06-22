package br.furb.bte.ia;

public class Point
{
    public int X, Y;

    public String GetDirectionFromPoint(int x, int y)
    {
        if (Y < y) return "North";
        if (Y > y) return "South";
        if (X > x) return "East";
        if (X < x) return "West";

        // returning null to force exception so I know if this is broken
        // SO BAD! 
        return null;
    }

    public void MoveInDirection(String direction)
    {
	char firstChar = direction.toUpperCase().charAt(0);
//        String temp = direction.Substring(0, 1).ToUpper();
//        int firstChar = (int)temp[0];

        switch (firstChar)
        {
            case 'N':
                --Y;
                break;
            case 'S':
                ++Y;
                break;
            case 'W':
                --X;
                break;
            case 'E':
                ++X;
                break;
        }
    }

    public Point()
    {
        X = 0; Y = 0;
    }

    public Point(int x, int y)
    {
        X = x;
        Y = y;
    }
}