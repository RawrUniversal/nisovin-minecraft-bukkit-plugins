package com.nisovin.MagicSpells.Spells;

import java.util.Random;

import net.minecraft.server.EntityTNTPrimed;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class ExplodeSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "explode";
	
	private boolean checkPlugins;
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
		
		checkPlugins = config.getBoolean("spells." + spellName + ".check-plugins", true);
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
				return true;
			} else {
				// backfire chance
				if (backfireChance > 0) {
					Random rand = new Random();
					if (rand.nextInt(10000) < backfireChance) {
						target = player.getLocation().getBlock();
					}					
				}
				if (checkPlugins) {
					// check plugins
					EntityTNTPrimed e = new EntityTNTPrimed(((CraftWorld)target.getWorld()).getHandle(), target.getX(), target.getY(), target.getZ());
					CraftTNTPrimed c = new CraftTNTPrimed((CraftServer)Bukkit.getServer(), e);
					ExplosionPrimeEvent event = new ExplosionPrimeEvent(c, explosionSize, false);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return false;
					}
				}
				createExplosion(player, target.getLocation(), explosionSize);
			}
		}
		return false;
	}
	
	public void createExplosion(Player player, Location location, float size) {
		((CraftWorld)location.getWorld()).getHandle().createExplosion(checkPlugins?((CraftPlayer)player).getHandle():null, location.getX(), location.getY(), location.getZ(), size, false);
	}
	
}