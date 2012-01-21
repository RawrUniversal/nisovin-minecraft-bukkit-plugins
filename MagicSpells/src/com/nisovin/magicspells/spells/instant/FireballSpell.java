package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;

public class FireballSpell extends InstantSpell {
	
	private boolean requireEntityTarget;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private int additionalDamage;
	private boolean noExplosion;
	private boolean noFire;
	private String strNoTarget;
	
	private HashMap<Fireball,Float> fireballs;
	
	public FireballSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		requireEntityTarget = getConfigBoolean("require-entity-target", false);
		obeyLos = getConfigBoolean("obey-los", true);
		targetPlayers = getConfigBoolean("target-players", false);
		checkPlugins = getConfigBoolean("check-plugins", true);
		additionalDamage = config.getInt("spells." + spellName + ".additional-damage", 0);
		noExplosion = config.getBoolean("spells." + spellName + ".no-explosion", false);
		noFire = config.getBoolean("spells." + spellName + ".no-fire", false);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You cannot throw a fireball there.");
		
		fireballs = new HashMap<Fireball,Float>();
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range);
			if (target == null || target.getType() == Material.AIR) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				fizzle(player);
				return PostCastAction.ALREADY_HANDLED;
			} else {				
				// get a target if required
				boolean selfTarget = false;
				if (requireEntityTarget) {
					LivingEntity entity = getTargetedEntity(player, range, targetPlayers, obeyLos);
					if (entity == null) {
						sendMessage(player, strNoTarget);
						fizzle(player);
						return PostCastAction.ALREADY_HANDLED;
					} else if (entity instanceof Player && checkPlugins) {
						// run a pvp damage check
						EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, entity, DamageCause.ENTITY_ATTACK, 1);
						Bukkit.getServer().getPluginManager().callEvent(event);
						if (event.isCancelled()) {
							sendMessage(player, strNoTarget);
							fizzle(player);
							return PostCastAction.ALREADY_HANDLED;
						}
					}
					if (entity.equals(player)) {
						selfTarget = true;
					}
				}
				
				// create fireball
				Location loc;
				if (!selfTarget) {
					loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2)).toLocation(player.getWorld(), player.getLocation().getYaw(), player.getLocation().getPitch());
				} else {
					loc = player.getLocation().toVector().add(player.getLocation().getDirection().setY(0).multiply(2)).toLocation(player.getWorld(), player.getLocation().getYaw()+180, 0);
				}
				Fireball fireball = player.getWorld().spawn(loc, Fireball.class);
				fireball.setShooter(player);
				fireballs.put(fireball,power);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getEntity() instanceof Fireball) {
			Fireball fireball = (Fireball)event.getEntity();
			if (fireballs.containsKey(fireball)) {
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

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if (additionalDamage > 0 && !event.isCancelled() && event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
			if (evt.getDamager() instanceof Fireball) {
				Fireball fireball = (Fireball)evt.getDamager();
				if (fireball.getShooter() instanceof Player && fireballs.containsKey(fireball)) {
					float power = fireballs.get(fireball);
					event.setDamage(Math.round((event.getDamage() + additionalDamage) * power));
				}
			}
		}
	}
	
}