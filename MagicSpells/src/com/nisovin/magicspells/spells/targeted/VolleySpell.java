package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class VolleySpell extends TargetedLocationSpell {

	private int arrows;
	private int speed;
	private int spread;
	private int shootInterval;
	
	public VolleySpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		arrows = getConfigInt("arrows", 10);
		speed = getConfigInt("speed", 20);
		spread = getConfigInt("spread", 150);
		shootInterval = getConfigInt("shoot-interval", 0);
	}
	
	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			
			Block target;
			try {
				target = player.getTargetBlock(null, range>0?range:100);
			} catch (IllegalStateException e) {
				target = null;
			}
			if (target == null || target.getType() == Material.AIR) {
				return noTarget(player);
			} else {
				volley(player, target.getLocation(), power);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void volley(Player player, Location target, float power) {
		Location spawn = player.getLocation();
		spawn.setY(spawn.getY()+3);
		Vector v = target.toVector().subtract(spawn.toVector()).normalize();
		
		if (shootInterval <= 0) {
			int arrows = Math.round(this.arrows*power);
			for (int i = 0; i < arrows; i++) {
				Arrow a = player.getWorld().spawnArrow(spawn, v, (speed/10.0F), (spread/10.0F));
				a.setVelocity(a.getVelocity());
				a.setShooter(player);
			}
		} else {
			new ArrowShooter(player, spawn, v);
		}
		
		playSpellEffects(player, target);
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		volley(caster, target, power);
		return true;
	}
	
	private class ArrowShooter implements Runnable {
		Player player;
		Location spawn;
		Vector dir;
		int count;
		int taskId;
		
		ArrowShooter(Player player, Location spawn, Vector dir) {
			this.player = player;
			this.spawn = spawn;
			this.dir = dir;
			this.count = 0;
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, 0, shootInterval);
		}
		
		@Override
		public void run() {
			Arrow a = player.getWorld().spawnArrow(spawn, dir, (speed/10.0F), (spread/10.0F));
			a.setVelocity(a.getVelocity());
			a.setShooter(player);
			if (++count >= arrows) {
				Bukkit.getScheduler().cancelTask(taskId);
			}
		}
	}

}