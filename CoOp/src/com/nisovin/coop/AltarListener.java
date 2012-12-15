package com.nisovin.coop;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class AltarListener implements Listener {

	BlockFace[] directions = new BlockFace[] { BlockFace.SELF, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };
	Material baseType = Material.DIAMOND_BLOCK;
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
			if (type == Material.GRASS || type == Material.SAND || type == Material.STONE || type == Material.GRAVEL || type == Material.MYCEL || type == Material.DIRT || type == Material.LEAVES || type == Material.LOG) {
				b = b.getRelative(BlockFace.UP);
				for (BlockFace dir : directions) {
					b.getRelative(dir).setType(baseType);
				}
				b = b.getRelative(BlockFace.UP);
				b.setType(Material.BEACON);
				beacons.put(chunk, b);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onChunkUnload(ChunkUnloadEvent event) {
		if (beacons.containsKey(event.getChunk())) {
			Block b = beacons.remove(event.getChunk());
			removeBeacon(b);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.hasBlock() && event.getClickedBlock().getType() == Material.BEACON) {
			if (beacons.containsValue(event.getClickedBlock())) {
				// get ender world
				World endWorld = null;
				for (World w : Bukkit.getWorlds()) {
					if (w.getEnvironment() == Environment.THE_END) {
						endWorld = w;
						break;
					}
				}
				if (endWorld == null) return;
				
				// check if ender world is in use
				if (endWorld.getPlayers().size() > 0) {
					event.getPlayer().sendMessage(CoopPlugin.chatColor + "There is already a party in a fight. Please wait until they are done.");
					return;
				}
				
				// remove beacon
				removeBeacon(event.getClickedBlock());
				
				// spawn beacon in the end
				Block b = endWorld.getHighestBlockAt(endWorld.getSpawnLocation());
				for (BlockFace dir : directions) {
					b.getRelative(dir).setType(baseType);
				}
				b = b.getRelative(BlockFace.UP);
				b.setType(Material.BEACON);
				
				// teleport party to end				
				Party party = Party.getParty(event.getPlayer());
				if (party == null) {
					event.getPlayer().teleport(endWorld.getHighestBlockAt(endWorld.getSpawnLocation()).getLocation().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5));
				} else {
					Location loc = endWorld.getSpawnLocation();
					for (Player p : party.getMembers()) {
						Location l = loc.clone().add(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
						p.teleport(endWorld.getHighestBlockAt(l).getLocation());
					}
				}
			} else if (event.getClickedBlock().getWorld().getEnvironment() == Environment.THE_END) {
				// remove beacon
				removeBeacon(event.getClickedBlock());				
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().isOp()) return;
		Block b = event.getBlock();
		if (b.getWorld().getEnvironment() == Environment.THE_END && b.getType() != Material.ENDER_STONE) {
			event.setCancelled(true);
			return;
		}
		if (b.getType() == Material.BEACON) {
			if (beacons.containsValue(b)) {
				event.setCancelled(true);
			}
		} else if (b.getType() == baseType) {
			b = b.getRelative(BlockFace.UP);
			for (BlockFace dir : directions) {
				Block temp = b.getRelative(dir);
				if (temp.getType() == Material.BEACON && beacons.containsValue(temp)) {
					event.setCancelled(true);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer().isOp()) return;
		Block b = event.getBlock();
		if (b.getWorld().getEnvironment() == Environment.THE_END && b.getType() != Material.ENDER_STONE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() == SpawnReason.NATURAL && event.getLocation().getWorld().getEnvironment() == Environment.THE_END) {
			event.setCancelled(true);
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
