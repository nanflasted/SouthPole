package GenericClient;

import android.util.Log;

import java.net.*;
import java.io.*;

import Utility.SouthPoleUtil.Command;
import Utility.SouthPoleUtil.ServerResponse;
import Utility.SouthPoleUtil.Tile;

public class Client{

	private static DataInputStream read;
	private static DataOutputStream write;		
	private static String hostName = "172.19.86.22";
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
			switch(c){
				case GETCOND:
					for(int x = 0; x != 5; x++){
						for(int y = 0; y !=5; y++){
							LocalState.localEnv[x][y] = Tile.values()[read.readInt()];
						}
					}
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
		ServerResponse test = sendServerCommand(Command.GETCOND);
	
		for(int x = 0; x != 5; x++){
			for(int y = 0; y !=5; y++){				
				System.out.println(x + " " + y + " " + LocalState.localEnv[x][y]);
			}
		}
		

		test = sendServerCommand(Command.LOGIN);
		System.out.println(test.toString());
		
		LocalState.username = "admin";
		test = sendServerCommand(Command.LOGIN);
		System.out.println(test.toString());
	}
	

}
