package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

public class OnBlockCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		int blockId = player.getLocation().subtract(0, 1, 0).getBlock().getTypeId();
		String[] ids = var.split(",");
		for (String id : ids) {
			if (blockId == Integer.parseInt(id)) {
				return true;
			}
		}
		return false;
	}

}
