package com.nisovin.craftball;

import java.awt.Polygon;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Field {
	protected Polygon region;
	protected int fieldY;
	protected ItemStack ballItem;
	protected boolean enableKick;
	protected boolean enableThrow;
	protected double hKickPower;
	protected double vKickPower;
	protected double throwPower;
	protected boolean fire;
	protected int pickupDelay;
	
	public Field() {
		region = new Polygon();
	}
	
	public boolean inField(Item item) {
		if (item.getItemStack().getTypeId() == ballItem.getTypeId() && item.getItemStack().getDurability() == ballItem.getDurability() && 
				region.contains(item.getLocation().getBlockX(), item.getLocation().getBlockZ()) && 
				fieldY + 1 < item.getLocation().getY() && item.getLocation().getY() < fieldY + 4) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
