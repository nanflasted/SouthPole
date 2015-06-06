package Server.Resource;

import java.util.*;
import Utility.SPU.*;


public class MapOverlay {

	private Tile terrain;
	private ArrayList<String> users;
	
	public MapOverlay(Tile ground)
	{
		terrain = ground;
		users = new ArrayList<String>();
	}
	
	public void changeTerrain(Tile newTerrain)
	{
		terrain = newTerrain;
	}
	
	public Tile getTile()
	{
		return terrain;
	}
	
	public ArrayList<String> getUserList()
	{
		return users;
	}
	
	public void addUser(UserData user)
	{
		users.add(user.getName());
	}
	
	public boolean removeUser(UserData user)
	{
		return users.remove(user.getName());
	}
	
	
}
