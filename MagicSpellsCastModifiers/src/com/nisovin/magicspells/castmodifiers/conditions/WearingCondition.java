package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.castmodifiers.Condition;

public class WearingCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		int typeId = Integer.parseInt(var);
		PlayerInventory inv = player.getInventory();
		if (inv.getHelmet() != null && inv.getHelmet().getTypeId() == typeId) {
			return true;
		} else if (inv.getChestplate() != null && inv.getChestplate().getTypeId() == typeId) {
			return true;
		} else if (inv.getLeggings() != null && inv.getLeggings().getTypeId() == typeId) {
			return true;
		} else if (inv.getBoots() != null && inv.getBoots().getTypeId() == typeId) {
			return true;
		} else {
			return false;
		}
	}

}
