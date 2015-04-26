package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import Utility.SPU;
import Utility.SPU.*;

public class SubServerMap implements java.io.Serializable {
	private Tile[][] map;
	private int size;
	
	
	public SubServerMap(int size)
	{
		this.size = size;
		map = SPU.generateWorld(size);
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
