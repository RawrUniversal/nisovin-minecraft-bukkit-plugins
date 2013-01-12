package com.nisovin.yapp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPromoteDemote implements CommandExecutor {

	private YAPP plugin;
	
	public CommandPromoteDemote(YAPP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0) {
			return false;
		}
		
		// get user
		User user = null;
		if (args[0].startsWith("o:")) {
			user = YAPP.getPlayerUser(args[0].substring(2));
		} else {
			Player player = Bukkit.getPlayer(args[0]);
			if (player != null) {
				user = YAPP.getPlayerUser(player.getName());
			}
		}
		if (user == null) {
			sender.sendMessage(YAPP.ERROR_COLOR + "That player could not be found");
			return true;
		}
		
		// get world
		String world = null;
		if (args.length > 1) {
			world = args[1];
		}
		
		// promote or demote
		boolean success = false;
		String var = "set";
		if (command.getName().equals("yapppromote")) {
			success = plugin.promote(user, world, sender);
			var = "promote";
		} else if (command.getName().equals("yappdemote")) {
			success = plugin.demote(user, world, sender);
			var = "demote";
		}
		
		if (success) {
			user.save();
			
			// reload player
			Player player = user.getPlayer();
			if (player != null && player.isOnline()) {
				plugin.unloadPlayer(player);
				plugin.loadPlayerPermissions(player);
			} else {
				plugin.unloadPlayer(user.getName());
			}
			
			// send message
			Group group = user.getPrimaryGroup(world);
			sender.sendMessage(YAPP.TEXT_COLOR + "Player " + YAPP.HIGHLIGHT_COLOR + user.getName() + YAPP.TEXT_COLOR + " has been " + var + "d to group " + YAPP.HIGHLIGHT_COLOR + group.getName());
		} else {
			sender.sendMessage(YAPP.ERROR_COLOR + "Unable to " + var + " player " + YAPP.HIGHLIGHT_COLOR + user.getName());
		}
		
		return true;
	}

}
