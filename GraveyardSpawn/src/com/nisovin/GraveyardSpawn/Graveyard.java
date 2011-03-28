package com.nisovin.GraveyardSpawn;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Graveyard {
	private String name;
	private World world;
	private int x, y, z;
	
	public Graveyard(String line, Server server) {
		String [] data = line.split(",");

		String name = data[0];
		World world = server.getWorld(data[1]);
		int x = Integer.parseInt(data[2]);
		int y = Integer.parseInt(data[3]);
		int z = Integer.parseInt(data[4]);
		
		setup(name, world, x, y, z);
	}
	
	public Graveyard(String name, World world, int x, int y, int z) {
		setup(name, world, x, y, z);
	}
	
	public Graveyard(String name, Location loc) {
		setup(name, loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	private void setup(String name, World world, int x, int y, int z) {
		this.name = name;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public String getName() {
		return name;
	}
	
	public Location getLocation() {
		return new Location(world, x + 0.5, y, z + 0.5);
	}
	
	public double calculateDistanceFrom(Player player) {
		if (!player.getWorld().getName().equalsIgnoreCase(world.getName())) {
			return -1;
		}
		Location loc = player.getLocation();
		return Math.sqrt(Math.pow(x-loc.getBlockX(),2) + Math.pow(y-loc.getBlockY(),2) + Math.pow(z-loc.getBlockZ(),2));
	}
	
	public String getSaveString() {
		return name + "," + world.getName() + "," + x + "," + y + "," + z;
	}
	
}
