package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

public class MagicPlayerListener extends PlayerListener {

	private MagicSpells plugin;
	
	private HashSet<Player> noCast = new HashSet<Player>();
	private HashMap<Player,Long> lastCast = new HashMap<Player, Long>();
	
	public MagicPlayerListener(MagicSpells plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM_HELD, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TOGGLE_SNEAK, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TOGGLE_SPRINT, this, Event.Priority.Monitor, plugin);
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
		// first check if player is interacting with a special block
		boolean noInteract = false;
		if (event.hasBlock()) {
			Material m = event.getClickedBlock().getType();
			if (m == Material.WOODEN_DOOR || 
					m == Material.BED || 
					m == Material.WORKBENCH ||
					m == Material.CHEST || 
					m == Material.FURNACE || 
					m == Material.LEVER ||
					m == Material.STONE_BUTTON) {
				noInteract = true;
			}
		}
		if (noInteract) {
			// special block -- don't do normal interactions
			noCast.add(event.getPlayer());
		} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// cast: moved back to player animation
		} else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// right click -- cycle spell and/or process mana pots
			Player player = event.getPlayer();
			ItemStack inHand = player.getItemInHand();
			
			// cycle spell
			Spell spell = null;
			if (!player.isSneaking()) {
				spell = MagicSpells.getSpellbook(player).nextSpell(inHand);
			} else {
				spell = MagicSpells.getSpellbook(player).prevSpell(inHand);
			}
			if (spell != null) {
				MagicSpells.sendMessage(player, MagicSpells.strSpellChange, "%s", spell.getName());
			}
			
			// check for mana pots
			if (MagicSpells.enableManaBars && MagicSpells.manaPotions != null) {
				ItemStack item = inHand.clone();
				item.setAmount(1);
				if (MagicSpells.manaPotions.containsKey(item)) {
					// check cooldown
					if (MagicSpells.manaPotionCooldown > 0) {
						Long c = MagicSpells.manaPotionCooldowns.get(player);
						if (c != null && c > System.currentTimeMillis()) {
							MagicSpells.sendMessage(player, MagicSpells.strManaPotionOnCooldown.replace("%c", ""+(int)((c-System.currentTimeMillis())/1000)));
							return;
						}
					}
					// add mana
					int amt = MagicSpells.manaPotions.get(item);
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
		
		// call spell listeners
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_INTERACT);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerInteract(event);
			}
		}
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Player p = event.getPlayer();
		if (noCast.contains(p)) {
			noCast.remove(p);
			lastCast.put(p, System.currentTimeMillis());
		} else {
			// left click -- cast spell
			ItemStack inHand = event.getPlayer().getItemInHand();
			Spell spell = null;
			try {
				spell = MagicSpells.getSpellbook(event.getPlayer()).getActiveSpell(inHand);
			} catch (NullPointerException e) {
			}
			if (spell != null && spell.canCastWithItem()) {
				// first check global cooldown
				Long lastCastTime = lastCast.get(p);
				if (lastCastTime != null && lastCastTime + 500 > System.currentTimeMillis()) {
					return;
				} else {
					lastCast.put(p, System.currentTimeMillis());
				}
				// cast spell
				spell.cast(p);
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
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.PLAYER_TOGGLE_SPRINT);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onPlayerToggleSprint(event);
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
