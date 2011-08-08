package com.nisovin.realrp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandEmote implements CommandExecutor {

	private RealRP plugin;
	
	public CommandEmote(RealRP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (RealRP.settings().emEnableEmotes && sender instanceof Player) {
			Player player = (Player)sender;
			String emote = "";
			for (String s : args) {
				emote += s + " ";
			}
			plugin.getEmoteManager().sendGenericEmote(player, emote.trim());
		}
		return true;
	}

}
