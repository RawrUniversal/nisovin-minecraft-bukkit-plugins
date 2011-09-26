package com.nisovin.realrp.npc;

import net.minecraft.server.ItemInWorldManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.inventory.ItemStack;

public class NPC {

	private NPCEntity entity;
	
	public NPC(String name, Location location) {
		net.minecraft.server.World w = ((CraftWorld)location.getWorld()).getHandle();
		entity = new NPCEntity(((CraftServer)Bukkit.getServer()).getServer(), w, name, new ItemInWorldManager(w));
		entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		w.addEntity(entity);
	}
	
	public Location getLocation() {
		return new Location(entity.world.getWorld(), entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
	}
	
	public void moveTo(Location location) {
		entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public void setItemInHand(ItemStack item) {
		entity.setItemInHand(item);
	}
	
	public void despawn() {
		entity.die();
	}
	
	public NPCEntity getEntity() {
		return entity;
	}
	
}
