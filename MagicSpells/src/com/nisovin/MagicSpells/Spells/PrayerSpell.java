package com.nisovin.MagicSpells.Spells;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class PrayerSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "prayer";
	
	private int amountHealed;
	private String strAtFullHealth;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new PrayerSpell(config, spellName));
		}
	}

	public PrayerSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		amountHealed = config.getInt("spells." + spellName + ".amount-healed", 10);
		strAtFullHealth = config.getString("spells." + spellName + ".str-at-full-health", "You are already at full health.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (player.getHealth() == 20) {
				sendMessage(player, strAtFullHealth);
				return true;
			} else {
				int health = player.getHealth() + amountHealed;
				if (health > 20) {
					health = 20;
				}
				player.setHealth(health);
			}
		}
		return false;
	}

}
