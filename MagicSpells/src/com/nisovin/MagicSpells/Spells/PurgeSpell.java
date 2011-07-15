package com.nisovin.MagicSpells.Spells;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class PurgeSpell extends InstantSpell {
	
	public PurgeSpell(Configuration config, String spellName) {
		super(config, spellName);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			List<Entity> entities = player.getNearbyEntities(range*2, range*2, range*2);
			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && !(entity instanceof Player)) {
					((LivingEntity)entity).setHealth(0);
				}
			}
		}
		return false;
	}	
	
}