package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import net.minecraft.server.EntityFireball;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class FireballSpell extends InstantSpell {

	private static final String SPELL_NAME = "fireball";
	
	private int additionalDamage;
	private boolean noExplosion;
	private boolean noFire;
	private String strNoTarget;
	
	private HashSet<EntityFireball> fireballs;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FireballSpell(config, spellName));
		}
	}
	
	public FireballSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		additionalDamage = config.getInt("spells." + spellName + ".additional-damage", 0);
		noExplosion = config.getBoolean("spells." + spellName + ".no-explosion", false);
		noFire = config.getBoolean("spells." + spellName + ".no-fire", false);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You cannot throw a fireball there.");
		
		fireballs = new HashSet<EntityFireball>();
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
				Location blockLoc = target.getLocation();				
				blockLoc.setX(blockLoc.getX()+.5);
				blockLoc.setY(blockLoc.getY()+.5);
				blockLoc.setZ(blockLoc.getZ()+.5);
				
				Vector path = blockLoc.toVector().subtract(player.getEyeLocation().toVector());		
				EntityFireball fireball = new EntityFireball(((CraftWorld)player.getWorld()).getHandle(), ((CraftPlayer)player).getHandle(), path.getX(), path.getY(), path.getZ());
				Vector v = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2));
				fireball.setPosition(v.getX(), v.getY(), v.getZ());
				((CraftWorld)player.getWorld()).getHandle().addEntity(fireball);
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
		
		if (((CraftEntity)event.getEntity()).getHandle() instanceof EntityFireball) {
			EntityFireball fireball = (EntityFireball)(((CraftEntity)event.getEntity()).getHandle());
			if (fireballs.contains(fireball)) {
				if (noExplosion) {
					event.setCancelled(true);
					Location loc = fireball.getBukkitEntity().getLocation();
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
					fireball.die();
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
			if (((CraftEntity)evt.getProjectile()).getHandle() instanceof EntityFireball && evt.getDamager() instanceof Player) {
				EntityFireball fireball = (EntityFireball)(((CraftEntity)evt.getProjectile()).getHandle());
				if (fireballs.contains(fireball)) {
					event.setDamage(event.getDamage() + additionalDamage);
				}
			}
		}
	}
	
}