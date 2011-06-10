package com.nisovin.MagicSpells.Spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class ZapSpell extends InstantSpell {

	private static final String SPELL_NAME = "zap";
	
	private String strCantZap;
	private int[] disallowedBlockTypes;
	private boolean dropBlock;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new ZapSpell(config, spellName));
		}
	}
	
	public ZapSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strCantZap = config.getString("spells." + spellName + ".str-cant-zap", "");
		
		String[] disallowed = config.getString("spells." + spellName + ".disallowed-block-types","0,7").split(",");
		disallowedBlockTypes = new int [disallowed.length];
		for (int i = 0; i < disallowed.length; i++) {
			disallowedBlockTypes[i] = Integer.parseInt(disallowed[i]);
		}
		dropBlock = config.getBoolean("spells." + spellName + ".drop-block", false);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get targeted block
			Block target = player.getTargetBlock(null, range>0?range:100);
			if (target != null) {
				// check for disallowed block
				for (int i = 0; i < disallowedBlockTypes.length; i++) {
					if (target.getTypeId() == disallowedBlockTypes[i]) {
						sendMessage(player, strCantZap);
						return true;
					}
				}
				// check for protection
				BlockBreakEvent event = new BlockBreakEvent(target, player);
				MagicSpells.plugin.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					// a plugin cancelled the event
					sendMessage(player, strCantZap);
					return true;
				} else {
					// drop block
					if (dropBlock) {
						target.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1, target.getData()));
					}
					// remove block
					target.setType(Material.AIR);
				}
			} else {
				sendMessage(player, strCantZap);
				return true;
			}
		}
		return false;
	}
}
