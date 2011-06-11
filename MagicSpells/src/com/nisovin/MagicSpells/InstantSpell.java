package com.nisovin.MagicSpells;

import java.util.ArrayList;
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
		// get nearby living entities, filtered by player targeting options
		List<Entity> ne = player.getNearbyEntities(range, range, range);
		ArrayList<LivingEntity> entities = new ArrayList<LivingEntity>(); 
		for (Entity e : ne) {
			if (e instanceof LivingEntity) {
				if ((targetPlayers || !(e instanceof Player)) && (targetNonPlayers || e instanceof Player)) {
					entities.add((LivingEntity)e);
				}
			}
		}
		
		// find target
		LivingEntity target = null;
		BlockIterator bi = new BlockIterator(player, range);
		Block b;
		Location l;
		int bx, by, bz;
		double ex, ey, ez;
		// loop through player's line of sight
		while (bi.hasNext()) {
			b = bi.next();
			bx = b.getX();
			by = b.getY();
			bz = b.getZ();			
			if (checkLos && !MagicSpells.losTransparentBlocks.contains(b.getTypeId())) {
				// line of sight is broken, stop without target
				break;
			} else {
				// check for entities near this block in the line of sight
				for (LivingEntity e : entities) {
					l = e.getLocation();
					ex = l.getX();
					ey = l.getY();
					ez = l.getZ();
					if ((bx-.75 <= ex && ex <= bx+1.75) && (bz-.75 <= ez && ez <= bz+1.75) && (by-1 <= ey && ey <= by+2.5)) {
						// entity is close enough, set target and stop
						target = e;
						break;
					}
				}
			}
		}
		
		return target;
	}
}
