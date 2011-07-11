package com.nisovin.MagicSpells.Spells;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Util.BlockPlatform;

public class WindwalkSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "windwalk";
	
	private Material platformBlock;
	private int size;
	private boolean cancelOnLogout;
	private boolean cancelOnTeleport;
	
	private HashMap<String,BlockPlatform> windwalkers;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new WindwalkSpell(config, spellName));
		}		
	}

	public WindwalkSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		platformBlock = Material.getMaterial(config.getInt("spells." + spellName + ".platform-block", Material.GLASS.getId()));
		size = config.getInt("spells." + spellName + ".size", 2);
		cancelOnLogout = config.getBoolean("spells." + spellName + ".cancel-on-logout", true);
		cancelOnTeleport = config.getBoolean("spells." + spellName + ".cancel-on-teleport", true);
		
		windwalkers = new HashMap<String,BlockPlatform>();
		
		addListener(Event.Type.PLAYER_MOVE);
		addListener(Event.Type.PLAYER_TOGGLE_SNEAK);
		addListener(Event.Type.BLOCK_BREAK);
		if (cancelOnLogout) {
			addListener(Event.Type.PLAYER_QUIT);
		}
		if (cancelOnTeleport) {
			addListener(Event.Type.PLAYER_TELEPORT);
		}
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (windwalkers.containsKey(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			windwalkers.put(player.getName(), new BlockPlatform(platformBlock, Material.AIR, player.getLocation().getBlock().getRelative(0,-1,0), size, true, "square"));
			startSpellDuration(player);
		}
		return false;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (windwalkers.containsKey(event.getPlayer().getName())) {
			Player player = event.getPlayer();
			if (isExpired(player)) {
				turnOff(player);
			} else {
				if (!player.isSneaking()) {
					Block block = event.getTo().subtract(0,1,0).getBlock();
					boolean moved = windwalkers.get(player.getName()).movePlatform(block);
					if (moved) {
						addUse(player);
						chargeUseCost(player);
					}
				}
			}
		}
	}
	
	@Override
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (windwalkers.containsKey(event.getPlayer().getName()) && event.isSneaking()) {
			Player player = event.getPlayer();
			if (isExpired(player)) {
				turnOff(player);
			} else {
				Block block = player.getLocation().subtract(0,2,0).getBlock();
				boolean moved = windwalkers.get(player.getName()).movePlatform(block);
				if (moved) {
					addUse(player);
					chargeUseCost(player);
				}
			}			
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (windwalkers.size() > 0 && event.getBlock().getType() == platformBlock) {
			for (BlockPlatform platform : windwalkers.values()) {
				if (platform.blockInPlatform(event.getBlock())) {
					event.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		turnOff(event.getPlayer());
	}
	
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (windwalkers.containsKey(event.getPlayer().getName())) {
			if (!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName()) || event.getFrom().toVector().distanceSquared(event.getTo().toVector()) > 50*50) {
				turnOff(event.getPlayer());
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		BlockPlatform platform = windwalkers.get(player.getName());
		if (platform != null) {
			platform.destroyPlatform();
			windwalkers.remove(player.getName());
			sendMessage(player, strFade);
		}
	}
	
	@Override
	protected void turnOff() {
		for (BlockPlatform platform : windwalkers.values()) {
			platform.destroyPlatform();
		}
		windwalkers.clear();
	}

}
