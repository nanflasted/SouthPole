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
public class SouthPoleUtil {
	
	//CONVERT TO INT:Command.SIGNUP.ordinal();
	//CONVERT TO ENUM:Command.values()[(int)Command.SIGNUP.ordinal()];
	public static enum Command{
		LOGIN,
		SIGNUP,
		GETCOND,
		MOVEUP,
		MOVELEFT,
		MOVEDOWN,
		MOVERIGHT
	}
	
	public static enum ServerResponse{
		LOGIN_FAIL,
		LOGIN_OK,
		ACCOUNT_CREATE_FAIL,
		ACCOUNT_CREATE_OK
	}
	
	public static String dataISReadLine(DataInputStream stream) throws IOException
	{
		StringBuilder res = new StringBuilder();
		char temp;
		while ((temp = stream.readChar())!='\n')
		{
			res.append(temp);
		}
		return res.toString();
	}
	
}
