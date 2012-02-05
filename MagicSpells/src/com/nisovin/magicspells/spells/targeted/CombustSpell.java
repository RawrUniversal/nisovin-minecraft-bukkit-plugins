package com.nisovin.magicspells.spells.targeted;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class CombustSpell extends TargetedSpell {
	
	private boolean targetPlayers;
	private int fireTicks;
	private int fireTickDamage;
	private boolean preventImmunity;
	private boolean obeyLos;
	private boolean checkPlugins;
	private String strNoTarget;
	
	private HashMap<Integer, CombustData> combusting = new HashMap<Integer, CombustData>();
	
	public CombustSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		targetPlayers = getConfigBoolean("target-players", false);
		fireTicks = getConfigInt("fire-ticks", 100);
		fireTickDamage = getConfigInt("fire-tick-damage", 1);
		preventImmunity = getConfigBoolean("prevent-immunity", true);
		obeyLos = getConfigBoolean("obey-los", true);
		checkPlugins = getConfigBoolean("check-plugins", true);
		strNoTarget = getConfigString("str-no-target", "");
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			final LivingEntity target = getTargetedEntity(player, range>0?range:100, targetPlayers, obeyLos);
			if (target == null) {
				sendMessage(player, strNoTarget);
				fizzle(player);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				if (target instanceof Player && checkPlugins) {
					// call other plugins
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, 1);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						fizzle(player);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
				int duration = Math.round(fireTicks*power);
				combusting.put(target.getEntityId(), new CombustData(power, target.getWorld().getFullTime() + duration));
				target.setFireTicks(duration);
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						CombustData data = combusting.get(target.getEntityId());
						if (data != null) {
							if (target.getWorld().getFullTime() > data.expires) {
								combusting.remove(target.getEntityId());
							}
						}
					}
				}, duration+2);
				
				// TODO: manually send messages with replacements
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.isCancelled() || event.getCause() != DamageCause.FIRE_TICK) return;
		
		CombustData data = combusting.get(event.getEntity().getEntityId());
		if (data != null) {
			if (event.getEntity().getWorld().getFullTime() <= data.expires) { 
				event.setDamage(Math.round(fireTickDamage * data.power));
			}
			if (preventImmunity) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						((LivingEntity)event.getEntity()).setNoDamageTicks(0);
					}
				}, 0);
			}
		}
	}
	
	private class CombustData {
		float power;
		long expires;
		
		CombustData(float power, long expires) {
			this.power = power;
			this.expires = expires;
		}
	}
}