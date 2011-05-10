package com.nisovin.MineCal;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class CalBlockListener extends BlockListener {

	MineCal cal;
	
	public CalBlockListener(MineCal cal) {
		this.cal = cal;
		
		cal.getServer().getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, this, Event.Priority.Monitor, cal);
		cal.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Monitor, cal);
	}
	
	@Override
	public void onSignChange(SignChangeEvent event) {
		if (!event.isCancelled()) {
			boolean isCalSign = false;
			for (int i = 0; i < 4; i++) {
				if (event.getLine(i).contains("[cal]")) {
					isCalSign = true;
					break;
				}
			}
			if (isCalSign) {
				cal.signs.put(event.getBlock().getLocation(), event.getLines().clone());
				cal.initSign(event);
				cal.saveSigns();
			}
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (!event.isCancelled() && (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			if (cal.signs.containsKey(block.getLocation())) {
				cal.signs.remove(block.getLocation());
				cal.saveSigns();
			}
		}
	}
	
}
