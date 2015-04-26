package Server;


import java.io.*;
import java.util.*;
import java.sql.*;

import Utility.SouthPoleUtil;


public class ServerProcess {
	
	private static Connection getDB(int server) throws Exception
	{
		Class.forName("org.sqlite.JDBC");
		return DriverManager.getConnection("jdbc.sqlite:"+"..\\data\\"+new Integer(server).toString()+".db");
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
			 rs = s.executeQuery("SELECT username FROM USERS ORDER BY username ASC");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return SouthPoleUtil.ServerResponse.LOGIN_FAIL.ordinal();
		}
		if (rs!=null)
		{
			
		}
		return -1;
	}
	
	public static int signup(String un, String pw, int portNumber)
	{
		return 3;
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
			System.out.println("Database File Created at"+dbname);
			stmt = c.createStatement();
			stmt.executeUpdate("CREATE DATABASE USERS");
			System.out.println("User Database Created");
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
