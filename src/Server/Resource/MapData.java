package Server.Resource;

import Utility.SPU.*;
import Utility.Management.*;

public class MapData {
	private MapOverlay[][] overlay;
	private int size;
	
	public MapData(int size)
	{
		this.size= size;
	}
	
	public void setOverlay(MapOverlay newOverlay, int x, int y)
	{
		overlay[x][y] = newOverlay;
	}
	
	public MapOverlay getOverlay(int x, int y)
	{
		return overlay[x][y];
	}
	
	public void setWorld(MapOverlay[][] world)
	{
		this.overlay = world;
	}
	
	public Tile getTile(int x, int y)
	{
		return overlay[x][y].getTile();
	}
}
