package Server.Resource;

public class TownData {

	private int x;
	private int y;
	private String name;
	
	public TownData(int x, int y, String name)
	{
		this.x = x;
		this.y = y;
		this.name = name;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public String getName()
	{
		return name;
	}
}
