package com.nisovin.goldenmonacle;

import net.minecraft.server.v1_5_R2.EntityEnderDragon;
import net.minecraft.server.v1_5_R2.Packet24MobSpawn;
import net.minecraft.server.v1_5_R2.Packet29DestroyEntity;
import net.minecraft.server.v1_5_R2.Packet40EntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class VolatileCode {

	EntityEnderDragon enderDragon;
	
	public VolatileCode() {
		// initialize timer bar
		World world = Bukkit.getWorlds().get(0);
		enderDragon = new EntityEnderDragon(((CraftWorld)world).getHandle());
		Location loc = world.getSpawnLocation();
		enderDragon.setPositionRotation(loc.getX(), loc.getY() + 250, loc.getZ(), 0, 0);
		enderDragon.setCustomName("Time Remaining");
	}
	
	public void sendEnderDragonToAllPlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(new Packet24MobSpawn(enderDragon));
		}
	}
	
	public void sendEnderDragonToPlayer(Player p) {
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(new Packet24MobSpawn(enderDragon));
	}
	
	public void removeEnderDragonForAllPlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(new Packet29DestroyEntity(enderDragon.id));
		}
	}
	
	public void setDragonHealth(int health) {
		enderDragon.setHealth(health);
		enderDragon.getDataWatcher().watch(16, Integer.valueOf(health));
		Packet40EntityMetadata packet = new Packet40EntityMetadata(enderDragon.id, enderDragon.getDataWatcher(), false);
		for (Player p : Bukkit.getOnlinePlayers()) {
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		}
	}
}
