package com.nisovin.realrp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.realrp.chat.ChatManager;

public class CommandJoin implements CommandExecutor {
	
	private RealRP plugin;
	
	public CommandJoin(RealRP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			ChatManager chat = plugin.getChatManager();
			if (chat == null) {
				return true;
			} else {
				boolean joined = chat.joinChannel(player, args[0]);
				if (joined) {
					RealRP.sendMessage(player, RealRP.settings().csSwitchChannelStr, "%c", args[0]);
				} else {
					player.sendMessage("can't join channel");
				}
			}
		}
		
		return true;
	}

}
