using System; 
using System.Collections;
using System.IO;
using System.Reflection;
using System.Text;
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

public class MenuScript : MonoBehaviour {
	
	// These vars hold the menus for toggling.
	public Canvas startMenu, quitMenu, optionsMenu, loginMenu, registerQuestionMenu, registrationMenu;
	
	// Options menu contents
	public Slider musicScroll, sfxScroll;
	public InputField musicBox, sfxBox;
	public Toggle musicTgl, sfxTgl;
	public AudioSource music;
	
	// Login menu contents
	public InputField username, password;
	public int colorChange;
	public Text errorMessage;
	public string ip = "127.0.0.1";
	public int handshake = -775644979; // this is actually the value of "connectpls".hashCode() in java.
	
	public bool musicMuted, sfxMuted; //Used later for saving user prefs.

	// Use this for initialization
	void Start () {
		startMenu = startMenu.GetComponent<Canvas>();
		quitMenu = quitMenu.GetComponent<Canvas> ();
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		loginMenu = loginMenu.GetComponent<Canvas> ();
		registerQuestionMenu = registerQuestionMenu.GetComponent<Canvas> ();
		registrationMenu = registrationMenu.GetComponent<Canvas> ();
		
		musicScroll = musicScroll.GetComponent<Slider> ();
		sfxScroll = sfxScroll.GetComponent<Slider> ();
		musicBox = musicBox.GetComponent<InputField> ();
		sfxBox = sfxBox.GetComponent<InputField> ();
		musicTgl = musicTgl.GetComponent<Toggle> ();
		sfxTgl = sfxTgl.GetComponent<Toggle> ();
		music = music.GetComponent<AudioSource> ();
		music.ignoreListenerVolume = true; // enables individual handling of music and sfx volume levels
		
		username = username.GetComponent<InputField> ();
		password = password.GetComponent<InputField> ();
		errorMessage = errorMessage.GetComponent<Text> ();
		
		// Music is not muted by default. Might change due to user prefs.
		musicMuted = false;
		sfxMuted = false;
		
		quitMenu.enabled = false;
		optionsMenu.enabled = false;
		loginMenu.enabled = false;
		registerQuestionMenu.enabled = false;
		registrationMenu.enabled = false;
		errorMessage.enabled = false;
		
		loadPrefs (); // may change bool values above according to user prefs file if it exists.
	}
	
	// Remember to save and load user prefs as necessary in these modes
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
		savePrefs ();
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
	
	// Changes the slider value if the user puts a number in the text box to its right.
	public void MusicBoxChanged() {
		if (musicBox.text.Equals ("-"))
			musicBox.text = "";
		if (!musicBox.text.Equals ("")) {
			int input = Convert.ToInt16 (musicBox.text);
			int valToSet = (input > 100) ? 100 : input;
			musicScroll.value = valToSet;
			if (!musicMuted)
				music.volume = valToSet / 100.0f;
		}
	}
	
	// Changes the text in the music box if the slider value changes.
	public void MusicSliderChanged() {
		musicBox.text = "" + musicScroll.value;
		if (!musicMuted)
			music.volume = musicScroll.value / 100.0f;
	}
	
	// Changes the slider value if the user puts a number in the text box to its right.
	public void SFXBoxChanged() {
		if (sfxBox.text.Equals ("-"))
			sfxBox.text = "";
		if (!sfxBox.text.Equals ("")) {
			int input = Convert.ToInt16 (sfxBox.text);
			int valToSet = (input > 100) ? 100 : input;
			sfxScroll.value = valToSet;
			if (!sfxMuted)
				AudioListener.volume = valToSet / 100.0f;
		}
	}
	
	// Changes the text in the SFX box if the slider value changes.
	public void SFXSliderChanged() {
		sfxBox.text = "" + sfxScroll.value;
		if (!sfxMuted) 
			AudioListener.volume = sfxScroll.value / 100.0f;
	}
	
	// Changes muting of music.
	public void MuteMusicToggled() {
		music.volume = musicTgl.isOn ? 0.0f : musicScroll.value / 100.0f;
		musicMuted = musicTgl.isOn;
	}
	
	// Changes muting of sound effects.
	public void MuteSFXToggled() {
		AudioListener.volume = sfxTgl.isOn ? 0.0f : sfxScroll.value / 100.0f;
		sfxMuted = sfxTgl.isOn;
	}
	
