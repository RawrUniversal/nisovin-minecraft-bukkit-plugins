package com.nisovin.coop;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class AltarListener implements Listener {

	BlockFace[] directions = new BlockFace[] { BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };
	Random random = new Random();
	
	Map<Chunk, Block> beacons = new HashMap<Chunk, Block>();
	
	public AltarListener(CoopPlugin plugin) {
		
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if (random.nextInt(100) == 0) {
			Chunk chunk = event.getChunk();
			int x = random.nextInt(10) + 3;
			int z = random.nextInt(10) + 3;
			Block b = chunk.getBlock(x, 1, z);
			b = b.getWorld().getHighestBlockAt(b.getLocation()).getRelative(BlockFace.DOWN);
			Material type = b.getType();
			System.out.println("checking type: " + type);
			if (type == Material.GRASS || type == Material.SAND || type == Material.STONE || type == Material.GRAVEL || type == Material.MYCEL || type == Material.DIRT || type == Material.LEAVES || type == Material.LOG) {
				b = b.getRelative(BlockFace.UP);
				for (BlockFace dir : directions) {
					b.getRelative(dir).setType(Material.DIAMOND_BLOCK);
				}
				b = b.getRelative(BlockFace.UP);
				b.setType(Material.BEACON);
				beacons.put(chunk, b);
				System.out.println("beacon spawned " + b.getX() + "," + b.getZ());
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (beacons.containsKey(event.getChunk())) {
			Block b = beacons.remove(event.getChunk());
			removeBeacon(b);
			System.out.println("Beacon removed " + b.getX() + "," + b.getZ());
		}
	}
	
	public void removeAllBeacons() {
		for (Block b : beacons.values()) {
			removeBeacon(b);
		}
		beacons.clear();
	}
	
	private void removeBeacon(Block b) {
		b.setType(Material.AIR);
		b = b.getRelative(BlockFace.DOWN);
		for (BlockFace dir : directions) {
			b.getRelative(dir).setType(Material.AIR);
		}
	}
	
}
