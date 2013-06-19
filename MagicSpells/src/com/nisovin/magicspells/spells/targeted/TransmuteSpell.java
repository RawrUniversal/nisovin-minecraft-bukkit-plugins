package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class TransmuteSpell extends TargetedLocationSpell {

	int[] blockTypes;
	int transmuteType;
	byte transmuteData;
	BlockFace[] checkDirs = new BlockFace[] { BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	boolean checkAll = false;
	
	public TransmuteSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		List<Integer> list = getConfigIntList("transmutable-types", null);
		if (list == null) list = new ArrayList<Integer>();
		if (list.size() == 0) list.add(Material.IRON_BLOCK.getId());
		blockTypes = new int[list.size()];
		for (int i = 0; i < blockTypes.length; i++) {
			blockTypes[i] = list.get(i);
		}
		Arrays.sort(blockTypes);
		
		transmuteType = getConfigInt("transmute-type", Material.GOLD_BLOCK.getId());
		transmuteData = (byte)getConfigInt("transmute-data", 0);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block block = player.getTargetBlock(MagicSpells.getTransparentBlocks(), range);
			if (block == null) {
				return noTarget(player);
			}
			
			if (!canTransmute(block)) {
				return noTarget(player);
			}
			
			block.setTypeIdAndData(transmuteType, transmuteData, true);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		Block block = target.getBlock();
		if (canTransmute(block)) {
			block.setTypeIdAndData(transmuteType, transmuteData, true);
			return true;
		} else {
			Vector v = target.getDirection();
			block = target.clone().add(v).getBlock();
			if (canTransmute(block)) {
				block.setTypeIdAndData(transmuteType, transmuteData, true);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean canTransmute(Block block) {
		return Arrays.binarySearch(blockTypes, block.getTypeId()) >= 0;
	}

}
