package Server;

import java.net.*;
import java.io.*;
import java.util.*;

import Server.Resource.*;
import Utility.SPU;
import Utility.SPU.*;

public class SubServerMap implements java.io.Serializable {
	
	private static final long serialVersionUID = -7587568064925349058L;
	private Tile[][] map;
	private int size;
	private ArrayList<String>[][] mapOverlay; 
	private ArrayList<TownData> towns = new ArrayList<TownData>();
	
	public SubServerMap(int size) throws Exception
	{
		this.size = size;
		mapOverlay = new ArrayList[size][size];
		map = generateWorld(size);
		spawnTowns();
	}
	
	
	public int size()
	{
		return size;
	}
	
	public Tile getTile(int x, int y)
	{
		return map[x][y];
	}
	
    public void spawnTowns() throws Exception
	{
    	int i = 0;
    	ArrayList<String> names = SPU.generateName();
    	while (i<(SPU.DEFAULT_MAP_SIZE/10))
    	{
    		int spawnX = (int)(Math.random()*(SPU.DEFAULT_MAP_SIZE-2*SPU.WATER_BORDER_SIZE))+SPU.WATER_BORDER_SIZE;
    		int spawnY = (int)(Math.sqrt((Math.pow((SPU.DEFAULT_MAP_SIZE/2-SPU.WATER_BORDER_SIZE),2)-Math.pow(spawnX, 2)))+0.5);
    		if(map[spawnX][spawnY] != SPU.Tile.TOWN)
    		{
    			map[spawnX][spawnY] = SPU.Tile.TOWN;
    			int k = (int)(Math.random()*names.size());
    			String name = names.get(k);
    			names.remove(k);
    			towns.add(new TownData(spawnX,spawnY,name));
    			i++;
    			mapOverlay[spawnX][spawnY] = ad2Overlay("T",towns.get(towns.size()-1).getName(),mapOverlay[spawnX][spawnY]);
    		}
    	}
	}
	
    
    
    //==================================user stuff===============================
    
    public void spawnUser(String un, int x, int y)
    {
    	mapOverlay[x][y] = ad2Overlay("U",un,mapOverlay[x][y]);
    }
    
    public boolean move(String un, int x, int y, int direction)
	{
		mapOverlay[x][y] = removeFromOverlay("U",un,mapOverlay[x][y]);
		int targetX = SPU.moveX(x,direction);
		int targetY = SPU.moveY(y,direction);
		mapOverlay[targetX][targetY] = ad2Overlay("U",un,mapOverlay[targetX][targetY]);
		return true;
	}
    
    
    
    
    
    //=====================overlay stuff============================================
    
    
    
    public int containedInOverlay(String type, String s, ArrayList<String> target)
    {
    	int k = 0;
    	for(String i:target)
    	{
    		if ((i.equals(s))&&(target.get(0).substring(k, k).equals(type)))
    		{
    			return k;
    		}
    	}
    	return -1;
    }
    
    public ArrayList<String> ad2Overlay(String type, String s, ArrayList<String> target) //add to overlay
    {
    	ArrayList<String> temp = target;
    	temp.add(s);
    	temp.set(0,temp.get(0)+type);
    	return temp;
    }
    
    public ArrayList<String> removeFromOverlay(String type, String s, ArrayList<String> target)
    {
    	ArrayList<String> temp = target;
    	int i = containedInOverlay(type,s,target);
    	if (i==-1)
    	{
    		System.err.println("Overlay error: trying to remove nonexistant overlay");
    		return temp;
    	}
    	String control = temp.get(0);
    	temp.set(0,control.substring(0, i-1)+control.substring(i+1, control.length()));
    	temp.remove(i);
    	return temp;
    }
    
	
	
}
