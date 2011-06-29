package com.nisovin.nethertrees;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherTrees extends JavaPlugin {
	
	protected static int TRUNK_TYPE = Material.LOG.getId();
	protected static int LEAF_TYPE = Material.GLOWSTONE.getId();
	protected static int SAPLING_TYPE = Material.DEAD_BUSH.getId();
	protected static int SAPLING_DROP_CHANCE = 30;
	protected static int MIN_TREE_HEIGHT = 6;
	protected static int TREE_HEIGHT_VARIANCE = 4;
	protected static int CARDINAL_BRANCH_CHANCE = 85;
	protected static int DIAGONAL_BRANCH_CHANCE = 35;
	protected static int MIN_BRANCH_LENGTH = 3;
	protected static int BRANCH_LENGTH_VARIANCE = 2;
	protected static int MIN_LEAF_LENGTH = 3;
	protected static int LEAF_LENGTH_VARIANCE = 4;

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnable() {
		new NetherTreeBlockListener(this);
		new NetherTreeWorldListener(this);		
		System.out.println("NetherTrees enabled");
	}
	
}