	// Load user preferences (if the file exists).
	public void loadPrefs() {
		// Get preferences file if it exists (catch block covers otherwise -- see below)
		StreamReader sr = null;
		try {
			sr = new StreamReader ("Assets/misc/userPrefs.ini");
		}catch (System.Exception e) {
			// The prefs file doesn't exist (i.e. user hasn't even changed options yet). So, do nothing.
			print(e.StackTrace);
			return;
		}
		
		// Now that we have the prefs file, read each line and get values as strings.
		System.String volumeLine = sr.ReadLine (),
		sfxLine = sr.ReadLine (),
		musicMuteLine = sr.ReadLine (),
		sfxMuteLine = sr.ReadLine ();
		
		sr.Close ();
		
		// Grab the actual values from the lines.
		System.String mVol = volumeLine.Substring (12),
		sfxVol = sfxLine.Substring (10),
		mMute = musicMuteLine.Substring (11),
		sfxMute = sfxMuteLine.Substring (9);
		
		// Set muted bools first.
		musicTgl.isOn = System.Boolean.Parse (mMute);
		sfxTgl.isOn = System.Boolean.Parse (sfxMute);
		MuteMusicToggled ();
		MuteSFXToggled ();
		
		// Then, set sliders (and use methods to change BOTH boxes and volume).
		musicScroll.value = Convert.ToInt16 (mVol);
		sfxScroll.value = Convert.ToInt16 (sfxVol);
		MusicSliderChanged ();
		SFXSliderChanged ();
	}
	
	// Save user preferences.
	public void savePrefs() {
		// Start w/ music volume, SFX volume, and mute statuses.
		int musicVolume = (int)musicScroll.value,
		sfxVolume = (int)sfxScroll.value;
		// Gather info to put in the file.
		System.Text.StringBuilder sb = new System.Text.StringBuilder ();
		sb.AppendLine ("MusicVolume=" + musicVolume);
		sb.AppendLine ("SFXVolume=" + sfxVolume);
		sb.AppendLine ("MusicMuted=" + musicMuted);
		sb.AppendLine ("SFXMuted=" + sfxMuted);
		
		// Open a file for writing + overwrite the old settings (or create a new file).
		StreamWriter sw = new StreamWriter ("Assets/misc/userPrefs.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}
	
	public void MPQuit() {
		loginMenu.enabled = false;
		registrationMenu.enabled = false;
		errorMessage.enabled = false;
		startMenu.enabled = true;
	}

	public void connectToMainServer(bool firstTime) {
		// Connect to server address at port 1337 (main server).
		// CAUTION: Ted's IP currently unknown. Will ask later for testing. For now IP is set to 127.0.0.1.*********

		// TO DO: Provisions in case main server or subserver is down.
		Socket cnxn = null;

		try {
			// 1st step: Connect to main server and send handshake.
			cnxn = new Socket (ip, 1337);

			ObjectOutputStream output = new ObjectOutputStream (cnxn.getOutputStream ());
			output.writeInt (handshake);
			output.flush ();
			
			// Must now send username.
			output.writeObject (username.text);
			output.flush ();

			// Receive whatever port the server sends (random or determined).
			ObjectInputStream input = new ObjectInputStream (cnxn.getInputStream ());
			int nextPort = input.readInt ();

			// Close streams and connection.
			input.close ();
			output.close ();
			cnxn.close ();

			// At this point, either log in or sign up.
			if (firstTime)
				signup (nextPort);
			else
				login (nextPort);
					
		} catch (java.lang.Exception e) {
			if (cnxn == null)
				print ("Failed to connect");
			print ("idk");

			print (e.getStackTrace());
			return;
		} catch (System.Exception e) {
			if (cnxn == null)
				print ("Failed to connect");
			print ("idk");
			print (e.StackTrace);
			return;
		} 
	}

	public void login (int port) {
		// TO DO: Make provisions in case the user is not signed up. OR the subserver is down.
		// TO DO: Save login data on successful login.
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

			output.writeObject(username.text);
			output.flush ();

			output.writeObject(password.text);
			output.flush ();

			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			if (input.readInt () != SPU.ServerResponse.LOGIN_OK.ordinal()) {
				// Login failed.
				errorMessage.enabled = true;
				//output.writeInt(SPU.Command.DISCONNECT.ordinal());
				//output.flush();
				input.close();
				output.close ();
				cnxn.close ();
				return;
			}

			// At this point, login was successful.
			// HOWEVER no graphical stuff is in place to support the player doing stuff on the map.
			// I will temporarily force logout once logged in.
			errorMessage.enabled = false;
			output.writeInt (SPU.Command.LOGOUT.ordinal());
			output.flush();
			input.close ();
			output.close ();
			cnxn.close ();
			loginMenu.enabled = false;
			startMenu.enabled = true;
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
			output.writeObject(username.text);
			output.flush ();

			output.writeObject (password.text);
			output.flush ();

			// Check if acc was created
			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			bool actCreated = true;
			if (input.readInt () != SPU.ServerResponse.ACCOUNT_CREATE_OK.ordinal()) {
				print ("Account creation failed.");
				actCreated = false;
			}

			// At this point, the user is (hopefully) signed up for the server on the given port. So, log in.
			cnxn.close ();
			output.close();
			input.close();

			if (actCreated) 
				login (port);

		}catch (java.lang.Exception e) {
			print("Encountered a Java exception:\n");
			e.printStackTrace();
		}catch (System.Exception e) {
			print ("Encountered a C# exception:\n");
			print (e.StackTrace);
		}
	}

	public void LoginClick() {
		connectToMainServer (false);
	}

	public void RegisterClick() {
		connectToMainServer (true);
	}

	// Useless for me AFAIK.
	void Update () {}
}