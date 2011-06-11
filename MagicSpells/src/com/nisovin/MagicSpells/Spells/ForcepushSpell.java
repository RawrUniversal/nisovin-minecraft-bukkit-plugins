package com.nisovin.MagicSpells.Spells;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class ForcepushSpell extends InstantSpell {

	private static final String SPELL_NAME = "forcepush";
	
	private boolean targetPlayers;
	private int force;
	private int yForce;
	private int maxYForce;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new ForcepushSpell(config, spellName));
		}
	}
	
	public ForcepushSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		force = config.getInt("spells." + spellName + ".pushback-force", 30);
		yForce = config.getInt("spells." + spellName + ".additional-vertical-force", 15);
		maxYForce = config.getInt("spells." + spellName + ".max-vertical-force", 20);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			knockback(player, range, targetPlayers);
		}
		return false;
	}
	
	public void knockback(Player player, int range, boolean targetPlayers) {
	    Vector p = player.getLocation().toVector();
		List<Entity> entities = player.getNearbyEntities(range, range, range);
		Vector e, v;
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity && (targetPlayers || !(entity instanceof Player))) {
				e = entity.getLocation().toVector();
				v = e.subtract(p).normalize().multiply(force/10.0);
				v.setY(v.getY() * (yForce/10.0));
				if (v.getY() > (maxYForce/10.0)) {
					v.setY(maxYForce/10.0);
				}
				entity.setVelocity(v);
			}
	    }
	}
	
	/*public void knockback(Player player, int range, boolean targetPlayers) {
	    Vector p = player.getLocation().toVector();
		List<LivingEntity> entities = player.getWorld().getLivingEntities();
		double dx, dy, dz;
		Vector e, v;
		for (LivingEntity entity : entities) {
			dx = entity.getLocation().getX() - player.getLocation().getX();
			dy = entity.getLocation().getY() - player.getLocation().getY();
			dz = entity.getLocation().getZ() - player.getLocation().getZ();
			if (Math.abs(dx) < range && Math.abs(dy) < range && Math.abs(dz) < range && (targetPlayers || !(entity instanceof Player))) {
				e = entity.getLocation().toVector();
				v = e.subtract(p).normalize().multiply(force/10.0);
				v.setY(v.getY() * (yForce/10.0));
				entity.setVelocity(v);
			}
	    }
		p = null;
		e = null;
		v = null;
		entities = null;
	}*/

}
