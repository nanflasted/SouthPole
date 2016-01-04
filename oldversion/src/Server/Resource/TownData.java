package Server.Resource;

import java.util.*;
import java.io.*;

import Utility.SPU;

@SuppressWarnings("serial")
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
	
	public void generateResources()
	{
		items = new ArrayList<ItemData>();
		doges = new ArrayList<DogeData>();
		for (int i = 0; i < SPU.INITRS; i++)
		{
			items.add(new ItemData());
			for (int j = 0; j < 3; j++)
				doges.add(new DogeData());
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
		out.writeObject(items);
		out.writeObject(doges);
	}
	
	//we all know what's being read
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
	{
		in.defaultReadObject();
		items = (ArrayList<ItemData>)in.readObject();
		doges = (ArrayList<DogeData>)in.readObject();
	}
	public void spawn()
	{
		int spawnX = (int)(Math.random()*(SPU.DEFAULT_MAP_SIZE-2*SPU.WATER_BORDER_SIZE))+SPU.WATER_BORDER_SIZE;
		int spawnY = (int)(Math.sqrt((Math.pow((SPU.DEFAULT_MAP_SIZE/2-SPU.WATER_BORDER_SIZE),2)-Math.pow(spawnX, 2)))+0.5);
		x = spawnX;
		y = spawnY;
	}
	
	public boolean processPurchase(Purchasable target)
	{
		for (Purchasable t : items)
		{
			if (t.equals(target)){
				return items.remove(target);
			}
		}
		for (Purchasable t : doges)
		{
			if (t.equals(target))
			{
				return items.remove(target);
			}
		}
		return false;
	}
}
