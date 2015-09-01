using System;
using System.Collections;
using System.IO;
using System.Threading;
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
#pragma warning disable 0414

// This class manages the game and connection stuff, in-game and in the main menu.
public class GameScript : MonoBehaviour {

	// Constants -- may be changed later
	string ip = "127.0.0.1";
	int handshake = -775644979; // this is actually the value of "connectpls".hashCode() in java.

	// Command enum constants	
	static int LOGIN = SPU.Command.LOGIN.ordinal(),
		 SIGNUP = SPU.Command.SIGNUP.ordinal(),
		 GETCOND =SPU.Command.GETCOND.ordinal(),
		 MOVEUP =SPU.Command.MOVEUP.ordinal(),
		 MOVEDOWN =SPU.Command.MOVEDOWN.ordinal(),
		 MOVELEFT =SPU.Command.MOVELEFT.ordinal(),
		 MOVERIGHT =SPU.Command.MOVERIGHT.ordinal(),
		 LOGOUT =SPU.Command.LOGOUT.ordinal(),
		 DISCONNECT = SPU.Command.DISCONNECT.ordinal();

	// Other fields
	public InputField usernameLogin, passwordLogin, usernameReg, passwordReg;
	public Text loginErrorMessage, regFailedMessage;
	public Canvas startMenu;
	public static int cmd = SPU.Command.GETCOND.ordinal(); // used MUCH later in processing user commands to server
	public static bool logout = false; // used for commands also, when logging out
	public bool isQuitting = false; // used in case the user wants to quit the game entirely
	public System.Threading.Thread userInputThread = null;
	public AsyncOperation load = null;
	public bool isLoading = false;

