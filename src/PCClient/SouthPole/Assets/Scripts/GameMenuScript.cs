using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.UI;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

public class GameMenuScript : MonoBehaviour {

	public Canvas pauseMenu, quitConfirmMenu, mainMenuOptionCanvas, quitOptionCanvas, optionsMenu;
	public Text exitText;

	// Use this for initialization
	public void Start () {

		pauseMenu = pauseMenu.GetComponent<Canvas> ();
		quitConfirmMenu = quitConfirmMenu.GetComponent<Canvas> ();
		mainMenuOptionCanvas = mainMenuOptionCanvas.GetComponent<Canvas> ();
		quitOptionCanvas = quitOptionCanvas.GetComponent<Canvas> ();
		optionsMenu = optionsMenu.GetComponent<Canvas> ();
		exitText = exitText.GetComponent<Text> ();

		pauseMenu.enabled = false;
		quitConfirmMenu.enabled = false;
		mainMenuOptionCanvas.enabled = false;
		quitOptionCanvas.enabled = false;
		optionsMenu.enabled = false;
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
		((OptionsScript)(optionsMenu.GetComponent(typeof(OptionsScript)))).savePrefs ();
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

	public void QuitToMainMenu() {
		((GameScript)(GameObject.Find ("Connection Manager").GetComponent (typeof(GameScript)))).logout ();
		Application.LoadLevel (0);
	}

	public void QuitToDesktop() {
		((GameScript)(GameObject.Find ("Connection Manager").GetComponent (typeof(GameScript)))).logout ();
		// Wait a bit for connection to close
		new WaitForSeconds (3.0f);
		Application.Quit ();
	}

	// Update is used to respond to key presses
	public void Update () {
		// Check for keys pressed
		if (Input.GetKeyDown (KeyCode.Escape)) {
			// Toggle pause menu if pressed

			pauseMenu.enabled = !pauseMenu.enabled;
			if (quitConfirmMenu.enabled) {
				quitConfirmMenu.enabled = false;
				mainMenuOptionCanvas.enabled = false;
				quitOptionCanvas.enabled = false;
			}
			if (optionsMenu.enabled) {
				optionsMenu.enabled = false;
				((OptionsScript)(optionsMenu.GetComponent(typeof(OptionsScript)))).savePrefs ();
			}
		}
	}
}