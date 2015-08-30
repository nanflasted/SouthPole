using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;
using System.Collections;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168
#pragma warning disable 0219
#pragma warning disable 0414

// This script basically just manages button sounds, which play when hovering over and clicking on a button.
public class ButtonScript : MonoBehaviour, IPointerEnterHandler, IPointerDownHandler {

	public AudioSource hoverSound, clickSound; // self-explanatory

	// Use this for initialization
	void Start () {
		hoverSound = hoverSound.GetComponent<AudioSource> ();
		clickSound = clickSound.GetComponent<AudioSource> ();
	}

	// "Hover" sound
	public void OnPointerEnter(PointerEventData d){
		hoverSound.Play ();
	}

	// Click sound
	public void OnPointerDown(PointerEventData d) {
		clickSound.Play ();
	}
}