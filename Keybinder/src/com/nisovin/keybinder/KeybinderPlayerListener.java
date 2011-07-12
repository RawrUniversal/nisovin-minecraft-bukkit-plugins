package com.nisovin.keybinder;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import com.nisovin.keybinder.Keybind.KeybindState;

public class KeybinderPlayerListener extends PlayerListener {

	public KeybinderPlayerListener(Keybinder plugin) {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Lowest, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		Keybinder.keybinds.put(event.getPlayer(), new KeybindSet(event.getPlayer()));
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		KeybindSet keybinds = Keybinder.keybinds.get(event.getPlayer());
		if (keybinds != null) {
			keybinds.save();
			Keybinder.keybinds.remove(event.getPlayer());
		}
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Keybind keybind = Keybinder.newKeybinds.get(event.getPlayer());
		if (keybind != null && keybind.getState() == KeybindState.WAITING_ON_COMMAND) {
			Player player = event.getPlayer();
			keybind.setCommand(event.getMessage().substring(1));
			player.sendMessage("The command has been bound to the " + keybind.getKey().name().replace("KEY_", "") + " key.");
			Keybinder.keybinds.get(player).addKeybind(keybind);
			event.setCancelled(true);
		}
	}
	
}
