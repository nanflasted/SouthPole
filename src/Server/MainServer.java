package Server;

/**
 * @author NanflasTed
 *
 */
import java.net.*;
import java.util.*;
import java.io.*;


public class MainServer {
	private ArrayList<SubServer> worlds;
	private int worldNumber;
	
	public MainServer(int startingPort, int endingPort)
	{
		worldNumber = endingPort-startingPort+1;
		worlds = new ArrayList<SubServer>();
		for (int i = startingPort; i <= endingPort; i++)
		{
			worlds.add(new SubServer());
			worlds.get(i).start();
		}
	}
	
	/**
	 * @param args Starting Port and Ending Port
	 */
	public static void main(String args[])
	{
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
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
