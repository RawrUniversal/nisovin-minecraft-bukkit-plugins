package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.Util.SpellAnimation;

public class GeyserSpell extends InstantSpell {
	
	private int damage;
	private double velocity;
	private int tickInterval;
	private int geyserHeight;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private String strNoTarget;

	public GeyserSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		damage = getConfigInt(config, "damage", 0);
		velocity = getConfigInt(config, "velocity", 10) / 10.0D;
		tickInterval = getConfigInt(config, "animation-speed", 2);
		geyserHeight = getConfigInt(config, "geyser-height", 4);
		obeyLos = getConfigBoolean(config, "obey-los", true);
		targetPlayers = getConfigBoolean(config, "target-players", false);
		checkPlugins = getConfigBoolean(config, "check-plugins", true);
		strNoTarget = getConfigString(config, "str-no-target", "No target found.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target == null) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// check plugins
			if (target instanceof Player && checkPlugins) {
				EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, damage);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					sendMessage(player, strNoTarget);
					return PostCastAction.ALREADY_HANDLED;
				}				
			}
			
			// do damage and launch target
			if (damage > 0) {
				target.damage(damage, player);				
			}
			if (velocity > 0) {
				target.setVelocity(new Vector(0, velocity, 0));
			}
			
			// create animation
			if (geyserHeight > 0) {
				List<Entity> allNearby = target.getNearbyEntities(50, 50, 50);
				List<Player> playersNearby = new ArrayList<Player>();
				for (Entity e : allNearby) {
					if (e instanceof Player) {
						playersNearby.add((Player)e);
					}
				}
				new GeyserAnimation(target.getLocation(), playersNearby);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private class GeyserAnimation extends SpellAnimation {

		private Location start;
		private List<Player> nearby;
		
		public GeyserAnimation(Location start, List<Player> nearby) {
			super(0, tickInterval, true);
			this.start = start;
			this.nearby = nearby;
		}

		@Override
		protected void onTick(int tick) {
			if (tick > geyserHeight*2) {
				stop();
			} else if (tick < geyserHeight) {
				Block block = start.clone().add(0,tick,0).getBlock();
				if (block.getType() == Material.AIR) {
					for (Player p : nearby) {
						p.sendBlockChange(block.getLocation(), Material.STATIONARY_WATER, (byte)0);
					}
				}
			} else {
				int n = geyserHeight-(tick-geyserHeight)-1; // top to bottom
				//int n = tick-height; // bottom to top
				Block block = start.clone().add(0, n, 0).getBlock();
				for (Player p : nearby) {
					p.sendBlockChange(block.getLocation(), block.getType(), block.getData());
				}
			}
		}
		
	}

}
