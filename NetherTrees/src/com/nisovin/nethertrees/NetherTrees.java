package com.nisovin.nethertrees;

import org.bukkit.plugin.java.JavaPlugin;

public class NetherTrees extends JavaPlugin {

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEnable() {
		new NetherTreeBlockListener(this);
		new NetherTreeWorldListener(this);		
		System.out.println("NetherTrees enabled");
	}
	
}
