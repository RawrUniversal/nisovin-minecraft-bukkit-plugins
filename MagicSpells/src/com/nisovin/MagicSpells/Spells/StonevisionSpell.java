package com.nisovin.MagicSpells.Spells;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class StonevisionSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "stonevision";
	
	private int range;
	private int transparentType;
	
	private HashMap<String,TransparentBlockSet> seers;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new StonevisionSpell(config, spellName));
		}
		
	}

	public StonevisionSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		addListener(Event.Type.PLAYER_MOVE);
		
		range = config.getInt("spells." + spellName + ".range", 4);
		transparentType = config.getInt("spells." + spellName + ".transparent-type", Material.STONE.getId());
		
		seers = new HashMap<String, TransparentBlockSet>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (seers.containsKey(player.getName())) {
			turnOff(player);
		} else if (state == SpellCastState.NORMAL) {
			seers.put(player.getName(), new TransparentBlockSet(player, range, transparentType));
		}
		return false;
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (seers.containsKey(p.getName())) {
			if (isExpired(p)) {
				turnOff(p);
			}
			boolean moved = seers.get(p.getName()).moveTransparency();
			if (moved) {
				addUse(p);
				chargeUseCost(p);
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		seers.get(player.getName()).removeTransparency();
		seers.remove(player.getName());
	}

	@Override
	protected void turnOff() {
		for (TransparentBlockSet tbs : seers.values()) {
			tbs.removeTransparency();
		}
		seers.clear();
	}
	
	private class TransparentBlockSet {
		Player player;
		Block center;
		int range;
		int type;
		HashSet<Block> blocks;
		
		public TransparentBlockSet(Player player, int range, int type) {
			this.player = player;
			this.center = player.getLocation().getBlock();
			this.range = range;
			this.type = type;
			
			blocks = new HashSet<Block>();
			
			setTransparency();
		}
		
		public void setTransparency() {
			HashSet<Block> newBlocks = new HashSet<Block>();
			
			// get blocks to set to transparent
			int px = center.getX();
			int py = center.getY();
			int pz = center.getZ();
			Block block;
			for (int x = px - range; x <= px + range; x++) {
				for (int y = py - range; y <= py + range; y++) {
					for (int z = pz - range; z <= pz + range; z++) {
						block = center.getWorld().getBlockAt(x,y,z);
						if (block.getType() == Material.getMaterial(type)) {
							player.sendBlockChange(block.getLocation(), Material.GLASS, (byte)0);
							newBlocks.add(block);
						}
					}
				}
			}
			
			// remove old transparent blocks
			for (Block b : blocks) {
				if (!newBlocks.contains(b)) {
					player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
				}
			}
			
			// update block set
			blocks = newBlocks;
			
		}
		
		public boolean moveTransparency() {
			if (player.isDead()) {
				player = Bukkit.getServer().getPlayer(player.getName());
			}
			Location loc = player.getLocation();
			if (!center.getWorld().equals(loc.getWorld()) || center.getX() != loc.getBlockX() || center.getY() != loc.getBlockY() || center.getZ() != loc.getBlockZ()) {
				// moved
				this.center = loc.getBlock();
				setTransparency();
				return true;
			}
			return false;
		}
		
		public void removeTransparency() {
			for (Block b : blocks) {
				player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
			}
			blocks = null;
		}
	}

}
