using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.UI;

// IKVM and Java stuff
// **NOTE FOR UNITY WEB PLAYER: StackOverflow response says some IKVM stuff DOES NOT WORK on there.**
// **BEWARE if you are having trouble getting IKVM to work.**
using java.lang;
using java.io;
using java.net;
using Utility;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168
#pragma warning disable 0219

// This class manages the game and connection stuff, in-game and in the main menu.
public class GameScript : MonoBehaviour {

	// Constants -- may be changed later (except for cmd - leave this alone)
	string ip = "127.0.0.1";
	int handshake = -775644979; // this is actually the value of "connectpls".hashCode() in java.
	int cmd = -1; // used MUCH later in processing user commands to server

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

	// Other fields -- input fields and messages.
	InputField usernameLogin, passwordLogin, usernameReg, passwordReg;
	Text loginErrorMessage, regFailedMessage;

	// Initialization
	void Start () {
		DontDestroyOnLoad (this);
		usernameLogin = usernameLogin.GetComponent<InputField> ();
		passwordLogin = passwordLogin.GetComponent<InputField> ();
		usernameReg = usernameReg.GetComponent<InputField> ();
		passwordReg = passwordReg.GetComponent<InputField> ();
		loginErrorMessage = loginErrorMessage.GetComponent<Text> ();
		regFailedMessage = regFailedMessage.GetComponent<Text> ();
	}

