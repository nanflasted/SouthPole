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
import Utility.SouthPoleUtil.ServerResponse;;


public class SubServer extends Thread{

	private ServerSocket listener;
	private Socket currentClient;
	private int portNumber;
	private int clientNumber = 0;
	
	
	
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
		
		private int login(String un, String pw)
		{
			System.out.println("login from user " + un + " with password " + pw);
			return (un.equals("admin"))?1:0;
		}
		
		private int signup(String un, String pw)
		{
			System.out.println("sign up from user " + un + " with password " + pw);
			return 3;
		}
		
		private int process(int state) throws IOException
		{
			String username, password;
			switch (Command.values()[state])
			{
				case LOGIN:
					username = SouthPoleUtil.dataISReadLine(read);
					password = SouthPoleUtil.dataISReadLine(read);
					return login(username,password);
				case SIGNUP:
					username = SouthPoleUtil.dataISReadLine(read);
					password = SouthPoleUtil.dataISReadLine(read);
					return signup(username,password);
				case GETCOND:
					return -1;
				case MOVEDOWN:
					return -1;
				case MOVELEFT:
					return -1;
				case MOVERIGHT:
					return -1;
				case MOVEUP:
					return -1;
				default:
					return -1;
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
				write.writeInt(process(state));
				read.close();
				write.close();
				client.close();
				decCN();
				return;
			}
			catch(Exception e)
			{
				System.err.println(e.getMessage());
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
