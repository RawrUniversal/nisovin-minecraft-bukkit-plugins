package com.nisovin.coop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Party {

	private static Map<String, Party> playerParties = new HashMap<String, Party>();
	
	public static Party getParty(Player player) {
		return playerParties.get(player.getName().toLowerCase());
	}
	
	public static boolean inParty(Player player) {
		return playerParties.containsKey(player.getName().toLowerCase());
	}
	
	private String leader = null;
	private Set<String> members = new HashSet<String>();
	
	public Party(Player leader) {
		this.leader = leader.getName().toLowerCase();
		members.add(this.leader);
	}
	
	public boolean addMember(Player player) {
		String name = player.getName().toLowerCase();
		if (members.contains(name)) return false;
		playerParties.put(name, this);
		members.add(name);
		return true;
	}
	
	public boolean removeMember(Player player) {
		String name = player.getName().toLowerCase();
		if (members.contains(name)) {
			playerParties.remove(name);
			members.remove(name);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isMember(Player player) {
		return members.contains(player.getName().toLowerCase());
	}
	
	public boolean isLeader(Player player) {
		return player.getName().equalsIgnoreCase(leader);
	}
	
	public int size() {
		return members.size();
	}
	
	public List<Player> getMembers() {
		List<Player> players = new ArrayList<Player>();
		for (String name : members) {
			Player p = Bukkit.getPlayerExact(name);
			if (p != null) {
				players.add(p);
			}
		}
		return players;
	}
	
	public List<Player> getMembersInRangeOf(Player player) {
		return getMembersInRangeOf(player, true);
	}
	
	public List<Player> getMembersInRangeOf(Player player, boolean includeTarget) {
		List<Player> players = new ArrayList<Player>();
		if (includeTarget) {
			players.add(player);
		}
		List<Entity> nearbyEntities = player.getNearbyEntities(16, 8, 16);
		for (Entity e : nearbyEntities) {
			if (e instanceof Player && members.contains(((Player)e).getName().toLowerCase())) {
				players.add((Player)e);
			}
		}
		return players;
	}
	
	public void sendMessage(String message) {
		for (String name : members) {
			Player player = Bukkit.getPlayerExact(name);
			if (player != null && player.isValid()) {
				player.sendMessage(CoopPlugin.chatColor + message);
			}
		}
	}
	
}
