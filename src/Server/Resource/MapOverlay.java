package Server.Resource;

import Utility.SPU.*;

public class MapOverlay {

	private Tile terrain;
	
	public MapOverlay(Tile ground)
	{
		terrain = ground;
	}
	
	public void changeTerrain(Tile newTerrain)
	{
		terrain = newTerrain;
	}
	
	public Tile getTile()
	{
		return terrain;
	}
}
