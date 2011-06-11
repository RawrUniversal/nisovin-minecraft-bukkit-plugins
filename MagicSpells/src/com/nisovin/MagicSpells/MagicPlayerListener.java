package com.nisovin.MagicSpells;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MagicPlayerListener extends PlayerListener {

	private MagicSpells plugin;
	
	public MagicPlayerListener(MagicSpells plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		Spellbook spellbook = new Spellbook(event.getPlayer(), plugin);
		MagicSpells.spellbooks.put(event.getPlayer().getName(), spellbook);
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_JOIN);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerJoin(event);
			}
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		MagicSpells.spellbooks.remove(event.getPlayer().getName());
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_QUIT);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerQuit(event);
			}
		}
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack inHand = event.getPlayer().getItemInHand();
			Spell spell = MagicSpells.spellbooks.get(event.getPlayer().getName()).nextSpell(inHand.getTypeId());
			if (spell != null) {
				Spell.sendMessage(event.getPlayer(), spell.formatMessage(MagicSpells.strSpellChange, "%s", spell.getName()));
			}
		}
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_INTERACT);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerInteract(event);
			}
		}
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			ItemStack inHand = event.getPlayer().getItemInHand();
			Spell spell = null;
			try {
				spell = MagicSpells.spellbooks.get(event.getPlayer().getName()).getActiveSpell(inHand.getTypeId());
			} catch (NullPointerException e) {				
			}
			if (spell != null && spell.canCastWithItem()) {
				spell.cast(event.getPlayer());
			}			
		}
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_MOVE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerMove(event);
			}
		}
	}
	
	
	
}
