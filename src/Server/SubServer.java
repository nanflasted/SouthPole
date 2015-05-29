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
	private MapData map;
	private MapManager mapMgr;
	
	public SubServer(int portNumber, DBConnectionPool dbpool, MapManager mgr)
	{
		port = portNumber;
		this.dbpool = dbpool;
		this.mapMgr = mgr;
		map = mapMgr.load(portNumber);
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
		while (true)
		{	
			try
			{				
				client = server.accept();
				new ClientHandler(this, client).start();
				client.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public MapManager getMapMgr()
	{
		return mapMgr;
	}
	
	public DBConnectionPool getDBPool()
	{
		return dbpool;
	}
	
	public int getPort()
	{
		return port;
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
}