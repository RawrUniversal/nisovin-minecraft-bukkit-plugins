package com.nisovin.keybinder;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkitcontrib.event.input.InputListener;
import org.bukkitcontrib.event.input.KeyPressedEvent;

import com.nisovin.keybinder.Keybind.KeybindState;

public class KeybinderInputListener extends InputListener {

	public KeybinderInputListener(Keybinder plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onKeyPressedEvent(KeyPressedEvent event) {
		Player player = event.getPlayer();
		Keybind keybind = Keybinder.newKeybinds.get(event.getPlayer());
		if (keybind != null && keybind.getState() == KeybindState.WAITING_ON_KEYBIND) {
			keybind.setKey(event.getKey());
			event.getPlayer().sendMessage("Key selected. Please type the command you wish to bind.");
		} else {
			KeybindSet keybindset = Keybinder.keybinds.get(player);
			if (keybindset != null) {
				keybindset.onKeyPress(event.getKey());
			}
		}
	}
	
}
