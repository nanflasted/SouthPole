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
	
	private UserData data;
	private class DCTask extends TimerTask
	{
		private ClientHandler handler;
		protected DCTask(ClientHandler handler)
		{
			this.handler = handler;
		}
		
		public void run()
		{
			try
			{
				if (handler.data!=null)
				{
					handler.logout();
				}
				else
				{
					if (in!=null) {in.close();}
					if (out!=null) {out.close();}
					if ((client!=null)&&(!client.isClosed())) {client.close();}
				}
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
		autoDC = new DCTask(this);
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
			while ((state = in.readInt())!=SPU.Command.LOGOUT.ordinal())
			{
				process(state);
			}
			process(state);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void process(int state)
	{
		autoDC.cancel();
		autoDC = new DCTask(this);
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
			move(SPU.Command.STAY.ordinal());
			break;
		case MOVEUP:
			move(SPU.Command.MOVEUP.ordinal());
			break;
		case MOVEDOWN:
			move(SPU.Command.MOVEDOWN.ordinal());
			break;
		case MOVELEFT:
			move(SPU.Command.MOVELEFT.ordinal());
			break;
		case MOVERIGHT:
			move(SPU.Command.MOVERIGHT.ordinal());
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
			data = (UserData)new ObjectInputStream(rsset.getBinaryStream("UserData")).readObject();
			server.userLogin(data);
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
			
			ObjectOutputStream temp = new ObjectOutputStream(new FileOutputStream("temp.ser"));
			temp.writeObject(thisUser);
			temp.flush();
			temp.close();
			
			conn = dbpool.getConnection();
			PreparedStatement pst = conn.getPS("INSERT INTO "+new Integer(server.getPort()).toString()+" (username,password,UserData) VALUES (?,?,?,?);");
			pst.setString(1, thisUser.getName());
			pst.setString(2, pw);
			pst.setBinaryStream(3, new ObjectInputStream(new FileInputStream("temp.ser")));
			pst.executeUpdate();
			pst.close();
			dbpool.freeConnection(conn);
			
			out.writeInt(SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			out.writeInt(SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal());
		}
	}

	private void move(int dir)
	{
		MapManager mapmgr = server.getMapMgr();
		mapmgr.moveUser(data.getName(), dir);
		data.move(mapmgr.load(), dir);
		out.writeInt(data.getVisibility());
		out.writeObject(data.look());
		out.flush();
	}
	
	private void logout()
	{
		server.userLogoff(data.getName());
		dbpool = server.getDBPool();
		try
		{
			ObjectOutputStream temp = new ObjectOutputStream(new FileOutputStream("temp.ser"));
			temp.writeObject(data);
			temp.flush();
			temp.close();
			
			DBConnection conn = dbpool.getConnection();
			PreparedStatement pst = conn.getPS("UPDATE "+new Integer(server.getPort()).toString()+" SET UserData = ? WHERE un = " + data.getName() + ";");
			pst.setBinaryStream(1, new ObjectInputStream(new FileInputStream("temp.ser")));
			pst.executeUpdate();
			pst.close();
			dbpool.freeConnection(conn);
			
			
			in.close();
			out.close();
			client.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
