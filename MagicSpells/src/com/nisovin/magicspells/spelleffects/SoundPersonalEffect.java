package com.nisovin.magicspells.spelleffects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SoundPersonalEffect extends SpellEffect {

	public void playEffect(Entity entity, String param) {
		if (entity instanceof Player) {
			String sound = "random.pop";
			float volume = 1.0F;
			float pitch = 1.0F;
			if (param != null && param.length() > 0) {
				String[] data = param.split(" ");
				sound = data[0];
				if (data.length > 1) {
					volume = Float.parseFloat(data[1]);
				}
				if (data.length > 2) {
					pitch = Float.parseFloat(data[2]);
				}
			}
			System.out.println("Playing personal sound " + sound + " " + volume + " " + pitch);
		}
	}
	
}
