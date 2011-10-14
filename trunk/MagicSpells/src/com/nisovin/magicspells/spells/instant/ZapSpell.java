package com.nisovin.magicspells.spells.instant;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ZapSpell extends InstantSpell {
	
	private String strCantZap;
	private HashSet<Byte> transparentBlockTypes;
	private int[] disallowedBlockTypes;
	private boolean dropBlock;
	
	public ZapSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		strCantZap = config.getString("spells." + spellName + ".str-cant-zap", "");
		String[] transparent = config.getString("spells." + spellName + ".transparent-block-types","0,8,9").split(",");
		String[] disallowed = config.getString("spells." + spellName + ".disallowed-block-types","0,7,10,11").split(",");
		transparentBlockTypes = new HashSet<Byte>();
		for (String s : transparent) {
			transparentBlockTypes.add(Byte.parseByte(s));
		}
		disallowedBlockTypes = new int [disallowed.length];
		for (int i = 0; i < disallowed.length; i++) {
			disallowedBlockTypes[i] = Integer.parseInt(disallowed[i]);
		}
		dropBlock = config.getBoolean("spells." + spellName + ".drop-block", false);
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get targeted block
			Block target = player.getTargetBlock(transparentBlockTypes, range>0?range:100);
			if (target != null) {
				// check for disallowed block
				for (int i = 0; i < disallowedBlockTypes.length; i++) {
					if (target.getTypeId() == disallowedBlockTypes[i]) {
						sendMessage(player, strCantZap);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
				// check for protection
				BlockBreakEvent event = new BlockBreakEvent(target, player);
				MagicSpells.plugin.getServer().getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					// a plugin cancelled the event
					sendMessage(player, strCantZap);
					return PostCastAction.ALREADY_HANDLED;
				} else {
					// drop block
					if (dropBlock) {
						target.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1, target.getData()));
					}
					// show animation
					player.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, target.getTypeId());
					// remove block
					target.setType(Material.AIR);
				}
			} else {
				sendMessage(player, strCantZap);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
}