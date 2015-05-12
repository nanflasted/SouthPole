/**
 * 
 */
package Server;

/**
 * @author NanflasTed
 *
 */

import java.net.*;
import java.util.*;
import java.io.*;
import Utility.*;
import Utility.SPU.*;


public class SubServer extends Thread{

	private ServerSocket listener;
	private Socket currentClient;
	private int portNumber;
	private int clientNumber = 0;
	private ObjectInputStream mapRead;
	private ObjectOutputStream mapWrite;
	private SubServerMap map;
	private HashSet<String> onlineUsers = new HashSet<String>();
	
	class ClientHandler extends Thread
	{
		private Socket client;
		private DataInputStream read;
		private DataOutputStream write;		
		private int state;
	
		
		public synchronized void incCN()
		{
			clientNumber++;
			System.out.println(new Integer(clientNumber).toString() + " concurrent client connections");
		}
		public synchronized void decCN()
		{
			clientNumber--;
			System.out.println(new Integer(clientNumber).toString() + " concurrent client connections");
		}
		
		public ClientHandler(Socket client)
		{
			this.client = client;
			System.out.println("connection established from" + client.getInetAddress().toString());
		}
		
		private void login() throws Exception
		{
			String un = SPU.dataISReadLine(read);
			String pw = SPU.dataISReadLine(read);
			if (!onlineUsers.add(un))
			{
				write.writeInt(SPU.ServerResponse.LOGIN_FAIL.ordinal());
				System.out.println("user " + un + " already online!");
				return;
			}
			System.out.println("login from user " + un + " with password " + pw);
			boolean fail = (ServerProcess.login(un, pw, portNumber)!=SPU.ServerResponse.LOGIN_OK.ordinal());
			write.writeInt(fail?SPU.ServerResponse.LOGIN_FAIL.ordinal():SPU.ServerResponse.LOGIN_OK.ordinal());
			if (fail) {onlineUsers.remove(un);}
		}
		
		private void signup() throws Exception
		{
			String un = SPU.dataISReadLine(read);
			String pw = SPU.dataISReadLine(read);
			System.out.println("sign up from user " + un + " with password " + pw);
			write.writeInt(ServerProcess.signup(un, pw, portNumber));
		}
		
		private void move(int direction) throws Exception
		{
			String un = SPU.dataISReadLine(read);
			ServerProcess.move(un,map,direction);
		}
		
		private void getCond() throws Exception
		{
			String un = SPU.dataISReadLine(read);
			int[][] out = ServerProcess.getCond(un,map);
			for (int i = 0; i < 11; i++)
			{
				for (int j = 0; j < 11; j++)
				{
					write.writeInt(out[i][j]);
				}
			}
		}
		
		private void logout() throws Exception
		{
			String un = SPU.dataISReadLine(read);
			boolean online = onlineUsers.remove(un);
			if (online)
			{
				ServerProcess.logout(un,portNumber);
			}
			write.writeInt((online?SPU.ServerResponse.LOGOUT_OK.ordinal():SPU.ServerResponse.LOGOUT_FAIL.ordinal()));
			
		}
		
		private void process(int state) throws Exception
		{
			System.out.println(Command.values()[state].toString());
			switch (Command.values()[state])
			{
				case LOGIN:
					login();
					break;
				case LOGOUT:
					logout();
					break;
				case SIGNUP:
					signup();
					break;
				case GETCOND:
					getCond();
					break;
				case MOVEDOWN:
					move(0);
					break;
				case MOVELEFT:
					move(1);
					break;
				case MOVERIGHT:
					move(2);
					break;
				case MOVEUP:
					move(3);
					break;
				default:
					return;
			}
		}
		
		private boolean verify(int handShake)
		{
			return (handShake == "connectpls".hashCode());
		}
		
		public void run()
		{
			try
			{
				incCN();
				read = new DataInputStream(client.getInputStream());
				write = new DataOutputStream(client.getOutputStream());
				if (!verify(read.readInt()))
				{
					read.close();
					write.close();
					client.close();
					decCN();
					return;
				}
				System.out.println("verified");
				state = read.readInt();
				process(state);
				read.close();
				write.close();
				client.close();
				decCN();
				return;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				decCN();
				return;
			}
		}
	}
	
	public SubServer(int i)
	{
		portNumber = i;
	}
	

	public void run()
	{
//===================================================================
//server-end
		try
		{
			listener = new ServerSocket(portNumber);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
//====================================================================
//map
		if (new File("data/maps/"+new Integer(portNumber).toString()+".map").exists())
		{
			try
			{
				String sN = "data/maps/"+new Integer(portNumber).toString()+".map";
				FileInputStream fileIn = new FileInputStream(sN);
				mapRead = new ObjectInputStream(fileIn);
				map = (SubServerMap) mapRead.readObject();	
			}
			catch (IOException e)
			{
				//rekt
			}
			catch (ClassNotFoundException e)
			{
				//rekt;
			}
		}
		else
		{
			map = new SubServerMap(SPU.DEFAULT_MAP_SIZE);
			try
			{
				mapWrite = new ObjectOutputStream(new FileOutputStream("data/maps/"+new Integer(portNumber).toString() + ".map"));
				mapWrite.writeObject(map);
			}
			catch (Exception rek)
			{
				//rekt;
			}
		}
//======================================================================
//database
		String dbname = "data/info/"+new Integer(portNumber).toString() + ".db";
		if (!new File(dbname).exists())
		{
			ServerProcess.createDB(dbname);
		}
//======================================================================
//client-end
		while (true)
		{
			try
			{
				System.out.println("server waiting on port " + new Integer(portNumber).toString() 
						+ " at " + listener.getLocalSocketAddress().toString());
				currentClient = listener.accept();
				new ClientHandler(currentClient).start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
}
