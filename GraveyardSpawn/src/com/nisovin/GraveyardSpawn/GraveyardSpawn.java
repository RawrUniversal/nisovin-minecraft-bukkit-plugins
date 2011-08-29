package com.nisovin.GraveyardSpawn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;

public class GraveyardSpawn extends JavaPlugin {

	private final String GRAVEYARD_FILE_PATH = "graveyards.txt";
	
	public boolean SIGN_DROP = true;
	public int SIGN_REMOVE_DELAY = 300;
	
	public List<Graveyard> graveyards;
	protected HashMap<String,Location> deathLocation = new HashMap<String,Location>();

	@Override
	public void onEnable() {				
		loadGraveyards();		
		GYPlayerListener playerListener = new GYPlayerListener(this);		
		this.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, new GYEntityListener(this), Priority.Monitor, this);
		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Normal, this);
		this.getServer().getLogger().info("GraveyardSpawn plugin loaded!");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			String comm = command.getName();
			
			if (p.isOp() && comm.equalsIgnoreCase("gy")) {
				if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
					Graveyard gy = new Graveyard(args[1], p.getLocation());
					graveyards.add(gy);
					saveGraveyard(gy);
					p.sendMessage("Graveyard added: " + gy.getSaveString());
				} else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
					boolean success = false;
					for (Graveyard gy : graveyards) {
						if (gy.getName().equalsIgnoreCase(args[1])) {
							graveyards.remove(gy);
							saveAllGraveyards();
							p.sendMessage("Graveyard removed: " + gy.getSaveString());
							success = true;
							break;
						}
					}
					if (!success) {
						p.sendMessage("No such graveyard.");
					}
				} else if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
					boolean success = false;
					for (Graveyard gy : graveyards) {
						if (gy.getName().equalsIgnoreCase(args[1])) {
							p.teleport(gy.getLocation());
							Chunk chunk = gy.getLocation().getWorld().getChunkAt(gy.getLocation());
							chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
							p.sendMessage("You have teleported to graveyard '" + gy.getName() + "'.");
							success = true;
							break;
						}
					}
					if (!success) {
						p.sendMessage("No such graveyard.");
					}
				} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
					String list = "";
					for (Graveyard gy : graveyards) {
						if (list.equals("")) {
							list += gy.getName();
						} else {
							list += ", " + gy.getName();
						}
					}
					p.sendMessage("Graveyards: " + list);
				} else {
					p.sendMessage("Usage of /gy :");
					p.sendMessage("  /gy add <name> -- Adds a new graveyard at your location");
					p.sendMessage("  /gy remove <name> -- Removes the named graveyard");
					p.sendMessage("  /gy list -- Lists all graveyards");
					p.sendMessage("  /gy tp <name> -- Teleports to the named graveyard");
				}
				
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public void loadGraveyards() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		graveyards = new ArrayList<Graveyard>();
		
		File file = new File(folder, GRAVEYARD_FILE_PATH);
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				graveyards.add(new Graveyard(line, getServer()));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			
		}
	}
	
	public void saveGraveyard(Graveyard gy) {
		File file = new File(getDataFolder(), GRAVEYARD_FILE_PATH);
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			
			writer.append(gy.getSaveString());
			writer.newLine();
			
			writer.close();
			
		} catch (IOException e) {
		}
	}
	
	public void saveAllGraveyards() {
		File file = new File(getDataFolder(), GRAVEYARD_FILE_PATH);
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			
			for (Graveyard gy : graveyards) {
				writer.append(gy.getSaveString());
				writer.newLine();				
			}
			
			writer.close();
			
		} catch (IOException e) {
		}		
	}
	
	@Override
	public void onDisable() {
		
	}

}
