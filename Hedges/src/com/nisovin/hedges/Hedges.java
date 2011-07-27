package com.nisovin.hedges;

import org.bukkit.plugin.java.JavaPlugin;

public class Hedges extends JavaPlugin {

	@Override
	public void onEnable() {
		new HedgeBlockListener(this);
	}

	@Override
	public void onDisable() {		
	}
}
