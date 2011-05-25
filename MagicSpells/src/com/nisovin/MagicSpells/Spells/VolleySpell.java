package com.nisovin.MagicSpells.Spells;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class VolleySpell extends InstantSpell {

	private static final String SPELL_NAME = "volley";

	private int arrows;
	private int speed;
	private int spread;
	private String strNoTarget;

	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new VolleySpell(config, spellName));
		}
	}
	
	public VolleySpell(Configuration config, String spellName) {
		super(config, spellName);
		
		arrows = config.getInt("spells." + spellName + ".arrows", 10);
		speed = config.getInt("spells." + spellName + ".speed", 20);
		spread = config.getInt("spells." + spellName + ".spread", 150);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "No target found.");
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Location spawn = player.getLocation();
			spawn.setY(spawn.getY()+3);
			
			Block target = player.getTargetBlock(null, range>0?range:100);
			if (target == null || target.getType() == Material.AIR) {
				sendMessage(player, strNoTarget);
				return true;
			} else {
				Vector v = target.getLocation().toVector().subtract(spawn.toVector()).normalize();
				for (int i = 0; i < arrows; i++) {
					player.getWorld().spawnArrow(spawn, v, (speed/10.0F), (spread/10.0F));
				}
			}
		}
		return false;
	}

}