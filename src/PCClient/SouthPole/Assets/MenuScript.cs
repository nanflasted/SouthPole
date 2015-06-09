using System; 
using System.Collections;
using System.IO;
using System.Text;
using UnityEngine;
using UnityEngine.UI;

// **NOTE FOR UNITY WEB PLAYER: StackOverflow response says some IKVM stuff DOES NOT WORK on there.**
// **BEWARE if you are having trouble getting IKVM to work.**
using java.lang;
using java.io;
using java.net;

public class MenuScript : MonoBehaviour {
	
	// These vars hold the menus for toggling.
	public Canvas startMenu, quitMenu, optionsMenu, loginMenu;
	
	// Options menu contents
	public Slider musicScroll, sfxScroll;
	public InputField musicBox, sfxBox;
	public Toggle musicTgl, sfxTgl;
	public AudioSource music;
	
	// Login menu contents
	public InputField username, password;
	public int colorChange;
	public string ip = "127.0.0.1";
	public int handshake = -775644979; // this is actually the value of "connectpls".hashCode() in java (NOT IKVM)
	
	public bool musicMuted, sfxMuted; //Used later for saving user prefs.
	
	// Use this for initialization
	void Start () {
		startMenu = startMenu.GetComponent<Canvas>();
		quitMenu = quitMenu.GetComponent<Canvas> ();
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		loginMenu = loginMenu.GetComponent<Canvas> ();
		
		musicScroll = musicScroll.GetComponent<Slider> ();
		sfxScroll = sfxScroll.GetComponent<Slider> ();
		musicBox = musicBox.GetComponent<InputField> ();
		sfxBox = sfxBox.GetComponent<InputField> ();
		musicTgl = musicTgl.GetComponent<Toggle> ();
		sfxTgl = sfxTgl.GetComponent<Toggle> ();
		music = music.GetComponent<AudioSource> ();
		music.ignoreListenerVolume = true; // enables individual handling of music and sfx volumes
		
		username = username.GetComponent<InputField> ();
		password = password.GetComponent<InputField> ();
		
		// Music is not muted by default. Might change due to user prefs.
		musicMuted = false;
		sfxMuted = false;
		
		quitMenu.enabled = false;
		optionsMenu.enabled = false;
		loginMenu.enabled = false;
		
		loadPrefs (); // may change bool values above according to prefs file if it exists.
	}
	
	// Remember to save and load user prefs as necessary in these modes
	// (so, save prefs on exiting in-game options menus, and load upon entering either mode or
	//  changing scenes in-game.)
	
	// Start multiplayer.
	public void OnMPPress() {
		loginMenu.enabled = true;
		startMenu.enabled = false;
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
			sr = new StreamReader ("Assets/userPrefs.ini");
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
		StreamWriter sw = new StreamWriter ("Assets/userPrefs.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}
	
	public void MPQuit() {
		loginMenu.enabled = false;
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
			string name = username.text;
			output.writeObject (name);
			output.flush ();
			print (name);
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

		// Code goes here
	}
	
	public void signup (int port) {
		// TO DO: Make provisions in case the username is already taken. OR the subserver is down.

		// First, connect to the subserver at the given port.
		Socket cnxn = null;
		try {
			cnxn = new Socket(ip, port);
			ObjectOutputStream output = new ObjectOutputStream(cnxn.getOutputStream());
			//output.write(handshake);
			/* Ted:"Do NOT use output.write(), strictly stick to output.writeInt(), cuz otherwise the readInt()
			 * process on the server end wait for an entire Int of 4 bytes while you only write 1 byte, and 
			 * it recognizes the byte you wrote as some other int number because of endianness. More seriously,
			 * it could cause an EOFException on the server if you write() then close the stream; that will cause
			 * major trouble if the server was set to run continuously. I've corrected all the following write() 
			 * to writeInt(). And also, always flush()."
			 */
			output.writeInt (handshake);
			output.flush ();

			// Now that we have connected and sent our handshake, we can send commands.
			// Here we will just sign up, close the connection, and log in using the given name and PW.

			output.writeInt (1); // this corresponds to the sign-in command
			output.flush ();

			// Send username and PW, make sure account name is not taken.
			output.writeObject(username.text);
			output.flush ();


			ObjectInputStream input = new ObjectInputStream(cnxn.getInputStream());
			/* Ted:"you really can't do this. If you readInt() twice while the server end only writes 
			 * 1 int, you'll get stuck and so will the server cuz it is waiting for you to send other 
			 * stuff (your password in this case). Yes you will eventually time out, but at least that 
			 * thread will get stuck there forever."

			if (input.readInt() == 2) {
				//Temp
				print ("Name taken");
				output.write (8);
				output.flush ();
				cnxn.close ();
				output.close ();
				input.close ();
				return;
			}*/
			output.writeObject (password.text);
			output.flush ();

			//Check if acc was created
			/* Ted:"Just using this thing is enough. If you really need a 'name taken' error message from 
			 * server consider telling me or just add it to the Utility.SPU. Also, it might be a good 
			 * idea to figure out a way to import Utility.SPU into your client so that your client don't
			 * have hard coding. Using numbers will be extremely annoying when the SPU.Command gets 
			 * changed, then you'll have to rewrite every single command ordinals in your entire program, 
			 * not to say it will be extremely difficult to track them down because they are just numbers,
			 * unlike SPU.Command.ACCOUNT_CREATE_OK.ordinal(), which you can simply search for and replace."
			 */
			Thread.sleep (2000);
			int acstatus = input.readInt ();
			print (acstatus);
			if (acstatus != 3) { //Ted:"means to avoid this. avoid using 3, or 2 in the above case, directly."
				print ("REKT");
			}

			// Log out!
			/* Ted:"No need to log out when signing up. You are not logged in anyways."
			output.writeInt (8); // log-out command
			output.flush ();
			*/
			// At this point, the user is signed up for the server on the given port. So, log in and start playing!
			cnxn.close ();
			output.close();
			input.close();
			//login (port);
			print ("IT SUPER WORKED");

		}catch (java.lang.Exception e) {
			print("DIDNT WORK - Java");
		}catch (System.Exception e) {
			print ("DIDNT WORK - C#");
		}
	}

	public void LoginClick() {
		connectToMainServer (false);
	}

	public void RegisterClick() {
		connectToMainServer (true);
	}

	// Useless for me AFAIK.
	/* Ted:"This is for something to be changed every frame."
	 */
	void Update () {}
}