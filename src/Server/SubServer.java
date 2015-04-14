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
			for (int i = 0; i < 25; i++)
			{
				write.writeInt((int)(Math.random()*4));
			}
		}
		
		private void process(int state) throws IOException
		{
			switch (Command.values()[state])
			{
				case LOGIN:
					login();
				case SIGNUP:
					signup();
				case GETCOND:
					move(-1);
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
