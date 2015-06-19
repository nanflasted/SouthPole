package Server.Resource;

import java.io.*;
import java.util.*;

import Utility.SPU.*;


@SuppressWarnings("serial")
public class MapOverlay implements Serializable {

	private Tile terrain;
	private ArrayList<String> users;
	private TownData town = null;
	
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
	
	public boolean addTown(TownData newTown)
	{
		if (town != null) return false;
		town = newTown;
		return true;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		out.writeObject(terrain);
		out.writeObject(town);
		out.writeObject(users);		
	}
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		terrain = (Tile)in.readObject();
		town = (TownData)in.readObject();
		users = (ArrayList<String>)in.readObject();
	}
	
	public TownData getTown()
	{
		return town;
	}
}
