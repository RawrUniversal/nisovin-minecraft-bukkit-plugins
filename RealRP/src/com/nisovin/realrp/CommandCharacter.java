package com.nisovin.realrp;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.realrp.character.PlayerCharacter;

public class CommandCharacter implements CommandExecutor {

	private RealRP plugin;
	
	public CommandCharacter(RealRP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			// show usage
		} else if (args[0].equalsIgnoreCase("whois")) {
			whois(sender, args);
		} else if (args[0].equalsIgnoreCase("delete")) {
			delete(sender, args);
		}
		
		return true;
	}
	
	private void whois(CommandSender sender, String[] args) {
		if (args.length != 2) {
			// show usage
			sender.sendMessage("Usage: /char whois <name>");
		} else {
			PlayerCharacter pc = PlayerCharacter.match(args[1]);
			if (pc == null) {
				sender.sendMessage("No player found.");
			} else {
				sender.sendMessage(pc.getChatName() + " is " + pc.getInGameName());
			}
		}
	}
	
	private void delete(CommandSender sender, String[] args) {
		if (args.length != 2) {
			// show usage
		} else {
			List<Player> matches = plugin.getServer().matchPlayer(args[1]);
			if (matches == null || matches.size() != 1) {
				// error - no player found
			} else {
				Player player = matches.get(0);
				PlayerCharacter.delete(player);
				sender.sendMessage("Character for " + player.getName() + " deleted.");
			}
		}		
	}

}
