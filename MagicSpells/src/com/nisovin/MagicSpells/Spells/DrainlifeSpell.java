package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class DrainlifeSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "drainlife";
	
	private int animationSpeed;
	private boolean obeyLos;
	private boolean targetPlayers;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new DrainlifeSpell(config, spellName));
		}		
	}
	
	public DrainlifeSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		animationSpeed = config.getInt("spells." + spellName + ".animation-speed", 2);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target == null) {
				// fail: no target
			} else {
				new DrainlifeAnimation(player, target);
			}
		}
		return false;
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
			Vector direction = playerVector.subtract(start);
			BlockIterator iterator = new BlockIterator(player.getWorld(), start, direction, player.getEyeHeight(), (int)start.distance(playerVector));
			blocks = new ArrayList<Block>();
			Block b;
			while (iterator.hasNext()) {
				b = iterator.next();
				if (b != null && b.getType() == Material.AIR) {
					blocks.add(b);
				} else {
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
