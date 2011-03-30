package com.nisovin.MagicSystem.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSystem.*;

public class BlinkSpell extends WandSpell {
	
	private static final String SPELL_NAME = "blink";
	
	private int range = -1;
	private String strCantBlink = null;
	
	public BlinkSpell(Configuration config) {
		super(config, SPELL_NAME);
	}
	
	protected boolean castSpell(Player player, SpellCastState state) {
		if (state == SpellCastState.NORMAL) {
			BlockIterator iter = new BlockIterator(player);
			Block prev = null;
			Block found = null;
			while (iter.hasNext()) {
				Block b = iter.next();
				if (b.getType() == Material.AIR) {
					prev = b;
				} else {
					found = b;
					break;
				}
			}
			
			if (found != null) {
				Location loc = null;
				if (range > 0 && !inRange(found.getLocation(), player.getLocation(), range)) {
				} else if (found.getRelative(0,1,0).getType() == Material.AIR && found.getRelative(0,2,0).getType() == Material.AIR) {
					loc = found.getLocation();
					loc.setY(loc.getY() + 1);
				} else if (prev.getType() == Material.AIR && prev.getRelative(0,1,0).getType() == Material.AIR) {
					loc = prev.getLocation();
				}
				if (loc != null) {
					loc.setPitch(player.getLocation().getPitch());
					loc.setYaw(player.getLocation().getYaw());
					player.teleportTo(loc);
					sendMessage(player, strCastSelf);
					// TODO: send messages to others
				} else {
					sendMessage(player, strCantBlink);
					return false;
				}
			}
		}
		return true;
	}

	private boolean inRange(Location loc1, Location loc2, int range) {
		return sq(loc1.getX()-loc2.getX()) + sq(loc1.getY()-loc2.getY()) + sq(loc1.getZ()-loc2.getZ()) < sq(range);
	}
	
	private double sq(double n) {
		return n*n;
	}

}
