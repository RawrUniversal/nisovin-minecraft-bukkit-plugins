package com.nisovin.craftball;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.util.Vector;

public class BallBlockListener extends BlockListener {

	CraftBall plugin;
	
	public BallBlockListener(CraftBall plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_DISPENSE, this, Event.Priority.Normal, plugin);
	}
	
	@Override
	public void onBlockDispense(BlockDispenseEvent event) {
		for (Field field : plugin.fields) {
			if (field.enableDispense && field.inField(event.getBlock().getLocation(), event.getItem())) {
				Vector v = new Vector(0,.2,-1).multiply(field.dispensePower); //event.getVelocity().normalize().multiply(field.dispensePower);
				event.setVelocity(v);
				return;
			}
		}
	}
	
}
