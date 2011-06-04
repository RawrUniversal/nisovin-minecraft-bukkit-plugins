package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class GillsSpell extends BuffSpell {

	private static final String SPELL_NAME = "gills";

	private HashSet<String> fishes;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new GillsSpell(config, spellName));
		}
	}
	
	public GillsSpell(Configuration config, String spellName) {
		super(config, spellName);
		addListener(Event.Type.ENTITY_DAMAGE);
		
		fishes = new HashSet<String>();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (fishes.contains(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			fishes.add(player.getName());
		}
		return false;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (!event.isCancelled() && event.getEntity() instanceof Player && event.getCause() == DamageCause.DROWNING) {
			Player player = (Player)event.getEntity();
			if (fishes.contains(player.getName())) {
				if (isExpired(player)) {
					turnOff(player);
				} else {
					addUse(player);
					boolean ok = chargeUseCost(player);
					if (ok) {
						event.setCancelled(true);
						player.setRemainingAir(player.getMaximumAir());
					}
				}
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		fishes.remove(player.getName());
		sendMessage(player, strFade);
	}
	
	@Override
	protected void turnOff() {
		
	}

}
