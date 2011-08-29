package com.nisovin.oldgods;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellListener;
import com.nisovin.magicspells.util.SpellReagents;

public class SListener extends SpellListener {

	private OldGods plugin;
	
	public SListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.High, plugin);	
	}

	@Override
	public void onSpellCast(SpellCastEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.WISDOM) {
			SpellReagents reagents = event.getReagents();
			reagents.setHealth(reagents.getHealth() / 2);
			reagents.setMana(reagents.getMana() / 2);
			HashSet<ItemStack> items = reagents.getItems();
			for (ItemStack item : items) {
				if (item.getAmount() > 1) {
					item.setAmount(item.getAmount() / 2);
				}
			}
		}
	}
	
	
	
}
