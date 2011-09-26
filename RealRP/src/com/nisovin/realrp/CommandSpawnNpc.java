package com.nisovin.realrp;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.nisovin.realrp.character.AnimatableNPC;
import com.nisovin.realrp.npc.NPCEntity;

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
		
		List<Entity> entities = player.getWorld().getEntities();
		int n = 0;
		int p = 0;
		for (Entity e : entities) {
			if (e instanceof NPCEntity) {
				n++;
				System.out.println(e.getLocation());
			} else if (e instanceof Player) {
				p++;
			}
		}
		System.out.println("npcs: " + n);
		System.out.println("players: " + p);
		
		return true;
	}

}
