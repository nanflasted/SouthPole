using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.UI;

// **NOTE FOR UNITY WEB PLAYER: StackOverflow response says some IKVM stuff DOES NOT WORK on there.**
// **BEWARE if you are having trouble getting IKVM to work.**
using java.lang;
using java.io;
using java.net;
using Utility;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168
#pragma warning disable 0219

// TO DO: Deal with connection ending if (sub)server goes down.

public class GameScript : MonoBehaviour {
	
	public string ip = "127.0.0.1";
	public int handshake = -775644979; // this is actually the value of "connectpls".hashCode() in java.
	public int cmd = -1; // used MUCH later in processing user commands to server

	// Command enum constants
	int LOGIN = SPU.Command.LOGIN.ordinal(),
		 SIGNUP = SPU.Command.SIGNUP.ordinal(),
		 GETCOND =SPU.Command.GETCOND.ordinal(),
		 MOVEUP =SPU.Command.MOVEUP.ordinal(),
		 MOVEDOWN =SPU.Command.MOVEDOWN.ordinal(),
		 MOVELEFT =SPU.Command.MOVELEFT.ordinal(),
		 MOVERIGHT =SPU.Command.MOVERIGHT.ordinal(),
		 LOGOUT =SPU.Command.LOGOUT.ordinal(),
		 DISCONNECT = SPU.Command.DISCONNECT.ordinal();

	public InputField usernameLogin, passwordLogin, usernameReg, passwordReg;
	public Text errorMessage;

	// Initialization
	void Start () {
		DontDestroyOnLoad (this);
		usernameLogin = usernameLogin.GetComponent<InputField> ();
		passwordLogin = passwordLogin.GetComponent<InputField> ();
		usernameReg = usernameReg.GetComponent<InputField> ();
		passwordReg = passwordReg.GetComponent<InputField> ();
		errorMessage = errorMessage.GetComponent<Text> ();
	}

	public void connectToMainServer(bool newAcct) {
		// Connect to server address at port 1337 (main server).
		
		// TO DO: Provisions in case main server or subserver is down.
		Socket cnxn = null;
		
		try {
			// 1st step: Connect to main server and send handshake.
			cnxn = new Socket (ip, 1337);
			
			ObjectOutputStream output = new ObjectOutputStream (cnxn.getOutputStream ());
			output.writeInt (handshake);
			output.flush ();
			
			// Must now send username.
			string username = newAcct ? usernameReg.text : usernameLogin.text;
			output.writeObject (username);
			output.flush ();
			
			// Receive whatever port the server sends (random or determined).
			ObjectInputStream input = new ObjectInputStream (cnxn.getInputStream ());
			int nextPort = input.readInt ();
			
			// Close streams and connection.
			input.close ();
			output.close ();
			cnxn.close ();
			
			// At this point, either log in or sign up.
			if (newAcct)
				signup (nextPort);
			else
				loginAndPlay (nextPort);
			
		} catch (java.lang.Exception e) {
			if (cnxn == null)
				print ("Failed to connect");
			print (e.getStackTrace());
			return;
		} catch (System.Exception e) {
			if (cnxn == null)
				print ("Failed to connect");
			print (e.StackTrace);
			return;
		} 
	}

	public void signup (int port) {
		// TO DO: Make provisions in case the username is already taken. OR the subserver is down.
		// TO DO: Prevent fields from being empty.
		
		// First, connect to the subserver at the given port.
		Socket cnxn = null;
		try {
			cnxn = new Socket(ip, port);
			ObjectOutputStream output = new ObjectOutputStream(cnxn.getOutputStream());
			
			// Send handshake
			output.writeInt (handshake);
			output.flush ();
			
			// Now that we have connected and sent our handshake, we can send commands.
			// Here we will just sign up, close the connection, and log in using the given name and PW.
			
			output.writeInt (SIGNUP);
			output.flush ();
			
			// Send username and PW.
			output.writeObject(usernameReg.text);
			output.flush ();
			
			output.writeObject (passwordReg.text);
			output.flush ();
			
			// Check if acc was created
			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			bool acctCreated = input.readInt () == SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal();
			if (!acctCreated) {
				// TO DO: UI message for account creation failure.
				print ("Account creation failed.");
			}
			
			// At this point, the user is (hopefully) signed up for the server on the given port. So, log in.
			// (Close connection and streams first!)
			output.close();
			input.close();
			cnxn.close ();
			
			if (acctCreated) {
				usernameLogin.text = usernameReg.text;
				passwordLogin.text = passwordReg.text;
				loginAndPlay (port);
			}
			
		}catch (java.lang.Exception e) {
			print("Encountered a Java exception:\n");
			print (e.getMessage());
		}catch (System.Exception e) {
			print ("Encountered a C# exception:\n");
			print (e.Message);
		}
	}
	
