package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class FirenovaSpell extends InstantSpell {

	private static final String SPELL_NAME = "firenova";

	private int fireRings;
	private int tickSpeed;
	
	private HashSet<Player> fireImmunity;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FirenovaSpell(config, spellName));
		}
	}
	
	public FirenovaSpell(Configuration config, String spellName) {
		super(config, spellName);
		addListener(Event.Type.ENTITY_DAMAGE);
		
		fireRings = config.getInt("spells." + spellName + ".fire-rings", 5);
		tickSpeed = config.getInt("spells." + spellName + ".tick-speed", 5);
		
		fireImmunity = new HashSet<Player>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			fireImmunity.add(player);
			new FirenovaAnimation(player);
		}
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK)) {
			Player p = (Player)event.getEntity();
			if (fireImmunity.contains(p)) {
				event.setCancelled(true);
				p.setFireTicks(0);
			}
		}
	}
	
	private class FirenovaAnimation implements Runnable {
		Player player;
		int i;
		Block center;
		HashSet<Block> fireBlocks;
		int taskId;
		
		public FirenovaAnimation(Player player) {
			this.player = player;
			
			i = 0;
			center = player.getLocation().getBlock();
			fireBlocks = new HashSet<Block>();
			
			taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, 0, tickSpeed);
		}
		
		public void run() {
			// remove old fire blocks
			for (Block block : fireBlocks) {
				if (block.getType() == Material.FIRE) {
					block.setType(Material.AIR);
				}
			}
			fireBlocks.clear();
						
			i += 1;
			if (i <= fireRings) {
				// set next ring on fire
				int bx = center.getX();
				int y = center.getY();
				int bz = center.getZ();
				for (int x = bx - i; x <= bx + i; x++) {
					for (int z = bz - i; z <= bz + i; z++) {
						if (Math.abs(x-bx) == i || Math.abs(z-bz) == i) {
							Block b = center.getWorld().getBlockAt(x,y,z);
							if (b.getType() == Material.AIR) {
								Block under = b.getRelative(BlockFace.DOWN);
								if (under.getType() == Material.AIR) {
									b = under;
								}
								b.setType(Material.FIRE);
								fireBlocks.add(b);
							} else if (b.getRelative(BlockFace.UP).getType() == Material.AIR) {
								b = b.getRelative(BlockFace.UP);
								b.setType(Material.FIRE);
								fireBlocks.add(b);
							}
						}
					}
				}
			} else {				
				// stop if done
				Bukkit.getServer().getScheduler().cancelTask(taskId);
				fireImmunity.remove(player);
			}
		}
	}

}