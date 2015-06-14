using System; 
using System.Collections;
using System.IO;
using System.Text;
using UnityEngine;
using UnityEngine.UI;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

public class StartMenuScript : MonoBehaviour {
	
	// These vars hold the menus for toggling.
	public Canvas startMenu, quitMenu, optionsMenu, loginMenu, registerQuestionMenu, registrationMenu;
	public GameObject connectionMgr;
	
	// Login menu contents
	public InputField username, password;
	public Text errorMessage;
	
	// Use this for initialization
	void Start () {
		startMenu = startMenu.GetComponent<Canvas>();
		quitMenu = quitMenu.GetComponent<Canvas> ();
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		loginMenu = loginMenu.GetComponent<Canvas> ();
		registerQuestionMenu = registerQuestionMenu.GetComponent<Canvas> ();
		registrationMenu = registrationMenu.GetComponent<Canvas> ();
		connectionMgr = connectionMgr.GetComponent<GameObject> ();
		
		username = username.GetComponent<InputField> ();
		password = password.GetComponent<InputField> ();
		errorMessage = errorMessage.GetComponent<Text> ();
		
		quitMenu.enabled = false;
		optionsMenu.enabled = false;
		loginMenu.enabled = false;
		registerQuestionMenu.enabled = false;
		registrationMenu.enabled = false;
		errorMessage.enabled = false;
	}
	
	// TO DO: Remember to save and load user prefs as necessary in these modes
	// (so, save prefs on exiting in-game options menus, and load upon entering either mode or
	//  changing scenes in-game.)

	// Load saved login data.
	public bool loadLogin() {
		StreamReader sr = null;	
		try {
			sr = new StreamReader ("Assets/misc/login.ini");
		} catch (System.Exception e) {
			// The login file doesn't exist (i.e. user hasn't even logged in yet). So, do nothing.
			print (e.Message);
			return false;
		}

		// Get username and password
		username.text = sr.ReadLine ().Substring (9);
		password.text = sr.ReadLine ().Substring (9);
		sr.Close ();
		return true;
	}

	// Save login data for quick multiplayer access.
	public void saveLogin() {
		// Gather info to put in the file.
		System.Text.StringBuilder sb = new System.Text.StringBuilder ();
		sb.AppendLine ("Username=" + username.text);
		sb.AppendLine ("Password=" + password.text);
		
		// Open a file for writing + overwrite the old settings (or create a new file).
		StreamWriter sw = new StreamWriter ("Assets/misc/login.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}

	// Start multiplayer.
	public void OnMPPress() {
		startMenu.enabled = false;
		
		// Attempt to load saved login data (name+pw).
		// If saved data does not exist, ask if the user is already registered.
		if (loadLogin ()) {
			loginMenu.enabled = true;
			return;
		}

		// At this point, loading login data must have failed, so ask the user if he/she has an account.
		registerQuestionMenu.enabled = true;
	}

	// This is for the RegisterQuestionMenu. If the user says he/she has played MP before, takes them to login screen.
	public void OnYesClick() {
		loginMenu.enabled = true;
		registerQuestionMenu.enabled = false;
	}

	// If the user says he/she has NOT played MP before, takes them to register screen.
	public void OnNoClick() {
		registrationMenu.enabled = true;
		registerQuestionMenu.enabled = false;
	}

	// Start single player.
	public void OnSPPress() {
		// code goes here
	}
	
	// Go to Options menu.
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
	
	// Pull up the quit menu if the Quit button on main menu is clicked.
	public void OnExitPress() {
		quitMenu.enabled = true;
		startMenu.enabled = false;
	}
	
	// If user cancels quitting the game, go back to main menu.
	public void ExitNotPressed() {
		quitMenu.enabled = false;
		startMenu.enabled = true;
	}
	
	// Quit the game...
	public void QuitGame() {
		Application.Quit ();
	}

	// Cancel logging in or registering for multiplayer, and go back to the main menu.
	public void MPQuit() {
		loginMenu.enabled = false;
		registrationMenu.enabled = false;
		errorMessage.enabled = false;
		startMenu.enabled = true;
	}

	public void LoginClick() {
		((GameScript)(connectionMgr.GetComponent(typeof(GameScript))))
													 .connectToMainServer (false);
	}
	
	public void RegisterClick() {
		((GameScript)(connectionMgr.GetComponent(typeof(GameScript))))
													 .connectToMainServer (true);
	}
}