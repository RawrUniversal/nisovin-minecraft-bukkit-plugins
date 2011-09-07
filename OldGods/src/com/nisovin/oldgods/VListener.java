package com.nisovin.oldgods;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.plugin.PluginManager;

public class VListener extends VehicleListener {

	OldGods plugin;
	
	public VListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.VEHICLE_DAMAGE, this, Event.Priority.Monitor, plugin);
	}

	@Override
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.OCEAN) {
			if (event.getVehicle() instanceof Boat && event.getAttacker() instanceof Player && ((Player)event.getAttacker()).hasPermission("oldgods.devout.ocean")) {
				Location pLoc = ((Player)event.getAttacker()).getLocation();
				Location vLoc = event.getVehicle().getLocation();
				vLoc.setYaw(pLoc.getYaw());
				event.getVehicle().teleport(vLoc);
				event.getVehicle().setVelocity(pLoc.getDirection().setY(0).normalize().multiply(.5));
			}
		}
	}
	
}
