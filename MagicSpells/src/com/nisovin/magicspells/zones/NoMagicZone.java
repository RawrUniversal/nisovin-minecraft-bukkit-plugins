package com.nisovin.magicspells.zones;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;

public abstract class NoMagicZone {

	private String message;
	private List<String> allowedSpells;
	
	public final void create(ConfigurationSection config) {
		message = config.getString("message", "You are in a no-magic zone.");
		allowedSpells = config.getStringList("allowed-spells");
		initialize(config);
	}
	
	public abstract void initialize(ConfigurationSection config);
	
	public boolean willFizzle(Player player, Spell spell) {
		return willFizzle(player.getLocation(), spell);
	}
	
	public boolean willFizzle(Location location, Spell spell) {
		if (allowedSpells != null && allowedSpells.contains(spell.getInternalName())) {
			return false;
		} else {
			return inZone(location);
		}
	}
	
	public abstract boolean inZone(Location location);
	
	public String getMessage() {
		return message;
	}
	
}
