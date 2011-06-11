package com.nisovin.MagicSpells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.config.Configuration;

public abstract class InstantSpell extends Spell {
	
	protected int range;
	private boolean castWithItem;
	private boolean castByCommand;
	
	public InstantSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		range = config.getInt("spells." + spellName + ".range", -1);
		castWithItem = config.getBoolean("spells." + spellName + ".can-cast-with-item", true);
		castByCommand = config.getBoolean("spells." + spellName + ".can-cast-by-command", true);
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
	
	protected LivingEntity getTargetedEntity(Player player, int range, int variance, boolean targetPlayers, boolean checkLos) {
		return getTargetedEntity(player, range, variance, targetPlayers, true, checkLos);
	}
	
	protected Player getTargetedPlayer(Player player, int range, int variance, boolean checkLos) {
		LivingEntity entity = getTargetedEntity(player, range, variance, true, false, checkLos);
		if (entity instanceof Player) {
			return (Player)entity;
		} else {
			return null;
		}
	}
	
	protected LivingEntity getTargetedEntity(Player player, int range, int variance, boolean targetPlayers, boolean targetNonPlayers, boolean checkLos) {
		// check LOS
		/*if (checkLos) {
			BlockIterator i = new BlockIterator(player, range);
			int r = 0;
			Block b;
			while (i.hasNext()) {
				b = i.next();
				if (!MagicSpells.losTransparentBlocks.contains(b.getTypeId())) {
					range = r;
					break;
				} else {
					r++;
				}
			}
		}*/
		
		List<Entity> entities = player.getNearbyEntities(range, range, range);
		
		double px = player.getLocation().getX();
		double py = player.getLocation().getY();
		double pz = player.getLocation().getZ();
				
		LivingEntity target = null;
		double distance = 0;
		double dx, dy, dz, dist, xzAngle, yAngle;
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity) {
				dx = entity.getLocation().getX() - px;
				dy = entity.getLocation().getY() - py;
				dz = entity.getLocation().getZ() - pz;
				if (/*Math.abs(dx) < range && Math.abs(dy) < range && Math.abs(dz) < range &&*/ (targetPlayers || !(entity instanceof Player)) && (targetNonPlayers || entity instanceof Player)) {
					dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
					xzAngle = Math.atan2(entity.getLocation().getZ() - player.getLocation().getZ(), entity.getLocation().getX() - player.getLocation().getX()) * 57.295F - 90;
					yAngle = Math.asin(dy / dist) * -57.295F;
					
					if (angleDiff(xzAngle, player.getLocation().getYaw()) < variance && Math.abs(yAngle - player.getLocation().getPitch()) < variance && (target == null || dist < distance)) {
						target = (LivingEntity)entity;
						distance = dist;
					}			
				}	
			}
		}
		
		entities = null;
		
		return target;
	}

	private double angleDiff(double angle1, double angle2) {
		double a = Math.abs(angle1-angle2) % 360;
		if (a <= 180) {
			return a;
		} else {
			return 360-a;
		}
	}
}
