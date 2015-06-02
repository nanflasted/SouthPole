using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System;
using System.IO;
using System.Text;
using System.Collections.Generic;

public class MenuScript : MonoBehaviour {

	//Main menu buttons and quit menu
	public Canvas startMenu, quitMenu, optionsMenu;
	public Text SPTitle;
	public Button mpBtn;
	public Button spBtn; 
	public Button optionsBtn; 
	public Button exitBtn;

	//Options menu stuff
	public Text optionsTitle;
	public Button OKButton;
	public Text musicVol, sfxVol;
	public Slider musicScroll, sfxScroll;
	public InputField musicBox, sfxBox;
	public Toggle musicTgl, sfxTgl;
	public Text musicMute, sfxMute;
	public AudioSource music;

	//Used later for saving user prefs.
	public bool musicMuted;
	public bool sfxMuted;

	// Use this for initialization
	void Start () {
		startMenu = startMenu.GetComponent<Canvas> ();
		quitMenu = quitMenu.GetComponent<Canvas> ();
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		SPTitle = SPTitle.GetComponent<Text> ();
		mpBtn = mpBtn.GetComponent<Button> ();
		spBtn = spBtn.GetComponent<Button> ();
		optionsBtn = optionsBtn.GetComponent<Button> ();
		exitBtn = exitBtn.GetComponent<Button> ();

		optionsTitle = optionsTitle.GetComponent<Text> ();
		OKButton = OKButton.GetComponent<Button> ();
		musicVol = musicVol.GetComponent<Text> ();
		sfxVol = sfxVol.GetComponent<Text> ();
		musicScroll = musicScroll.GetComponent<Slider> ();
		sfxScroll = sfxScroll.GetComponent<Slider> ();
		musicBox = musicBox.GetComponent<InputField> ();
		sfxBox = sfxBox.GetComponent<InputField> ();
		musicTgl = musicTgl.GetComponent<Toggle> ();
		sfxTgl = sfxTgl.GetComponent<Toggle> ();
		musicMute = musicMute.GetComponent<Text> ();
		sfxMute = sfxMute.GetComponent<Text> ();

		music = music.GetComponent<AudioSource> ();
		music.ignoreListenerVolume = true;

		//Temporary
		musicMuted = false;
		sfxMuted = false;

		quitMenu.enabled = false;
		optionsMenu.enabled = false;

		loadPrefs ();
	}

	// The following two methods will only be created once we get MP and SP modes working.
	// Remember to save and load user prefs as necessary in these modes
	// (so, save prefs on exiting in-game options menus, and load upon entering either mode or
	//  changing scenes in-game.)

	public void OnMPPress() {
		// code goes here
	}

	public void OnSPPress() {
		// code goes here
	}

	public void OptionsPress() {
		startMenu.enabled = false;
		optionsMenu.enabled = true;
	}

	public void OKPress() {
		optionsMenu.enabled = false;
		startMenu.enabled = true;
		savePrefs ();
	}
	
	public void OnExitPress() {
		quitMenu.enabled = true;
		mpBtn.enabled = false;
		spBtn.enabled = false;
		optionsBtn.enabled = false;
		exitBtn.enabled = false;
	}
	
	public void ExitNotPressed() {
		quitMenu.enabled = false;
		mpBtn.enabled = true;
		spBtn.enabled = true;
		optionsBtn.enabled = true;
		exitBtn.enabled = true;
	}
	
	public void QuitGame() {
		Application.Quit ();
	}

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

	public void MusicSliderChanged() {
		musicBox.text = "" + musicScroll.value;
		if (!musicMuted)
			music.volume = musicScroll.value / 100.0f;
	}
		
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
	
	public void SFXSliderChanged() {
		sfxBox.text = "" + sfxScroll.value;
		if (!sfxMuted) 
			AudioListener.volume = sfxScroll.value / 100.0f;
	}

	public void MuteMusicToggled() {
		//If music has been muted, mute it and change the bool to true.
		//Otherwise, unmute it and change bool to false.
		//To do: Save settings in I/O later.
		music.volume = musicTgl.isOn ? 0.0f : musicScroll.value / 100.0f;
		musicMuted = musicTgl.isOn;
	}

	public void MuteSFXToggled() {
		//If SFX have been muted, mute them and change the bool to true.
		//Otherwise, unmute them and change bool to false.
		//To do: Save settings in I/O later.
		AudioListener.volume = sfxTgl.isOn ? 0.0f : sfxScroll.value / 100.0f;
		sfxMuted = sfxTgl.isOn;
	}

	public void loadPrefs() {
		// First, get the preferences file.
		StreamReader sr = null;
		try {
			sr = new StreamReader ("Assets/userPrefs.ini");
		}catch (Exception e) {
			// The prefs file doesn't exist (i.e. user has not even changed his options yet).
			// So do nothing.
			return;
		}

		// Then, read each line and get values as strings.
		String volumeLine = sr.ReadLine (),
			   sfxLine = sr.ReadLine (),
			   musicMuteLine = sr.ReadLine (),
			   sfxMuteLine = sr.ReadLine ();
		
		sr.Close ();

		String mVol = volumeLine.Substring (12),
			   sfxVol = sfxLine.Substring (10),
			   mMute = musicMuteLine.Substring (11),
			   sfxMute = sfxMuteLine.Substring (9);

		// Set muted bools first.
		musicTgl.isOn = Boolean.Parse (mMute);
		sfxTgl.isOn = Boolean.Parse (sfxMute);
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
		StringBuilder sb = new StringBuilder ();
		sb.AppendLine ("MusicVolume=" + musicVolume);
		sb.AppendLine ("SFXVolume=" + sfxVolume);
		sb.AppendLine ("MusicMuted=" + musicMuted);
		sb.AppendLine ("SFXMuted=" + sfxMuted);

		// Open a file for writing + write stuff to it.
		StreamWriter sw = new StreamWriter ("Assets/userPrefs.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}

	// Update is USELESS	
	void Update () {
	}
}