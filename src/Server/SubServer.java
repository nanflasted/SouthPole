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
		private char state;
		
		public ClientHandler(Socket client)
		{
			this.client = client;
			System.out.println("connection established from" + client.getInetAddress().toString());
		}
		
		private boolean verify(int handShake)
		{
			return (handShake == "dankweed".hashCode());
		}
		
		public void run()
		{
			try
			{
				read = new DataInputStream(client.getInputStream());
				write = new DataOutputStream(client.getOutputStream());
				
				write.writeInt(69);write.writeInt(420);
				write.writeChars("Welcome to the Server!\n");
				if (!verify(read.readInt()))
				{
					read.close();
					write.close();
					client.close();
					return;
				}
				System.out.println("verified");
				switch (state)
				{
					case 't':
						//TODO
				}
				read.close();
				write.close();
				client.close();
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
						+ " at " + listener.getLocalSocketAddress().toString() + " with " + new Integer(clientNumber).toString()
						+ " current communications.");
				currentClient = listener.accept();
				clientNumber++;
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
