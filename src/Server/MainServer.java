package Server;

import java.sql.*;
import java.net.*;
import java.io.*;

import Utility.*;
import Utility.Management.*;


public class MainServer
{
	private DBConnectionPool dbpool;
	private DBConnection dbc;
	private ResultSet rsset;
	private ServerSocket listener;
	private Socket client;

	public MainServer(int sp, int ep, String hs, DBConnectionPool inputDBPool, ServerManager mgrref) throws Exception
	{
		try
		{
			if (sp <= 1337)
			{
				throw new Exception("Reserved Port Numbers");
			}
			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		dbpool = inputDBPool;
		for (int i = sp; i <= ep; i++)
		{
			SubServer toStart = new SubServer(i,hs,dbpool);
			mgrref.addSubServer(toStart);
			toStart.start();
		}
		listener = new ServerSocket(1337);
		while (true)
		{
			client = listener.accept();
			ObjectInputStream in = new ObjectInputStream(client.getInputStream());
			int handshake = in.readInt();
			if (!SPU.verifyHandshake(hs,handshake))
			{
				in.close();
				client.close();
				continue;
			}
			String un = (String)in.readObject();
			ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			dbc = dbpool.getConnection();
			rsset = dbc.executeQuery("SELECT * FROM redir WHERE username = '" + un + "'");
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
	
	/*
	public static void main(String[] args)
	{
		
	}
	*/
}