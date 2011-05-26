package com.nisovin.MagicSpells.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.*;

public class BlinkSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "blink";
	
	private String strCantBlink = null;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new BlinkSpell(config, spellName));
		}
		
	}
	
	public BlinkSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strCantBlink = config.getString("spells." + spellName + ".str-cant-blink", "You can't blink there.");
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			BlockIterator iter = new BlockIterator(player, range>0&&range<150?range:150);
			Block prev = null;
			Block found = null;
			Block b;
			while (iter.hasNext()) {
				b = iter.next();
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
					loc.setX(loc.getX()+.5);
					loc.setZ(loc.getZ()+.5);
					loc.setPitch(player.getLocation().getPitch());
					loc.setYaw(player.getLocation().getYaw());
					player.teleport(loc);
				} else {
					sendMessage(player, strCantBlink);
					return true;
				}
			}
				sendMessage(player, strCantBlink);
				return true;
			}
		}
		return false;
	}

}
