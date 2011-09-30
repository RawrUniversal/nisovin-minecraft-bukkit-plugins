package com.nisovin.basicwarp;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Warp {

	private Location location;
	private String world;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;
	
	public Warp(Location location) {
		this.location = location.clone();
		this.world = location.getWorld().getName();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
		this.yaw = location.getYaw();
		this.pitch = location.getPitch();
	}
	
	public Warp(String location) {
		String[] data = location.split(",");
		this.location = null;
		this.world = data[0];
		this.x = Double.parseDouble(data[1]);
		this.y = Double.parseDouble(data[2]);
		this.z = Double.parseDouble(data[3]);
		this.yaw = Float.parseFloat(data[4]);
		this.pitch = Float.parseFloat(data[5]);
	}
	
	public boolean teleport(Player player) {
		if (location == null) {
			World world = Bukkit.getServer().getWorld(this.world);
			if (world == null) {
				return false;
			}
			location = new Location(world, x, y, z, yaw, pitch);
		}
		player.teleport(location);
		Chunk chunk = location.getBlock().getChunk();
		location.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
		return true;
	}
	
	public String getSaveString() {
		return world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
	}
	
}
