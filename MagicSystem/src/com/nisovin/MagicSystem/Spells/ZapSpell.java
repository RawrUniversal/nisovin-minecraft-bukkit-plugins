package com.nisovin.MagicSystem.Spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSystem.MagicSystem;
import com.nisovin.MagicSystem.WandSpell;

public class ZapSpell extends WandSpell {

	private static final String SPELL_NAME = "zap";
	
	private String strCantZap;
	private int[] disallowedBlockTypes;
	private boolean dropBlock;
	
	public static void load(Configuration config) {
		if (config.getBoolean("spells." + SPELL_NAME + ".enabled", true)) {
			MagicSystem.spells.put(SPELL_NAME, new ZapSpell(config));
		}
	}
	
	public ZapSpell(Configuration config) {
		super(config, SPELL_NAME);
		
		strCantZap = config.getString("spells." + SPELL_NAME + ".str-cant-blink", "You can't zap that.");
		
		String[] disallowed = config.getString("spells." + SPELL_NAME + ".disallowed-block-types","0,7").split(",");
		disallowedBlockTypes = new int [disallowed.length];
		for (int i = 0; i < disallowed.length; i++) {
			disallowedBlockTypes[i] = Integer.parseInt(disallowed[i]);
		}
		dropBlock = config.getBoolean("spells." + SPELL_NAME + ".drop-block", false);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		System.out.println("cast");
		if (state == SpellCastState.NORMAL) {
			// get targeted block
			Block target = player.getTargetBlock(null, range>0?range:500);
			if (target != null) {
				// TODO: check for protection
				// check for disallowed block
				for (int i = 0; i < disallowedBlockTypes.length; i++) {
					if (target.getTypeId() == disallowedBlockTypes[i]) {
						sendMessage(player, strCantZap);
						return true;
					}
				}
				// drop block
				if (dropBlock) {
					// TODO: fix this
					target.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1, target.getData()));
				}
				// remove block
				target.setType(Material.AIR);
			} else {
				sendMessage(player, strCantZap);
				return true;
			}
		}
		return false;
	}
}
