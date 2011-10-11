package com.nisovin.magicspells.spells;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;

public class ManaSpell extends InstantSpell {

	private int mana;
	
	public ManaSpell(MagicConfig config, String spellName) {
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
