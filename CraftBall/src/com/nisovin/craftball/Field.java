package com.nisovin.craftball;

import java.awt.Polygon;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Field {
	protected Polygon region;
	protected int fieldY;
	protected int fieldHeight;
	protected ItemStack ballItem;
	
	protected boolean enableKick;
	protected double hKickPower;
	protected double vKickPower;
	
	protected boolean enableThrow;
	protected double throwPower;
	
	protected boolean enableDispense;
	protected double dispensePower;
	
	protected boolean enableBat;
	protected ItemStack batItem;
	protected int batDelay;
	protected double batPower;
	protected HashMap<String,Long> batters;
	
	protected boolean fire;
	protected int pickupDelay;
	
	public Field() {
		region = new Polygon();
		batters = new HashMap<String,Long>();
	}
	
	public boolean inField(Item item) {
		return inField(item.getLocation(), item.getItemStack());
	}
	
	public boolean inField(Player player) {
		Location loc = player.getLocation();
		loc.setY(player.getLocation().getY()+1);
		return inField(loc, null);
	}
	
	public boolean inField(Location location, ItemStack item) {
		if ((item == null || (item.getTypeId() == ballItem.getTypeId() && item.getDurability() == ballItem.getDurability())) && 
				region.contains(location.getBlockX(), location.getBlockZ()) && 
				fieldY + 1 < location.getY() && location.getY() < fieldY + fieldHeight) {
			return true;
		} else {
			return false;
		}		
	}
	
	
}
