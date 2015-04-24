package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import Utility.SouthPoleUtil;
import Utility.SouthPoleUtil.*;

public class SubServerMap implements java.io.Serializable {
	private Tile[][] map;
	private int size;
	
	
	public SubServerMap(int size)
	{
		this.size = size;
		map = SouthPoleUtil.generateWorld(size);
	}
	
	public int size()
	{
		return size;
	}
	
	public Tile getTile(int x, int y)
	{
		return map[x][y];
	}
	
}
