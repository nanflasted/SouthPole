package Server;

public class ServerProcess {
	public static int login(String un, String pw)
	{
		return 1;
	}
	
	public static int signup(String un, String pw)
	{
		return 3;
	}
	
	public static int[][] getCond(String un, SubServerMap map)
	{
		int [][] out = new int[5][5];
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 5; j++)
			{
				out[i][j] = map.getTile(i,j).ordinal();
			}
		}
		return out;
	}
	
	public static int[][] move(String un, SubServerMap map, int direction)
	{
		return getCond(un, map);
	}
}
