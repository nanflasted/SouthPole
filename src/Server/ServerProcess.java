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
	
	public static int login(String un, String pw, int portNumber) throws Exception
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
			 s.executeUpdate("INSERT INTO userinfo (username, password, userclass) "
			 		+ "VALUES ('" + un + "', '" + pw + "', '" + un + ".info');");
			 FileOutputStream fileout = new FileOutputStream("data/info/userclass/"+un+".info");
			 ObjectOutputStream infowrite = new ObjectOutputStream(fileout);
			 UserData newuser = new UserData(un,portNumber);
			 newuser.spawn();
			 infowrite.writeObject(newuser);
			 infowrite.close();
			 if (rs!=null)
			 {
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
			 System.out.println("User " + un + "signed up at server " + new Integer(portNumber).toString());
			 return SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal();	
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
