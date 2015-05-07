package Server;

import java.util.*;

import Utility.SPU;

public class UserData implements java.io.Serializable{

	private String un;
	private int server;
	private int x;
	private int y;
	
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
	}
}
