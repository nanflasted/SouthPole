/**
 * 
 */
package Utility;

/**
 * @author NanflasTed
 * @author WJLIDDY
 *
 */

import java.io.*;
import java.sql.*;
import java.util.*;

public class SPU {

    public static final int WATER_BORDER_SIZE = 15;
    public static final int DEFAULT_MAP_SIZE = 100;
    //CONVERT TO INT:Command.SIGNUP.ordinal();
    //CONVERT TO ENUM:Command.values()[(int)Command.SIGNUP.ordinal()];
    public static enum Command {
        LOGIN,
        SIGNUP,
        GETCOND,
        STAY,
        MOVEUP,
        MOVELEFT,
        MOVEDOWN,
        MOVERIGHT,
        LOGOUT
    }

    public static enum ServerResponse {
        LOGIN_FAIL,
        LOGIN_OK,
        ACCOUNT_CREATE_FAIL,
        ACCOUNT_CREATE_OK,
        LOGOUT_OK,
        LOGOUT_FAIL
    }

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


}
