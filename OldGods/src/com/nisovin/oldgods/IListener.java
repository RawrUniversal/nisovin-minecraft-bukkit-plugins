package com.nisovin.oldgods;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryListener;
import org.bukkit.plugin.PluginManager;

import com.nisovin.oldgods.godhandlers.CookingHandler;

public class IListener extends InventoryListener {

	private OldGods plugin;
	
	public IListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.FURNACE_BURN, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.FURNACE_SMELT, this, Event.Priority.Normal, plugin);
	}

	@Override
	public void onFurnaceBurn(FurnaceBurnEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.COOKING) {
			CookingHandler.onFurnaceBurn(event);
		}
	}

	@Override
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.COOKING) {
			CookingHandler.onFurnaceSmelt(event);
		}
	}
	
	
	
}
