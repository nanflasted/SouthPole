using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;
using System.Collections;

// Disable "variable declared but not used" warnings
#pragma warning disable 0168

public class ButtonScript : MonoBehaviour, IPointerEnterHandler, IPointerDownHandler {

	public AudioSource hover, click;

	// Use this for initialization
	void Start () {
		hover = hover.GetComponent<AudioSource> ();
		click = click.GetComponent<AudioSource> ();
	}
	
	public void OnPointerEnter(PointerEventData d){
		hover.Play ();
	}
	
	public void OnPointerDown(PointerEventData d) {
		click.Play ();
	}
}