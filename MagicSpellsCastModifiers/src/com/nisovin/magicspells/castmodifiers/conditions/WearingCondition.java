package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.castmodifiers.Condition;

public class WearingCondition extends Condition {

	int[] ids;
	short[] datas;
	boolean[] checkData;

	@Override
	public boolean setVar(String var) {
		try {
			String[] vardata = var.split(",");
			ids = new int[vardata.length];
			datas = new short[vardata.length];
			checkData = new boolean[vardata.length];
			for (int i = 0; i < vardata.length; i++) {
				if (vardata[i].contains(":")) {
					String[] subvardata = vardata[i].split(":");
					ids[i] = Integer.parseInt(subvardata[0]);
					datas[i] = Short.parseShort(subvardata[1]);
					checkData[i] = true;
				} else {
					ids[i] = Integer.parseInt(vardata[i]);
					datas[i] = 0;
					checkData[i] = false;
				}
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
		short thisdata = item.getDurability();
		for (int i = 0; i < ids.length; i++) {
			if (ids[i] == thisid && (!checkData[i] || datas[i] == thisdata)) {
				return true;
			}
		}
		return false;
	}

}
