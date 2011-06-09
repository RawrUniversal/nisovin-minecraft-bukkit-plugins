package com.nisovin.MagicSpells.Spells;

import net.minecraft.server.EntityFireball;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class FireballSpell extends InstantSpell {

	private static final String SPELL_NAME = "fireball";
	
	private boolean noExplosion;
	private boolean noFire;
	private String strNoTarget;
	
	private HashSet<EntityFireball> fireballs;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FireballSpell(config, spellName));
		}
	}
	
	public FireballSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		noExplosion = config.getBoolean("spells." + spellName + ".no-explosion", false);
		noFire = config.getBoolean("spells." + spellName + ".no-fire", false);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You cannot throw a fireball there.");
		
		fireballs = new HashSet<EntityFireball>();
		addListener(Event.Type.EXPLOSION_PRIME);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range);
			if (target == null || target.getType() == Material.AIR) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				return true;
			} else {
				Location blockLoc = target.getLocation();				
				blockLoc.setX(blockLoc.getX()+.5);
				blockLoc.setY(blockLoc.getY()+.5);
				blockLoc.setZ(blockLoc.getZ()+.5);
				
				Vector path = blockLoc.toVector().subtract(player.getEyeLocation().toVector());		
				EntityFireball fireball = new EntityFireball(((CraftWorld)player.getWorld()).getHandle(), ((CraftPlayer)player).getHandle(), path.getX(), path.getY(), path.getZ());
				Vector v = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(2));
				fireball.setPosition(v.getX(), v.getY(), v.getZ());
				((CraftWorld)player.getWorld()).getHandle().addEntity(fireball);
				fireballs.add(fireball);
			}
		}
		return false;
	}
	
	@Override
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (((CraftEntity)event.getEntity()).getHandle() instanceof EntityFireball) {
			EntityFireball fireball = (EntityFireball)(((CraftEntity)event.getEntity()).getHandle());
			if (fireballs.contains(fireball)) {
				if (noExplosion) {
					event.setCancelled(true);
				} else if (noFire) {
					event.setFire(false);
				} else {
					event.setFire(true);
				}
				fireballs.remove(fireball);
			}
		}
	}
	
	
}