package com.nisovin.nethertrees;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

public class NetherTreePopulator extends BlockPopulator {

	@Override
	public void populate(World world, Random random, Chunk chunk) {
		if (random.nextInt(100) < 50) {
			int x = random.nextInt(16);
			int z = random.nextInt(16);
			
			int airSpace = 0;
			Block treeBase = null;
			for (int y = random.nextInt(90)+10; y < 125; y++) {
				Block b = chunk.getBlock(x, y, z);
				if (treeBase == null && b.getType() == Material.AIR && b.getRelative(0,-1,0).getType() == Material.NETHERRACK) {
					treeBase = b;
					airSpace = 1;
				} else if (treeBase != null && b.getType() == Material.AIR) {
					airSpace++;
					if (airSpace > 10) {
						break;
					}
				} else if (treeBase != null && b.getType() != Material.AIR) {
					treeBase = null;
					airSpace = 0;
				}
			}
			
			if (treeBase != null) {
				generateTree(treeBase, random);
			}
		}
	}
	
	protected static void generateTree(Block treeBase, Random random) {
		int height = random.nextInt(NetherTrees.TREE_HEIGHT_VARIANCE)+NetherTrees.MIN_TREE_HEIGHT;
		for (int y = 0; y < height; y++) {
			treeBase.getRelative(0, y, 0).setTypeId(NetherTrees.TRUNK_TYPE);
		}
		Block treeTop = treeBase.getRelative(0,height,0);
		if (random.nextInt(100) < NetherTrees.CARDINAL_BRANCH_CHANCE) generateBranch(treeTop, 0, 1, height, random);
		if (random.nextInt(100) < NetherTrees.CARDINAL_BRANCH_CHANCE) generateBranch(treeTop, 1, 0, height, random);
		if (random.nextInt(100) < NetherTrees.CARDINAL_BRANCH_CHANCE) generateBranch(treeTop, 0, -1, height, random);
		if (random.nextInt(100) < NetherTrees.CARDINAL_BRANCH_CHANCE) generateBranch(treeTop, -1, 0, height, random);
		if (random.nextInt(100) < NetherTrees.DIAGONAL_BRANCH_CHANCE) generateBranch(treeTop, 1, 1, height, random);
		if (random.nextInt(100) < NetherTrees.DIAGONAL_BRANCH_CHANCE) generateBranch(treeTop, 1, -1, height, random);
		if (random.nextInt(100) < NetherTrees.DIAGONAL_BRANCH_CHANCE) generateBranch(treeTop, -1, 1, height, random);
		if (random.nextInt(100) < NetherTrees.DIAGONAL_BRANCH_CHANCE) generateBranch(treeTop, -1, -1, height, random);		
	}
	
	private static void generateBranch(Block treeTop, int xDir, int zDir, int treeHeight, Random random) {
		int branchLength = random.nextInt(NetherTrees.BRANCH_LENGTH_VARIANCE)+NetherTrees.MIN_BRANCH_LENGTH+((treeHeight-NetherTrees.MIN_TREE_HEIGHT)/2);
		for (int i = 1; i <= branchLength; i++) {
			setBlock(treeTop.getRelative(xDir*i, i==1||i==branchLength?0:1, zDir*i), NetherTrees.TRUNK_TYPE);
		}
		for (int i = 1; i < random.nextInt(NetherTrees.LEAF_LENGTH_VARIANCE+treeHeight-NetherTrees.MIN_TREE_HEIGHT)+NetherTrees.MIN_LEAF_LENGTH; i++) {
			setBlock(treeTop.getRelative(xDir*branchLength, -i, zDir*branchLength), NetherTrees.LEAF_TYPE);
		}
	}
	
	private static void setBlock(Block block, int typeId) {
		if (block.getType() == Material.AIR) { 
			block.setTypeId(typeId);
		}
	}

}
