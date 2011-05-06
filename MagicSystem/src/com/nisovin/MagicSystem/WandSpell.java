package com.nisovin.MagicSystem;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public abstract class WandSpell extends Spell {
	
	protected int range;
	
	public WandSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		range = config.getInt("spells." + spellName + ".range", -1);
		System.out.println(range);
	}

	protected boolean inRange(Location loc1, Location loc2, int range) {
		return sq(loc1.getX()-loc2.getX()) + sq(loc1.getY()-loc2.getY()) + sq(loc1.getZ()-loc2.getZ()) < sq(range);
	}
	
	private double sq(double n) {
		return n*n;
	}
}
