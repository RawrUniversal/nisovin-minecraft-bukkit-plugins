package com.nisovin.worldloader;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Party {

	private String leader;
	private Set<String> members;
	
	public Party(Player leader) {
		this.leader = leader.getName().toLowerCase();
		members = new HashSet<String>();
		members.add(leader.getName().toLowerCase());
	}
	
	public Player getLeader() {
		return Bukkit.getPlayer(leader);
	}
	
	public boolean isLeader(Player player) {
		return player.getName().toLowerCase().equals(leader);
	}
	
	public void add(Player player) {
		members.add(player.getName().toLowerCase());
		messageAll(player.getName() + " has joined the party.");
	}
	
	public void remove(Player player) {
		messageAll(player.getName() + " has left the party.");
		members.remove(player.getName().toLowerCase());
	}
	
	public void messageAll(String message) {
		for (String s : members) {
			Player p = Bukkit.getPlayerExact(s);
			if (p != null) {
				p.sendMessage(message);
			}
		}
	}
	
	public Set<String> getMembers() {
		return members;
	}
	
	public int size() {
		return members.size();
	}
	
	public void teleport(WorldInstance instance) {
		for (String s : members) {
			Player p = Bukkit.getPlayerExact(s);
			if (p != null && p.isOnline()) {
				instance.teleport(p);
			}
		}
	}
	
}
