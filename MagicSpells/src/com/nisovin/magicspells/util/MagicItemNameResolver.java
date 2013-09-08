package com.nisovin.magicspells.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

public class MagicItemNameResolver implements ItemNameResolver {

	Map<String, Material> nameMap = new HashMap<String, Material>();
	Random rand = new Random();
	
	public MagicItemNameResolver() {
		for (Material mat : Material.values()) {
			nameMap.put(mat.name().toLowerCase(), mat);
			nameMap.put(mat.name().toLowerCase().replace("_", ""), mat);
		}
	}
	
	@Override
	public ItemTypeAndData resolve(String string) {
		if (string == null || string.isEmpty()) return null;
		ItemTypeAndData item = new ItemTypeAndData();
		if (string.contains(":")) {
			String[] split = string.split(":");
			if (split[0].matches("[0-9]+")) {
				item.id = Integer.parseInt(split[0]);
			} else {
				Material mat = Material.getMaterial(split[0].toUpperCase());
				if (mat == null) return null;
				item.id = mat.getId();
			}
			if (split[1].matches("[0-9]+")) {
				item.data = Short.parseShort(split[1]);
			} else {
				return null;
			}
		} else {
			if (string.matches("[0-9]+")) {
				item.id = Integer.parseInt(string);
			} else {
				Material mat = Material.getMaterial(string.toUpperCase());
				if (mat == null) return null;
				item.id = mat.getId();
			}
		}
		return item;
	}
	
	@Override
	public MagicMaterial resolve2(String string) {
		if (string == null || string.isEmpty()) return null;
		
		String stype;
		String sdata;
		if (string.contains(":")) {
			String[] split = string.split(":", 2);
			stype = split[0].toLowerCase();
			sdata = split[1];
		} else {
			stype = string.toLowerCase();
			sdata = "";
		}
		
		Material type = nameMap.get(stype);
		if (type == null) {
			return resolveUnknown(stype, sdata);
		}
		
		if (type.isBlock()) {
			MaterialData data = null;
			if (type == Material.LOG) {
				data = getTree(sdata);
			}
			
			if (data != null) {
				return new MagicBlockMaterial(data);
			} else if (sdata.matches("^[0-9]+$")) {
				short durability = 0;
				try {
					durability = Short.parseShort(sdata);
				} catch (NumberFormatException e) {}
				return new MagicItemMaterial(type, durability);
			} else {
				return new MagicBlockMaterial(new MaterialData(type));
			}
		} else {
			short durability = 0;
			try {
				durability = Short.parseShort(sdata);
			} catch (NumberFormatException e) {}
			return new MagicItemMaterial(type, durability);
		}
	}
	
	private MagicMaterial resolveUnknown(String stype, String sdata) {
		return null;
	}
	
	
	
	private Tree getTree(String data) {
		TreeSpecies species = TreeSpecies.GENERIC;
		BlockFace dir = BlockFace.UP;
		if (data != null && data.length() > 0) {
			String[] split = data.split(":");
			if (split.length >= 1) {
				if (split[0].equalsIgnoreCase("birch")) {
					species = TreeSpecies.BIRCH;
				} else if (split[0].equalsIgnoreCase("jungle")) {
					species = TreeSpecies.JUNGLE;
				} else if (split[0].equalsIgnoreCase("redwood")) {
					species = TreeSpecies.REDWOOD;
				} else if (split[0].equalsIgnoreCase("random")) {
					species = TreeSpecies.values()[rand.nextInt(TreeSpecies.values().length)];
				}
			}
			if (split.length >= 2) {
				if (split[1].equalsIgnoreCase("east")) {
					dir = BlockFace.EAST;
				} else if (split[1].equalsIgnoreCase("west")) {
					dir = BlockFace.WEST;
				} else if (split[1].equalsIgnoreCase("north")) {
					dir = BlockFace.NORTH;
				} else if (split[1].equalsIgnoreCase("south")) {
					dir = BlockFace.SOUTH;
				} else if (split[1].equalsIgnoreCase("random")) {
					int r = rand.nextInt(3);
					if (r == 0) {
						dir = BlockFace.EAST;
					} else if (r == 1) {
						dir = BlockFace.NORTH;
					}
				}
			}
		}
		return new Tree(species, dir);
	}

}
