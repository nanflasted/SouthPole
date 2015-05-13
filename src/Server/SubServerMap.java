package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import Utility.SPU;
import Utility.SPU.*;

public class SubServerMap implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7587568064925349058L;
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
	
	public boolean move(String un, int direction)
	{
		/*
		 * TODO
		 * 1. store users on this map
		 * 2. move user un
		 */
		return true;
	}
	
}
