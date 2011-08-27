package com.nisovin.magicspells;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class MagicBlockListener extends BlockListener {
	
	public MagicBlockListener(MagicSpells plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Normal, plugin);
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.BLOCK_BREAK);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onBlockBreak(event);
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.BLOCK_PLACE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onBlockPlace(event);
			}
		}
	}	
	
}