package Server;


import java.io.*;
import java.util.*;
import java.sql.*;

import Utility.SPU;


public class ServerProcess {
	
	private static Connection getDB(int server) throws Exception
	{
		Class.forName("org.sqlite.JDBC");
		return DriverManager.getConnection("jdbc.sqlite:"+"data/info/"+new Integer(server).toString()+".db");
	}
	
	public static int login(String un, String pw, int portNumber) throws Exception
	{
		Connection c=null;
		Statement s=null;
		ResultSet rs=null;
		try
		{
			 c = getDB(portNumber);
			 s = c.createStatement();
			 rs = s.executeQuery("SELECT * FROM usersinfo WHERE username = '"+un + "';");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return SPU.ServerResponse.LOGIN_FAIL.ordinal();
		}
		if (rs!=null)
		{
			while (rs.next())
			{
				if (rs.getString("password").equals(pw))
				{
					return SPU.ServerResponse.LOGIN_OK.ordinal();
				}
			}
			rs.close();
		}
		if (c!=null)
		{
			c.close();
		}
		if (s!=null)
		{
			s.close();
		}
		return SPU.ServerResponse.LOGIN_FAIL.ordinal();
	}
	
	public static synchronized int signup(String un, String pw, int portNumber)
	{
		Connection c=null;
		Statement s=null;
		ResultSet rs=null;
		try
		{
			 c = getDB(portNumber);
			 s = c.createStatement();
			 rs = s.executeQuery("SELECT * FROM usersinfo WHERE username = '"+un + "';");
			 if (rs.next())
			 {
				 return SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
			 }
			 s.executeUpdate("INSERT INTO userinfo (username, password, userclass) "
			 		+ "VALUES ('" + un + "', '" + pw + "', '" + un + ".info');");
			 ObjectOutputStream infowrite = new ObjectOutputStream(new FileOutputStream("data/userclass/"+un+".info"));
			 infowrite.writeObject(new UserData(un,portNumber));
			 infowrite.close();
			 if (c!=null)
			 {
				 c.close();
			 }
			 if (s!=null)
			 {
				 s.close();
			 }
			 if (rs!=null)
			 {
				 rs.close();
			 }
			 return SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal();	
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			return SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
		}	
	}
	
	public static int[][] getCond(String un, SubServerMap map)
	{
		int [][] out = new int[5][5];
		for (int i = 0; i < 5; i++)
		{
			for (int j = 0; j < 5; j++)
			{
				out[i][j] = map.getTile(i,j).ordinal();
			}
		}
		return out;
	}
	
	public static int[][] move(String un, SubServerMap map, int direction)
	{
		return getCond(un, map);
	}
	
	public static void createDB(String dbname)
	{
		Connection c = null;
		Statement stmt = null;
		try
		{
			System.out.println("Initializing user database" + dbname);
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+dbname);
			System.out.println("Database File Created at "+dbname);
			stmt = c.createStatement();
			System.out.println("User Database Created");
			stmt.executeUpdate("CREATE TABLE userinfo ( username TEXT PRIMARY KEY, password TEXT, userclass TEXT);");
			System.out.println("User info Table created");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				stmt.close();
				c.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}
