package com.nisovin.magicspells;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public final class MultiSpell extends Spell {

	private boolean castWithItem;
	private boolean castByCommand;
	private boolean checkIndividualCooldowns;
	
	private ArrayList<Spell> spells;
	
	public MultiSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		castWithItem = getConfigBoolean("can-cast-with-item", true);
		castByCommand = getConfigBoolean("can-cast-by-command", true);
		checkIndividualCooldowns = getConfigBoolean("check-individual-cooldowns", false);

		spells = new ArrayList<Spell>();
		List<String> spellList = getConfigStringList("spells", null);
		if (spellList != null) {
			for (String s : spellList) {
				Spell spell = MagicSpells.getSpellByInternalName(s);
				if (spell != null) {
					spells.add(spell);
				} else {
					Bukkit.getServer().getLogger().severe("MagicSpells: no such spell '" + s + "' for multi-spell '" + spellName + "'");
				}
			}
		}
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// check cooldowns
			if (checkIndividualCooldowns) {
				for (Spell spell : spells) {
					if (spell.onCooldown(player)) {
						// a spell is on cooldown
						sendMessage(player, MagicSpells.strOnCooldown);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
			}
			
			for (Spell spell : spells) {
				spell.castSpell(player, SpellCastState.NORMAL, power, null);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean canCastWithItem() {
		return castWithItem;
	}

	@Override
	public boolean canCastByCommand() {
		return castByCommand;
	}

}
