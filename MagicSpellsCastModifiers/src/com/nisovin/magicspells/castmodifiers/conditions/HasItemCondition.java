package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.castmodifiers.Condition;

public class HasItemCondition extends Condition {

	int id;
	short data;
	boolean checkData;
	
	@Override
	public boolean setVar(String var) {
		try {
			if (var.contains(":")) {
				String[] vardata = var.split(":");
				id = Integer.parseInt(vardata[0]);
				data = Short.parseShort(vardata[1]);
				checkData = true;
			} else {
				id = Integer.parseInt(var);
				checkData = false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		if (checkData) {
			return player.getInventory().contains(new ItemStack(id, 1, data), 1);
		} else {
			return player.getInventory().contains(Material.getMaterial(id));
		}
	}

}
