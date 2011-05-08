package com.nisovin.MagicSpells.Spells;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class ForcepushSpell extends InstantSpell {

	private static final String SPELL_NAME = "forcepush";
	
	private int force;
	private int yForce;
	
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
		
		force = config.getInt("spells." + spellName + ".pushback-force", 30);
		yForce = config.getInt("spells." + spellName + ".additional-vertical-force", 15);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			knockback(player, range, false);
		}
		return false;
	}
	
	public void knockback(Player player, int range, boolean targetPlayers) {
	    Vector p = player.getLocation().toVector();
		List<LivingEntity> entities = player.getWorld().getLivingEntities();
		for (LivingEntity entity : entities) {
			double dx = entity.getLocation().getX() - player.getLocation().getX();
			double dy = entity.getLocation().getY() - player.getLocation().getY();
			double dz = entity.getLocation().getZ() - player.getLocation().getZ();
			if (Math.abs(dx) < range && Math.abs(dy) < range && Math.abs(dz) < range && (targetPlayers || !(entity instanceof Player))) {
				Vector e = entity.getLocation().toVector();
				Vector v = e.subtract(p).normalize().multiply(force/10.0);
				v.setY(v.getY() * (yForce/10.0));
				System.out.println(entity.toString() + v.toString());
				entity.setVelocity(v);
			}
	    }
	    
	}

}
