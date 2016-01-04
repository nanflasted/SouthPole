package GenericClient;

import android.util.Log;

import java.net.*;
import java.io.*;

import Utility.SPU.Command;
import Utility.SPU.ServerResponse;
import Utility.SPU.Tile;

public class Client{

    //Stream to read data from server
	private static DataInputStream read;
    //Stream to write data to server
	private static DataOutputStream write;
    //IP of server
	private static String hostName = "184.9.215.108";
    //Port Number of server: default 1337 until redirected.
	private static int portNumber = 1337;
    //True if we have been redirected, false otherwise
    private static boolean redir = false;
    //Amount of time to wait on the server before returning server unresponsive
    private static final int SERVER_TIMEOUT = 3000;
	
	public static ServerResponse sendServerCommand(Command c){
	    Socket server;
	    ServerResponse serverResponse = null;
		try {
            //initiate connection to server
            server = new Socket();

            //tries to connect to server: If fail, returns server unresponsive.
            try {
                server.connect(new InetSocketAddress(hostName, portNumber), SERVER_TIMEOUT);
            } catch (Exception e){
                return ServerResponse.SERVER_UNRESPONSIVE;
            }

            //we've connected to the server, open the streams.
            read = new DataInputStream(server.getInputStream());
			write = new DataOutputStream(server.getOutputStream());
			
			//do the handshake
			write.writeInt("connectpls".hashCode());

			//send our command
			write.writeInt(c.ordinal());

            //...and associated data.
			switch(c){
                //when we call getcond, or any movement, we expect an int representing how far we can see.
                //then, a bunch of ints representing the tiles we see.
                //TODO: Tell Ted visibility must be static on a day to day basis.
                //If it changes, we may be able to come in and out of contact with players.
				case GETCOND:
                case MOVEDOWN:
                case MOVELEFT:
                case MOVERIGHT:
                case MOVEUP:
                    Log.v("CliOps","Getting Condition: ");
                    //write our username.
                    write.writeChars(LocalState.username + '\n');
                    //get the amount of tiles we can see
                    getLocalView(read);
                    Log.v("CliOps","GetCond done");
					break;

                //Login: Sends a username and password. If sent to the main server, I get redirected
                //and this method is called again.
                // If sent to a client server, returns LOGIN_OK or LOGIN_FIAL
				case LOGIN:
                    Log.v("CliOps","LOGGING IN with redirection: " + redir);
                    if(!redir) {
                        //send username, get redirection port
                        write.writeChars(LocalState.username + '\n');
                        int port = read.readInt();
                        Log.v("CliOps","Redirected to " + port);
                        //Will always be sent to a port higher than the redirection server.
                        //If not, the user is already playing or user does not exist
                        if(port < portNumber) {
                            //TODO: Tell Ted to return User not existing vs User not playing
                            serverResponse = ServerResponse.LOGIN_FAIL;
                            break;
                        } else {
                            //Redirection sucessfull. Hit redirected server!
                            portNumber = port;
                            redir = true;
                            serverResponse = sendServerCommand(Command.LOGIN);
                            Log.v("CliOps","Ultimate Response on " + portNumber + " "+ serverResponse);
                            }
                    } else {
                        //redirected.
                        write.writeChars(LocalState.username + '\n');
                        write.writeChars(LocalState.password + '\n');
                        serverResponse = ServerResponse.values()[read.readInt()];
                        Log.v("CliOps","Logged into " +portNumber + "and got "+ serverResponse);
                    }
					break;

                //signing up to the game.
				case SIGNUP:
                    Log.v("CliOps","SIGNUP with redirection: " + redir);
                    if(!redir) {
                        //not redirected? We will be!
                        write.writeChars(LocalState.username + '\n');
                        int port = read.readInt();
                        Log.v("CliOps","Redirection to " + port);
                            if(port < portNumber) {
                                //TODO: Reject bad names and names already in use
                                serverResponse = ServerResponse.ACCOUNT_CREATE_FAIL;
                                break;
                            } else {
                                portNumber = port;
                                redir = true;
                                ServerResponse signResp = sendServerCommand(Command.SIGNUP);
                                Log.v("CliOps","Tried to create onto " +portNumber + " and got "+ signResp);
                                //At this point, signup was good! now just log in. close all old streams.
                                read.close();
                                write.close();
                                server.close();
                                Log.v("CliOps","Signup Successful. Now logging in on " + portNumber);
                                serverResponse = sendServerCommand(Command.LOGIN);
                            }
                        } else {
                            //just signing up on a redirection server
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

        //who needs testing
	}

    //from a stream, get the local view.
    private void getLocalView(DataInputStream read) throws IOException{
        LocalState.setViewSize(read.readInt());
        //fill them in accordingly
        for (int x = 0; x != LocalState.viewSize; x++) {
            for (int y = 0; y != LocalState.viewSize; y++) {
                Log.v("CliOps", "Getting Tile: " + x + " " + y);
                LocalState.localEnv[x][y] = Tile.values()[read.readInt()];
            }
        }
    }
}
