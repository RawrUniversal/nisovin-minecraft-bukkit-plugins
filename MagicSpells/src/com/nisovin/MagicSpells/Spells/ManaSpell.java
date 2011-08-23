package com.nisovin.MagicSpells.Spells;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class ManaSpell extends InstantSpell {

	private int mana;
	
	public ManaSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		mana = getConfigInt("mana", 25);
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int amount = Math.round(mana*power);
			boolean added = MagicSpells.mana.addMana(player, amount);
			if (!added) {
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}	

}
