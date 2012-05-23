package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class InBlockCondition extends Condition {

	int[] ids = new int[0];

	@Override
	public void setVar(String var) {
		String[] vardata = var.split(",");
		ids = new int[vardata.length];
		for (int i = 0; i < vardata.length; i++) {
			ids[i] = Integer.parseInt(vardata[i]);
		}
	}
	
	@Override
	public boolean check(Player player) {
		int blockId = player.getLocation().getBlock().getTypeId();
		for (int id : ids) {
			if (blockId == id) {
				return true;
			}
		}
		return false;
	}

}
