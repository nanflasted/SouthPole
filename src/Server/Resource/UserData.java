package Server.Resource;

import java.util.*;
import java.sql.*;

import Server.SubServerMap;
import Server.Resource.*;
import Utility.SPU;

@SuppressWarnings("serial")
public class UserData implements java.io.Serializable{

	private String un;
	private int dailyMoves = SPU.DAILY_MOVES;
	private int server;
	private int x;
	private int y;
	private int visibility; //how many tiles can be seen by this user around himself on each side
	private double health;
	private ArrayList<ItemData> supplies = new ArrayList<ItemData>();
	
	private Connection conn;
	private Statement stmt;
	private ResultSet rset;
	
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
	
	public int getMoves()
	{
		return dailyMoves;
	}
	
	public boolean move(MapData map, int direction)
	{
		if (dailyMoves==0) return false;
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
		dailyMoves -= 1;
		return true;
	}

	public String getName() 
	{
		return un;
	}

	public SPU.Tile[][] look(MapData map) 
	{
		SPU.Tile[][] res = new SPU.Tile[visibility*2+1][visibility*2+1];
		for (int i = x-visibility;i<=x+visibility;i++)
		{
			for (int j = y-visibility;j<=y+visibility;j++)
			{
				res[i][j] = map.getTile(i, j);
			}
		}
		return res;
	}
}

