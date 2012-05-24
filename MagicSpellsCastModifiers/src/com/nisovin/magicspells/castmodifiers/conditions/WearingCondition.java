package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.castmodifiers.Condition;

public class WearingCondition extends Condition {

	int[] ids;

	@Override
	public boolean setVar(String var) {
		try {
			String[] vardata = var.split(",");
			ids = new int[vardata.length];
			for (int i = 0; i < vardata.length; i++) {
				ids[i] = Integer.parseInt(vardata[i]);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean check(Player player) {
		PlayerInventory inv = player.getInventory();
		if (check(inv.getHelmet())) {
			return true;
		} else if (check(inv.getChestplate())) {
			return true;
		} else if (check(inv.getLeggings())) {
			return true;
		} else if (check(inv.getBoots())) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean check(ItemStack item) {
		if (item == null) return false;
		int thisid = item.getTypeId();
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == thisid) {
				return true;
			}
		}
		return false;
	}

}
