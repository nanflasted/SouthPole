package Utility.Management;

import java.sql.*;
import java.util.ArrayList;
import java.io.*;

import Server.Resource.*;
import Utility.SPU;
import Utility.SPU.Tile;

public class MapManager {

	private DBConnectionPool dbpool;
	public MapManager(DBConnectionPool dbpool)
	{
		this.dbpool = dbpool;
	}
	
	public MapData load(int server)
	{
		DBConnection dbc;
		try
		{
			dbc = dbpool.getConnection();
			PreparedStatement pst = dbc.getPS("SELECT map FROM serverdata WHERE portNumber = " + new Integer(server).toString()+";");
			ResultSet rsset = pst.executeQuery();
			MapData serverMap = (MapData)((new ObjectInputStream(rsset.getBinaryStream("map")).readObject()));
			rsset.close();
			pst.close();
			dbpool.freeConnection(dbc);
			return serverMap;
		}
		catch(Exception e)
		{
			System.err.println("Map loading failure!");
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}
	
	public void spawnUser(MapData map, UserData user)
	{
		map.getOverlay(user.getX(),user.getY()).addUser(user);
	}
	
	public void moveUser(MapData map, UserData user, int dir)
	{
		try
		{
			boolean flag = map.getOverlay(user.getX(), user.getY()).removeUser(user);
			if (!flag) {throw new Exception("Fatal: map and user out of sync");}
			map.getOverlay(SPU.moveX(user.getX(), dir), SPU.moveY(user.getY(), dir)).addUser(user);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public static void generateWorld(MapData map, int size) {

        MapOverlay[][] world = new MapOverlay[size][size];

        double distFromCenterToOcean = ((size / 2.0) - SPU.WATER_BORDER_SIZE);

        //generate the world.
        for (int x = 0; x != size; x++) {
            //System.out.println("");
            for (int y = 0; y != size; y++) {
                //100 % light snow at border, 100% heavy snow at center.
                double distFromCenter = Math.sqrt(Math.pow((x - (size/2)), 2) + Math.pow((y - (size/2)), 2));
                if (distFromCenter >= distFromCenterToOcean)
                    world[x][y] = new MapOverlay(Tile.WATER);
                else if ((distFromCenter / distFromCenterToOcean) > Math.random())
                    world[x][y] = new MapOverlay(Tile.SNOW_LIGHT);
                else
                    world[x][y] = new MapOverlay(Tile.SNOW_HEAVY);
                //add a mountain every once in a while
                if (Math.random() < .02 && !(world[x][y].getTile().equals(Tile.WATER))) {
                    world[x][y].changeTerrain(Tile.MOUNTAIN);
                }
               
            }
        }
        map = new MapData(size);
        map.setWorld(world);
    }
}
