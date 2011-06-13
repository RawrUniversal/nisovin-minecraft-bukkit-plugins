package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class FlamewalkSpell extends BuffSpell {

	private static final String SPELL_NAME = "flamewalk";
	
	private int range;
	private int fireTicks;
	private int tickInterval;
	private boolean targetPlayers;
	private boolean checkPlugins;
	
	private HashSet<String> flamewalkers;
	private Burner burner;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FlamewalkSpell(config, spellName));
		}
	}
	
	public FlamewalkSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		range = config.getInt("spells." + spellName + ".range", 8);
		fireTicks = config.getInt("spells." + spellName + ".fire-ticks", 80);
		tickInterval = config.getInt("spells." + spellName + ".tick-interval", 100);
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		checkPlugins = config.getBoolean("spells." + spellName + ".check-plugins", true);
		
		flamewalkers = new HashSet<String>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (flamewalkers.contains(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			flamewalkers.add(player.getName());
			if (burner == null) {
				burner = new Burner();
			}
		}
		return false;
	}	
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		sendMessage(player, strFade);
		flamewalkers.remove(player.getName());
		if (flamewalkers.size() == 0 && burner != null) {
			burner.stop();
			burner = null;
		}
	}
	
	@Override
	protected void turnOff() {
		flamewalkers.clear();
		if (burner != null) {
			burner.stop();
			burner = null;
		}
	}

	private class Burner implements Runnable {
		int taskId;
		
		public Burner() {
			taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, tickInterval, tickInterval);
		}
		
		public void stop() {
			Bukkit.getServer().getScheduler().cancelTask(taskId);
		}
		
		public void run() {
			for (String s : flamewalkers) {
				Player player = Bukkit.getServer().getPlayer(s);
				if (player != null) {
					if (isExpired(player)) {
						turnOff(player);
						continue;
					}
					List<Entity> entities = player.getNearbyEntities(range, range, range);
					for (Entity entity : entities) {
						if (entity instanceof Player) {
							if (entity != player && targetPlayers) {
								if (checkPlugins) {
									EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, entity, DamageCause.ENTITY_ATTACK, 1);
									Bukkit.getServer().getPluginManager().callEvent(event);
									if (event.isCancelled()) {
										continue;
									}
								}
								entity.setFireTicks(fireTicks);
								addUse(player);
								chargeUseCost(player);
							}
						} else if (entity instanceof LivingEntity) {
							entity.setFireTicks(fireTicks);
							addUse(player);
							chargeUseCost(player);
						}
					}
				}
			}
		}
	}



}