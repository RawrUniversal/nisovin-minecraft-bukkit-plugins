package com.nisovin.mobbehaviors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class WorldBehaviors {

	private BehaviorSet defaultBehaviorSet = null;
	private LinkedHashMap<Region,BehaviorSet> regions = new LinkedHashMap<WorldBehaviors.Region, BehaviorSet>();
	
	public WorldBehaviors(ConfigurationSection config, MobBehaviors plugin) {
		if (config.contains("DefaultBehaviorSet")) {
			String setName = config.getString("DefaultBehaviorSet", null);
			if (setName != null && !setName.isEmpty()) {
				defaultBehaviorSet = plugin.behaviorSets.get(setName);
			}
		}
		
		Set<String> keys = config.getKeys(false);
		if (keys != null && keys.size() > 0) {
			for (String key : keys) {
				if (!key.equals("DefaultBehaviorSet")) {
					ConfigurationSection sec = config.getConfigurationSection(key);
					String pt1 = sec.getString("Point1");
					String pt2 = sec.getString("Point2");
					String setName = sec.getString("BehaviorSet");
					
					Region region = createRegion(pt1, pt2);
					BehaviorSet set = plugin.behaviorSets.get(setName);
					
					if (region != null && set != null) {
						regions.put(region, set);
					} else {
						plugin.getLogger().severe("Error with region '" + key + "' in world '" + config.getName() + "'");
					}
				}
			}
		}
	}
	
	public List<BehaviorSet> getApplicableBehaviorSets(Location location) {
		ArrayList<BehaviorSet> behaviorSets = new ArrayList<BehaviorSet>();
		for (Region region : regions.keySet()) {
			if (region.contains(location)) {
				behaviorSets.add(regions.get(region));
			}
		}
		if (defaultBehaviorSet != null) {
			behaviorSets.add(defaultBehaviorSet);
		}
		return behaviorSets;
	}
	
	private Region createRegion(String point1, String point2) {
		double x1, x2, y1, y2, z1, z2;
		
		try {
			String[] pt1 = point1.split(",");
			x1 = Double.parseDouble(pt1[0]);
			y1 = Double.parseDouble(pt1[1]);
			z1 = Double.parseDouble(pt1[2]);
			
			String[] pt2 = point2.split(",");
			x2 = Double.parseDouble(pt2[0]);
			y2 = Double.parseDouble(pt2[1]);
			z2 = Double.parseDouble(pt2[2]);
		} catch (NumberFormatException e) {
			return null;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}		
		
		double temp;		
		if (x1 > x2) {
			temp = x1;
			x1 = x2;
			x2 = temp;
		}
		if (y1 > y2) {
			temp = y1;
			y1 = y2;
			y2 = temp;
		}
		if (z1 > z2) {
			temp = z1;
			z1 = z2;
			z2 = temp;
		}
		
		return new Region(x1, x2, y1, y2, z1, z2);
	}
	
	private class Region {
		double x1, x2, y1, y2, z1, z2;
		
		public Region(double x1, double x2, double y1, double y2, double z1, double z2) {
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.z1 = z1;
			this.z2 = z2;
		}
		
		public boolean contains(Location location) {
			double x = location.getX();
			double y = location.getY();
			double z = location.getZ();
			return ((x1 <= x && x <= x2) && (y1 <= y && y <= y2) && (z1 <= z && z <= z2));
		}
	}
	
}
