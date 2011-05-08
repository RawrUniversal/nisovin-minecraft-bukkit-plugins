package com.nisovin.MagicSpells.Spells;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class ExplodeSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "explode";
	
	private boolean requireTntPerm;
	private int explosionSize;
	private int backfireChance;
	private String strNoTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new ExplodeSpell(config, spellName));
		}
	}
	
	public ExplodeSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		requireTntPerm = config.getBoolean("spells." + spellName + ".require-build-tnt-perm", true);
		explosionSize = config.getInt("spells." + spellName + ".explosion-size", 4);
		backfireChance = config.getInt("spells." + spellName + ".backfire-chance", 0);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "Cannot explode there.");
	}
	
	public boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range);
			if (target == null || target.getType() == Material.AIR) {
				// fail: no target
				sendMessage(player, strNoTarget);
			} else {
				// backfire chance
				if (backfireChance > 0) {
					Random rand = new Random();
					if (rand.nextInt(10000) < backfireChance) {
						target = player.getLocation().getBlock();
					}					
				}
				boolean goAhead = true;
				if (requireTntPerm) {
					// check permissions
					BlockCanBuildEvent event = new BlockCanBuildEvent(target, Material.TNT.getId(), true);
					MagicSpells.plugin.getServer().getPluginManager().callEvent(event);
					if (!event.isBuildable()) {
						goAhead = false;
					}
				}
				if (goAhead) {
					createExplosion(target.getLocation(), explosionSize);
				}
			}
		}
		return false;
	}
	
	public void createExplosion(Location location, float size) {
		((CraftWorld)location.getWorld()).getHandle().createExplosion(null, location.getX(), location.getY(), location.getZ(), size, false);
	}
	
}