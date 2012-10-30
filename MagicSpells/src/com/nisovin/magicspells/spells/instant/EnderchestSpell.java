package com.nisovin.magicspells.spells.instant;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class EnderchestSpell extends InstantSpell {

	public EnderchestSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			player.openInventory(player.getEnderChest());
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
