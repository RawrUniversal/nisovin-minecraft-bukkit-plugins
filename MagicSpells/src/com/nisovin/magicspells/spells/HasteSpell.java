package com.nisovin.magicspells.spells;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.MobEffect;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.magicspells.BuffSpell;
import com.nisovin.magicspells.MagicSpells;

public class HasteSpell extends BuffSpell {

	private int strength;
	
	private HashMap<Player,HasteEffectApplier> hasted;
	
	public HasteSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strength = getConfigInt("effect-strength", 4);
		
		hasted = new HashMap<Player,HasteEffectApplier>();
	}

	@Override
	protected PostCastAction castSpell(final Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (duration > 0) {
				int amplifier = Math.round(strength * power);
				if (useCostInterval > 0) {
					if (!hasted.containsKey(player)) {
						HasteEffectApplier effect = new HasteEffectApplier(player, amplifier);
						int taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, effect, 0, useCostInterval*20);
						effect.setTaskId(taskId);
						hasted.put(player,effect);
					}
					startSpellDuration(player);
				} else {
					setMobEffect(player, 1, duration*20, amplifier);
					Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
						public void run() {
							sendMessage(player, strFade);
						}
					}, duration*20 + 10);
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}



	@Override
	protected void turnOff(Player player) {
		HasteEffectApplier effect = hasted.get(player);
		if (effect != null) {
			effect.stop();
			hasted.remove(player);
			sendMessage(player, strFade);
		}
	}
	
	@Override
	protected void turnOff() {
		for (Map.Entry<Player,HasteEffectApplier> entry : hasted.entrySet()) {
			entry.getValue().stop();
		}
		hasted.clear();
	}
	
	private class HasteEffectApplier implements Runnable {
		
		private boolean firstRun = true;
		private int taskId;
		private Player player;
		private int strength;
		
		public HasteEffectApplier(Player player, int strength) {
			this.player = player;
			this.strength = strength;
		}
		
		public void setTaskId(int id) {
			this.taskId = id;
		}
		
		public void run() {
			if (isExpired(player)) {
				turnOff(player);
				return;
			}
			if (firstRun) {
				firstRun = false;
			} else {
				addUseAndChargeCost(player);
			}
			if (hasted.containsKey(player)) {
				setMobEffect(player, 1, useCostInterval*20 + 10, strength);
			}
		}
		
		public void stop() {
			Bukkit.getScheduler().cancelTask(taskId);
		}
	}
	
	public void setMobEffect(LivingEntity entity, int type, int duration, int amplifier) {		
		((CraftLivingEntity)entity).getHandle().d(new MobEffect(type, duration, amplifier));
	}

}
