package com.nisovin.MineCal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class SignLocation {

	private String worldName;
	private int x;
	private int y;
	private int z;
	
	public SignLocation(Location location) {
		worldName = location.getWorld().getName();
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
	}
	
	public SignLocation(String worldName, int x, int y, int z) {
		this.worldName = worldName;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public Location getLocation() {
		return new Location(getWorld(), x, y, z);
	}
	
	public Block getBlock() {
		return getWorld().getBlockAt(x, y, z);
	}
	
	@Override
	public int hashCode() {
		return (worldName + "," + x + "," + y + "," + z).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SignLocation) {
			SignLocation loc = (SignLocation)o;
			return loc.worldName.equals(worldName) && loc.x == x && loc.y == y && loc.z == z;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return worldName + ":" + x + "," + y + "," + z;
	}
	
}
