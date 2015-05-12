package Server;


import java.io.*;
import java.util.*;
import java.sql.*;

import Utility.SPU;


public class ServerProcess {
	
	private static Connection getDB(int server) throws Exception
	{
		Class.forName("org.sqlite.JDBC");
		return DriverManager.getConnection("jdbc:sqlite:"+"data/info/"+new Integer(server).toString()+".db");
	}
	
	public static synchronized int login(String un, String pw, int portNumber) throws Exception
	{
		Connection c=null;
		Statement s=null;
		ResultSet rs=null;
		try
		{
			 c = getDB(portNumber);
			 s = c.createStatement();
			 rs = s.executeQuery("SELECT * FROM userinfo WHERE username = '"+un + "';");
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
					System.out.println("User " + un + "logged in at server " + new Integer(portNumber).toString());
					return SPU.ServerResponse.LOGIN_OK.ordinal();
				}
			}
			rs.close();
		}
		if (s!=null)
		{
			s.close();
		}
		if (c!=null)
		{
			c.close();
		}
		return SPU.ServerResponse.LOGIN_FAIL.ordinal();
	}
	
	public static void logout(String un, int portNumber, UserData data) throws IOException
	{
		ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream("/data/info/userclass/"+un+".info"));
		writer.writeObject(data);
		writer.close();
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
			 rs = s.executeQuery("SELECT * FROM userinfo WHERE username = '"+un + "';");
			 if (rs.next())
			 {
				 return SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
			 }
			 rs.close();
			 s.close();
			 c.close();
			 c = getDB(portNumber);
			 s = c.createStatement();
			 s.executeUpdate("INSERT INTO userinfo (username, password, userclass) "
			 		+ "VALUES ('" + un + "', '" + pw + "', '" + un + ".info');");
			 FileOutputStream fileout = new FileOutputStream("data/info/userclass/"+un+".info");
			 ObjectOutputStream infowrite = new ObjectOutputStream(fileout);
			 UserData newuser = new UserData(un,portNumber);
			 newuser.spawn();
			 infowrite.writeObject(newuser);
			 infowrite.close();
			 s.close();
			 c.close();
			 System.out.println("User " + un + "signed up at server " + new Integer(portNumber).toString());
			 return SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal();	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
		}
		finally
		{
			try
			{
				rs.close();
				s.close();
				c.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static int[][] getCond(String un, SubServerMap map, UserData data)
	{
		return move(un,map,SPU.Command.STAY.ordinal(),data);
	}
	
	public static int[][] move(String un, SubServerMap map, int direction, UserData data)
	{
		if (!map.move(un,direction))
		{
			return null;
		}
		if (!data.move(map,direction))
		{
			return null;
		}
		int v = data.getVisibility();
		int [][] out = new int[v][v];
		for (int i = data.getX()-v; i <= data.getX()+v; i++)
		{
			for (int j = data.getY()-v; j <= data.getY()+v; j++)
			{
				out[i][j] = map.getTile(i,j).ordinal();
			}
		}
		return out;
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
