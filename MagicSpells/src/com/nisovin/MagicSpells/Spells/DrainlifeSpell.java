package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class DrainlifeSpell extends InstantSpell {
	
	private int damage;
	private int heal;
	private int animationSpeed;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private String strNoTarget;
	
	public DrainlifeSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		damage = getConfigInt("damage", 2);
		heal = getConfigInt("heal", 2);
		animationSpeed = getConfigInt("animation-speed", 2);
		obeyLos = getConfigBoolean("obey-los", true);
		targetPlayers = getConfigBoolean("target-players", false);
		checkPlugins = getConfigBoolean("check-plugins", true);
	}
	
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target == null) {
				// fail: no target
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				if (target instanceof Player && checkPlugins) {
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, damage);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
				target.damage(damage, player);
				int h = player.getHealth()+heal;
				if (h>20) h=20;
				player.setHealth(h);
				new DrainlifeAnimation(player, target);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private class DrainlifeAnimation implements Runnable {
		
		private int taskId;
		private int i;
		private ArrayList<Block> blocks;
		private World world;
		
		public DrainlifeAnimation(Player player, LivingEntity target) {			
			// get blocks to animate
			Vector start = target.getLocation().toVector();
			Vector playerVector = player.getLocation().toVector();
			double distanceSq = start.distanceSquared(playerVector);
			Vector direction = playerVector.subtract(start);
			BlockIterator iterator = new BlockIterator(player.getWorld(), start, direction, player.getEyeHeight(), range);
			blocks = new ArrayList<Block>();
			Block b;
			while (iterator.hasNext()) {
				b = iterator.next();
				if (b != null && b.getType() == Material.AIR) {
					blocks.add(b);
				} else {
					break;
				}
				if (b.getLocation().toVector().distanceSquared(start) > distanceSq) {
					break;
				}
			}
			
			// start animation
			world = player.getWorld();
			if (blocks.size() > 0) {
				i = 0;
				taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, animationSpeed, animationSpeed);
			}
		}

		@Override
		public void run() {
			if (blocks.size() > i) {
				Block b = blocks.get(i);
				world.playEffect(b.getLocation(), Effect.SMOKE, 4);
				i++;
			} else {
				Bukkit.getServer().getScheduler().cancelTask(taskId);
			}
		}
		
	}

}
