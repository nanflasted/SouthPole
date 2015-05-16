package Server;

import java.net.*;
import java.io.*;
import java.util.*;

import Utility.SPU;
import Utility.SPU.*;

public class SubServerMap implements java.io.Serializable {
	
	private static final long serialVersionUID = -7587568064925349058L;
	private Tile[][] map;
	private int size;
	private ArrayList<Object>[][] mapOverlay; 
	
	
	public SubServerMap(int size)
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
            	mapOverlay[x][y] = new ArrayList<Object>();
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
                mapOverlay[x][y].set(0, "T");
                /*
                 * overlay rules:
                 * mapOverlay[?][?].get(0) is always a string that represents what follows:
                 * 'T' means a tile
                 * 'C' means a town/city
                 * 'U' means a user
                 * " " means an empty space in the storage
                 * and to add more
                 * then details are stored in -.get(1~inf)
                 * e.g.: 'TUCUUU' means there are a terrain, a city, and 4 users on this tile,
                 * the storage sequence is as represented.
                 */
                mapOverlay[x][y].add(world[x][y]);
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
	
    public void spawnTowns()
	{
    	int i = 0;
    	while (i<(SPU.DEFAULT_MAP_SIZE/10))
    	{
    		int spawnX = (int)(Math.random()*(SPU.DEFAULT_MAP_SIZE-2*SPU.WATER_BORDER_SIZE))+SPU.WATER_BORDER_SIZE;
    		int spawnY = (int)(Math.sqrt((Math.pow((SPU.DEFAULT_MAP_SIZE/2-SPU.WATER_BORDER_SIZE),2)-Math.pow(spawnX, 2)))+0.5);
    		if(map[spawnX][spawnY] != SPU.Tile.TOWN)
    		{
    			map[spawnX][spawnY] = SPU.Tile.TOWN;
    			i++;
    			mapOverlay[spawnX][spawnY].set(1, SPU.Tile.TOWN);
    		}
    	}
	}
	
    public void spawnUser(String un, int x, int y)
    {
    	mapOverlay[x][y].set(0,((String)mapOverlay[x][y].get(0))+"U");
    	mapOverlay[x][y].add(un);
    }
    
    
    public ArrayList<Object> ad2Overlay(String type, Object o, ArrayList<Object> target) //add to overlay
    {
    	ArrayList<Object> temp = target;
    	temp.add(o);
    	temp.set(0,(String)temp.get(0)+type);
    	return temp;
    }
    
    public ArrayList<Object> removeFromOverlay(Object o, ArrayList<Object> target)
    {
    	ArrayList<Object> temp = target;
    	int i = 0;
    	for (Object obj : temp)
    	{
    		if (obj.equals(o))
    		{
    			temp.remove(i);
    			String control = (String)temp.get(0);
    			temp.set(0, control.substring(0, i-1)+control.substring(i+1, control.length()));
    			break;
    		}
    		i++;
    	}
    	return temp;
    }
    
	public boolean move(String un, int x, int y, int direction)
	{
		mapOverlay[x][y] = removeFromOverlay(un,mapOverlay[x][y]);
		int targetX = SPU.moveX(x,direction);
		int targetY = SPU.moveY(y,direction);
		mapOverlay[targetX][targetY] = ad2Overlay("U",un,mapOverlay[targetX][targetY]);
		return true;
	}
	
}
