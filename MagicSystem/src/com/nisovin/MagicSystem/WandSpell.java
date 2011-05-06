package com.nisovin.MagicSystem;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

public abstract class WandSpell extends Spell {
	
	protected int range;
	private boolean castWithItem;
	private boolean castByCommand;
	
	public WandSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		range = config.getInt("spells." + spellName + ".range", -1);
		canCastWithItem = config.getBoolean("spells." + spellName + ".can-cast-with-item", true);
		canCastByCommand = config.getBoolean("spells." + spellName + ".can-cast-by-command", true);
	}
	
	public boolean canCastWithItem() {
		return castWithItem;
	}
	
	public boolean canCastByCommand() {
		return castByCommand;
	}

	protected boolean inRange(Location loc1, Location loc2, int range) {
		return sq(loc1.getX()-loc2.getX()) + sq(loc1.getY()-loc2.getY()) + sq(loc1.getZ()-loc2.getZ()) < sq(range);
	}
	
	private double sq(double n) {
		return n*n;
	}
	
	protected LivingEntity getTargetedEntity(Player player, int range, boolean targetPlayers) {

		double variance = Math.PI / 4;

		List<Entity> nearby = player.getNearbyEntities(range * 2, range * 2, range * 2);
		
		double px = player.getLocation().getX();
		double py = player.getLocation().getY();
		double pz = player.getLocation().getZ();
				
		LivingEntity target = null;
		int distance = 0;
		for (Entity entity : nearby) {
			if (entity instanceof LivingEntity && (targetPlayers || !(entity instanceof Player))) {
				double dx = entity.getLocation().getX() - px;
				double dy = entity.getLocation().getY() - py;
				double dz = entity.getLocation().getZ() - pz;
				double dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
				float xzAngle = Math.atan2(entity.getLocation.getZ() - player.getLocation.getZ(), entity.getLocation.getX() - player.getLocation().getX());
				float yAngle = Math.asin(dy / dist);
				
				if (angleDiff(xzAngle, player.getLocation().getYaw()) < variance && Math.abs(yAngle - player.getLocation().getPitch()) < variance && (target == null || dist < distance)) {
					target = entity;
					distance = dist;
				}			
			}	
		}
		
		return target;
	}

	private double angleDiff(double angle1, double angle2) {
		double a = Math.abs(angle1-angle2) % (Math.PI*2);
		if (a <= Math.PI) {
			return a;
		} else {
			return Math.PI*2-a;
		}
	}
}
