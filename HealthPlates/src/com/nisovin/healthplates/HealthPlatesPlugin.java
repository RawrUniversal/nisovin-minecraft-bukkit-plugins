package com.nisovin.healthplates;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class HealthPlatesPlugin extends JavaPlugin implements Listener {
	
	private ProtocolManager protocolManager;
	
	@Override
	public void onEnable() {
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketChanger(this));
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		protocolManager.removePacketListeners(this);
		HandlerList.unregisterAll((Plugin)this);
		protocolManager = null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender.isOp()) {
			if (args.length == 0) return false;
			if (args[0].equalsIgnoreCase("disable")) {
				onDisable();
				sender.sendMessage("Health Plates disabled.");
			} else if (args[0].equalsIgnoreCase("enable")) {
				onDisable();
				onEnable();
				sender.sendMessage("Health Plates enabled.");
			} else if (args[0].equalsIgnoreCase("update") && args.length == 2) {
				Player player = Bukkit.getPlayer(args[1]);
				if (player != null) {
					protocolManager.updateEntity(player, player.getWorld().getPlayers());
				} else {
					sender.sendMessage("No such player.");
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		
		final Player player = (Player)event.getEntity();
		if (player.hasPermission("healthplates.nocolor")) return;
		
		final ChatColor currentColor = getColor(player.getHealth());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				ChatColor newColor = getColor(player.getHealth());
				if (currentColor != newColor) {
					protocolManager.updateEntity(player, getNearbyPlayers(player));
				}
			}
		}, 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		
		final Player player = (Player)event.getEntity();
		if (player.hasPermission("healthplates.nocolor")) return;
		
		ChatColor currentColor = getColor(player.getHealth());
		ChatColor newColor = getColor(player.getHealth() + event.getAmount());
		if (currentColor != newColor) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					protocolManager.updateEntity(player, getNearbyPlayers(player));
				}
			}, 1);
		}
	}
	
	public ChatColor getColor(int health) {
		if (health > 16) {
			return null;
		} else if (health > 12) {
			return ChatColor.YELLOW;
		} else if (health > 8) {
			return ChatColor.GOLD;
		} else if (health > 4) {
			return ChatColor.RED;
		} else {
			return ChatColor.DARK_RED;
		}
	}
	
	private List<Player> getNearbyPlayers(Player player) {
		List<Entity> entities = player.getNearbyEntities(50, 30, 50);
		List<Player> players = new ArrayList<Player>();
		for (Entity e : entities) {
			if (e instanceof Player && !((Player)e).hasPermission("healthplates.noview")) {
				players.add((Player)e);
			}
		}
		return players;
	}
	
	public void debug(String string) {
		System.out.println(string);
	}

}
