package GenericClient;

import java.net.*;
import java.io.*;

import Utility.SouthPoleUtil.Command;
import Utility.SouthPoleUtil.ServerResponse;

public class Client{

	private static DataInputStream read;
	private static DataOutputStream write;		
	private static String hostName = "172.20.88.219";
	private static int portNumber = 1337;
	
	public static ServerResponse sendServerCommand(Command c){
	    Socket server;
	    ServerResponse serverResponse = null;
		try {
			//connect to server
			server = new Socket(hostName, portNumber);
			read = new DataInputStream(server.getInputStream());
			write = new DataOutputStream(server.getOutputStream());
			
			//handshake
			write.writeInt("connectpls".hashCode());
			//send our commands
			write.writeInt(c.ordinal());
			System.out.println("test");
			switch(c){
				case GETCOND:
					//get an update of the environment around me.
					break;
				case LOGIN:
					write.writeChars(LocalState.username + '\n');
					write.writeChars(LocalState.password + '\n');
					serverResponse = ServerResponse.values()[read.readInt()];
					break;
				case MOVEDOWN:
					break;
				case MOVELEFT:
					break;
				case MOVERIGHT:
					break;
				case MOVEUP:
					break;
				case SIGNUP:
					write.writeChars(LocalState.username + '\n');
					write.writeChars(LocalState.password + '\n');
					serverResponse = ServerResponse.values()[read.readInt()];
					break;
				default:
					break;
			
			}


		     
			read.close();
			write.close();	
			server.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		return serverResponse;
	}

	public static void main(String args[]){
		ServerResponse u = ServerResponse.values()[sendServerCommand(Command.SIGNUP).ordinal()];
		System.out.println("got " + u.toString());
	}
	

}
