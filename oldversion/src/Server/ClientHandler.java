package Server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import Server.Resource.*;
import Utility.Management.*;
import Utility.*;


public class ClientHandler extends Thread{

	private Socket client=null;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	private SubServer server;
	private DBConnectionPool dbpool;

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
					out.writeInt(-1337);
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
			int clienths = in.readInt();
			if(!SPU.verifyHandshake(server.getHandShake(),clienths))
			{
				in.close();
				out.close();
				client.close();
				return;
			}
			int state;
			do
			{
				state = in.readInt();
				process(state);
			}
			while ((state!=SPU.Command.LOGOUT.ordinal())&&(state!=SPU.Command.SIGNUP.ordinal())&&(state!=SPU.Command.DISCONNECT.ordinal()));
			server.removeHandler(this);
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
		case REQUEST:
			request();
			break;
		case PURCHASE:
			purchaseFromTown();
			break;
		case LOGOUT:
			logout();
			break;
		case DISCONNECT:
			disconnect();
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
			PreparedStatement pst = conn.getPS("SELECT * FROM userdata WHERE username = ? AND server = ?;");
			pst.setString(1, un);
			pst.setInt(2, server.getPort());
			ResultSet rsset = pst.executeQuery();
			if (!rsset.next())
			{
				out.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				out.flush(); // insert lenny
				return;
			}
			if (server.isOnline(un))
			{
				out.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				out.flush();
				return;
			}
			if (!(rsset.getString("password").equals((String)in.readObject())))
			{
				out.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				out.flush();
				return;
			}
			byte[] userByte = rsset.getBytes("class");
			data = (UserData)(new ObjectInputStream(new ByteArrayInputStream(userByte)).readObject());
			server.userLogin(data);
			out.writeInt(SPU.ServerResponse.LOGIN_OK.ordinal());
			out.flush();
			rsset.close();
			pst.close();
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
			PreparedStatement pst = conn.getPS("SELECT * FROM userdata" +" WHERE username = ?;");
			pst.setString(1, un);
			ResultSet rsset = pst.executeQuery();
			if (rsset.next())
			{
				out.writeInt(SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal());
				out.flush();
				return;
			}
			rsset.close();
			pst.close();
			dbpool.freeConnection(conn);
			conn = dbpool.getConnection();
			pst = conn.getPS("INSERT INTO redir (username, server) VALUES (?,?);");
			pst.setString(1,un);
			pst.setInt(2, server.getPort());
			pst.executeUpdate();
			pst.close();
			dbpool.freeConnection(conn);
			String pw = (String)in.readObject();

			UserData thisUser = new UserData(un,server.getPort());
			thisUser.spawn();
			MapManager.spawnUser(server.getMap(),thisUser);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream temp = new ObjectOutputStream(baos);
			temp.writeObject(thisUser);
			temp.flush();
			byte[] userByte = baos.toByteArray();
			temp.close();
			baos.close();

			conn = dbpool.getConnection();
			pst = conn.getPS("INSERT INTO userdata" +" (username,password,class,server) VALUES (?,?,?,?);");
			pst.setString(1, thisUser.getName());
			pst.setString(2, pw);
			pst.setBinaryStream(3, new ByteArrayInputStream(userByte));
			pst.setInt(4, server.getPort());
			pst.executeUpdate();
			pst.close();
			dbpool.freeConnection(conn);

			out.writeInt(SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal());
			out.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				out.writeInt(SPU.ServerResponse.ACCOUNT_CREATE_FAIL.ordinal());
				out.flush();
			}
			catch(Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}

	private void move(int dir)
	{
		MapManager.moveUser(server.getMap(),data, dir);
		data.move(server.getMap(), dir);
		try
		{
			out.writeInt(data.getVisibility());
			out.writeObject(data.look(server.getMap()));
			out.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	//give back the information of a certain tile
	private void request()
	{
		int i, j;
		try
		{
			i = in.readInt();
			j = in.readInt();
			MapOverlay tileInfo = server.getMap().getOverlay(data.getX()+i, data.getY()+j);
			out.writeObject(tileInfo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void purchaseFromTown()
	{
		try
		{
			int i,j;
			i = in.readInt();
			j = in.readInt();
			Purchasable target = (Purchasable)in.readObject();
			MapOverlay overlay = server.getMap().getOverlay(data.getX()+i, data.getY()+j);
			if (!overlay.getTown().processPurchase(target)) {out.writeInt(SPU.ServerResponse.MOVE_FAIL.ordinal()); return;}
			if (!data.spend(target.getPrice())) {out.writeInt(SPU.ServerResponse.MOVE_FAIL.ordinal()); return;}
			out.writeInt(SPU.ServerResponse.MOVE_OK.ordinal());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void logout()
	{
		server.userLogoff(data.getName());
		dbpool = server.getDBPool();
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream temp = new ObjectOutputStream(baos);
			temp.writeObject(data);
			temp.flush();
			byte[] userByte = baos.toByteArray();
			temp.close();
			baos.close();

			DBConnection conn = dbpool.getConnection();
			PreparedStatement pst = conn.getPS("UPDATE "+new Integer(server.getPort()).toString()+" SET UserData = ? WHERE un = " + data.getName() + ";");
			pst.setBinaryStream(1, new ByteArrayInputStream(userByte));
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

	private void disconnect() //force disconnect
	{
		try
		{
			in.close();
			out.close();
			client.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void forceStop()
	{
		try {
			out.writeInt(-1337);
		} catch (IOException e) {
			e.printStackTrace();
		}
		logout();
		server.removeHandler(this);
		this.interrupt();
	}
}
