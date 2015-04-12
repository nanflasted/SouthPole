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


public class SubServer extends Thread{

	private ServerSocket listener;
	private Socket currentClient;
	private int portNumber;

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
		
		private boolean verify(long handShake)
		{
			return true;
		}
		
		public void run()
		{
			try
			{
				read = new DataInputStream(client.getInputStream());
				write = new DataOutputStream(client.getOutputStream());
				write.writeChars("Welcome to the server");
				if (!verify(read.readLong()))
				{
					write.writeChars("Verification Error!");
					read.close();
					write.close();
					client.close();
					return;
				}
				state = read.readChar();
				switch (state)
				{
					case 't':
						//do stuff
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
		int clientNumber = 0;
		try
		{
			listener = new ServerSocket(portNumber);
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			return;
		}
		while (clientNumber <= 5)
		{
			try
			{
				System.out.println("server waiting on port" + new Integer(portNumber).toString() 
						+ "at" + listener.getInetAddress().toString() + "with" + new Integer(clientNumber).toString()
						+ "current communications");
				currentClient = listener.accept();
				clientNumber++;
				new ClientHandler(currentClient).start();	
				clientNumber--;
			}
			catch (Exception e)
			{
				System.err.println(e.getMessage());
				return;
			}
		}
	}
}
