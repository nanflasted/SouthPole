package GenericClient;

import android.util.Log;

import java.net.*;
import java.io.*;

import Utility.SPU.Command;
import Utility.SPU.ServerResponse;
import Utility.SPU.Tile;

public class Client{

	private static DataInputStream read;
	private static DataOutputStream write;		
	private static String hostName = "184.9.215.108";
	private static int portNumber = 1337;
    //tells us if we have been redirected yet
    private static boolean redir = false;
    private static final int SERVER_TIMEOUT = 3000;
	
	public static ServerResponse sendServerCommand(Command c){
	    Socket server;
	    ServerResponse serverResponse = null;
		try {
			//connect to server
			//server = new Socket(hostName, portNumber);
            server = new Socket();
            try {
                server.connect(new InetSocketAddress(hostName, portNumber), SERVER_TIMEOUT);
            } catch (Exception e){
                return ServerResponse.SERVER_UNRESPONSIVE;
            }
            read = new DataInputStream(server.getInputStream());
			write = new DataOutputStream(server.getOutputStream());
			
			//handshake
			write.writeInt("connectpls".hashCode());
			//send our commands
			write.writeInt(c.ordinal());
			switch(c){
				case GETCOND:
                    Log.v("CliOps","Getting Condition: ");
                    write.writeChars(LocalState.username + '\n');
					for(int x = 0; x != LocalState.viewSize; x++){
						for(int y = 0; y != LocalState.viewSize; y++){
                            Log.v("CliOps","Getting Tile: " + x + " " + y);
							LocalState.localEnv[x][y] = Tile.values()[read.readInt()];
						}
					}
                    Log.v("CliOps","GetCond done");
					break;
				case LOGIN:
                    Log.v("CliOps","LOGGING IN with redirection: " + redir);
                    if(!redir) {
                        write.writeChars(LocalState.username + '\n');
                        int port = read.readInt();
                        Log.v("CliOps","Redirected to " + port);
                        if(port < portNumber) {
                            //login failure - does not exist.
                            serverResponse = ServerResponse.LOGIN_FAIL;
                            break;
                        } else {
                                portNumber = port;
                                redir = true;
                                serverResponse = sendServerCommand(Command.LOGIN);
                                Log.v("CliOps","Ultimate Response on " + portNumber + " "+ serverResponse);
                            }
                        } else {
                            write.writeChars(LocalState.username + '\n');
                            write.writeChars(LocalState.password + '\n');
                            serverResponse = ServerResponse.values()[read.readInt()];

                            Log.v("CliOps","Logged into " +portNumber + "and got "+ serverResponse);
                        }

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
                    Log.v("CliOps","SIGNUP with redirection: " + redir);
                    if(!redir) {
                        write.writeChars(LocalState.username + '\n');
                        int port = read.readInt();
                        Log.v("CliOps","Redirection to " + port);
                            if(port < portNumber) {
                                //login failure - does not exist.
                                //If server is right should never git
                                serverResponse = ServerResponse.ACCOUNT_CREATE_FAIL;
                                break;
                            } else {
                                portNumber = port;
                                redir = true;
                                ServerResponse signResp = sendServerCommand(Command.SIGNUP);

                                Log.v("CliOps","Tried to create onto " +portNumber + "and got "+ signResp);
                                read.close();
                                write.close();
                                server.close();
                                Log.v("CliOps","Signup Successful. Now logging in on " + portNumber);
                                serverResponse = sendServerCommand(Command.LOGIN);
                            }
                        } else {

                            Log.v("CliOps","Signing up @ " + portNumber + " ....");
                            write.writeChars(LocalState.username + '\n');
                            write.writeChars(LocalState.password + '\n');
                            serverResponse = ServerResponse.values()[read.readInt()];
                        }
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


		test = sendServerCommand(Command.LOGIN);
		System.out.println(test.toString());
		
		LocalState.username = "admin";
		test = sendServerCommand(Command.LOGIN);
		System.out.println(test.toString());
	}
	

}
