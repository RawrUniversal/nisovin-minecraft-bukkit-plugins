package com.nisovin.realrp.character;

import java.util.HashMap;

import org.bukkit.Location;

public class NPCManager {

	private HashMap<String,AnimatableNPC> npcs;
	
	public NPCManager() {
		npcs = new HashMap<String,AnimatableNPC>();
	}
	
	public void createNewNpc(String name, String skin, Location position) {
		AnimatableNPC npc = new AnimatableNPC(name, skin, position);
		npcs.put(name, npc);
	}
	
}
