package com.nisovin.keybinder;

import org.bukkitcontrib.keyboard.Keyboard;
import org.bukkitcontrib.keyboard.KeyboardBinding;
import org.bukkitcontrib.player.ContribPlayer;

public class Keybind implements KeyboardBinding {
	
	private KeybindState state;
	private Keyboard key;
	private String command;
	
	public Keybind(Keyboard key, String command) {
		this.key = key;
		this.command = command;
		state = KeybindState.NORMAL;
	}
	
	public Keybind() {
		state = KeybindState.WAITING_ON_KEYBIND;
	}
	
	public void setKey(Keyboard key) {
		this.key = key;
		state = KeybindState.WAITING_ON_COMMAND;
	}
	
	public void setCommand(String command) {
		this.command = command;
		state = KeybindState.NORMAL;
	}

	public KeybindState getState() {
		return state;
	}
	
	public Keyboard getKey() {
		return key;
	}
	
	public String getCommand() {
		return command;
	}
	
	@Override
	public void onPostKeyPress(ContribPlayer player) {
		player.performCommand(command);
	}

	@Override
	public void onPostKeyRelease(ContribPlayer arg0) {
	}

	@Override
	public void onPreKeyPress(ContribPlayer arg0) {
	}

	@Override
	public void onPreKeyRelease(ContribPlayer arg0) {
	}
	
	protected enum KeybindState {
		WAITING_ON_KEYBIND, WAITING_ON_COMMAND, NORMAL
	}

}
