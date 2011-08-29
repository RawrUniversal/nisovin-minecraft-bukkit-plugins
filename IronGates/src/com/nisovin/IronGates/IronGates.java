package com.nisovin.IronGates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;


public class IronGates extends JavaPlugin {

	private final String GATES_FILE_NAME = "gates.txt";
	
	public HashMap<String, Gate> gates;
	public HashMap<String, Gate> newGates = new HashMap<String, Gate>();
	public HashMap<String, Long> immunity = new HashMap<String, Long>();
	
	@Override
	public void onEnable() {		
		loadGates();

		IGPlayerListener playerListener = new IGPlayerListener(this);
		IGEntityListener entityListener = new IGEntityListener(this);
		IGBlockListener blockListener = new IGBlockListener(this);

		//this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (command.getName().equalsIgnoreCase("gate")) {
				if (p.hasPermission("irongates.new") && args.length == 2 && args[0].equalsIgnoreCase("new")) {
					newGates.put(p.getName(), new Gate(args[1]));
					p.sendMessage("New gate '" + args[1] + "' created. Set entrance and exit now.");
				} else if (p.hasPermission("irongates.new") && args.length == 1 && args[0].equalsIgnoreCase("entrance")) {
					Gate gate = newGates.get(p.getName());
					if (gate != null) {
						gate.setEntrance(p.getLocation());
						p.sendMessage("Entrance for gate '" + gate.getName() + "' set.");
						if (gate.isReadyForSave()) {
							p.sendMessage("Use /gate save to save the gate.");
						}
					} else {
						p.sendMessage("Use /gate new <name> first.");
					}
				} else if (p.hasPermission("irongates.new") && args.length == 1 && args[0].equalsIgnoreCase("exit")) {
					Gate gate = newGates.get(p.getName());
					if (gate != null) {
						gate.setExit(p.getLocation());
						p.sendMessage("Exit for gate '" + gate.getName() + "' set.");
						if (gate.isReadyForSave()) {
							p.sendMessage("Use /gate save to save the gate.");
						}
					} else {
						p.sendMessage("Use /gate new <name> first.");
					}
				} else if (p.hasPermission("irongates.new") && args.length == 1 && args[0].equalsIgnoreCase("key")) {
					Gate gate = newGates.get(p.getName());
					if (gate != null) {
						gate.setKey(p.getItemInHand());
						p.sendMessage("Key for gate '" + gate.getName() + "' set to ID# " + p.getItemInHand().getTypeId() + ".");
					} else {
						p.sendMessage("Use /gate new <name> first.");
					}
				} else if (p.hasPermission("irongates.new") && args.length == 1 && args[0].equalsIgnoreCase("save")) {
					Gate gate = newGates.get(p.getName());
					if (gate != null) {
						if (gate.isReadyForSave()) {
							gates.put(gate.getEntranceString(), gate);
							saveGate(gate);
							newGates.remove(p.getName());
							p.sendMessage("Gate '" + gate.getName() + "' saved.");
						} else {
							p.sendMessage("Define entrance and exit before saving.");
						}
					} else {
						p.sendMessage("No gate to save.");
					}
				} else if (p.hasPermission("irongates.list") && args.length == 1 && args[0].equalsIgnoreCase("list")) {
					String list = "";
					for (Gate gate : gates.values()) {
						if (list.equals("")) {
							list = gate.getName();
						} else {
							list += ", " + gate.getName();
						}
					}
					p.sendMessage("Gates: " + list);
				} else if (p.hasPermission("irongates.tp") && args.length == 2 && args[0].equalsIgnoreCase("tp")) {
					for (Gate gate : gates.values()) {
						if (gate.getName().equalsIgnoreCase(args[1])) {
							gate.teleportPlayerToExit(p);
							p.sendMessage("You have teleported to the exit of gate '" + gate.getName() + "'.");
							break;
						}
					}
				} else if (p.hasPermission("irongates.new") || p.hasPermission("irongates.list") || p.hasPermission("irongates.tp")) {
					p.sendMessage("Usage of /gate :");
					if (p.hasPermission("irongates.new")) p.sendMessage("  /gate new <name> -- Start creating a new gate");
					if (p.hasPermission("irongates.new")) p.sendMessage("  /gate entrance -- Set your gate entrance to your location");
					if (p.hasPermission("irongates.new")) p.sendMessage("  /gate exit -- Set your gate exit to your location");
					if (p.hasPermission("irongates.new")) p.sendMessage("  /gate key -- Set your gate key to your in-hand item");
					if (p.hasPermission("irongates.new")) p.sendMessage("  /gate save -- Save your gate");
					if (p.hasPermission("irongates.list")) p.sendMessage("  /gate list -- List all gates");
					if (p.hasPermission("irongates.tp")) p.sendMessage("  /gate tp <name> -- Teleport to the exit of the named gate");
					
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}
	
	public void loadGates() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		gates = new HashMap<String,Gate>();
		
		File file = new File(folder, GATES_FILE_NAME);
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				Gate gate = new Gate(line, getServer());
				gates.put(gate.getEntranceString(), gate);
			}
			scanner.close();
		} catch (FileNotFoundException e) {	
		}
	}
	
	public void saveGate(Gate gate) {
		File file = new File(getDataFolder(), GATES_FILE_NAME);
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			
			writer.append(gate.getSaveString());
			writer.newLine();
			
			writer.close();
			
		} catch (IOException e) {
		}
	}
	
	public void saveAllGates() {
		File file = new File(getDataFolder(), GATES_FILE_NAME);
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			
			for (Gate gate : gates.values()) {
				writer.append(gate.getSaveString());
				writer.newLine();
			}
			
			writer.close();
			
		} catch (IOException e) {
		}		
	}

}
