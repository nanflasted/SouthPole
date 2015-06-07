package Server;

import java.sql.*;
import java.net.*;
import java.io.*;

import Utility.*;
import Utility.Management.*;


public class MainServer extends Thread
{
	
	private boolean running;
	
	private DBConnectionPool dbpool;
	private DBConnection dbc;
	private ResultSet rsset;
	private ServerSocket listener;
	private Socket client;
	private int sp,ep;
	private String hs;
	private DBConnectionPool inputDBPool;
	private ServerManager mgrref;
	
	public MainServer(int sp, int ep, String hs, DBConnectionPool inputDBPool, ServerManager mgrref)
	{
		this.sp = sp;
		this.ep = ep;
		this.hs = hs;
		this.inputDBPool = inputDBPool;
		this.mgrref = mgrref;
	}
	
	public void run()
	{
		running = true;
		try
		{
			if (sp <= 1337)
			{
				throw new Exception("Reserved Port Numbers");
			}
			dbpool = inputDBPool;
			for (int i = sp; i <= ep; i++)
			{
				SubServer toStart = new SubServer(i,hs,dbpool);
				mgrref.addSubServer(toStart);
				toStart.start();
			}
			listener = new ServerSocket(1337);
			System.out.println("Main Server Awaiting at 1337");
			while (running)
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
					out.writeInt((int)(Math.random()*(ep-sp))+sp);
				}
				in.close();
				out.close();
				client.close();
				dbpool.freeConnection(dbc);
			}
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void forceStop()
	{
		running = false;
		try
		{
			listener.close();
			if (client!= null) client.close();
			dbpool.shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/*
	public static void main(String[] args)
	{
		
	}
	*/
}