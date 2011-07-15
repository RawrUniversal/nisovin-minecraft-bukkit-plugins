package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class FireballSpell extends InstantSpell {
	
	private boolean requireEntityTarget;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private int additionalDamage;
	private boolean noExplosion;
	private boolean noFire;
	private String strNoTarget;
	
	private HashSet<Fireball> fireballs;
	
	public FireballSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		requireEntityTarget = getConfigBoolean(config, "require-entity-target", false);
		obeyLos = getConfigBoolean(config, "obey-los", true);
		targetPlayers = getConfigBoolean(config, "target-players", false);
		checkPlugins = getConfigBoolean(config, "check-plugins", true);
		additionalDamage = config.getInt("spells." + spellName + ".additional-damage", 0);
		noExplosion = config.getBoolean("spells." + spellName + ".no-explosion", false);
		noFire = config.getBoolean("spells." + spellName + ".no-fire", false);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You cannot throw a fireball there.");
		
		fireballs = new HashSet<Fireball>();
		addListener(Event.Type.EXPLOSION_PRIME);
		if (additionalDamage > 0) {
			addListener(Event.Type.ENTITY_DAMAGE);
		}
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range);
			if (target == null || target.getType() == Material.AIR) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				return true;
			} else {				
				// get a target if required
				if (requireEntityTarget) {
					LivingEntity entity = getTargetedEntity(player, range, targetPlayers, obeyLos);
					if (entity == null) {
						sendMessage(player, strNoTarget);
						return true;
					} else if (entity instanceof Player && checkPlugins) {
						// run a pvp damage check
						EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, entity, DamageCause.ENTITY_ATTACK, 1);
						Bukkit.getServer().getPluginManager().callEvent(event);
						if (event.isCancelled()) {
							sendMessage(player, strNoTarget);
							return true;
						}
					}
				}
				
				// create fireball
				Location loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
				Fireball fireball = player.getWorld().spawn(loc, Fireball.class);
				fireball.setShooter(player);
				fireballs.add(fireball);
			}
		}
		return false;
	}
	
	@Override
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getEntity() instanceof Fireball) {
			Fireball fireball = (Fireball)event.getEntity();
			if (fireballs.contains(fireball)) {
				if (noExplosion) {
					event.setCancelled(true);
					Location loc = fireball.getLocation();
					final HashSet<Block> fires = new HashSet<Block>();
					for (int x = loc.getBlockX()-1; x <= loc.getBlockX()+1; x++) {
						for (int y = loc.getBlockY()-1; y <= loc.getBlockY()+1; y++) {
							for (int z = loc.getBlockZ()-1; z <= loc.getBlockZ()+1; z++) {
								if (loc.getWorld().getBlockTypeIdAt(x,y,z) == 0) {
									Block b = loc.getWorld().getBlockAt(x,y,z);
									b.setTypeIdAndData(Material.FIRE.getId(), (byte)15, false);
									fires.add(b);
								}
							}
						}						
					}
					fireball.remove();
					if (fires.size() > 0) {
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
							@Override
							public void run() {
								for (Block b : fires) {
									if (b.getType() == Material.FIRE) {
										b.setType(Material.AIR);
									}
								}
							}							
						}, 20);
					}
				} else if (noFire) {
					event.setFire(false);
				} else {
					event.setFire(true);
				}
				fireballs.remove(fireball);
			}
		}
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (!event.isCancelled() && event instanceof EntityDamageByProjectileEvent) {
			EntityDamageByProjectileEvent evt = (EntityDamageByProjectileEvent)event;
			if (evt.getProjectile() instanceof Fireball && evt.getDamager() instanceof Player) {
				Fireball fireball = (Fireball)evt.getProjectile();
				if (fireballs.contains(fireball)) {
					event.setDamage(event.getDamage() + additionalDamage);
				}
			}
		}
	}
	
}