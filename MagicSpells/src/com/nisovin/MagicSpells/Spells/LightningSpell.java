package com.nisovin.MagicSpells.Spells;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class LightningSpell extends InstantSpell {

	private static final String SPELL_NAME = "lightning";
	
	private String strCastFail;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new LightningSpell(config, spellName));
		}
	}
	
	public LightningSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strCastFail = config.getString("spells." + spellName + ".str-cast-fail", "");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range>0?range:500);
			if (target != null && target.getWorld().getHighestBlockYAt(target.getLocation()) == target.getY()+1) {
				target.getWorld().strikeLightning(target.getLocation());
			} else {
				sendMessage(player, strCastFail);
				return true;
			}
		}
		return false;
	}
}
