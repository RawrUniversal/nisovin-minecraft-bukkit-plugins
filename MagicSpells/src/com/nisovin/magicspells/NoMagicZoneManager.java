package com.nisovin.magicspells;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.util.MagicConfig;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class NoMagicZoneManager {
	
	private HashSet<NoMagicZone> zones;

	public NoMagicZoneManager(MagicConfig config) {
		zones = new HashSet<NoMagicZone>();
		
		WorldGuardPlugin worldGuard = null;
		if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
			worldGuard = (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		}
		
		List<String> zoneList = config.getStringList("no-magic-zones", null);
		if (zoneList != null) {
			for (String s : zoneList) {
				String[] data = s.split(":");
				String worldName = data[1];
				if (data[0].equalsIgnoreCase("worldguard") && worldGuard != null) {
					World w = Bukkit.getServer().getWorld(worldName);
					if (w != null) {
						RegionManager rm = worldGuard.getRegionManager(w);
						if (rm != null) {
							ProtectedRegion region = rm.getRegion(data[2]);
							if (region != null) {
								zones.add(new NoMagicZone(worldName, region));
							} else {
								Bukkit.getServer().getLogger().severe("MagicSpells: Invalid no-magic zone WorldGuard region: " + data[2]);
							}
						} else {
							Bukkit.getServer().getLogger().severe("MagicSpells: Failed to get WorldGuard region manager for world: " + data[1]);
						}
					} else {
						Bukkit.getServer().getLogger().severe("MagicSpells: Invalid no-magic zone world: " + data[1]);						
					}
				} else if (data[0].equalsIgnoreCase("cuboid")) {
					try {
						String[] p1 = data[2].split(",");
						String[] p2 = data[3].split(",");
						Vector point1 = new Vector(Integer.parseInt(p1[0]), Integer.parseInt(p1[1]), Integer.parseInt(p1[2]));
						Vector point2 = new Vector(Integer.parseInt(p2[0]), Integer.parseInt(p2[1]), Integer.parseInt(p2[2]));
						zones.add(new NoMagicZone(worldName, point1, point2));
					} catch (NumberFormatException e) {
						Bukkit.getServer().getLogger().severe("MagicSpells: Invalid no-magic zone defined cuboid: " + s);							
					} catch (ArrayIndexOutOfBoundsException e) {
						Bukkit.getServer().getLogger().severe("MagicSpells: Invalid no-magic zone defined cuboid: " + s);							
					}
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
