package com.nisovin.keybinder;

import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitcontrib.player.ContribPlayer;

public class Keybinder extends JavaPlugin {

	protected static Keybinder plugin;
	protected static HashMap<Player,Keybind> newKeybinds;
	protected static HashMap<Player,KeybindSet> keybinds;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		File folder = this.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		newKeybinds = new HashMap<Player,Keybind>();
		keybinds = new HashMap<Player,KeybindSet>();
		
		for (Player p : getServer().getOnlinePlayers()) {
			keybinds.put(p, new KeybindSet(p));
		}
		
		new KeybinderInputListener(this);
		new KeybinderPlayerListener(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		
		Player player = (Player)sender;
		
		if (((ContribPlayer)player).isEnabledBukkitContribSinglePlayerMod()) {
			if (args.length == 0) {
				newKeybinds.put(player, new Keybind());
				player.sendMessage("Please press the key you wish to bind.");
			} else if (args[0].equalsIgnoreCase("list")) {
				int page = 1;
				if (args.length == 2) {
					page = Integer.parseInt(args[1]);
				}
				keybinds.get(player).showList(page);
			} else if (args[0].equalsIgnoreCase("unbind")) {
				keybinds.get(player).unbind(args[1]);
			} else if (args[0].equalsIgnoreCase("cancel")) {
				if (newKeybinds.containsKey(player)) {
					newKeybinds.remove(player);
					player.sendMessage("New keybind cancelled.");
				}
			}
		} else {
			player.sendMessage("You need the BukkitContrib mod to do this.");
		}
		return true;
	}

	@Override
	public void onDisable() {
		for (KeybindSet keybind : keybinds.values()) {
			keybind.save();
		}
	}

}
