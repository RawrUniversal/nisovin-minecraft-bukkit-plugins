package com.nisovin.MineCal;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;

public class CalWorldListener extends WorldListener {

	private MineCal cal;
	
	public CalWorldListener(MineCal cal) {
		this.cal = cal;
		
		cal.getServer().getPluginManager().registerEvent(Event.Type.CHUNK_LOAD, this, Event.Priority.Monitor, cal);
	}
	
	@Override
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Location loc : cal.signs.keySet()) {
			if (loc.getBlockX() >> 4 == event.getChunk().getX() && loc.getBlockZ() >> 4 == event.getChunk().getZ()) {
				cal.updateSign(loc);
			}
		}
	}
	
}
