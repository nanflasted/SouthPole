package Utility.Management;

import java.sql.*;
import java.io.*;

import Server.Resource.*;
import Utility.SPU;
import Utility.SPU.Tile;

public class MapManager {
		
	public static MapData load(int server, DBConnectionPool dbpool)
	{
		DBConnection dbc;
		try
		{
			dbc = dbpool.getConnection();
			PreparedStatement pst = dbc.getPS("SELECT map FROM serverdata WHERE portNumber = " + new Integer(server).toString()+";");
			ResultSet rsset = pst.executeQuery();
			if(!rsset.next()) throw new Exception("Fatal: Map not found!");
			byte[] mapByte = rsset.getBytes("map");
			MapData serverMap = (MapData)(new ObjectInputStream(new ByteArrayInputStream(mapByte)).readObject());
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
	
	public static void create(MapData data, int server, DBConnectionPool dbpool)
	{
		try
		{
			DBConnection dbc = dbpool.getConnection();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			ObjectOutputStream tmp = new ObjectOutputStream(baos);
			tmp.writeObject(data);
			//tmp.flush();
			tmp.close();
			byte[] temp = baos.toByteArray();
			PreparedStatement pst = dbc.getPS("INSERT INTO serverdata VALUES (?,?);");
			pst.setInt(1, server);
			pst.setBinaryStream(2, new ByteArrayInputStream(temp));
			pst.executeUpdate();
			pst.close();
			dbpool.freeConnection(dbc);
		}
		catch(Exception e)
		{
			System.err.println("Map saving failure!");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void save(MapData data, int server, DBConnectionPool dbpool)
	{
		try
		{
			DBConnection dbc = dbpool.getConnection();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
			ObjectOutputStream tmp = new ObjectOutputStream(baos);
			tmp.writeObject(data);
			tmp.close();
			byte[] temp = baos.toByteArray();
			PreparedStatement pst = dbc.getPS("UPDATE serverdata SET map = ? WHERE portNumber = ?");
			pst.setBinaryStream(1, new ByteArrayInputStream(temp));
			pst.setInt(2, server);
			pst.executeUpdate();
			pst.close();
			dbpool.freeConnection(dbc);
		}
		catch(Exception e)
		{
			System.err.println("Map saving failure!");
			e.printStackTrace();
			System.exit(1);
		}
	}
	public static void spawnUser(MapData map, UserData user)
	{
		map.getOverlay(user.getX(),user.getY()).addUser(user);
	}
	
	public static void moveUser(MapData map, UserData user, int dir)
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
        map.setWorld(world);
    }
}
