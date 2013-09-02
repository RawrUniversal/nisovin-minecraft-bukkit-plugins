package com.nisovin.magicspells.spelleffects;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;

public class FireworksEffect extends SpellEffect {

	boolean flicker = false;
	boolean trail = false;
	int type = 0;
	int[] colors = new int[] { 0xFF0000 };
	int[] fadeColors = new int[] { 0xFF0000 };
	int flightDuration = 0;
	
	@Override
	public void loadFromString(String string) {
		if (string != null && !string.isEmpty()) {
			String[] data = string.split(" ");
			if (data.length >= 1 && data[0].equalsIgnoreCase("yes")) {
				flicker = true;
			}
			if (data.length >= 2 && data[1].equalsIgnoreCase("yes")) {
				trail = true;
			}
			if (data.length >= 3) {
				type = Integer.parseInt(data[2]);
			}
			if (data.length >= 4) {
				String[] c = data[3].split(",");
				colors = new int[c.length];
				for (int i = 0; i < c.length; i++) {
					colors[i] = Integer.parseInt(c[i], 16);
				}
			}
			if (data.length >= 5) {
				String[] c = data[4].split(",");
				fadeColors = new int[c.length];
				for (int i = 0; i < c.length; i++) {
					fadeColors[i] = Integer.parseInt(c[i], 16);
				}
			}
			if (data.length >= 6) {
				flightDuration = Integer.parseInt(data[5]);
			}
		}
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
		flicker = config.getBoolean("flicker", false);
		trail = config.getBoolean("trail", false);
		type = config.getInt("type", type);
		flightDuration = config.getInt("flight", flightDuration);
		
		List<String> c = config.getStringList("colors");
		if (c != null && c.size() > 0) {
			colors = new int[c.size()];
			for (int i = 0; i < colors.length; i++) {
				try {
					colors[i] = Integer.parseInt(c.get(i), 16);
				} catch (NumberFormatException e) {
					colors[i] = 0;
				}
			}
		}
		
		List<String> fc = config.getStringList("fade-colors");
		if (fc != null && fc.size() > 0) {
			fadeColors = new int[fc.size()];
			for (int i = 0; i < fadeColors.length; i++) {
				try {
					fadeColors[i] = Integer.parseInt(fc.get(i), 16);
				} catch (NumberFormatException e) {
					fadeColors[i] = 0;
				}
			}
		}
	}

	@Override
	public void playEffect(Location location) {
		MagicSpells.getVolatileCodeHandler().createFireworksExplosion(location, flicker, trail, type, colors, fadeColors, flightDuration);
	}
	
}
