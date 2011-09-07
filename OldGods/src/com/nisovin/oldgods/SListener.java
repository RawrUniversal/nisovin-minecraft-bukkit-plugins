package com.nisovin.oldgods;


import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellListener;
import com.nisovin.oldgods.godhandlers.WisdomHandler;

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
			WisdomHandler.onSpellCast(event);
		}
	}
	
}
