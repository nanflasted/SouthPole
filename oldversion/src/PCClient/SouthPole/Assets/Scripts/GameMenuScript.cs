﻿using System;
using System.Collections;
using System.IO;
using UnityEngine;
using UnityEngine.UI;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168
#pragma warning disable 0219
#pragma warning disable 0414

// This class manages the in-game menu.
// From there, you can choose to continue playing the game, change options, quit to the main menu, or quit the game entirely.
public class GameMenuScript : MonoBehaviour {

	// Menus used
	public Canvas pauseMenu, quitConfirmMenu, mainMenuOptionCanvas, quitOptionCanvas, optionsMenu;

	public Text exitText; // this text displays when quitting to either the main menu or to the desktop, and changes based on your option.

	// Use this for initialization
	void Start () {

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

	// Return to the game.
	public void ContinueClick() {
		pauseMenu.enabled = false;
	}

	// Open the options menu.
	public void OptionsClick() {
		pauseMenu.enabled = false;
		optionsMenu.enabled = true;

		((OptionsScript)(optionsMenu.GetComponent (typeof(OptionsScript)))).saveVals ();
	}

	// Close the options menu and save changes.
	public void OkClick() {
		pauseMenu.enabled = true;
		optionsMenu.enabled = false;
		((OptionsScript)(optionsMenu.GetComponent(typeof(OptionsScript)))).savePrefs ();
	}

	// Close the options menu and revert changes.
	public void CancelClick() {
		pauseMenu.enabled = true;
		optionsMenu.enabled = false;
		((OptionsScript)(optionsMenu.GetComponent (typeof(OptionsScript)))).Revert ();
	}

	// Ask if the user wants to return to the main menu.
	public void MainMenuClick() {
		pauseMenu.enabled = false;
		quitConfirmMenu.enabled = true;
		exitText.text = "Are you sure you want to return to the main menu?";
		mainMenuOptionCanvas.enabled = true;
		quitOptionCanvas.enabled = false;
	}

	// Log out and go back to the main menu.
	public void QuitToMainMenu() {
		// I have not been able to test this method or QuitToDesktop().
		// If you experience problems, try removing the comment slashes from the next line and commenting out the line after it.
		// ((GameScript)(GameObject.Find ("Connection Manager"))).setLogout (false);
		((GameScript)(GameObject.Find ("Connection Manager").GetComponent (typeof(GameScript)))).setLogout (false);
	}

	// Ask if the user wants to quit to the desktop.
	public void QuitClick() {
		pauseMenu.enabled = false;
		quitConfirmMenu.enabled = true;
		exitText.text = "Are you sure you want to quit South Pole?";
		mainMenuOptionCanvas.enabled = false;
		quitOptionCanvas.enabled = true;
	}

	// Log out and close the game entirely.
	public void QuitToDesktop() {
		// I have not been able to test this method or QuitToMainMenu().
		// If you experience problems, try removing the comment slashes from the next line and commenting out the line after it.
		// ((GameScript)(GameObject.Find ("Connection Manager"))).setLogout (true);
		((GameScript)(GameObject.Find ("Connection Manager").GetComponent (typeof(GameScript)))).setLogout (true);
	}

	// The "No" button appears when the user is asked if he/she wants to quit to the main menu or to the desktop.
	// If this button is clicked, the user simply goes back to the pause menu.
	public void NoClick() {
		pauseMenu.enabled = true;
		quitConfirmMenu.enabled = false;
		mainMenuOptionCanvas.enabled = false;
		quitOptionCanvas.enabled = false;
	}
}