package com.nisovin.oldgods;

public class EffectTicker implements Runnable {

	private OldGods plugin;
	
	public EffectTicker(OldGods plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		God god = plugin.currentGod();
		
		if (god == God.HEALING) {
			
		}
	}

}
