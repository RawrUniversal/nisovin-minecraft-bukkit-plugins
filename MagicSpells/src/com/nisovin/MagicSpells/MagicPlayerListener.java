package com.nisovin.MagicSpells;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class MagicPlayerListener extends PlayerListener {

	private MagicSpells plugin;
	
	public MagicPlayerListener(MagicSpells plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM_HELD, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TOGGLE_SNEAK, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		// set up spell book
		Spellbook spellbook = new Spellbook(event.getPlayer(), plugin);
		MagicSpells.spellbooks.put(event.getPlayer().getName(), spellbook);
		
		// set up mana bar
		if (MagicSpells.mana != null) {
			MagicSpells.mana.createManaBar(event.getPlayer());
		}
		
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
			Player player = event.getPlayer();
			ItemStack inHand = player.getItemInHand();
			
			// cycle spell
			Spell spell = null;
			if (!player.isSneaking()) {
				spell = MagicSpells.getSpellbook(player).nextSpell(inHand.getTypeId());
			} else {
				spell = MagicSpells.getSpellbook(player).prevSpell(inHand.getTypeId());
			}
			if (spell != null) {
				Spell.sendMessage(player, MagicSpells.strSpellChange, "%s", spell.getName());
			}
			
			// check for mana pots
			if (MagicSpells.enableManaBars && MagicSpells.manaPotions != null) {
				MaterialData mat = inHand.getData();
				if (mat == null) {
					mat = new MaterialData(inHand.getType());
				}
				if (MagicSpells.manaPotions.containsKey(mat)) {
					// check cooldown
					if (MagicSpells.manaPotionCooldown > 0) {
						Long c = MagicSpells.manaPotionCooldowns.get(player);
						if (c != null && c > System.currentTimeMillis()) {
							Spell.sendMessage(player, MagicSpells.strManaPotionOnCooldown.replace("%c", ""+(int)((c-System.currentTimeMillis())/1000)));
							return;
						}
					}
					// add mana
					int amt = MagicSpells.manaPotions.get(mat);
					boolean added = MagicSpells.mana.addMana(player, amt);
					if (added) {
						// set cooldown
						if (MagicSpells.manaPotionCooldown > 0) {
							MagicSpells.manaPotionCooldowns.put(player, System.currentTimeMillis() + MagicSpells.manaPotionCooldown*1000);
						}
						// remove item
						if (inHand.getAmount() == 1) {
							inHand = null;
						} else {
							inHand.setAmount(inHand.getAmount()-1);
						}
						player.setItemInHand(inHand);
					}
				}
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
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_ITEM_HELD);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onItemHeldChange(event);
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
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_TELEPORT);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerTeleport(event);
			}
		}
	}
	
	@Override
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_TOGGLE_SNEAK);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerToggleSneak(event);
			}
		}
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_COMMAND_PREPROCESS);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerCommandPreprocess(event);
			}
		}
	}
	
	
	
}
