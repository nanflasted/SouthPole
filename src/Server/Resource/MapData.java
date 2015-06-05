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
	
	public void setOverlay(MapOverlay[][] overlay)
	{
		this.overlay = overlay;
	}
	
	public Tile getTile(int x, int y)
	{
		return overlay[x][y].getTile();
	}
}
