package com.nisovin.magicspells;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.nisovin.magicspells.util.MagicListener;

public class MagicBlockListener extends BlockListener implements MagicListener {
	
	private boolean disabled = false;
	
	public MagicBlockListener(MagicSpells plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Normal, plugin);
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.BLOCK_BREAK);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onBlockBreak(event);
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.BLOCK_PLACE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onBlockPlace(event);
			}
		}
	}

	@Override
	public void disable() {
		disabled = true;
	}	
	
}