	public void loginAndPlay (int port) {
		// TO DO: Make provisions in case the user is not signed up. OR the subserver is down.
		// TO DO: Prevent fields from being empty.
		
		// First, connect to the subserver at the given port.
		Socket cnxn = null;
		try {
			cnxn = new Socket(ip, port);
			ObjectOutputStream output = new ObjectOutputStream(cnxn.getOutputStream());
			
			// Send handshake
			output.writeInt (handshake);
			output.flush ();
			
			// Now that we have connected and sent our handshake, we can send commands.
			// First: log in.
			output.writeInt (LOGIN);
			output.flush ();
			
			output.writeObject(usernameLogin.text);
			output.flush ();
			
			output.writeObject(passwordLogin.text);
			output.flush ();
			
			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			if (input.readInt () != SPU.ServerResponse.LOGIN_OK.ordinal()) {
				// Login failed.
				errorMessage.enabled = true;
				output.writeInt(DISCONNECT);
				output.flush ();
				input.close();
				output.close ();
				cnxn.close ();
				return;
			}
			// At this point, login was successful.
			((StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)))).saveLogin();
			errorMessage.enabled = false;

			// Need to get the map first so the user can see stuff. First send the command, then receive the map and visibility.
			output.writeInt (GETCOND);
			output.flush ();

			int visibility = input.readInt ();
			SPU.Tile[][] map = (SPU.Tile[][])(input.readObject ());		
			Application.LoadLevel (1);	// TO DO MUCH LATER: Draw the map, using visibility to determine visible tiles, and put this in the new scene.

			// At this point, process commands one at a time.
			cmd = getCommand();
			while (cmd != LOGOUT) {
				output.writeInt (cmd);
				output.flush ();
				visibility = input.readInt();
				map = (SPU.Tile[][])(input.readObject());
				cmd = getCommand();
				// TO DO: Add a waiting period so users don't accidentally make a million moves, and so the server doesn't process a million commands.
			}
			// At this point, user is ready to log out.
			output.writeInt (LOGOUT);
			output.flush();
			input.close ();
			output.close ();
			cnxn.close ();

		}catch (java.lang.Exception e) {
			if (cnxn == null)
				print ("Failed to connect to subserver.\n");
			e.printStackTrace ();
		}catch (System.Exception e) {
			print (e.StackTrace);
		}
	}

	public void logout() {
		cmd = LOGOUT;
	}
	
	public int getCommand() {
		// Commands are decided by the button pressed (or clicked -- see pause menu buttons).
		// If the user is not logging out, then s/he may move or do nothing depending on keys pressed.
		// Mouse support may be added once we get the map visually implemented.
		if (cmd == LOGOUT)
			return LOGOUT;

		if (Input.GetKeyDown (KeyCode.W) || Input.GetKeyDown(KeyCode.UpArrow))
			return MOVEUP;
		if (Input.GetKeyDown (KeyCode.A) || Input.GetKeyDown(KeyCode.LeftArrow))
			return MOVELEFT;
		if (Input.GetKeyDown (KeyCode.S) || Input.GetKeyDown(KeyCode.DownArrow))
			return MOVEDOWN;
		if (Input.GetKeyDown (KeyCode.D) || Input.GetKeyDown(KeyCode.RightArrow))
			return MOVERIGHT;

		// If the user is not moving, we can just update the map (to see surroundings).
		return GETCOND;
	}
}