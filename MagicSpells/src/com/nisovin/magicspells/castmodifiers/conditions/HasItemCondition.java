package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
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
			for (ItemStack item : player.getInventory().getContents()) {
				if (item != null && item.getTypeId() == id && item.getDurability() == data) {
					return true;
				}
			}
			return false;
		} else {
			return player.getInventory().contains(Material.getMaterial(id));
		}
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) {
			return check((Player)target);
		} else {
			return false;
		}
	}

}
