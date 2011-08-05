package com.nisovin.realrp.interact;

import org.bukkit.entity.Player;

import com.nisovin.realrp.RealRP;
import com.nisovin.realrp.character.AnimatableNPC;

public class InteractRequest implements Comparable<InteractRequest> {

	private long requestTime;
	private int id;
	private Player player;
	private AnimatableNPC npc;
	private String description;
	
	public InteractRequest(Player player, AnimatableNPC npc, String description) {
		requestTime = System.currentTimeMillis();
		this.player = player;
		this.npc = npc;
		this.description = description;
	}

	@Override
	public int compareTo(InteractRequest o) {
		if (requestTime > o.requestTime) {
			return 1;
		} else if (requestTime < o.requestTime) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public String getAge() {
		String s = "";
		// get age
		long age = System.currentTimeMillis() - requestTime;
		// get hours
		long hours = age / (1000*60*60);
		if (hours > 0) {
			s += hours + "h";
		}
		// get minutes
		age = age % (1000*60*60);
		long minutes = age / (1000*60);
		if (minutes > 0) {
			s += minutes + "m";
		}
		if (s.isEmpty()) {
			s = "<1m";
		}
		return s;
	}
	
	public AnimatableNPC getNpc() {
		return npc;
	}
	
	public String getRequestLine() {
		return RealRP.settings().irRequestLineFormat
				.replace("%id", id+"")
				.replace("%age", getAge())
				.replace("%player", player.getDisplayName())
				.replace("%npc", npc.getChatName())
				.replace("%description", description);
	}
	
}
