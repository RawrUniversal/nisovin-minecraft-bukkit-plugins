package com.nisovin.coop;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

public class HealthPlateManager implements Listener {

	private CoopPlugin plugin;
	private ProtocolManager protocolManager;
	
	public HealthPlateManager(CoopPlugin plugin) {
		this.plugin = plugin;
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketChanger(plugin, this));
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public void updatePartyHealthPlates(Party party) {
		List<Player> members = party.getMembers();
		for (Player p : members) {
			protocolManager.updateEntity(p, members);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		
		final Player player = (Player)event.getEntity();
		if (player.hasPermission("healthplates.nocolor")) return;
		
		final Party party = Party.getParty(player);
		if (party == null) return;
		
		final ChatColor currentColor = getColor(player.getHealth());
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (player.isValid()) {
					ChatColor newColor = getColor(player.getHealth());
					if (currentColor != newColor) {
						protocolManager.updateEntity(player, party.getMembers());
					}
				}
			}
		}, 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		
		final Player player = (Player)event.getEntity();
		if (player.hasPermission("healthplates.nocolor")) return;
		
		final Party party = Party.getParty(player);
		if (party == null) return;
		
		ChatColor currentColor = getColor(player.getHealth());
		ChatColor newColor = getColor(player.getHealth() + event.getAmount());
		if (currentColor != newColor) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					protocolManager.updateEntity(player, party.getMembers());
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
	
	public void destroy() {
		protocolManager.removePacketListeners(plugin);
		HandlerList.unregisterAll(this);
		protocolManager = null;
	}
}
