package com.nisovin.magicspells.graphicaleffects;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class GraphicalEffect {
	
	public void showEffect(Entity entity, String param) {
		showEffect(entity.getLocation(), param);
	}
	
	public void showEffect(Location location, String param) {
		
	}
	
	public void showEffect(Location location1, Location location2, String param) {
		
	}
	
	private static HashMap<String, GraphicalEffect> effects = new HashMap<String, GraphicalEffect>();
	
	public static GraphicalEffect getEffectByName(String name) {
		return effects.get(name);
	}
	
	public static void addEffect(String name, GraphicalEffect effect) {
		effects.put(name, effect);
	}
	
	static {
		effects.put("blockbreak", new BlockBreakEffect());
		effects.put("ender", new EnderSignalEffect());
		effects.put("explosion", new ExplosionEffect());
		effects.put("lightning", new LightningEffect());
		effects.put("potion", new PotionEffect());
		effects.put("smoke", new SmokeEffect());
		effects.put("smoketrail", new SmokeTrailEffect());
		effects.put("spawn", new MobSpawnerEffect());
	}
	
}
