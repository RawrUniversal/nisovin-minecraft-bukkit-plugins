package com.nisovin.barnyard;


import net.minecraft.server.v1_5_R3.*;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class VolatileCode {

	EntityEnderDragon enderDragon;
	
	public VolatileCode() {
		// initialize timer bar
		enderDragon = new EntityEnderDragon(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
		enderDragon.setPositionRotation(-130, 26, 1064, 115, 0);
		enderDragon.setCustomName("Time Remaining Until Elimination");
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
