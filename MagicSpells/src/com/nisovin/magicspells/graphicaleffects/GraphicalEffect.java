package com.nisovin.magicspells.graphicaleffects;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class GraphicalEffect {
	
	public void playEffect(Entity entity, String param) {
		playEffect(entity.getLocation(), param);
	}
	
	public void playEffect(Location location, String param) {
		
	}
	
	public void playEffect(Location location1, Location location2, String param) {
		
	}
	
	private static HashMap<String, GraphicalEffect> effects = new HashMap<String, GraphicalEffect>();
	
	public static GraphicalEffect getEffectByName(String name) {
		return effects.get(name);
	}
	
	public static void addEffect(String name, GraphicalEffect effect) {
		effects.put(name, effect);
	}
	
	static {
		effects.put("bigsmoke", new BigSmokeEffect());
		effects.put("blockbreak", new BlockBreakEffect());
		effects.put("ender", new EnderSignalEffect());
		effects.put("explosion", new ExplosionEffect());
		effects.put("lightning", new LightningEffect());
		effects.put("potion", new PotionEffect());
		effects.put("smoke", new SmokeEffect());
		effects.put("smokeswirl", new SmokeSwirlEffect());
		effects.put("smoketrail", new SmokeTrailEffect());
		effects.put("spawn", new MobSpawnerEffect());
		effects.put("splash", new SplashPotionEffect());
	}
	
}
