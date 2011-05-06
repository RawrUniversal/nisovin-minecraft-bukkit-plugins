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
		addListener(Event.Type.ENTITY_DAMAGE);		
		safefallers = new HashSet<String>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (safefallers.contains(player.getName()) {
			safefallers.remove(player.getName());
			sendMessage(player, strFade);
		} else if (state == SpellState.NORMAL) {
			safefallers.add(player.getName());
		}
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (!event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			if (safefallers.contains(player.getName())) {
				if (isExpired(player)) {
					turnOff(player);
				} else {
					addUse(player);
					boolean ok = chargeUseCost(player);
					if (ok) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		safefallers.remove(player.getName());
	}
	
	@Override
	protected void turnOff() {
		
	}
	
}