	// Connect to server address at port 1337 (main server) and get a new port on which to login or sign up.
	public void connectToMainServer(bool newAcct) {
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
			cnxn.setSoTimeout(15000); // 15-sec timeout for input reads
			ObjectInputStream input = new ObjectInputStream (cnxn.getInputStream ());
			int nextPort = input.readInt ();
			
			// Close streams and connection.
			input.close ();
			output.close ();
			cnxn.close ();
			
			// We got a port now! At this point, either log in or sign up.
			if (newAcct)
				signup (nextPort);
			else
				loginAndPlay (nextPort);
			
		} catch (java.lang.Exception e) {
			// Display some kind of error window if there was a connection error (basically a Java exception).
			// If the connection is null, the connection attempt failed; otherwise, the connection timed out.
			StartMenuScript sms = (StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)));
			if (cnxn == null)
				sms.RaiseErrorWindow("Failed to connect. Check your connection settings. The main server may be down.");
			else
				sms.RaiseErrorWindow("Connection timed out. Check your connection. The main server may have gone down.");
		} catch (System.Exception e) {
			// This handles C# exceptions. These shouldn't happen, which is why the errors are printed to the console (for us to test ourselves).
			print ("Encountered a C# exception:");
			print (e.Message);
		} 
	}

	// Sign up on a given port. If registration is successful, the client will automatically log in for the user to begin playing.
	// Automatically called by connectToMainServer().
	public void signup (int port) {
		// Check if user left either field blank.
		if (usernameReg.text.Equals ("") || passwordReg.text.Equals ("")) {
			regFailedMessage.enabled = true;
			return;
		}

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
			cnxn.setSoTimeout(15000);
			bool acctCreated = input.readInt () == SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal(); 
			if (!acctCreated) {
				// Display an error message if registration failed.
				StartMenuScript  sms = (StartMenuScript)(GameObject.Find("Start Menu").GetComponent(typeof(StartMenuScript)));
				sms.RaiseErrorWindow("Account creation failed. That name may already be taken.");
			}
			
			// At this point, the user is (hopefully) signed up for the server on the given port. So, log in.
			// (Close connection and streams first!)
			output.close();
			input.close();
			cnxn.close ();
			regFailedMessage.enabled = false;

			// If registration succeeded, at this point the client will auto-login and start playing the game.
			if (acctCreated) {
				usernameLogin.text = usernameReg.text;
				passwordLogin.text = passwordReg.text;
				loginAndPlay (port);
			}
			
		}catch (java.lang.Exception e) {
			// Display some kind of error window if there was a connection error (basically a Java exception).
			// If the connection is null, the connection attempt failed; otherwise, the connection timed out.
			StartMenuScript sms = (StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)));
			if (cnxn == null)
				sms.RaiseErrorWindow("Failed to connect. Check your connection settings. The subserver may be down.");
			else
				sms.RaiseErrorWindow("Connection timed out. Check your connection. The subserver may have gone down.");
		}catch (System.Exception e) {
			// This handles C# exceptions. These shouldn't happen, which is why the errors are printed to the console (for us to test ourselves).
			print ("Encountered a C# exception:\n");
			print (e.Message);
		}
	}

	// This method allows the user to log in and play (if login is successful). Automatically called by either connectToMainServer() or signup().
	public void loginAndPlay (int port) {
		// Check if user left either field blank.
		if (usernameLogin.text.Equals ("") || passwordLogin.text.Equals ("")) {
			loginErrorMessage.enabled = true;
			return;
		}

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

			// Check if login was successful or failed
			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			cnxn.setSoTimeout(15000);
			if (input.readInt () != SPU.ServerResponse.LOGIN_OK.ordinal()) {
				// Login failed at this point. Disconnect from the server so the user can try again.
				loginErrorMessage.enabled = true;
				output.writeInt(DISCONNECT);
				output.flush ();
				input.close();
				output.close ();
				cnxn.close ();
				return;
			}
			// At this point, login was successful.
			((StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)))).saveLogin();
			loginErrorMessage.enabled = false;

			// Need to get the map first so the user can see stuff. First send the command, then receive the map and visibility.
			output.writeInt (GETCOND);
			output.flush ();

			int visibility = input.readInt ();
			SPU.Tile[][] map = (SPU.Tile[][])(input.readObject ());		
			Application.LoadLevel (1);	// load the game.
			// TO DO MUCH LATER: Draw the map, using visibility to determine visible tiles, and put this in the new scene.

			// At this point, process move commands one at a time (or logout if the user chooses).
			cmd = getCommand();
			while (cmd != LOGOUT) {
				// First write the command...
				output.writeInt (cmd);
				output.flush ();
				// ...then receive the updated visibility and map from the server.
				visibility = input.readInt();
				map = (SPU.Tile[][])(input.readObject());
				cmd = getCommand();

				// This is a tiny waiting period so users don't send a million commands per second and so the server doesn't have to process as many commands.
				System.Threading.Thread.Sleep(250);
			}
			// At this point, user is ready to log out (cmd == LOGOUT).
			output.writeInt (LOGOUT);
			output.flush();
			input.close ();
			output.close ();
			cnxn.close ();

		}catch (java.lang.Exception e) {
			// Deal with a failed connection attempt
			if (cnxn == null) {
				StartMenuScript sms = (StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)));
				sms.RaiseErrorWindow("Failed to connect. Check your connection settings. The subserver may be down.");
			}
			else {
				// Return to main menu, since connection has timed out.
				Application.LoadLevel (0);
				StartMenuScript sms = (StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)));
				sms.RaiseErrorWindow("Connection timed out. Check your connection. The subserver may have gone down.");
			}
		}catch (System.Exception e) {
			// This handles C# exceptions. These shouldn't happen, which is why the errors are printed to the console (for us to test ourselves).
			print ("Encountered a C# exception:");
			print (e.Message);
		}
	}

	// This gets the next command based on user input.
	// If the command is set to logout (based on the method logout() -- see below), then it just returns logout as well.
	public int getCommand() {
		// Commands are decided by the button pressed (or clicked -- see pause menu buttons).
		// If the user is not logging out, then s/he may move or do nothing depending on keys pressed.
		// Mouse support can be added once we get the map visually implemented.
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

	// This can be called by the methods QuitToMainMenu() and QuitToDesktop() in GameMenuScript.
	// Once called, the command is set to logout and it will be the next (and last) command processed in the loginAndPlay() method above.
	// (It will be called only if the user confirms he/she wants to quit to either the main menu or the desktop.)
	public void logout() {
		cmd = LOGOUT;
	}
}