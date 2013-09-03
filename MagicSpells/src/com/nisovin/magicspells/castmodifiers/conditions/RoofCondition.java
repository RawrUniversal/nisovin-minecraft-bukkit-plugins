package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class RoofCondition extends Condition {

	int height = 10;
	
	@Override
	public boolean setVar(String var) {
		if (var != null && var.matches("^[0-9]+$")) {
			height = Integer.parseInt(var);
		}
		return true;
	}

	@Override
	public boolean check(Player player) {
		return check(player, player);
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		Block b = target.getLocation().add(0, 2, 0).getBlock();
		for (int i = 0; i < height; i++) {
			if (b.getType() != Material.AIR) {
				return true;
			} else {
				b = b.getRelative(BlockFace.UP);
			}
		}
		return false;
	}

}
