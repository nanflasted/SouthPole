package Server.Resource;

import java.util.*;

import Utility.SPU;

public class TownData implements java.io.Serializable{

	private int x;
	private int y;
	private String name;
	private ArrayList<ItemData> items;
	private ArrayList<DogeData>	doges;
	
	public TownData(String name)
	{
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
	
	public void spawn()
	{
		int spawnX = (int)(Math.random()*(SPU.DEFAULT_MAP_SIZE-2*SPU.WATER_BORDER_SIZE))+SPU.WATER_BORDER_SIZE;
		int spawnY = (int)(Math.sqrt((Math.pow((SPU.DEFAULT_MAP_SIZE/2-SPU.WATER_BORDER_SIZE),2)-Math.pow(spawnX, 2)))+0.5);
		x = spawnX;
		y = spawnY;
	}
}
