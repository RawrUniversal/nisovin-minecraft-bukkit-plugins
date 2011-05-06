package com.nisovin.MagicSystem.Spells;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSystem.WandSpell;

public class DestroySpell extends WandSpell {
	
	private static final String SPELL_NAME = "destroy";
	
	public DestroySpell(Configuration config) {
		super(config, SPELL_NAME);
	}
	
	public boolean castSpell(Player player, SpellCastState state, String[] args) {
		
		return false;
	}

	public void createExplotion(Location location) {
		//createExplosion(null, location, 4.0);
	}
	
	public void createExplosion(Entity causedBy, Location location, float size) {
		//location.getWorld().getHandle().a(causedBy.getHandle(), location.getX(), location.getY(), location.getZ(), size);
	}
	
}