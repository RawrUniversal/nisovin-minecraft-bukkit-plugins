package com.nisovin.magicspells.spells.instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ProjectileSpell extends InstantSpell {

	private Class<? extends Projectile> projectileClass;
	private double velocity;
	private boolean requireHitEntity;
	private boolean cancelDamage;
	private boolean removeProjectile;
	private int maxDistanceSquared;
	private List<String> spellNames;
	private List<TargetedSpell> spells;
	private int aoeRadius;
	private boolean targetPlayers;
	private boolean allowTargetChange;
	private String strHitCaster;
	private String strHitTarget;
	
	private HashMap<Projectile, ProjectileInfo> projectiles;
	
	public ProjectileSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		String projectileType = getConfigString("projectile", "arrow");
		if (projectileType.equalsIgnoreCase("arrow")) {
			projectileClass = Arrow.class;
		} else if (projectileType.equalsIgnoreCase("snowball")) {
			projectileClass = Snowball.class;
		} else if (projectileType.equalsIgnoreCase("egg")) {
			projectileClass = Egg.class;
		} else if (projectileType.equalsIgnoreCase("enderpearl")) {
			projectileClass = EnderPearl.class;
		} else if (projectileType.equalsIgnoreCase("potion")) {
			projectileClass = ThrownPotion.class;
		}
		if (projectileClass == null) {
			MagicSpells.error("Invalid projectile type on spell '" + internalName + "'");
		}
		velocity = getConfigFloat("velocity", 0);
		requireHitEntity = getConfigBoolean("require-hit-entity", false);
		cancelDamage = getConfigBoolean("cancel-damage", true);
		removeProjectile = getConfigBoolean("remove-projectile", true);
		maxDistanceSquared = getConfigInt("max-distance", 0);
		maxDistanceSquared = maxDistanceSquared * maxDistanceSquared;
		spellNames = getConfigStringList("spells", null);
		aoeRadius = getConfigInt("aoe-radius", 0);
		targetPlayers = getConfigBoolean("target-players", false);
		allowTargetChange = getConfigBoolean("allow-target-change", true);
		strHitCaster = getConfigString("str-hit-caster", "");
		strHitTarget = getConfigString("str-hit-target", "");
		
		projectiles = new HashMap<Projectile, ProjectileInfo>();
	}
	
	@Override
	public void initialize() {
		super.initialize();
		spells = new ArrayList<TargetedSpell>();
		if (spellNames != null) {
			for (String spellName : spellNames) {
				Spell spell = MagicSpells.getSpellByInternalName(spellName);
				if (spell != null && (spell instanceof TargetedEntitySpell || spell instanceof TargetedLocationSpell)) {
					spells.add((TargetedSpell)spell);
				} else {
					MagicSpells.error("Projectile spell '" + internalName + "' attempted to add invalid spell '" + spellName + "'.");
				}
			}
		}
		if (spells.size() == 0) {
			MagicSpells.error("Projectile spell '" + internalName + "' has no spells!");
		}
		
		if (projectileClass == EnderPearl.class) {
			registerEvents(new EnderTpListener());
		} else if (projectileClass == Egg.class) {
			registerEvents(new EggListener());
		} else if (projectileClass == ThrownPotion.class) {
			registerEvents(new PotionListener());
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (projectileClass != null) {
				Projectile projectile = player.launchProjectile(projectileClass);
				if (velocity > 0) {
					projectile.setVelocity(player.getLocation().getDirection().multiply(velocity));
				}
				projectiles.put(projectile, new ProjectileInfo(player, power));
				playGraphicalEffects(1, projectile);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Projectile)) return;
		
		Projectile projectile = (Projectile)event.getDamager();
		ProjectileInfo info = projectiles.get(projectile);
		if (info == null || event.isCancelled()) return;
				
		if (!info.done && (maxDistanceSquared == 0 || projectile.getLocation().distanceSquared(info.start) <= maxDistanceSquared) && event.getEntity() instanceof LivingEntity) { 
			
			if (aoeRadius == 0) {
				LivingEntity target = (LivingEntity)event.getEntity();
				
				// check player
				if (!targetPlayers && target instanceof Player) return;
				
				// call target event
				SpellTargetEvent evt = new SpellTargetEvent(this, info.player, target);
				Bukkit.getPluginManager().callEvent(evt);
				if (evt.isCancelled()) {
					return;
				} else if (allowTargetChange) {
					target = evt.getTarget();
				}
				
				// run spells
				for (TargetedSpell spell : spells) {
					if (spell instanceof TargetedEntitySpell) {
						((TargetedEntitySpell)spell).castAtEntity(info.player, target, info.power);
						playGraphicalEffects(2, target);
					} else if (spell instanceof TargetedLocationSpell) {
						((TargetedLocationSpell)spell).castAtLocation(info.player, target.getLocation(), info.power);
						playGraphicalEffects(2, target.getLocation());
					}
				}
				
				// send messages
				String entityName;
				if (target instanceof Player) {
					entityName = ((Player)target).getDisplayName();
				} else {
					EntityType entityType = target.getType();
					entityName = MagicSpells.getEntityNames().get(entityType);
					if (entityName == null) {
						entityName = entityType.getName();
					}
				}
				sendMessage(info.player, strHitCaster, "%t", entityName);
				if (target instanceof Player) {
					sendMessage((Player)target, strHitTarget, "%a", info.player.getDisplayName());
				}
			} else {
				aoe(projectile, info);
			}
			
			info.done = true;
			
		}
		
		if (cancelDamage) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		final Projectile projectile = (Projectile)event.getEntity();
		ProjectileInfo info = projectiles.get(projectile);
		if (info != null) {
			if (!requireHitEntity && !info.done && (maxDistanceSquared == 0 || projectile.getLocation().distanceSquared(info.start) <= maxDistanceSquared)) {
				if (aoeRadius == 0) {
					for (TargetedSpell spell : spells) {
						if (spell instanceof TargetedLocationSpell) {
							((TargetedLocationSpell)spell).castAtLocation(info.player, projectile.getLocation(), info.power);
							playGraphicalEffects(2, projectile.getLocation());
						}
					}
					sendMessage(info.player, strHitCaster);
				} else {
					aoe(projectile, info);
				}
				info.done = true;
			}
			// remove it from world
			if (removeProjectile) {
				projectile.remove();
			}
			// remove it later just in case it didn't hit anything
			Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
				public void run() {
					projectiles.remove(projectile);
				}
			}, 0);
		}
	}
	
	private void aoe(Projectile projectile, ProjectileInfo info) {
		playGraphicalEffects(4, projectile.getLocation());
		List<Entity> entities = projectile.getNearbyEntities(aoeRadius, aoeRadius, aoeRadius);
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity && (targetPlayers || !(entity instanceof Player)) && !entity.equals(info.player)) {
				LivingEntity target = (LivingEntity)entity;
				
				// call target event
				SpellTargetEvent evt = new SpellTargetEvent(this, info.player, target);
				Bukkit.getPluginManager().callEvent(evt);
				if (evt.isCancelled()) {
					continue;
				} else if (allowTargetChange) {
					target = evt.getTarget();
				}
				
				// run spells
				for (TargetedSpell spell : spells) {
					if (spell instanceof TargetedEntitySpell) {
						((TargetedEntitySpell)spell).castAtEntity(info.player, target, info.power);
						playGraphicalEffects(2, target);
					} else if (spell instanceof TargetedLocationSpell) {
						((TargetedLocationSpell)spell).castAtLocation(info.player, target.getLocation(), info.power);
						playGraphicalEffects(2, target.getLocation());
					}
				}
				
				// send message if player
				if (target instanceof Player) {
					sendMessage((Player)target, strHitTarget, "%a", info.player.getDisplayName());
				}
			}
		}
		sendMessage(info.player, strHitCaster);
	}
	
	public class EnderTpListener implements Listener {
		@EventHandler(ignoreCancelled=true)
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			if (event.getCause() == TeleportCause.ENDER_PEARL) {
				for (Projectile projectile : projectiles.keySet()) {
					if (projectile.getLocation().equals(event.getTo())) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	public class EggListener implements Listener {
		@EventHandler(ignoreCancelled=true)
		public void onCreatureSpawn(CreatureSpawnEvent event) {
			if (event.getSpawnReason() == SpawnReason.EGG) {
				for (Projectile projectile : projectiles.keySet()) {
					if (projectile.getLocation().equals(event.getLocation())) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	public class PotionListener implements Listener {
		@EventHandler(ignoreCancelled=true)
		public void onPotionSplash(PotionSplashEvent event) {
			if (projectiles.containsKey(event.getPotion())) {
				event.setCancelled(true);
			}
		}
	}
	
	private class ProjectileInfo {
		Player player;
		Location start;
		float power;
		boolean done;
		
		public ProjectileInfo(Player player, float power) {
			this.player = player;
			this.start = player.getLocation().clone();
			this.power = power;
			this.done = false;
		}
	}

}
