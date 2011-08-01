package com.nisovin.realrp;

import org.bukkit.plugin.java.JavaPlugin;
import org.martin.bukkit.npclib.NPCManager;

import com.nisovin.realrp.character.*;
import com.nisovin.realrp.chat.*;

public class RealRP extends JavaPlugin {

	private static RealRP plugin;
	
	private EmoteManager emotes;
	
	public static RealRP getPlugin() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		AnimatableNPC.npcManager = new NPCManager(this);
		this.getCommand("spawnnpc").setExecutor(new CommandSpawnNpc(this));
	}
	
	public Emote getEmote(String emote) {
		return emotes.getEmote(emote);
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
