package com.nisovin.magicspells.spells.instant;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ParticleProjectileSpell extends InstantSpell {

	float projectileVelocity;
	float projectileGravity;
	
	int tickInterval;
	float ticksPerSecond;
	
	String particleName;
	float particleSpeed;
	int particleCount;
	float particleHorizontalSpread;
	float particleVerticalSpread;
	
	int maxDistanceSquared;
	
	boolean hitPlayers;
	boolean hitNonPlayers;
	boolean hitGround;
	boolean hitAir;
	
	String landSpellName;
	TargetedSpell spell;

	public ParticleProjectileSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		projectileVelocity = getConfigFloat("projectile-velocity", 10F);
		projectileGravity = getConfigFloat("projectile-gravity", 0.25F);
		tickInterval = getConfigInt("tick-interval", 2);
		ticksPerSecond = 20F / (float)tickInterval;
		particleName = getConfigString("particle-name", "reddust");
		particleSpeed = getConfigFloat("particle-speed", 0.3F);
		particleCount = getConfigInt("particle-count", 15);
		particleHorizontalSpread = getConfigFloat("particle-horizontal-spread", 0.3F);
		particleVerticalSpread = getConfigFloat("particle-vertical-spread", 0.3F);
		maxDistanceSquared = getConfigInt("max-distance", 15);
		maxDistanceSquared *= maxDistanceSquared;
		hitPlayers = getConfigBoolean("hit-players", false);
		hitNonPlayers = getConfigBoolean("hit-non-players", true);
		hitGround = getConfigBoolean("hit-ground", true);
		hitAir = getConfigBoolean("hit-air", false);
		landSpellName = getConfigString("spell", "explode");
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		Spell s = MagicSpells.getSpellByInternalName(landSpellName);
		if (s != null && s instanceof TargetedSpell) {
			spell = (TargetedSpell)s;
		} else {
			MagicSpells.error("ParticleProjectileSpell " + internalName + " has an invalid spell defined!");
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			new ProjectileTracker(player, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	class ProjectileTracker implements Runnable {
		
		Player caster;
		float power;
		Location startLocation;
		Location currentLocation;
		Vector currentVelocity;
		int taskId;
		List<LivingEntity> inRange;
		
		public ProjectileTracker(Player caster, float power) {
			this.caster = caster;
			this.power = power;
			this.startLocation = caster.getEyeLocation();
			this.currentLocation = startLocation.clone();
			this.currentVelocity = caster.getLocation().getDirection().multiply(projectileVelocity / ticksPerSecond);
			this.taskId = MagicSpells.scheduleRepeatingTask(this, 0, tickInterval);
			if (hitPlayers || hitNonPlayers) {
				this.inRange = currentLocation.getWorld().getLivingEntities();
				Iterator<LivingEntity> iter = inRange.iterator();
				int maxDistanceSquaredTimesTwo = maxDistanceSquared * 2;
				while (iter.hasNext()) {
					LivingEntity e = iter.next();
					if (!hitPlayers && e instanceof Player) {
						iter.remove();
					} else if (!hitNonPlayers && !(e instanceof Player)) {
						iter.remove();
					} else if (e.getLocation().distanceSquared(currentLocation) > maxDistanceSquaredTimesTwo) {
						iter.remove();
					}
				}
			}
		}
		
		@Override
		public void run() {
			currentLocation.add(currentVelocity);
			
			MagicSpells.getVolatileCodeHandler().playParticleEffect(currentLocation, particleName, particleHorizontalSpread, particleVerticalSpread, particleSpeed, particleCount, 32, 0F);
			
			if (projectileGravity != 0) {
				currentVelocity.setY(currentVelocity.getY() - (projectileGravity / ticksPerSecond));
			}
			
			if (currentLocation.getBlock().getType() != Material.AIR) {
				stop();
				if (hitGround && spell != null && spell instanceof TargetedLocationSpell) {
					((TargetedLocationSpell)spell).castAtLocation(caster, currentLocation, power);
				}
			} else if (currentLocation.distanceSquared(startLocation) > maxDistanceSquared) {
				stop();
				if (hitAir && spell != null && spell instanceof TargetedLocationSpell) {
					((TargetedLocationSpell)spell).castAtLocation(caster, currentLocation, power);
				}
			} else if (inRange != null) {
				for (LivingEntity e : inRange) {
					if (e.getLocation().distanceSquared(currentLocation) < 2.2) {
						stop();
						if (spell != null) {
							if (spell instanceof TargetedEntitySpell) {
								((TargetedEntitySpell)spell).castAtEntity(caster, e, power);
							} else if (spell instanceof TargetedLocationSpell) {
								((TargetedLocationSpell)spell).castAtLocation(caster, currentLocation, power);
							}
						}
						break;
					}
				}
			}
		}
		
		public void stop() {
			MagicSpells.cancelTask(taskId);
			startLocation = null;
			currentLocation = null;
			currentVelocity = null;
			inRange.clear();
			inRange = null;
		}
		
	}
	
}
