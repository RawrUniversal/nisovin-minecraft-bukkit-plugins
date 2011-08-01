package com.nisovin.realrp.character;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.Player;

public class CharacterCreator {
	
	private static ArrayList<State> stateOrder = new ArrayList<State>();
	static {
		stateOrder.add(State.GET_FIRST_NAME); 
		stateOrder.add(State.GET_LAST_NAME); 
		stateOrder.add(State.GET_AGE); 
		stateOrder.add(State.GET_SEX); 
		stateOrder.add(State.GET_DESCRIPTION); 
	}
	
	private static final String firstNameRegex = "^[A-Z][a-z][A-Za-z]+$";
	
	private Player player;
	private State state;	
	private String firstName;
	
	public CharacterCreator(Player player) {
		this.player = player;
		this.state = State.GET_FIRST_NAME;
	}
	
	public void nextState() {
		int i = stateOrder.indexOf(state) + 1;
		if (i == stateOrder.size()) {
			// done
		} else {
			state = stateOrder.get(i);
		}
	}
	
	public void askForInformation() {
		if (state == State.GET_FIRST_NAME) {
			player.sendMessage("Please enter your character's first name.");
		}
	}
	
	public void onChat(String message) {
		if (state == State.GET_FIRST_NAME) {
			if (message.matches(firstNameRegex)) {
				firstName = message;
				player.sendMessage("Your first name is: " + firstName);
				nextState();
				askForInformation();
			}
		}
	}
	
	private enum State {
		GET_FIRST_NAME,
		GET_LAST_NAME,
		GET_AGE,
		GET_SEX,
		GET_DESCRIPTION
	}
	
}
