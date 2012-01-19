package com.nisovin.worldloader;

import java.io.File;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class WorldBase {

	private String name;
	private String folder;
	private String description;
	private int minPlayers;
	private int maxPlayers;
	private long seed;
	private String start;
	private boolean monsters;
	private List<Integer> breakable;
	private List<Integer> placeable;
	
	public WorldBase(String name, ConfigurationSection config) {
		this.name = name;
		this.folder = config.getString("folder");
		this.description = config.getString("description");
		this.minPlayers = config.getInt("min-players", 0);
		this.maxPlayers = config.getInt("max-players", 100);
		this.seed = config.getLong("seed");
		this.start = config.getString("start","");
		this.monsters = config.getBoolean("monsters", true);
		this.breakable = config.getIntegerList("breakable");
		this.placeable = config.getIntegerList("placeable");
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean canPartyJoin(Party party) {
		int size = party.size();
		return (minPlayers < size && size < maxPlayers);
	}
	
	public long getSeed() {
		return seed;
	}
	
	public boolean monstersEnabled() {
		return monsters;
	}
	
	public boolean canBreak(Block block) {
		if (breakable == null) {
			return true;
		} else {
			return breakable.contains(block.getTypeId());
		}
	}
	
	public boolean canPlace(Block block) {
		if (placeable == null) {
			return true;
		} else {
			return placeable.contains(block.getTypeId());
		}
	}
	
	public File getWorldFolder() {
		return new File(WorldLoader.plugin.getDataFolder(), "worlds" + File.separator + this.folder);
	}
	
	public String getStartLocationString() {
		return this.start;
	}
	
}
