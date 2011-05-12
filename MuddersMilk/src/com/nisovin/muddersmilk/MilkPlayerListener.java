package com.nisovin.muddersmilk;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class MilkPlayerListener extends PlayerListener {

	MuddersMilk plugin;
	Random random;
	
	public MilkPlayerListener(MuddersMilk plugin) {
		this.plugin = plugin;
		this.random = new Random();
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Lowest, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, this, Event.Priority.Lowest, plugin);
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType() == Material.MILK_BUCKET) {
			event.getPlayer().setItemInHand(new ItemStack(Material.BUCKET));
			int drunkLevel = plugin.moreDrunk(event.getPlayer());
			if (drunkLevel == 1) {
				event.getPlayer().sendMessage(ChatColor.YELLOW + "You are feeling tipsy.");
			} else if (drunkLevel == 5) {
				event.getPlayer().sendMessage(ChatColor.YELLOW + "You are completely smashed.");
			} else if (drunkLevel > 5) {
				event.getPlayer().damage(drunkLevel-5);
				event.getPlayer().sendMessage(ChatColor.YELLOW + "You are getting alcohol poisoning!");
			}
		}
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (!event.isCancelled()) {
			int drunkLevel = plugin.getDrunkLevel(event.getPlayer());
			if (drunkLevel > 0) {
				String msg = event.getMessage();
				boolean changed = false;			
				if (random.nextInt(100) < (50+drunkLevel*5)) {
					msg = msg.replaceAll("s([^h])", "sh$1");
					changed = true;
				}
				if (random.nextInt(100) < 15+drunkLevel*5) {
					msg += "... hic!";
					changed = true;
				}
				if (changed) {
					event.setMessage(msg);
				}
			}
		}
	}
	
}
