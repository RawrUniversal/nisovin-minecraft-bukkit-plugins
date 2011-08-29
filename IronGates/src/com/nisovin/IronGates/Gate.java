package com.nisovin.IronGates;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Gate {

	private String name;
	public int key;
	private boolean consume;
	private Location entrance;
	private Location exit;
	
	public Gate(String name) {
		this.name = name;
		entrance = null;
		exit = null;
		key = -1;
		consume = false;
	}
	
	public Gate(String line, Server server) {
		String [] data = line.split(":");
		String [] entranceData = data[3].split(",");
		String [] exitData = data[4].split(",");
		
		this.name = data[0];
		this.key = Integer.parseInt(data[1]);
		this.consume = data[2].equals("yes") ? true : false;
		this.entrance = new Location(server.getWorld(entranceData[0]), Integer.parseInt(entranceData[1]), Integer.parseInt(entranceData[2]), Integer.parseInt(entranceData[3]));
		this.exit = new Location(server.getWorld(exitData[0]), Integer.parseInt(exitData[1]), Integer.parseInt(exitData[2]), Integer.parseInt(exitData[3]));
		this.exit.setYaw(Float.parseFloat(exitData[4]));
	}
	
	public Gate(String name, Location entrance, Location exit) {
		this.name = name;
		this.entrance = entrance;
		this.exit = exit;
	}
	
	public void teleportPlayerToExit(Player player) {		
		Location l = new Location(exit.getWorld(), exit.getBlockX() + .5, exit.getBlockY() + .5, exit.getBlockZ() + .5);
		l.setYaw(exit.getYaw());
		player.teleport(l);
		Chunk chunk = l.getWorld().getChunkAt(l);
		chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
	}
	
	public String getName() {
		return name;
	}
	
	public Location getEntrance() {
		return entrance;
	}
	
	public Location getExit() {
		Location l = new Location(exit.getWorld(), exit.getBlockX() + .5, exit.getBlockY() + .5, exit.getBlockZ() + .5);
		l.setYaw(exit.getYaw());
		return l;
	}
	
	public String getEntranceString() {
		return entrance.getWorld().getName() + "," + entrance.getBlockX() + "," + entrance.getBlockY() + "," + entrance.getBlockZ();
	}
	
	public void setEntrance(Location loc) {
		this.entrance = loc;
	}
	
	public void setExit(Location loc) {
		this.exit = loc;
	}
	
	public void setKey(ItemStack i) {
		this.key = i.getTypeId();
	}
	
	public void setConsume(String s) {
		if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on")) {
			this.consume = true;
		} else {
			this.consume = false;
		}
	}
	
	public boolean isReadyForSave() {
		if (entrance != null && exit != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getSaveString() {
		return name + ":" + key + ":" + (consume?"yes":"no") + ":" +
			entrance.getWorld().getName() + "," + entrance.getBlockX() + "," + entrance.getBlockY() + "," + entrance.getBlockZ() + ":" +
			exit.getWorld().getName() + "," + exit.getBlockX() + "," + exit.getBlockY() + "," + exit.getBlockZ() + "," + exit.getYaw();
	}
	
}
