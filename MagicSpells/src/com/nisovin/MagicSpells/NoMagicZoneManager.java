package com.nisovin.MagicSpells;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class NoMagicZoneManager {
	
	private HashSet<NoMagicZone> zones;

	public NoMagicZoneManager(Configuration config) {
		zones = new HashSet<NoMagicZone>();
		
		WorldGuardPlugin worldGuard = null;
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
			worldGuard = (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		}
		
		List<String> zoneList = config.getStringList("no-magic-zones", null);
		if (zoneList != null) {
			for (String s : zoneList) {
				String[] data = s.split(":");
				if (data[0].equalsIgnoreCase("worldguard") && worldGuard != null) {
					World w = Bukkit.getServer().getWorld(data[1]);
					zones.add(new NoMagicZone(worldGuard.getRegionManager(w).getRegion(data[2])));
				} else if (data[0].equalsIgnoreCase("cuboid")) {
					String[] p1 = data[1].split(",");
					String[] p2 = data[2].split(",");
					Vector point1 = new Vector(Integer.parseInt(p1[0]), Integer.parseInt(p1[1]), Integer.parseInt(p1[2]));
					Vector point2 = new Vector(Integer.parseInt(p2[0]), Integer.parseInt(p2[1]), Integer.parseInt(p2[2]));
					zones.add(new NoMagicZone(point1, point2));
				}
			}
		}
	}
	
	public boolean inNoMagicZone(Player player) {
		return inNoMagicZone(player.getLocation());
	}
	
	public boolean inNoMagicZone(Location location) {
		for (NoMagicZone zone : zones) {
			if (zone.inZone(location)) {
				return true;
			}
		}
		return false;
	}
	
	public int zoneCount() {
		return zones.size();
	}
	
}
