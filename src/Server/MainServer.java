package Server;

/**
 * @author NanflasTed
 *
 */
import java.net.*;
import java.util.*;
import java.io.*;
import Utility.*;


public class MainServer {
	private ArrayList<SubServer> worlds;
	private int worldNumber;
	public MainServer(int startingPort, int endingPort)
	{
		new RedirServer(startingPort, endingPort).start();
		worldNumber = endingPort-startingPort+1;
		worlds = new ArrayList<SubServer>();
		for (int i = startingPort; i <= endingPort; i++)
		{
			worlds.add(new SubServer(i));
			worlds.get(i-startingPort).start();
		}
	}
	
	/**
	 * @param args Starting Port and Ending Port
	 */
	public static void main(String args[])
	{
		if (Integer.parseInt(args[0])<=1337)
		{
			System.err.println("Used reserved port number");
			System.exit(1);
		}
		if (args.length != 2) 
		{
			System.err.println("Wrong Number of Arguments: MainServer <Starting Port> <Ending Port>");
			System.exit(1);
		}
		try
		{
			MainServer server = new MainServer(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
