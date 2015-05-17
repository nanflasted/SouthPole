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
	
	public Tile[][] generateWorld(int size) {

        Tile[][] world = new Tile[size][size];

        double distFromCenterToOcean = ((size / 2.0) - SPU.WATER_BORDER_SIZE);

        //generate the world.
        for (int x = 0; x != size; x++) {
            //System.out.println("");
            for (int y = 0; y != size; y++) {
                //100 % light snow at border, 100% heavy snow at center.
            	mapOverlay[x][y] = new ArrayList<String>();
            	mapOverlay[x][y].add("");
                double distFromCenter = Math.sqrt(Math.pow((x - (size/2)), 2) + Math.pow((y - (size/2)), 2));
                if (distFromCenter >= distFromCenterToOcean)
                    world[x][y] = Tile.WATER;
                else if ((distFromCenter / distFromCenterToOcean) > Math.random())
                    world[x][y] = Tile.SNOW_LIGHT;
                else
                    world[x][y] = Tile.SNOW_HEAVY;
                //add a mountain every once in a while
                if (Math.random() < .02 && !(world[x][y] == Tile.WATER)) {
                    world[x][y] = Tile.MOUNTAIN;
                }
                /*
                 * overlay rules:
                 * mapOverlay[?][?].get(0) is always a string that represents what follows:
                 * 'T' means a town/city
                 * 'G' means the terrain of current tile
                 * 'U' means a user
                 * " " means an empty space in the storage
                 * and more to be added
                 * then details are stored in -.get(1~inf)
                 * e.g.: 'UTUUUU' means there are a city, and 5 users on this tile,
                 * the storage sequence is as represented.
                 */
                mapOverlay[x][y] = ad2Overlay("G",world[x][y].name(),mapOverlay[x][y]);
            }
        }

        return world;
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
