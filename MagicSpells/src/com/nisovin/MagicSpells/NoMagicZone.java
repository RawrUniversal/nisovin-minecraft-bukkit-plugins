package com.nisovin.MagicSpells;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class NoMagicZone {

	private ProtectedRegion region = null;
	private Vector point1 = null;
	private Vector point2 = null;
	
	public NoMagicZone(ProtectedRegion region) {
		this.region = region;
	}
	
	public NoMagicZone(Vector v1, Vector v2) {
		int minx, miny, minz, maxx, maxy, maxz;
		if (v1.getX() < v2.getX()) {
			minx = v1.getBlockX();
			maxx = v2.getBlockX();
		} else {
			minx = v2.getBlockX();
			maxx = v1.getBlockX();
		}
		if (v1.getY() < v2.getY()) {
			miny = v1.getBlockY();
			maxy = v2.getBlockY();
		} else {
			miny = v2.getBlockY();
			maxy = v1.getBlockY();
		}
		if (v1.getZ() < v2.getZ()) {
			minz = v1.getBlockZ();
			maxz = v2.getBlockZ();
		} else {
			minz = v2.getBlockZ();
			maxz = v1.getBlockZ();
		}
		point1 = new Vector(minx, miny, minz);
		point2 = new Vector(maxx, maxy, maxz);
	}
	
	public boolean inZone(Player player) {
		return inZone(player.getLocation());
	}
	
	public boolean inZone(Location location) {
		if (region != null) {
			com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ());
			return region.contains(v);
		} else {
			int x = location.getBlockX();
			int y = location.getBlockY();
			int z = location.getBlockZ();
			if (point1.getBlockX() <= x && x <= point2.getBlockX() &&
					point1.getBlockY() <= y && y <= point2.getBlockY() &&
					point1.getBlockZ() <= z && z <= point2.getBlockZ()) {
				return true;
			} else {
				return false;
			}
		}
	}
	
}
