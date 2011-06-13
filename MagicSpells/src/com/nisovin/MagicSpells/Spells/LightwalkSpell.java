package com.nisovin.MagicSpells.Spells;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class LightwalkSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "lightwalk";
	
	private HashMap<String,Block> lightwalkers;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new LightwalkSpell(config, spellName));
		}
		
	}

	public LightwalkSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		addListener(Event.Type.PLAYER_MOVE);
		
		lightwalkers = new HashMap<String,Block>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (lightwalkers.containsKey(player.getName())) {
			turnOff(player);
			return true;
		} else {
			lightwalkers.put(player.getName(), null);
		}
		return false;
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (lightwalkers.containsKey(event.getPlayer().getName())) {
			Player p = event.getPlayer();
			Block oldBlock = lightwalkers.get(p.getName());
			Block newBlock = p.getLocation().getBlock().getFace(BlockFace.DOWN);
			if ((oldBlock == null || !oldBlock.equals(newBlock)) && allowedType(newBlock.getType()) && newBlock.getType() != Material.AIR) {
				if (isExpired(p)) {
					turnOff(p);
				} else {
					if (oldBlock != null) {
						p.sendBlockChange(oldBlock.getLocation(), oldBlock.getType(), oldBlock.getData());
					}
					p.sendBlockChange(newBlock.getLocation(), Material.GLOWSTONE, (byte)0);
					lightwalkers.put(p.getName(), newBlock);
					addUse(p);
					chargeUseCost(p);
				}
			}
		}
	}
	
	private boolean allowedType(Material mat) {
		return mat == Material.DIRT || 
			mat == Material.GRASS ||
			mat == Material.GRAVEL ||
			mat == Material.STONE ||
			mat == Material.COBBLESTONE ||
			mat == Material.WOOD || 
			mat == Material.LOG || 
			mat == Material.NETHERRACK ||
			mat == Material.SOUL_SAND ||
			mat == Material.SAND ||
			mat == Material.SANDSTONE ||
			mat == Material.GLASS ||
			mat == Material.WOOL ||
			mat == Material.DOUBLE_STEP ||
			mat == Material.BRICK ||
			mat == Material.OBSIDIAN;
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		Block b = lightwalkers.get(player.getName());
		if (b != null) {
			player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
			lightwalkers.remove(player.getName());
		}
		sendMessage(player, strFade);
	}

	@Override
	protected void turnOff() {
		for (String s : lightwalkers.keySet()) {
			Player p = Bukkit.getServer().getPlayer(s);
			if (p != null) {
				Block b = lightwalkers.get(s);
				p.sendBlockChange(b.getLocation(), b.getType(), b.getData());
			}
		}
		lightwalkers.clear();
	}

}
