package com.nisovin.nethertrees;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldListener;

public class NetherTreeWorldListener extends WorldListener {

	public NetherTreeWorldListener(NetherTrees plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.WORLD_INIT, this, Event.Priority.Normal, plugin);
	}
	
	@Override
	public void onWorldInit(WorldInitEvent event) {
		World world = event.getWorld();
		if (world.getEnvironment() == Environment.NETHER) {
			System.out.println("Adding NetherTree populator to world " + world.getName());
			world.getPopulators().add(new NetherTreePopulator());
		}
	}
	
}
