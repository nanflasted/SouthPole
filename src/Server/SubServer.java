package Server;

import java.io.IOException;
import java.net.*;
import java.util.*;

import Utility.Management.*;
import Server.Resource.*;


public class SubServer extends Thread
{
	private int port;
	private DBConnectionPool dbpool;
	private Socket client;
	private ServerSocket server;
	private ArrayList<String> onlineUsers = new ArrayList<String>();
	private ArrayList<UserData> onlineUserData = new ArrayList<UserData>();
	private ArrayList<ClientHandler> handlers = new ArrayList<ClientHandler>();
	private MapData map;
	private String hs;
	
	public SubServer(int portNumber, String hs, DBConnectionPool dbpool)
	{
		port = portNumber;
		this.hs = hs;
		this.dbpool = dbpool;
		map = MapManager.load(portNumber,dbpool);
	}
	
	public void run()
	{
		try
		{
			server = new ServerSocket(port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("Subserver started at " + new Integer(port).toString());
		while (true)
		{	
			try
			{				
				client = server.accept();
				ClientHandler ch = new ClientHandler(this, client);
				addHandler(ch);
				ch.start();
				client.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public DBConnectionPool getDBPool()
	{
		return dbpool;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void addHandler(ClientHandler ch)
	{
		handlers.add(ch);
	}
	
	public void removeHandler(ClientHandler ch)
	{
		handlers.remove(ch);
	}
	
	public synchronized UserData getUserData(String un)
	{
		return onlineUserData.get(onlineUsers.indexOf(un));
	}
	
	public synchronized boolean isOnline(String un)
	{
		return onlineUsers.contains(un);
	}
	
	public synchronized void userLogin(UserData data)
	{
		onlineUserData.add(data);
		onlineUsers.add(data.getName());
	}
	
	public synchronized void userLogoff(String un)
	{
		int i = onlineUsers.indexOf(un);
		onlineUsers.remove(i);
		onlineUserData.remove(i);
	}
	
	public synchronized MapData getMap()
	{
		return map;
	}
	
	public String getHandShake()
	{
		return hs;
	}
	
	public void forceStop()
	{
		for (ClientHandler ch : handlers)
		{
			ch.forceStop();
		}
		MapManager.save(map, port, dbpool);
		this.interrupt();
	}
}