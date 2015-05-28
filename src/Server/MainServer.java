package Server;

import java.sql.*;
import java.util.*;
import java.net.*;
import java.io.*;

public class MainServer
{
	private Connection dbc;
	private Statement stmt;
	private ResultSet rsset;
	private ServerSocket listener;
	private Socket client;
	
	public MainServer(int sp, int ep) throws Exception
	{
		for (int i = sp; i <= ep; i++)
		{
			new SubServer(i).start();
		}
		listener = new ServerSocket(1337);
		while (true)
		{
			client = listener.accept();
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			String un = (String)in.readObject();
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			
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