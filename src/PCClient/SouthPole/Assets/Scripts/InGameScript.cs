using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.UI;

// TO DO: Volume on scene start.

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

public class InGameScript : MonoBehaviour {

	public Canvas pauseMenu, quitConfirmMenu, mainMenuOptionCanvas, quitOptionCanvas, optionsMenu;
	public Text exitText;

	// These are ONLY used for the method savePrefs() (which couldn't be implemented in OptionsScript).
	public InputField musicBox, sfxBox;
	public Toggle musicTgl, sfxTgl;

	// Use this for initialization
	public void Start () {
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		optionsMenu.enabled = false;

		pauseMenu = pauseMenu.GetComponent<Canvas> ();
		pauseMenu.enabled = false;

		quitConfirmMenu = quitConfirmMenu.GetComponent<Canvas> ();
		quitConfirmMenu.enabled = false;

		musicBox = musicBox.GetComponent<InputField> ();
		sfxBox = sfxBox.GetComponent<InputField> ();
		musicTgl = musicTgl.GetComponent<Toggle> ();
		sfxTgl = sfxTgl.GetComponent<Toggle> ();

		mainMenuOptionCanvas = mainMenuOptionCanvas.GetComponent<Canvas> ();
		quitOptionCanvas = quitOptionCanvas.GetComponent<Canvas> ();
		exitText = exitText.GetComponent<Text> ();
		mainMenuOptionCanvas.enabled = false;
		quitOptionCanvas.enabled = false;
	}

	public void ContinueClick() {
		pauseMenu.enabled = false;
	}

	public void OptionsClick() {
		pauseMenu.enabled = false;
		optionsMenu.enabled = true;
	}

	public void OkClick() {
		pauseMenu.enabled = true;
		optionsMenu.enabled = false;
		savePrefs ();
	}

	public void MainMenuClick() {
		pauseMenu.enabled = false;
		quitConfirmMenu.enabled = true;
		exitText.text = "Are you sure you want to return to the main menu?";
		mainMenuOptionCanvas.enabled = true;
		quitOptionCanvas.enabled = false;
	}

	public void QuitClick() {
		pauseMenu.enabled = false;
		quitConfirmMenu.enabled = true;
		exitText.text = "Are you sure you want to quit South Pole?";
		mainMenuOptionCanvas.enabled = false;
		quitOptionCanvas.enabled = true;
	}

	public void NoClick() {
		pauseMenu.enabled = true;
		quitConfirmMenu.enabled = false;
		mainMenuOptionCanvas.enabled = false;
		quitOptionCanvas.enabled = false;
	}

	// Save user preferences. EXACT replica of the method in MenuScript (but this can't transfer to the other script).
	public void savePrefs() {
		// Start w/ music volume, SFX volume, and mute statuses.
		int musicVolume = Convert.ToInt16(musicBox.text),
		sfxVolume = Convert.ToInt16 (sfxBox.text);
		// Gather info to put in the file.
		System.Text.StringBuilder sb = new System.Text.StringBuilder ();
		sb.AppendLine ("MusicVolume=" + musicVolume);
		sb.AppendLine ("SFXVolume=" + sfxVolume);
		sb.AppendLine ("MusicMuted=" + musicTgl.isOn);
		sb.AppendLine ("SFXMuted=" + sfxTgl.isOn);
		
		// Open a file for writing + overwrite the old settings (or create a new file).
		StreamWriter sw = new StreamWriter ("Assets/misc/userPrefs.ini");
		sw.Write (sb.ToString ());
		sw.Close ();
	}

	// Update is called once per frame
	public void Update () {
		// Check for keys pressed
		if (Input.GetKeyDown (KeyCode.Escape)) {
			// Toggle pause menu if pressed
			// TO DO: Check if this is possible while in another menu. If so, fix it.

			pauseMenu.enabled = !pauseMenu.enabled;
			if (quitConfirmMenu.enabled) {
				quitConfirmMenu.enabled = false;
				mainMenuOptionCanvas.enabled = false;
				quitOptionCanvas.enabled = false;
			}
			if (optionsMenu.enabled) {
				optionsMenu.enabled = false;
				savePrefs();
			}
		}
	}
}