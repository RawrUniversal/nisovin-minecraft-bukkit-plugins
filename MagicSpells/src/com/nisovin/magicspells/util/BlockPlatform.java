package com.nisovin.magicspells.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockPlatform {

	private Material platformType;
	private Material replaceType;
	private Block center;
	private int size;
	private boolean moving;
	private String type;
	private Set<Block> blocks;
		
	public BlockPlatform(Material platformType, Material replaceType, Block center, int size, boolean moving, String type) {
		this.platformType = platformType;
		this.replaceType = replaceType;
		this.center = center;
		this.size = size;
		this.moving = moving;
		this.type = type;
		
		if (moving) {
			blocks = new HashSet<Block>();
		}
		
		createPlatform();
	}
	
	public void createPlatform() {
		Set<Block> platform = new HashSet<Block>();
		
		// get platform blocks
		if (type.equals("square")) {
			Block block, above;
			for (int x = center.getX()-size; x <= center.getX()+size; x++) {
				for (int z = center.getZ()-size; z <= center.getZ()+size; z++) {
					int y = center.getY();
					block = center.getWorld().getBlockAt(x,y,z);
					above = block.getRelative(0,1,0);
					if ((block.getType() == replaceType && (block.getY() == 127 || blocks.contains(above) || above.getType() == Material.AIR)) || (blocks != null && blocks.contains(block))) {
						// only add if it's a replaceable block and has air above, or if it is already part of the platform
						platform.add(block);
					}
				}
			}
		} else if (type.equals("cube")) {
			Block block;
			for (int x = center.getX()-size; x <= center.getX()+size; x++) {
				for (int y = center.getY()-size; y <= center.getY()+size; y++) {
					for (int z = center.getZ()-size; z <= center.getZ()+size; z++) {
						block = center.getWorld().getBlockAt(x,y,z);
						if (block.getType() == replaceType || (blocks != null && blocks.contains(block))) {
							// only add if it's a replaceable block or if it is already part of the block set
							platform.add(block);
						}
					}
				}
			}
		}
		
		// remove old platform blocks
		if (moving) {
			for (Block block : blocks) {
				if (!platform.contains(block) && block.getType() == platformType) {
					block.setType(replaceType);
				}
			}
		}
		
		// add new platform blocks
		for (Block block : platform) {
			//if (blocks == null || !blocks.contains(block)) {
				block.setType(platformType);
			//}
		}
		
		// update platform block set
		if (moving) {
			blocks = platform;
		}
	}
	
	public boolean movePlatform(Block center) {
		return movePlatform(center, false);
	}
	
	public boolean movePlatform(Block center, boolean force) {
		if (force || isMoved(center)) {
			this.center = center;
			createPlatform();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isMoved(Block newCenter) {
		return isMoved(newCenter, true);
	}
	
	public boolean isMoved(Block newCenter, boolean allowDown) {
		if (!allowDown && newCenter.getY() < center.getY()) {
			return false;
		} else {
			return !newCenter.getLocation().equals(center.getLocation());
		}
	}
	
	public boolean isMovedHorizontally(Block newCenter) {
		Location loc1 = center.getLocation();
		Location loc2 = newCenter.getLocation();
		return loc1.getBlockX() != loc2.getBlockX() || loc1.getBlockZ() != loc2.getBlockZ();
	}
	
	public boolean blockInPlatform(Block block) {
		return blocks.contains(block);
	}
	
	public void destroyPlatform() {		
		// remove platform blocks
		if (moving) {
			for (Block block : blocks) {
				if (block.getType() == platformType) {
					block.setType(replaceType);
				}
			}
		}
		blocks = null;
	}
	
	
	public Block getCenter () {
		return this.center;
	}
	

}