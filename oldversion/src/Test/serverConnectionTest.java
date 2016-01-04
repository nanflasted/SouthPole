package Test;

import java.net.*;
import java.io.*;
public class serverConnectionTest {

	public static void main(String args[])
	{
		try
		{
			Socket tst = new Socket("127.0.0.1",1337);
			ObjectOutputStream oos = new ObjectOutputStream(tst.getOutputStream());
			oos.writeInt("connectpls".hashCode());
			oos.writeObject("dank");
			oos.close();
			tst.close();
		}
		catch(Exception e)
		{
			System.err.println("server rekt");
		}
	}
}
