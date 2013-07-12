package com.nisovin.magicspells.spells.instant;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ParticleProjectileSpell extends InstantSpell {

	float projectileVelocity;
	float projectileGravity;
	float projectileSpread;
	
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
	boolean stopOnHitEntity;
	
	String landSpellName;
	TargetedSpell spell;
	
	ParticleProjectileSpell thisSpell;
	Random rand = new Random();

	public ParticleProjectileSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		thisSpell = this;
		
		projectileVelocity = getConfigFloat("projectile-velocity", 10F);
		projectileGravity = getConfigFloat("projectile-gravity", 0.25F);
		projectileSpread = getConfigFloat("projectile-spread", 0F);
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
		stopOnHitEntity = getConfigBoolean("stop-on-hit-entity", true);
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
			this.currentVelocity = caster.getLocation().getDirection();
			if (projectileSpread > 0) {
				this.currentVelocity.add(new Vector(rand.nextFloat() * projectileSpread, rand.nextFloat() * projectileSpread, rand.nextFloat() * projectileSpread));
			}
			this.currentVelocity.multiply(projectileVelocity / ticksPerSecond);
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
				LivingEntity toRemove = null;
				for (LivingEntity e : inRange) {
					if (e.getLocation().distanceSquared(currentLocation) < 2.2) {
						if (spell != null) {
							if (spell instanceof TargetedEntitySpell) {
								ValidTargetChecker checker = spell.getValidTargetChecker();
								if (checker != null && !checker.isValidTarget(e)) {
									toRemove = e;
									break;
								}
								SpellTargetEvent event = new SpellTargetEvent(thisSpell, caster, e);
								Bukkit.getPluginManager().callEvent(event);
								if (event.isCancelled()) {
									toRemove = e;
									break;
								}
								((TargetedEntitySpell)spell).castAtEntity(caster, e, power);
							} else if (spell instanceof TargetedLocationSpell) {
								((TargetedLocationSpell)spell).castAtLocation(caster, currentLocation, power);
							}
						}
						if (stopOnHitEntity) {
							stop();
						} else {
							toRemove = e;
						}
						break;
					}
				}
				if (toRemove != null) {
					inRange.remove(toRemove);
				}
			}
		}
		
		public void stop() {
			MagicSpells.cancelTask(taskId);
			startLocation = null;
			currentLocation = null;
			currentVelocity = null;
			if (inRange != null) {
				inRange.clear();
				inRange = null;
			}
		}
		
	}
	
}
