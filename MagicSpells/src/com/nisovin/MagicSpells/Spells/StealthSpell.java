package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class StealthSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "stealth";
	
	private HashSet<String> stealthy;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new StealthSpell(config, spellName));
		}		
	}
	
	public StealthSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		addListener(Event.Type.ENTITY_TARGET);
		
		stealthy = new HashSet<String>();
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (stealthy.contains(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			stealthy.add(player.getName());
			startSpellDuration(player);
		}
		return false;
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (!event.isCancelled() && stealthy.size() > 0 && event.getTarget() instanceof Player) {
			Player player = (Player)event.getTarget();
			if (stealthy.contains(player.getName())) {
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
		stealthy.remove(player.getName());
		sendMessage(player, strFade);
	}
	
	@Override
	protected void turnOff() {
		stealthy.clear();
	}
	
}