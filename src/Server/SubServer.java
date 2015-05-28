package Server;

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
		server = new ServerSocket(port);
		while (true)
		{	
			try
			{				
				client = server.accept();
				new ClientHandler(client, dbpool, mapMgr).start();
				client.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}