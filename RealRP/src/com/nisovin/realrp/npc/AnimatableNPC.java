package com.nisovin.realrp.npc;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkitcontrib.BukkitContrib;
import org.martin.bukkit.npclib.NPCEntity;
import org.martin.bukkit.npclib.NPCManager;

public class AnimatableNPC {

	public static NPCManager npcManager;
	
	private String name;
	private String skin;
	private Location position;
	
	private NPCEntity entity;
	
	public AnimatableNPC(String name, String skin, Location position) {
		this.name = name;
		this.skin = skin;
		this.position = position;
	}
	
	public void show() {
		entity = npcManager.spawnNPC(name, position);
		if (!skin.equals("")) {
			BukkitContrib.getAppearanceManager().setGlobalSkin((HumanEntity)entity.getBukkitEntity(), skin);
		}
	}
	
	public void hide() {
		npcManager.despawn(name);
	}
	
}
