using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;
using System.Collections;

public class ButtonScript : MonoBehaviour, IPointerEnterHandler, IPointerDownHandler {

	public AudioSource hover;
	public AudioSource click;

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
	
	// Update is called once per frame
	void Update () {
	
	}
}
