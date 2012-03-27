package com.nisovin.simplecooldowns;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CooldownListener implements Listener {

	private SimpleCooldowns plugin;
	
	public CooldownListener(SimpleCooldowns plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();
		Command command = plugin.findCommand(msg);
		if (command != null) {
			boolean cancel = command.handleCommand(plugin, event.getPlayer(), msg);
			if (cancel) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			plugin.interruptWarmups((Player)event.getEntity(), "damage");
		}
	}
	
}
