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

// TO DO: Deal with connection ending if (sub)server goes down.

public class GameScript : MonoBehaviour {
	
	public string ip = "127.0.0.1";
	public int handshake = -775644979; // this is actually the value of "connectpls".hashCode() in java.

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
			output.writeInt (SPU.Command.LOGIN.ordinal());
			output.flush ();
			
			output.writeObject(usernameLogin.text);
			output.flush ();
			
			output.writeObject(passwordLogin.text);
			output.flush ();
			
			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			if (input.readInt () != SPU.ServerResponse.LOGIN_OK.ordinal()) {
				// Login failed.
				errorMessage.enabled = true;
				output.writeInt(SPU.Command.DISCONNECT.ordinal());
				output.flush ();
				input.close();
				output.close ();
				cnxn.close ();
				return;
			}
			
			// At this point, login was successful.
			((StartMenuScript)(GameObject.Find ("Start Menu").GetComponent(typeof(StartMenuScript)))).saveLogin();
			errorMessage.enabled = false;

			// Temporarily log out.
			output.writeInt (SPU.Command.LOGOUT.ordinal());
			output.flush();
			input.close ();
			output.close ();
			cnxn.close ();
			Application.LoadLevel(0);
		}catch (java.lang.Exception e) {
			if (cnxn == null)
				print ("Failed to connect to subserver.\n");
			e.printStackTrace ();
		}catch (System.Exception e) {
			print (e.StackTrace);
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
			
			output.writeInt (SPU.Command.SIGNUP.ordinal());
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
}