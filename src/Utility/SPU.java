/**
 * 
 */
package Utility;

/**
 * @author NanflasTed
 * @author WJLIDDY
 * Last Edit 5/18 by WJLIDDY
 *
 */

import java.io.*;
import java.util.*;

public class SPU {

	//Time to live definition: time to autodisconnect client when idle
	public static final int TTL = 30*60*1000; //30 minutes
	
	//Database related definitions
	public static final String DRIVERNAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String DBUN = "SouthPole";
	public static final String DBPW = "southpole";
	public static final String DBURL = "jdbc:sqlserver://127.0.0.1:1336, DatabaseName = SouthPole";
	public static final int DBTTL = 30*1000;
	
    //amount of water to put at edge of map
    public static final int WATER_BORDER_SIZE = 15;
    //length of edge of map in tiles
    public static final int DEFAULT_MAP_SIZE = 1000;
    //amount of moves given per day.
    public static final int DAILY_MOVES = 10;

    //Commands are requests sent to the server by to the client.
    //To encode a command to int for transmission, use: Command.XXX.ordinal();
    //To convert an int back to a command, use: Command.values()[(int)Command.XXX.ordinal()];
    public static enum Command {
        LOGIN,
        SIGNUP,
        GETCOND,
        STAY,
        MOVEUP,
        MOVELEFT,
        MOVEDOWN,
        MOVERIGHT,
        LOGOUT,
    }

    //A response sent from a server back to a client.
    public static enum ServerResponse {
        LOGIN_FAIL,
        LOGIN_OK,
        ACCOUNT_CREATE_FAIL,
        ACCOUNT_CREATE_OK,
        LOGOUT_OK,
        LOGOUT_FAIL,
        //used by client
        SERVER_UNRESPONSIVE
    }

    //A type of base tile on the map.
    public static enum Tile {
        SNOW_LIGHT,
        SNOW_HEAVY,
        WATER,
        MOUNTAIN,
        GOAL,
        TOWN
    }

    public static int moveX(int x, int direction)
    {
    	switch (Command.values()[direction])
    	{
    	case MOVELEFT:
    		return x;
    	case MOVERIGHT:
    		return x;
    	case MOVEUP:
    		return x-1;
    	case MOVEDOWN:
    		return x+1;
		default:
    		return -1;	
    	}
    }
    
    public static int moveY(int y, int direction)
    {
    	switch (Command.values()[direction])
    	{
    	case MOVELEFT:
    		return y-1;
    	case MOVERIGHT:
    		return y+1;
    	case MOVEUP:
    		return y;
    	case MOVEDOWN:
    		return y;
		default:
    		return -1;	
    	}
    }
    
    public static String dataISReadLine(DataInputStream stream) throws IOException {
        StringBuilder res = new StringBuilder();
        char temp;
        while ((temp = stream.readChar()) != '\n') {
            res.append(temp);
        }
        return res.toString();
    }

    //generates a list of town names
    public static ArrayList<String> generateName() throws Exception
    {
    	FileReader fin = new FileReader("data/namesdict.txt");
    	BufferedReader in = new BufferedReader(fin);
    	String line;
    	ArrayList<String> names = new ArrayList<String>();
    	while ((line = in.readLine())!=null)
    	{
    		names.add(line);
    	}
    	return names;
    }
    
    public static boolean verifyHandshake(int handshake)
    {
    	return (handshake == "connectpls".hashCode());
    }


}
