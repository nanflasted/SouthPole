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
import Utility.SouthPoleUtil.Command;
import Utility.SouthPoleUtil.ServerResponse;


public class SubServer extends Thread{

	private ServerSocket listener;
	private Socket currentClient;
	private int portNumber;
	private int clientNumber = 0;
	private ObjectInputStream mapRead;
	private ObjectOutputStream mapWrite;
	private SubServerMap map;
	
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
		
		private void login() throws IOException
		{
			String un = SouthPoleUtil.dataISReadLine(read);
			String pw = SouthPoleUtil.dataISReadLine(read);
			System.out.println("login from user " + un + " with password " + pw);
			write.writeInt(ServerProcess.login(un, pw));
		}
		
		private void signup() throws IOException
		{
			String un = SouthPoleUtil.dataISReadLine(read);
			String pw = SouthPoleUtil.dataISReadLine(read);
			System.out.println("sign up from user " + un + " with password " + pw);
			write.writeInt(ServerProcess.signup(un, pw));
		}
		
		private void move(int direction) throws IOException
		{
			String un = SouthPoleUtil.dataISReadLine(read);
			ServerProcess.move(un,map,direction);
		}
		
		private void getCond() throws IOException
		{
			System.out.println("rekt");
			String un = SouthPoleUtil.dataISReadLine(read);
			System.out.println("rekt");
			int[][] out = ServerProcess.getCond(un,map);
			
			for (int i = 0; i < 5; i++)
			{
				for (int j = 0; j < 5; j++)
				{
					write.writeInt(out[i][j]);
				}
			}
		}
		
		private void process(int state) throws IOException
		{
			System.out.println(Command.values()[state].toString());
			switch (Command.values()[state])
			{
				case LOGIN:
					login();
				case SIGNUP:
					signup();
				case GETCOND:
					getCond();
				case MOVEDOWN:
					move(0);
				case MOVELEFT:
					move(1);
				case MOVERIGHT:
					move(2);
				case MOVEUP:
					move(3);
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
				System.err.println(e.getMessage());
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
		try
		{
			listener = new ServerSocket(portNumber);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			return;
		}
		
		try
		{
			String sN = new Integer(portNumber).toString() + ".map";
			FileInputStream fileIn = new FileInputStream(sN);
			mapRead = new ObjectInputStream(fileIn);
			map = (SubServerMap) mapRead.readObject();	
		}
		catch (IOException e)
		{
			map = new SubServerMap(100);
			try
			{
				mapWrite = new ObjectOutputStream(new FileOutputStream(new Integer(portNumber).toString() + ".map"));
				mapWrite.writeObject(map);
			}
			catch (Exception rek)
			{
				//rekt;
			}
		}
		catch (ClassNotFoundException e)
		{
			//rekt;
		}
		
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
				System.err.println(e.getMessage());
				return;
			}
		}
	}
}
