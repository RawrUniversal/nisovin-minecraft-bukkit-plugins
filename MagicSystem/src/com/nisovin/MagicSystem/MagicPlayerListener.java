package com.nisovin.MagicSystem;

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.MagicSystem.Spellbook.SpellType;

public class MagicPlayerListener extends PlayerListener {

	private MagicSystem plugin;
	
	public MagicPlayerListener(MagicSystem plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		Spellbook spellbook = new Spellbook(event.getPlayer(), plugin);
		MagicSystem.spellbooks.put(event.getPlayer().getName(), spellbook);
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		MagicSystem.spellbooks.remove(event.getPlayer().getName());
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR) {
			ItemStack inHand = event.getPlayer().getItemInHand();
			Spell spell = MagicSystem.spellbooks.get(event.getPlayer().getName()).nextSpell(inHand);
			if (spell != null) {
				event.getPlayer().sendMessage("You are now using the " + spell.getName() + " spell.");
			}
		}
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			ItemStack inHand = event.getPlayer().getItemInHand();
			Spell spell = MagicSystem.spellbooks.get(event.getPlayer().getName()).getActiveSpell(inHand);
			if (spell != null && spell.canCastWithItem()) {
				spell.cast(event.getPlayer());
			}			
		}
	}
	
}
