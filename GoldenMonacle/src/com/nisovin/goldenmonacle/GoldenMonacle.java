package com.nisovin.goldenmonacle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class GoldenMonacle extends JavaPlugin implements Listener {

	// start and end time
	// control spawn points
	// give out items
	// top 10 scoreboard
	// prevent drops, block break, block place
	// special drop spawn points
	
	List<Location> spawnPoints;
	
	@Override
	public void onEnable() {

		World world = Bukkit.getWorlds().get(0);
		
		// load spawn points
		spawnPoints = new ArrayList<Location>();
		try {
			File spawnFile = new File(getDataFolder(), "spawnpoints.txt");
			if (spawnFile.exists()) {
				Scanner scanner = new Scanner(spawnFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine().trim();
					if (!line.isEmpty() && !line.startsWith("#")) {
						String[] data = line.split(",");
						Location location = new Location(world, Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Float.parseFloat(data[3]), 0F);
						spawnPoints.add(location);
					}
				}
			}
		} catch (Exception e) {
			getLogger().severe("ERROR READING SPAWN POINTS FILE");
			e.printStackTrace();
		}
	}
	
	private void saveSpawnPoints(CommandSender sender) {
		try {
			File file = new File(getDataFolder(), "spawnpoints.txt");
			if (file.exists()) file.delete();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Location loc : spawnPoints) {
				writer.write(loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "ERROR SAVING SPAWN POINTS FILE");
			e.printStackTrace();
		}
	}
	
}
