package com.nisovin.realrp;

import org.bukkit.plugin.java.JavaPlugin;
import org.martin.bukkit.npclib.NPCManager;

import com.nisovin.realrp.npc.AnimatableNPC;

public class RealRP extends JavaPlugin {

	
	@Override
	public void onEnable() {
		AnimatableNPC.npcManager = new NPCManager(this);
		this.getCommand("spawnnpc").setExecutor(new CommandSpawnNpc(this));
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
