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
	
	static int MIN_TREE_HEIGHT = 6;
	
	protected static void generateTree(Block treeBase, Random random) {
		int height = random.nextInt(4)+MIN_TREE_HEIGHT;
		for (int y = 0; y < height; y++) {
			treeBase.getRelative(0, y, 0).setType(Material.LOG);
		}
		Block treeTop = treeBase.getRelative(0,height,0);
		if (random.nextInt(100) < 35) generateBranch(treeTop, 1, 1, height, random);
		if (random.nextInt(100) < 35) generateBranch(treeTop, 1, -1, height, random);
		if (random.nextInt(100) < 35) generateBranch(treeTop, -1, 1, height, random);
		if (random.nextInt(100) < 35) generateBranch(treeTop, -1, -1, height, random);
		if (random.nextInt(100) < 85) generateBranch(treeTop, 0, 1, height, random);
		if (random.nextInt(100) < 85) generateBranch(treeTop, 1, 0, height, random);
		if (random.nextInt(100) < 85) generateBranch(treeTop, 0, -1, height, random);
		if (random.nextInt(100) < 85) generateBranch(treeTop, -1, 0, height, random);
		
	}
	
	private static void generateBranch(Block treeTop, int xDir, int zDir, int treeHeight, Random random) {
		int branchLength = random.nextInt(2)+3+((treeHeight-MIN_TREE_HEIGHT)/2);
		for (int i = 1; i <= branchLength; i++) {
			setBlock(treeTop.getRelative(xDir*i, i==1||i==branchLength?0:1, zDir*i), Material.LOG);
		}
		for (int i = 1; i < random.nextInt(4)+3+(treeHeight-MIN_TREE_HEIGHT); i++) {
			setBlock(treeTop.getRelative(xDir*branchLength, -i, zDir*branchLength), Material.GLOWSTONE);
		}
	}
	
	private static void setBlock(Block block, Material type) {
		if (block.getType() == Material.AIR) { 
			block.setType(type);
		}
	}

}
