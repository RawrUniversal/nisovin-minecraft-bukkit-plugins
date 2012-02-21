package com.nisovin.magicspells.spells.targeted;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ExplodeSpell extends TargetedLocationSpell {
	
	private int explosionSize;
	private int backfireChance;
	private boolean preventBlockDamage;
	private float damageMultiplier;
	private boolean ignoreCanceled;
	private String strNoTarget;
	
	private HashMap<Player,Float> recentlyExploded;
	
	public ExplodeSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		explosionSize = getConfigInt("explosion-size", 4);
		backfireChance = getConfigInt("backfire-chance", 0);
		preventBlockDamage = getConfigBoolean("prevent-block-damage", false);
		damageMultiplier = getConfigFloat("damage-multiplier", 0);
		ignoreCanceled = getConfigBoolean("ignore-canceled", false);
		strNoTarget = getConfigString("str-no-target", "Cannot explode there.");
		
		if (preventBlockDamage || damageMultiplier > 0) {
			recentlyExploded = new HashMap<Player,Float>();
		}
	}
	
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range);
			if (target == null || target.getType() == Material.AIR) {
				// fail: no target
				sendMessage(player, strNoTarget);
				fizzle(player);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				boolean exploded = explode(player, target.getLocation(), power);
				if (!exploded && !ignoreCanceled) {
					sendMessage(player, strNoTarget);
					fizzle(player);
					return PostCastAction.ALREADY_HANDLED;
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean explode(Player player, Location target, float power) {
		// backfire chance
		if (backfireChance > 0) {
			Random rand = new Random();
			if (rand.nextInt(10000) < backfireChance) {
				target = player.getLocation();
			}					
		}
		if (preventBlockDamage || damageMultiplier > 0) {
			recentlyExploded.put(player, power);
		}
		return player.getWorld().createExplosion(target, explosionSize * power);
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return explode(caster, target, power);
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityDamage(EntityDamageEvent event) {
		if (damageMultiplier > 0 && !event.isCancelled() && event instanceof EntityDamageByEntityEvent && event.getCause() == DamageCause.ENTITY_EXPLOSION) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
			if (evt.getDamager() instanceof Player && recentlyExploded.containsKey(evt.getDamager())) {
				float power = recentlyExploded.get(evt.getDamager());
				event.setDamage(Math.round(event.getDamage() * damageMultiplier * power));
			}
		}
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		if (event.isCancelled() || !preventBlockDamage) {
			if (recentlyExploded != null) {
				recentlyExploded.remove(event.getEntity());
			}
			return;
		}
		
		if (event.getEntity() instanceof Player && recentlyExploded.containsKey(event.getEntity())) {
			event.blockList().clear();
			event.setYield(0);
			recentlyExploded.remove(event.getEntity());
		}
	}
	
}