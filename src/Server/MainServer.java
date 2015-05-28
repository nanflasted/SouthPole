package Server;

import java.sql.*;
import java.util.*;
import java.net.*;
import java.io.*;

import Utility.*;
import Utility.DatabaseManagement.*;

public class MainServer
{
	private DBConnectionPool dbpool;
	private DBConnection dbc;
	private ResultSet rsset;
	private ServerSocket listener;
	private Socket client;
	
	public MainServer(int sp, int ep) throws Exception
	{
		dbpool = new DBConnectionPool();
		for (int i = sp; i <= ep; i++)
		{
			new SubServer(i,dbpool).start();
		}
		listener = new ServerSocket(1337);
		while (true)
		{
			client = listener.accept();
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			String un = (String)in.readObject();
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			dbc = dbpool.getConnection();
			rsset = dbc.executeQuery("SELECT server FROM redir WHERE username = '" + un + "'");
			if (rsset.next())
			{
				out.writeInt(rsset.getInt("server"));
			}
			else
			{
				out.writeInt(-1);
			}
			in.close();
			out.close();
			client.close();
			dbpool.freeConnection(dbc);
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			if (args.length!=2) 
			{
				throw new Exception("Wrong Number of Arguments: MainServer <Starting Port> <Ending Port>");
			}
			int sp = Integer.parseInt(args[0]);
			if (sp <= 1337)
			{
				throw new Exception("Reserved Port Number");
			}
			int ep = Integer.parseInt(args[1]);
			
			@SuppressWarnings("unused")
			MainServer ms = new MainServer(sp,ep);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}