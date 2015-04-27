package Server;

import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.*;
import java.sql.*;

import Utility.SPU;
import Utility.SPU.*;

public class RedirServer extends Thread{
	private ServerSocket listener;
	private Socket client;
	private Connection database;
	private Statement statement;
	private ResultSet rsset;
	private int startPort, endPort;
	
	
	private class ClientHandler extends Thread
	{
		private Socket client;
		private DataInputStream read;
		private DataOutputStream write;		
		private int state;
				
		private void connectDB()
		{
			try
			{
				Class.forName("org.sqlite.JDBC");
				database = DriverManager.getConnection("jdbc.sqlite:"+"data/info/userredir.db");
				statement = database.createStatement();
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
		
		private int login()
		{
			try
			{
				int port = ServerResponse.LOGIN_FAIL.ordinal();
				connectDB();
				String un = SPU.dataISReadLine(read);
				rsset = statement.executeQuery("SELECT * FROM redir WHERE username = '" + un +"';");
				while (rsset.next())
				{
					port = rsset.getInt("server");
				}
				return port;
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
				return ServerResponse.LOGIN_FAIL.ordinal();
			}
		}
		
		private synchronized int signup()
		{
			int port = ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
			try
			{
				connectDB();
				String un = SPU.dataISReadLine(read);
				rsset = statement.executeQuery("SELECT * FROM redir WHERE username = '" + un +"';");
				if (rsset.next())
				{
					return ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
				}
				port = startPort + un.hashCode() % (endPort - startPort + 1);
				statement.executeUpdate("INSERT INTO redir (username, server) VALUES ('" + un + "', "
					+ new Integer(port).toString() + ");");
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
				return ServerResponse.ACCOUNT_CREATE_FAIL.ordinal();
			}
			return port;
		}
		
		private boolean verify(int handShake)
		{
			return (handShake == "connectpls".hashCode());
		}
		
		public ClientHandler(Socket client)
		{
			this.client = client;
		}
		
		public void run()
		{
			try
			{
				read = new DataInputStream(client.getInputStream());
				write = new DataOutputStream(client.getOutputStream());
				if (!verify(read.readInt()))
				{
					read.close();
					write.close();
					client.close();
					return;
				}
				state = read.readInt();
				switch (Command.values()[state])
				{
					case LOGIN:
						login();
						break;
					case SIGNUP:
						signup();
						break;
					default:
						break;
				}
				read.close();
				write.close();
				client.close();
				return;
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
			}
		}
	}
	
	public RedirServer(int startPort, int endPort)
	{
		this.startPort = startPort;
		this.endPort = endPort;
	}
	
	public void run()
	{
		try
		{
			listener = new ServerSocket(1337);
			System.out.println("Redirection waiting on port " + new Integer(1337).toString() 
					+ " at " + listener.getLocalSocketAddress().toString());
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			return;
		}
		String dbname = "data/info/userredir.db";
		if (!new File(dbname).exists())
		{
			try
			{
				System.out.println("Initializing user redir database" + dbname);
				Class.forName("org.sqlite.JDBC");
				database = DriverManager.getConnection("jdbc:sqlite:"+dbname);
				System.out.println("Database File Created at "+dbname);
				statement = database.createStatement();
				System.out.println("User Database Created");
				statement.executeUpdate("CREATE TABLE redir (username TEXT PRIMARY KEY, server INTEGER);");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					statement.close();
					database.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		try
		{
			client = listener.accept();
			new ClientHandler(client).start();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
		}
	}
	
}
