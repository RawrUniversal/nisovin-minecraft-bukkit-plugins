package com.nisovin.magicspells.util;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;

public class BlockUtils {

	public static boolean isTransparent(Block block) {
		return MagicSpells.getTransparentBlocks().contains((byte)block.getTypeId());
	}
	
	public static Block getTargetBlock(LivingEntity entity, int range) {
		return entity.getTargetBlock(MagicSpells.getTransparentBlocks(), range);
	}
	
	public static List<Block> getLastTwoTargetBlock(LivingEntity entity, int range) {
		return entity.getLastTwoTargetBlocks(MagicSpells.getTransparentBlocks(), range);
	}
	
	public static boolean isPathable(Block block) {
		return isPathable(block.getType());
	}
	
	public static boolean isPathable(Material material) {
		return
				material == Material.AIR ||
				material == Material.SAPLING ||
				material == Material.WATER ||
				material == Material.STATIONARY_WATER ||
				material == Material.POWERED_RAIL ||
				material == Material.DETECTOR_RAIL ||
				material == Material.LONG_GRASS ||
				material == Material.DEAD_BUSH ||
				material == Material.YELLOW_FLOWER ||
				material == Material.RED_ROSE ||
				material == Material.BROWN_MUSHROOM ||
				material == Material.RED_MUSHROOM ||
				material == Material.TORCH ||
				material == Material.FIRE ||
				material == Material.REDSTONE_WIRE ||
				material == Material.CROPS ||
				material == Material.SIGN_POST ||
				material == Material.LADDER ||
				material == Material.RAILS ||
				material == Material.WALL_SIGN ||
				material == Material.LEVER ||
				material == Material.STONE_PLATE ||
				material == Material.WOOD_PLATE ||
				material == Material.REDSTONE_TORCH_OFF ||
				material == Material.REDSTONE_TORCH_ON ||
				material == Material.STONE_BUTTON ||
				material == Material.SNOW ||
				material == Material.SUGAR_CANE_BLOCK ||
				material == Material.VINE ||
				material == Material.WATER_LILY ||
				material == Material.NETHER_STALK;
	}
	
	public static boolean isSafeToStand(Location location) {
		return 
				isPathable(location.getBlock()) && 
				isPathable(location.add(0, 1, 0).getBlock()) && 
				(!isPathable(location.subtract(0, 2, 0).getBlock()) || !isPathable(location.subtract(0, 1, 0).getBlock()));
	}
	
}
