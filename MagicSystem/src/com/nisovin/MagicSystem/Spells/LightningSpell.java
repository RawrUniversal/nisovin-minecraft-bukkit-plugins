package com.nisovin.MagicSystem.Spells;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSystem.MagicSystem;
import com.nisovin.MagicSystem.WandSpell;

public class LightningSpell extends WandSpell {

	private static final String SPELL_NAME = "lightning";
	
	public static void load(Configuration config) {
		if (config.getBoolean("spells." + SPELL_NAME + ".enabled", true)) {
			MagicSystem.spells.put(SPELL_NAME, new LightningSpell(config));
		}
	}
	
	public LightningSpell(Configuration config) {
		super(config, SPELL_NAME);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range>0?range:500);
			if (target != null && target.getWorld().getHighestBlockYAt(target.getLocation()) == target.getY()) {
				target.getWorld().strikeLightning(target.getLocation());
			}
		}
		return true;
	}
}
