package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;

public class SafefallSpell extends BuffSpell {

	private HashSet<String> safefallers;
	
	public SafefallSpell(Configuration config, String spellName) {
		super(config, spellName);
		addListener(Event.Type.ENTITY_DAMAGE);
		
		safefallers = new HashSet<String>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (safefallers.contains(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			safefallers.add(player.getName());
			startSpellDuration(player);
		}
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL && !event.isCancelled() && event.getEntity() instanceof Player) {
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
		sendMessage(player, strFade);
	}
	
	@Override
	protected void turnOff() {
		
	}
	
}