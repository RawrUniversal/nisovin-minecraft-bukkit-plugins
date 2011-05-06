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
			if (inHand.getTypeId() == MagicSystem.wandSpellItem) {
				WandSpell newSpell = (WandSpell)MagicSystem.spellbooks.get(event.getPlayer().getName()).nextSpell(SpellType.WAND_SPELL);
				event.getPlayer().sendMessage("You are now using the " + newSpell.getName() + " spell.");
			}
		} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
		}
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			ItemStack inHand = event.getPlayer().getItemInHand();
			Spell spell = null;
			if (inHand.getTypeId() == MagicSystem.wandSpellItem) {
				spell = MagicSystem.spellbooks.get(event.getPlayer().getName()).getActiveSpell(SpellType.WAND_SPELL);
			}
			if (spell != null) {
				spell.cast(event.getPlayer());
			}			
		}
	}
	
}
