package com.nisovin.realrp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.realrp.character.AnimatableNPC;

public class CommandSpawnNpc implements CommandExecutor {

	@SuppressWarnings("unused")
	private RealRP plugin;
	
	public CommandSpawnNpc(RealRP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		if (player != null) {
			AnimatableNPC npc = new AnimatableNPC("test", "http://s3.amazonaws.com/MinecraftSkins/nisovin.png", player.getLocation());
			npc.show();
		}
		
		return true;
	}

}
