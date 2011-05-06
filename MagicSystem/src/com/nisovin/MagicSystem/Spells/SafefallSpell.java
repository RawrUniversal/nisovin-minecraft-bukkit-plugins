package com.nisovin.MagicSystem.Spells;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSystem.EnhanceSpell;
import com.nisovin.MagicSystem.MagicSystem;

public class SafefallSpell extends EnhanceSpell {

	private static final String SPELL_NAME = "safefall";

	private HashSet<String> safefallers;
	
	public static void load(Configuration config) {
		if (config.getBoolean("spells." + SPELL_NAME + ".enabled", true)) {
			MagicSystem.spells.put(SPELL_NAME, new SafefallSpell(config));
		}
	}
	
	public SafefallSpell(Configuration config) {
		super(config, SPELL_NAME);
		MagicSystem.entityDamageListeners.add(this);
		
		safefallers = new HashSet<String>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			if (safefallers.contains(player.getName())) {
				addUse(player);
				boolean ok = chargeUseCost(player);
				if (ok) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
	}
	
}