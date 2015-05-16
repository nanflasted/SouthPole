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
		int targetX = SPU.moveX(x,direction);
		int targetY = SPU.moveY(y,direction);
		if ((targetX==-1)||(targetY==-1))
		{
			return false;
		}
		if (map.getTile(targetX, targetY).equals(SPU.Tile.WATER))
		{
			return false;
		}
		x = targetX;
		y = targetY;
		return true;
	}
}
