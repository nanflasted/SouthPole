using System; 
using System.Collections;
using System.IO;
using System.Text;
using UnityEngine;
using UnityEngine.UI;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

// This script handles the main menu and allows for the user to play multiplayer, change options, or exit.
// The single-player mode is not yet supported.
public class StartMenuScript : MonoBehaviour {
	
	// These vars hold the menus for toggling.
	Canvas startMenu, quitMenu, optionsMenu, loginMenu, registerQuestionMenu, registrationMenu, errorWindow;
	GameScript connectionMgr; // manages the game and the connection -- see the script's source for details.
	
	// Login menu contents
	InputField username, password;
	Text loginErrorMessage, regFailedMessage, windowErrorMessage;
	
	// Use this for initialization
	void Start () {
		startMenu = startMenu.GetComponent<Canvas>();
		quitMenu = quitMenu.GetComponent<Canvas> ();
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		loginMenu = loginMenu.GetComponent<Canvas> ();
		registerQuestionMenu = registerQuestionMenu.GetComponent<Canvas> ();
		registrationMenu = registrationMenu.GetComponent<Canvas> ();
		errorWindow = errorWindow.GetComponent<Canvas> ();
		connectionMgr = connectionMgr.GetComponent<GameScript> ();
		
		username = username.GetComponent<InputField> ();
		password = password.GetComponent<InputField> ();
		loginErrorMessage = loginErrorMessage.GetComponent<Text> ();
		regFailedMessage = regFailedMessage.GetComponent<Text> ();
		windowErrorMessage = windowErrorMessage.GetComponent<Text> ();
		
		quitMenu.enabled = false;
		optionsMenu.enabled = false;
		loginMenu.enabled = false;
		registerQuestionMenu.enabled = false;
		registrationMenu.enabled = false;
		errorWindow.enabled = false;
		loginErrorMessage.enabled = false;
		regFailedMessage.enabled = false;
	}

	/*****          Multiplayer Mode Methods (NO CONNECTION STUFF)          *****/

	// Start multiplayer.
	public void OnMPPress() {
		startMenu.enabled = false;
		
		// Attempt to load saved login data (name+pw).
		// If saved data does not exist, the client will ask the user if he/she is already registered.
		if (loadLogin ()) {
			loginMenu.enabled = true; // go to login menu.
			return;
		}
		
		// At this point, loading login data must have failed, so ask the user if he/she has an account.
		// Depending on the user's answer, he/she will be taken to either a login or registration menu.
		registerQuestionMenu.enabled = true;
	}
	
	// Load saved login data.
	public bool loadLogin() {
		// Try to load data from the login file ("login.ini").
		StreamReader sr = null;	
		try {
			sr = new StreamReader ("Assets/misc/login.ini");
		} catch (Exception e) {
			// The login file doesn't exist (i.e. user hasn't even logged in yet).
			return false; // loading login data failed!
		}

		// Get username and password from the file if it exists.
		username.text = sr.ReadLine ().Substring (9);
		password.text = sr.ReadLine ().Substring (9);
		sr.Close ();
		return true; // loading login data was successful!
	}

	// Save login data for quick multiplayer access.
	public void saveLogin() {
		// Gather info to put in the file.
		StringBuilder sb = new StringBuilder ();
		sb.AppendLine ("Username=" + username.text);
		sb.AppendLine ("Password=" + password.text);
		
		// Open a file for writing + overwrite the old settings (or create a new file).
		StreamWriter sw = new StreamWriter ("Assets/misc/login.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}

	// This is for the RegisterQuestionMenu. If the user says he/she has played MP before ("Yes"), this takes them to the login screen.
	public void OnYesClick() {
		loginMenu.enabled = true;
		registerQuestionMenu.enabled = false;
	}

	// If the user says he/she has NOT played MP before ("No"), this takes them to the registration menu.
	public void OnNoClick() {
		registrationMenu.enabled = true;
		registerQuestionMenu.enabled = false;
	}

	// Cancel login or registration for multiplayer and go back to the main menu.
	public void MPQuit() {
		loginMenu.enabled = false;
		registrationMenu.enabled = false;
		loginErrorMessage.enabled = false;
		regFailedMessage.enabled = false;
		startMenu.enabled = true;
	}

	// When the user clicks "Login" on the login menu, attempt to login with their username and password.
	public void LoginClick() {
		connectionMgr.connectToMainServer (false);
	}
	
	// When the user clicks "Register" on the registration menu, attempt to sign up with their username and password.
	public void RegisterClick() {
		connectionMgr.connectToMainServer (true);
	}

	// This method is used to display errors that happen while connecting (or currently connected) to the server.
	// Can be used to show that a connection attempt failed, a connection timed out, or that account registration failed.
	// (The text parameter is used to show the error message.)
	public void RaiseErrorWindow(string text) {
		errorWindow.enabled = true;
		windowErrorMessage.text = text;
	}

	// When the user clicks on the "OK" button in the error window, close the window.
	public void CloseErrorWindow() {
		errorWindow.enabled = false;
	}

	/*****          Single-Player Methods (CURRENTLY NOTHING, BASICALLY)          *****/

	// Start single player.
	public void OnSPPress() {
		// code goes here lol
	}

	/*****          Options Methods          *****/

	// Go to Options menu from main menu.
	public void OptionsPress() {
		startMenu.enabled = false;
		optionsMenu.enabled = true;
	}
	
	// Finished changing options. Go back to main menu and save preferences.
	public void OKPress() {
		optionsMenu.enabled = false;
		startMenu.enabled = true;
		((OptionsScript)(optionsMenu.GetComponent(typeof(OptionsScript)))).savePrefs ();
	}

	/*****          Quit Game Methods          *****/
	
	// Pulls up the quit menu if the Quit button on main menu is clicked.
	public void OnExitPress() {
		quitMenu.enabled = true;
		startMenu.enabled = false;
	}
	
	// If user cancels quitting the game, go back to main menu.
	public void ReturnToMainMenu() {
		quitMenu.enabled = false;
		startMenu.enabled = true;
	}
	
	// Quit the game...
	public void QuitGame() {
		Application.Quit ();
	}
}