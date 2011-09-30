package com.nisovin.basicwarp;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class BasicWarp extends JavaPlugin {

	private HashMap<String,Warp> warps = new HashMap<String,Warp>();
	
	@Override
	public void onEnable() {
		loadWarps();
	}	

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("warp") && args.length == 1 && sender instanceof Player) {
			Player player = (Player)sender;
			String warpName = args[0].toLowerCase();
			Warp warp = warps.get(warpName);
			if (warp == null) {
				player.sendMessage(ChatColor.LIGHT_PURPLE + "No such warp.");
			} else {
				if (!player.hasPermission("basicwarp.warp.*") && !player.hasPermission("basicwarp.warp." + warpName)) {
					player.sendMessage(ChatColor.LIGHT_PURPLE + "No such warp.");
				} else {
					boolean success = warp.teleport(player);
					if (success) {
						player.sendMessage(ChatColor.LIGHT_PURPLE + "Zow!");
					} else {
						player.sendMessage(ChatColor.LIGHT_PURPLE + "Unable to warp.");
					}
				}
			}			
			return true;
		} else if (command.getName().equalsIgnoreCase("setwarp") && args.length == 1 && sender instanceof Player) {
			Player player = (Player)sender;
			if (!player.hasPermission("basicwarp.setwarp")) {
				player.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that.");
			} else {
				String warpName = args[0].toLowerCase();
				if (warps.containsKey(warpName)) {
					player.sendMessage(ChatColor.LIGHT_PURPLE + "A warp with that name already exists.");
				} else {
					warps.put(warpName, new Warp(player.getLocation()));
					saveWarps();
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Warp point " + ChatColor.WHITE + warpName + ChatColor.LIGHT_PURPLE + " created.");
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("listwarps")) {
			if (!sender.hasPermission("basicwarp.listwarps")) {
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that.");
			} else {
				TreeSet<String> warpList = new TreeSet<String>();
				boolean all = sender.hasPermission("basicwarp.warp.*");
				for (String warpName : warps.keySet()) {
					if (all || sender.hasPermission("basicwarp.warp." + warpName)) {
						warpList.add(warpName);
					}
				}
				StringBuilder sb = new StringBuilder();
				sb.append(ChatColor.LIGHT_PURPLE + "Warps:" + ChatColor.WHITE + " ");
				for (String s : warpList) {
					sb.append(s);
					sb.append(" ");
				}
				String list = sb.toString();
				boolean first = true;
				while (list.length() > 50) {
					int index = list.lastIndexOf(' ', 50);
					sender.sendMessage((!first?"   ":"") + list.substring(0, index));
					list = list.substring(index+1);
					first = false;
				}
				sender.sendMessage((!first?"   ":"") + list);
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("removewarp") && args.length == 1) {
			if (!sender.hasPermission("basicwarp.removewarp")) {
				sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that.");
			} else {
				String warpName = args[0].toLowerCase();
				if (!warps.containsKey(warpName)) {
					sender.sendMessage(ChatColor.LIGHT_PURPLE + "No such warp.");
				} else {
					warps.remove(warpName);
					saveWarps();
					sender.sendMessage(ChatColor.LIGHT_PURPLE + "Warp " + ChatColor.WHITE + warpName + ChatColor.LIGHT_PURPLE + " removed.");
				}
			}
			return true;
		}
		
		return false;
	}

	private void loadWarps() {
		warps.clear();
		
		File file = new File(getDataFolder(), "warps.yml");
		if (file.exists()) {
			Configuration config = new Configuration(file);
			config.load();
			List<String> warpKeys = config.getKeys();
			for (String key : warpKeys) {
				warps.put(key, new Warp(config.getString(key)));
			}
		}
	}
	
	private void saveWarps() {
		File file = new File(getDataFolder(), "warps.yml");
		if (file.exists()) {
			file.delete();
		}
		Configuration config = new Configuration(file);
		for (String key : warps.keySet()) {
			config.setProperty(key, warps.get(key).getSaveString());
		}
		config.save();
	}

	@Override
	public void onDisable() {
		
	}

}
