package Server;

import java.util.*;

import Utility.SPU;

public class UserData implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String un;
	private int server;
	private int x;
	private int y;
	private int visibility; //how many tiles can be seen by this user around himself on each side
	
	public UserData(String un, int server)
	{
		this.un = un;
		this.server = server;
	}
	
	public void spawn()
	{
		int spawnX = (int)(Math.random()*(SPU.DEFAULT_MAP_SIZE-2*SPU.WATER_BORDER_SIZE))+SPU.WATER_BORDER_SIZE;
		int spawnY = (int)(Math.sqrt((Math.pow((SPU.DEFAULT_MAP_SIZE/2-SPU.WATER_BORDER_SIZE),2)-Math.pow(spawnX, 2)))+0.5);
		x = spawnX;
		y = spawnY;
		visibility = 5;
	}
	
	public int getVisibility()
	{
		return visibility;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public boolean move(SubServerMap map, int direction)
	{
		SPU.Tile targetTile;
		switch (SPU.Command.values()[direction])
		{
			case MOVEDOWN:
				targetTile = map.getTile(x, y+1);
				break;
			case MOVELEFT:
				targetTile = map.getTile(x-1, y);
				break;
			case MOVERIGHT:
				targetTile = map.getTile(x+1, y);
				break;
			case MOVEUP:
				targetTile = map.getTile(x, y-1);
				break;
			case STAY:
				return true;
			default:
				return false;
		}
		if (targetTile.equals(SPU.Tile.WATER))
		{
			return false;
		}
		switch (SPU.Command.values()[direction])
		{
			case MOVEDOWN:
				y=y+1;
				break;
			case MOVELEFT:
				x=x-1;
				break;
			case MOVERIGHT:
				x=x+1;
				break;
			case MOVEUP:
				y=y-1;
				break;
			default:
				return false;
		}
		return true;
	}
}
