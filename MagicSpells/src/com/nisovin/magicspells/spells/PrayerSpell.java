package com.nisovin.magicspells.spells;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.magicspells.InstantSpell;

public class PrayerSpell extends InstantSpell {
	
	private int amountHealed;
	private String strAtFullHealth;

	public PrayerSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		amountHealed = config.getInt("spells." + spellName + ".amount-healed", 10);
		strAtFullHealth = config.getString("spells." + spellName + ".str-at-full-health", "You are already at full health.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (player.getHealth() == 20) {
				sendMessage(player, strAtFullHealth);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				int health = player.getHealth() + Math.round(amountHealed*power);
				if (health > 20) {
					health = 20;
				}
				player.setHealth(health);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
