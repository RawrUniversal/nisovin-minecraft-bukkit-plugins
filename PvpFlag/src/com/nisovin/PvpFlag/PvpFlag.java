package com.nisovin.PvpFlag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class PvpFlag extends JavaPlugin {

	public int COMBAT_DURATION = 30;
	public boolean COLOR_NAMEPLATES = true;
	public boolean OP_OVERRIDE = false;

	public HashSet<String> flagged = new HashSet<String>();
	public HashMap<String, Long> lastPvpActivity = new HashMap<String, Long>();
	public HashMap<String, String> loginMessage = new HashMap<String, String>();

	@Override
	public void onEnable() {
		loadFlaggedList();
		
		Configuration config = getConfiguration();
		COMBAT_DURATION = config.getInt("combat-duration", 30);
		COLOR_NAMEPLATES = config.getBoolean("color-nameplates", true);
		OP_OVERRIDE = config.getBoolean("op-override", true);
		
		new PvpPlayerListener(this);
		new PvpEntityListener(this);
		
        PluginDescriptionFile pdFile = this.getDescription();
        getServer().getLogger().info(pdFile.getName() + " v." + pdFile.getVersion() + " is enabled!" );
	}
	
	public boolean isFlagged(Player player) {
		return flagged.contains(player.getName());
	}
	
	public void setLastActivity(Player player) {
		lastPvpActivity.put(player.getName(), System.currentTimeMillis());
	}
	
	public boolean inCombat(Player player) {
		Long lastActivity = lastPvpActivity.get(player.getName());
		if (lastActivity == null) {
			return false;
		} else if (lastActivity + COMBAT_DURATION*1000 > System.currentTimeMillis()) {
			return true;
		} else {
			return false;
		}			
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender instanceof Player && command.getName().equalsIgnoreCase("pvp")) {
			Player player = (Player)sender;
			if (args.length == 1 && args[0].equalsIgnoreCase("on")) {
				// turning on
				if (isFlagged(player)) {
					// already on
					player.sendMessage("Your PvP flag is already on.");
				} else {
					// flag them
					flagged.add(player.getName());
					if (COLOR_NAMEPLATES) {
						loginMessage.put(player.getName(), "You are now flagged for Pvp!");
						player.kickPlayer("Flag set. Please log back in.");
					} else {
						player.sendMessage("You are now flagged for PvP!");
					}
				}
			} else if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
				// turning off
				if (!isFlagged(player)) {
					// already off
					player.sendMessage("Your PvP flag is already off.");
				} else if (inCombat(player)) {
					// is in combat, can't change
					player.sendMessage("You can't change your flag while in combat.");
				} else {
					// unflag them
					flagged.remove(player.getName());
					if (COLOR_NAMEPLATES) {
						loginMessage.put(player.getName(), "You are no longer flagged for Pvp.");
						player.kickPlayer("Flag set. Please log back in.");
					} else {
						player.sendMessage("You are no longer flagged for PvP.");
					}
				}
			} else {
				// show help
				if (isFlagged(player)) {
					player.sendMessage("You are currently flagged for PvP.");
					player.sendMessage("To remove your flag, use '/pvp off'.");
				} else {
					player.sendMessage("You are not flagged for PvP.");
					player.sendMessage("To enable PvP, use '/pvp on'.");
				}
				if (COLOR_NAMEPLATES) {
					player.sendMessage("You will be required to re-login after changing your flag.");
				}
			}
			return true;
		}
		return false;
	}
	
	private void loadFlaggedList() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "flagged.txt");
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					flagged.add(line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {			
		}
	}
	
	private void saveFlaggedList() {
		File file = new File(getDataFolder(), "flagged.txt");
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			
			for (String s : flagged) {
				writer.append(s);
				writer.newLine();
			}
			
			writer.close();
			
		} catch (IOException e) {
		}		
	}
	
	@Override
	public void onDisable() {
		saveFlaggedList();
        PluginDescriptionFile pdFile = this.getDescription();
        getServer().getLogger().info(pdFile.getName() + " v." + pdFile.getVersion() + " is disabled." );
	}

}
