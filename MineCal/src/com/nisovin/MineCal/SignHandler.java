package com.nisovin.MineCal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class SignHandler implements Listener {

	private MineCal plugin;
	
	public Hashtable<SignLocation, String[]> signs = new Hashtable<SignLocation,String[]>();
	
	public SignHandler(MineCal plugin) {
		this.plugin = plugin;
		
		loadSigns();
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public void disable() {
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onChunkLoad(final ChunkLoadEvent event) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				Chunk chunk = event.getChunk();
				for (SignLocation loc : signs.keySet()) {
					if (loc.getX() >> 4 == chunk.getX() && loc.getZ() >> 4 == chunk.getZ()) {
						updateSign(loc);
					}
				}
			}
		}, 2);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onSignChange(SignChangeEvent event) {
		if (!event.getPlayer().hasPermission("rpcal.createsign")) {
			plugin.debug("Player " + event.getPlayer().getName() + " attempted to create a calendar sign without permission");
			return;
		}
		
		boolean isCalSign = false;
		for (int i = 0; i < 4; i++) {
			if (event.getLine(i).contains("[cal]")) {
				isCalSign = true;
				break;
			}
		}
		if (isCalSign) {
			signs.put(new SignLocation(event.getBlock().getLocation()), event.getLines().clone());
			initSign(event);
			saveSigns();
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (!event.isCancelled() && (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			SignLocation loc = new SignLocation(block.getLocation());
			if (signs.containsKey(loc)) {
				signs.remove(loc);
				saveSigns();
			}
		}
	}
	
	public boolean updateSign(SignLocation loc) {
		Block block = loc.getBlock();
		Material mat = block.getType();
		if (mat == Material.SIGN_POST || mat == Material.WALL_SIGN) {
			plugin.debug("Updating sign contents at " + loc.toString());
			Sign sign = (Sign)block.getState();
			String[] lines = signs.get(loc);
			for (int i = 0; i < lines.length; i++) {
				sign.setLine(i, lines[i].replace("[cal]","").replace("%y", plugin.year).replace("%m", plugin.month).replace("%w", plugin.weekday).replace("%o", plugin.dayName).replace("%d", plugin.dayOfMonth+""));
			}
			sign.update();
			return true;
		}
		plugin.debug("Tried updating sign at " + loc.toString() + ", but sign is missing");
		return false;
	}
	
	public void updateAllSigns() {
		List<SignLocation> toRemove = new ArrayList<SignLocation>();
		for (SignLocation loc : signs.keySet()) {
			World world = loc.getWorld();
			if (world != null && world.isChunkLoaded(loc.getX() >> 4, loc.getZ() >> 4)) {
				boolean exists = updateSign(loc);
				if (!exists) {
					toRemove.add(loc);
				}
			}
		}
		if (toRemove.size() > 0) {
			for (SignLocation loc : toRemove) {
				signs.remove(loc);
			}
			saveSigns();
		}
	}
	
	private void initSign(SignChangeEvent event) {
		for (int i = 0; i < 4; i++) {
			event.setLine(i, event.getLine(i).replace("[cal]","").replace("%y", plugin.year).replace("%m", plugin.month).replace("%w", plugin.weekday).replace("%o", plugin.dayName).replace("%d", plugin.dayOfMonth+""));
		}		
	}
	
	private void loadSigns() {
		File folder = plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "signs.txt");
		signs.clear();
		
		try {
			Scanner scanner = new Scanner(file);
			if (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					String[] data = line.split("\\|",8);
					SignLocation loc = new SignLocation(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
					String[] lines = {data[4],data[5],data[6],data[7]};
					signs.put(loc, lines);
				}
				
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}		
	}
	
	private void saveSigns() {
		File file = new File(plugin.getDataFolder(), "signs.txt");
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			for (SignLocation loc : signs.keySet()) {
				String[] lines = signs.get(loc);
				writer.append(loc.getWorldName() + "|" + loc.getX() + "|" + loc.getY() + "|" + loc.getZ());
				for (int i = 0; i < lines.length; i++) {
					writer.append("|" + lines[i]);
				}
				writer.newLine();
			}
			writer.close();			
		} catch (IOException e) {
		}
		
		plugin.debug("Saved sign data");
	}
	
}
