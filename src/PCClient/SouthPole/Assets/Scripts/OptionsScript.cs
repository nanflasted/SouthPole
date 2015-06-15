using System;
using System.Collections;
using System.IO;
using System.Text;
using UnityEngine;
using UnityEngine.UI;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

// This script deals with managing user preferences in the main menu and in the game.
// Currently only audio volumes/muting are included, but we can add other options 
// (i.e. graphics, resolution) later on in development.
public class OptionsScript : MonoBehaviour {
	
	AudioSource music; // background music

	// Options menu contents
	Slider musicScroll, sfxScroll;
	InputField musicBox, sfxBox;
	Toggle musicTgl, sfxTgl;
	bool musicMuted, sfxMuted; //Used later for saving user prefs.
	
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
		
		// Music is not muted by default. Might change due to saved user preferences.
		musicMuted = false;
		sfxMuted = false;
		
		loadPrefs (); // may change bool values above according to user prefs file if it exists.
		music.Play (); // Play background music on awake. (This should NOT be toggled in the Unity editor due to a volume bug.)
	}

	/*****          Music volume settings          *****/

	// Changes the slider value (and volume if not muted) if the user puts a number in the text box to its right.
	public void MusicBoxChanged() {
		// The following statement prevents the user from entering negative volumes.
		if (musicBox.text.Equals ("-"))
			musicBox.text = "";

		// The slider value will change if a nonnegative value is input to the text box.
		if (!musicBox.text.Equals ("")) {
			int input = Convert.ToInt16 (musicBox.text);
			int valToSet = (input > 100) ? 100 : input; // keep volume level at or below 100
			musicScroll.value = valToSet;
			if (!musicMuted)
				music.volume = valToSet / 100.0f;  // set volume if not muted
		}
	}
	
	// Changes the text in the music box if the slider value changes.
	public void MusicSliderChanged() {
		musicBox.text = "" + musicScroll.value;
		if (!musicMuted)
			music.volume = musicScroll.value / 100.0f; // set volume if not muted
	}
	
	// Changes muting of music.
	public void MuteMusicToggled() {
		music.volume = musicTgl.isOn ? 0.0f : musicScroll.value / 100.0f;
		musicMuted = musicTgl.isOn;
	}

	/*****          Sound effects volume settings          *****/

	// Changes the SFX slider value (and SFX volume if not muted) if the user puts a number in the text box to its right.
	public void SFXBoxChanged() {
		// Prevent user from entering negative volume
		if (sfxBox.text.Equals ("-"))
			sfxBox.text = "";

		// The slider value will change if a nonnegative value is input to the text box.
		if (!sfxBox.text.Equals ("")) {
			int input = Convert.ToInt16 (sfxBox.text);
			int valToSet = (input > 100) ? 100 : input; // keep volume level under 100
			sfxScroll.value = valToSet;
			if (!sfxMuted)
				AudioListener.volume = valToSet / 100.0f; // change SFX volume if not muted
		}
	}
	
	// Changes the text in the SFX box if the slider value changes.
	public void SFXSliderChanged() {
		sfxBox.text = "" + sfxScroll.value;
		if (!sfxMuted) 
			AudioListener.volume = sfxScroll.value / 100.0f;
	}
	
	// Changes muting of sound effects.
	public void MuteSFXToggled() {
		AudioListener.volume = sfxTgl.isOn ? 0.0f : sfxScroll.value / 100.0f;
		sfxMuted = sfxTgl.isOn;
	}

	/*****	          Saving and Loading User Preferences          *****/

	// Load user preferences (if the file exists) and set the corresponding options in the game.
	public void loadPrefs() {
		// Get preferences file if it exists (catch block covers otherwise -- see below)
		StreamReader sr = null;
		try {
			sr = new StreamReader ("Assets/misc/userPrefs.ini");
		}catch (Exception e) {
			// The prefs file doesn't exist (i.e. user hasn't even changed options yet). So, do nothing.
			return;
		}
		
		// Now that we have the prefs file, read each line and get values as strings.
		String volumeLine = sr.ReadLine (),
				   sfxLine = sr.ReadLine (),
				   musicMuteLine = sr.ReadLine (),
				   sfxMuteLine = sr.ReadLine ();
		
		sr.Close (); // done reading from file
		
		// Grab the actual values from the lines.
		String mVol = volumeLine.Substring (12),
				   sfxVol = sfxLine.Substring (10),
				   mMute = musicMuteLine.Substring (11),
				   sfxMute = sfxMuteLine.Substring (9);
		
		// Set muted bools first.
		musicTgl.isOn = Boolean.Parse (mMute);
		sfxTgl.isOn = Boolean.Parse (sfxMute);

		// Change mute statuses and volumes depending on values from prefs file.
		MuteMusicToggled (); 
		MuteSFXToggled ();
		
		// Then, set sliders (and use methods to change BOTH boxes and volume).
		musicScroll.value = Convert.ToInt16 (mVol);
		sfxScroll.value = Convert.ToInt16 (sfxVol);

		// Set text box values automatically
		MusicSliderChanged (); 
		SFXSliderChanged ();
	}

	// Save user preferences.
	public void savePrefs() {
		// Start w/ music volume, SFX volume, and mute statuses.
		int musicVolume = Convert.ToInt16(musicBox.text),
			 sfxVolume = Convert.ToInt16 (sfxBox.text);
		// Gather info to put in the file.
		StringBuilder sb = new StringBuilder ();
		sb.AppendLine ("MusicVolume=" + musicVolume);
		sb.AppendLine ("SFXVolume=" + sfxVolume);
		sb.AppendLine ("MusicMuted=" + musicTgl.isOn);
		sb.AppendLine ("SFXMuted=" + sfxTgl.isOn);
		
		// Open a file for writing + overwrite the old settings (or create a new file).
		StreamWriter sw = new StreamWriter ("Assets/misc/userPrefs.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}
}