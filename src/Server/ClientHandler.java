package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import Server.Resource.UserData;
import Utility.Management.*;
import Utility.*;


public class ClientHandler extends Thread{

	private Socket client=null;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private SubServer server;
	private DBConnectionPool dbpool;
	private int port;
	
	private Timer timer;
	private class DCTask extends TimerTask
	{
		private Socket client=null;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		protected DCTask(Socket client,ObjectInputStream in, ObjectOutputStream out)
		{
			this.client = client;
			this.in = in;
			this.out = out;
		}
		
		public void run()
		{
			try
			{
				if (in!=null) {in.close();}
				if (out!=null) {out.close();}
				if ((client!=null)&&(!client.isClosed())) {client.close();}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	private DCTask autoDC;
	
	
	public ClientHandler(SubServer server, Socket client) throws Exception
	{
		this.server = server;
		port = server.getPort();
		this.client = client;
		in = new ObjectInputStream(client.getInputStream());
		out = new ObjectOutputStream(client.getOutputStream());
		timer = new Timer();
		autoDC = new DCTask(client,in,out);
		timer.schedule(autoDC, SPU.TTL);
	}
	
	
	
	public void run()
	{
		try
		{
			dbpool = server.getDBPool();
			if(!SPU.verifyHandshake(in.readInt()))
			{
				in.close();
				out.close();
				client.close();
				return;
			}
			int state;
			while ((state = in.readInt())!=SPU.Command.DISCONNECT.ordinal())
			{
				process(state);
			}
			process(state);
			in.close();
			out.close();
			client.close();
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void process(int state)
	{
		autoDC.cancel();
		autoDC = new DCTask(client,in,out);
		timer.schedule(autoDC, SPU.TTL);
		switch (SPU.Command.values()[state])
		{
		case LOGIN:
			login();
			break;
		case SIGNUP:
			signup();
			break;
		case GETCOND:
			move(SPU.Command.STAY);
			break;
		case MOVEUP:
			move(SPU.Command.MOVEUP);
			break;
		case MOVEDOWN:
			move(SPU.Command.MOVEDOWN);
			break;
		case MOVELEFT:
			move(SPU.Command.MOVELEFT);
			break;
		case MOVERIGHT:
			move(SPU.Command.MOVERIGHT);
			break;		
		case LOGOUT:
			logout();
			break;
		default:
			return;
		}
	}
	
	private void login()
	{
		dbpool = server.getDBPool();
		try
		{
			DBConnection conn = dbpool.getConnection();
			String un = (String)in.readObject();
			ResultSet rsset = conn.executeQuery("SELECT * FROM " + new Integer(port).toString() + " WHERE username = " + un + ";");
			if (!rsset.next())
			{
				out.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				return;
			}
			if (server.isOnline(un))
			{
				out.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				return;
			}
			if (!(rsset.getString("password").equals((String)in.readObject())))
			{
				out.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				return;
			}
			rsset.next();
			UserData thisUser = (UserData)new ObjectInputStream(rsset.getBinaryStream("UserData")).readObject();
			server.userLogin(thisUser);
			rsset.close();
			dbpool.freeConnection(conn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void signup()
	{
		dbpool = server.getDBPool();
		try
		{
			DBConnection conn = dbpool.getConnection();
			String un = (String)in.readObject();
			ResultSet rsset = conn.executeQuery("SELECT * FROM "+new Integer(server.getPort()).toString()+" WHERE username = " + un + ";");
			if (rsset.next())
			{
				out.writeInt(SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal());
				return;
			}
			rsset.close();
			dbpool.freeConnection(conn);
			String pw = (String)in.readObject();
			UserData thisUser = new UserData(un,server.getPort());
			thisUser.spawn();
			MapManager mapMgr = server.getMapMgr();
			mapMgr.spawnUser(thisUser.getX(),thisUser.getY());
			conn = dbpool.getConnection();
			conn.executeUpdate("INSERT INTO ")
		}
	}

}
