package Utility.Management;

import java.sql.*;
import java.io.*;
import Server.Resource.*;

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
}