	// Initialization
	void Start () {
		DontDestroyOnLoad (this);
		usernameLogin = usernameLogin.GetComponent<InputField> ();
		passwordLogin = passwordLogin.GetComponent<InputField> ();
		usernameReg = usernameReg.GetComponent<InputField> ();
		passwordReg = passwordReg.GetComponent<InputField> ();
		loginErrorMessage = loginErrorMessage.GetComponent<Text> ();
		regFailedMessage = regFailedMessage.GetComponent<Text> ();
		startMenu = startMenu.GetComponent<Canvas> ();
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
			cnxn.setSoTimeout(10000); // 10-sec timeout for input reads
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
			// If the socket is null, the connection attempt failed; otherwise, the connection timed out, or something else happened.
			StartMenuScript sms = (StartMenuScript)(startMenu.GetComponent(typeof(StartMenuScript)));
			if (cnxn == null)
				sms.RaiseErrorWindow("Failed to connect. Check your connection settings. The main server may be down.");
			else if (e.GetType() == typeof(SocketTimeoutException)) 
				sms.RaiseErrorWindow("Connection timed out. Check your connection. The main server may have gone down.");
			else
				sms.RaiseErrorWindow("An unknown exception occurred when trying to connect to the main server.");
		} catch (System.Exception e) {
			// This handles C# exceptions. These shouldn't happen, which is why the errors are printed to the console (for us to test for ourselves).
			print ("Encountered a C# exception:\n");
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
		bool acctCreated = false;
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
			cnxn.setSoTimeout(10000);
			acctCreated = input.readInt () == SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal(); 
			if (!acctCreated) {
				// Display an error message if registration failed.
				StartMenuScript  sms = (StartMenuScript)(startMenu.GetComponent(typeof(StartMenuScript)));
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
				((StartMenuScript)(startMenu.GetComponent(typeof(StartMenuScript)))).RegToLogin();
				loginAndPlay (port);
			}
			
		}catch (java.lang.Exception e) {
			// Display some kind of error window if there was a connection error (basically a Java exception).
			// If the socket is null, the connection attempt failed; otherwise, the connection timed out, or something else happened.
			StartMenuScript sms = (StartMenuScript)(startMenu.GetComponent(typeof(StartMenuScript)));
			if (cnxn == null)
				sms.RaiseErrorWindow("Failed to connect. Check your connection settings. The subserver may be down.");
			else if (e.GetType() == typeof(SocketTimeoutException) && !acctCreated)
				sms.RaiseErrorWindow("Connection timed out. Check your connection. The subserver may have gone down.");
			else if (acctCreated)
				sms.RaiseErrorWindow("Connection timed out, but registration was successful. The subserver may have gone down suddenly.");
			else
				sms.RaiseErrorWindow("An unknown exception occurred when trying to connect to the main server.");
		}catch (System.Exception e) {
			// This handles C# exceptions. These shouldn't happen, which is why the errors are printed to the console (for us to test for ourselves).
			print ("Encountered a C# exception:\n");
			print (e.StackTrace);
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
		bool playerInGame = false; // tracks whether the player is in-game (i.e. not in the main menu)
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
			cnxn.setSoTimeout(10000);
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
			((StartMenuScript)(startMenu.GetComponent(typeof(StartMenuScript)))).saveLogin();
			loginErrorMessage.enabled = false;

			// Need to get the map first so the user can see stuff. First send the command, then receive the map and visibility.
			output.writeInt (GETCOND);
			output.flush ();

			int visibility = input.readInt ();
			SPU.Tile[][] map = (SPU.Tile[][])(input.readObject ());

			// Load the game and start necessary threads.
			isLoading = true;
			StartCoroutine("LoadGame");
			while (isLoading);
			playerInGame = true;
			Destroy(GameObject.Find("Login Menu"));
			Destroy(GameObject.Find("Registration Menu"));
			Destroy (GameObject.Find ("Main Menu Music"));

			// Create a thread to process user inputs (i.e. for the menu and for player movement)
			userInputThread = new System.Threading.Thread(new ThreadStart(new UserInputThread().ProcessInput));
			userInputThread.Start();
			while (!userInputThread.IsAlive); //loop until thread activates

			// TO DO MUCH LATER: Draw the map, using visibility to determine visible tiles, and put this in the new scene.

			// At this point, process move commands one at a time (or logout if the user chooses).
		
			while (cmd != LOGOUT) {
				// First write the command...
				output.writeInt (cmd);
				output.flush ();
				// ...then receive the updated visibility and map from the server.
				visibility = input.readInt();
				map = (SPU.Tile[][])(input.readObject());
			}

			// At this point, user is ready to log out (cmd == LOGOUT).
			output.writeInt (LOGOUT);
			output.flush();

			// The game can just exit everything completely if the user is quitting to desktop.
			if (isQuitting)
				Application.Quit ();

			input.close ();
			output.close ();
			cnxn.close ();
			Destroy (GameObject.Find ("BG Music"));
			isLoading = true;
			StartCoroutine("LoadMainMenu");
			while (isLoading);
			Destroy (this);

		}catch (java.lang.Exception e) {
			// Deal with a failed connection attempt
			StartMenuScript sms = (StartMenuScript)(startMenu.GetComponent(typeof(StartMenuScript)));
			if (cnxn == null) 
				sms.RaiseErrorWindow("Failed to connect. Check your connection settings. The subserver may be down.");
			else if (e.GetType() == typeof(SocketTimeoutException) && !playerInGame)
				sms.RaiseErrorWindow("Connection timed out. Check your connection. The subserver may have gone down.");
			else {
				// Return to the main menu, since connection has timed out.
				Destroy (startMenu);
				Destroy (GameObject.Find ("BG Music"));
				userInputThread.Abort();
				userInputThread.Join();
				isLoading = true;
				StartCoroutine("LoadMainMenu");
				while (isLoading);
				StartMenuScript sms2 = ((StartMenuScript)(GameObject.Find("Start Menu").GetComponent<StartMenuScript>()));
				sms2.RaiseErrorWindow("Connection timed out. Check your connection. The subserver may have gone down.");
				Destroy (this);
			}
		}catch (System.Exception e) {
			// This handles C# exceptions. These shouldn't happen, which is why the errors are printed to the console (for us to test for ourselves).
			print ("Encountered a C# exception:\n");
			print (e.StackTrace);
		}
	}

	// This can be called by the methods QuitToMainMenu() and QuitToDesktop() in GameMenuScript.
	// Once called, the command is set to logout and it will be the next (and last) command processed in the loginAndPlay() method above.
	// (It will be called only if the user confirms he/she wants to quit to either the main menu or the desktop.)
	// IsQuitting refers to whether or not the user is quitting to desktop.
	public void setLogout(bool isQuitting) {
		logout = true;
		this.isQuitting = isQuitting;
	}

	IEnumerator LoadMainMenu() {
		load = Application.LoadLevelAsync(0);
		yield return load;
		isLoading = false;
	}

	IEnumerator LoadGame() {
		load = Application.LoadLevelAsync(1);
		yield return load;
		isLoading = false;
	}

	// This thread handles displaying the in-game menu as well as processing movement commands (and logging out, kind of).
	public class UserInputThread {
		public void ProcessInput() {
			bool menuEnabled = false;
			Canvas pauseMenu = GameObject.Find("Pause Menu").GetComponent<Canvas>(),
				quitConfirmMenu = GameObject.Find ("QuitConfirmationMenu").GetComponent<Canvas>(),
				mainMenuOptionCanvas = GameObject.Find ("MM Yes Button Canvas").GetComponent<Canvas>(),
				quitOptionCanvas = GameObject.Find("Quit Button Canvas").GetComponent<Canvas>(),
				optionsMenu = GameObject.Find("Options Menu").GetComponent<Canvas>();

			while (true) {
				// Part 1: Menu Stuff

				// Check if Esc key is pressed first.
				if (Input.GetKeyDown(KeyCode.Escape)) {

					// Toggle pause menu if pressed
					pauseMenu.enabled = !pauseMenu.enabled;

					// Check if menu screen(s) are open (i.e. main, options, or quit dialog)
					if (!quitConfirmMenu.enabled && !optionsMenu.enabled)
						menuEnabled=pauseMenu.enabled;

					// If any other menus are currently open, close them and return to the pause menu.
					if (quitConfirmMenu.enabled) {
						quitConfirmMenu.enabled = false;
						mainMenuOptionCanvas.enabled = false;
						quitOptionCanvas.enabled = false;
					}
					if (optionsMenu.enabled) {
						optionsMenu.enabled = false;
						((OptionsScript)(optionsMenu.GetComponent(typeof(OptionsScript)))).Revert();
					}
				}

				// Part 2: Move Commands (and logging out, if applicable)

				// If the user chose to log out (through the menu), do NOT process the move commands.
				if (logout) {
					cmd = LOGOUT;
					break;
				}

				// By default, the character stays in place while getting the new map info from the server.
				cmd = GETCOND;

				// If the menu screen is up, do NOT process other move commands (i.e. go to the next iteration).
				if (menuEnabled)
					continue;

				// Now, if the user is not logging out, determine the next command to be called by checking what keys are pressed
				//    (WASD or arrow keys). Priority-based ("Else if" for each statement).
				if (Input.GetKeyDown (KeyCode.W) || Input.GetKeyDown(KeyCode.UpArrow))
					cmd = MOVEUP;
				else if (Input.GetKeyDown (KeyCode.A) || Input.GetKeyDown(KeyCode.LeftArrow))
					cmd = MOVELEFT;
				else if (Input.GetKeyDown (KeyCode.S) || Input.GetKeyDown(KeyCode.DownArrow))
					cmd = MOVEDOWN;
				else if (Input.GetKeyDown (KeyCode.D) || Input.GetKeyDown(KeyCode.RightArrow))
					cmd = MOVERIGHT;
			}
		}
	};
}