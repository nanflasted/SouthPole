using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.UI;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

// TO DO: Volume on start menu.

public class OptionsScript : MonoBehaviour {

	// Options menu contents
	public Slider musicScroll, sfxScroll;
	public InputField musicBox, sfxBox;
	public Toggle musicTgl, sfxTgl;
	public AudioSource music;
	public bool musicMuted, sfxMuted; //Used later for saving user prefs.
	
	// Initialization
	void Start () {
		musicScroll = musicScroll.GetComponent<Slider> ();
		sfxScroll = sfxScroll.GetComponent<Slider> ();
		musicBox = musicBox.GetComponent<InputField> ();
		sfxBox = sfxBox.GetComponent<InputField> ();
		musicTgl = musicTgl.GetComponent<Toggle> ();
		sfxTgl = sfxTgl.GetComponent<Toggle> ();
		music = music.GetComponent<AudioSource> ();
		music.ignoreListenerVolume = true; // enables individual handling of music and sfx volume levels
		
		// Music is not muted by default. Might change due to user prefs.
		musicMuted = false;
		sfxMuted = false;
		
		loadPrefs (); // may change bool values above according to user prefs file if it exists.
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

	// ( ͡° ͜ʖ ͡°)
	void Update () {}
}
