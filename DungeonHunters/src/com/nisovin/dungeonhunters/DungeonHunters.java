package com.nisovin.dungeonhunters;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DungeonHunters {

	int minPlayersPerTeam = 5;
	int minMonsterCount = 5;
	
	String[] teamNames = { "Blue", "Red", "Green", "Yellow", "Aqua", "Purple", "Gold", "Maroon" };
	ChatColor[] teamColors = { ChatColor.BLUE, ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.DARK_RED };
	Color[] armorColors = { Color.BLUE, Color.RED, Color.LIME, Color.YELLOW, Color.AQUA, Color.PURPLE, Color.ORANGE, Color.MAROON };
	
	Location[] teamSpawns = new Location[8];
	Location monsterSpawn;
	
	List<HunterTeam> teams;
	Map<String, HunterTeam> playersToTeams;
	Set<String> monsters;
	
	public void splitPlayers() {
		List<Player> players = Arrays.asList(Bukkit.getOnlinePlayers());
		int count = players.size();
		
		// determine team count
		int teamCount = 0;
		int[] tests = { 8, 6, 4, 3, 2 };
		for (int test : tests) {
			if (count - minMonsterCount / test >= minPlayersPerTeam) {
				teamCount = test;
			}
		}
		
		// determine players per team
		int playersPerTeam = 0;
		if (teamCount > 0) {
			playersPerTeam = count - minMonsterCount / teamCount;
		} else {
			teamCount = 1;
			playersPerTeam = count / 2;
		}
		
		// create teams and split players
		Collections.shuffle(players);
		int p = 0;
		for (int t = 0; t < teamCount; t++) {
			HunterTeam team = new HunterTeam(teamNames[t], teamColors[t], armorColors[t]);
			teams.add(team);
			Location spawn = teamSpawns[t];
			for (int i = 0; i < playersPerTeam; i++) {
				Player player = players.get(p);
				team.addPlayer(player);
				playersToTeams.put(player.getName(), team);
				player.teleport(spawn);
				p++;
			}
		}
		for (; p < count; p++) {
			Player player = players.get(p);
			monsters.add(player.getName());
			player.teleport(monsterSpawn);
		}
	}
	
	public void checkForEmptyTeams() {
		Iterator<HunterTeam> iter = teams.iterator();
		while (iter.hasNext()) {
			HunterTeam team = iter.next();
			if (team.size() == 0) {
				iter.remove();
			}
		}
	}
	
	public void checkForEndOfGame() {
		if (teams.size() == 1) {
			HunterTeam team = teams.get(0);
			Bukkit.broadcastMessage(ChatColor.GOLD + "TEAM " + team.getChatColor() + team.getName().toUpperCase() + ChatColor.GOLD + " WINS!");
			endGame();
		}
	}
	
	public void endGame() {
		
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		HunterTeam team = playersToTeams.remove(player.getName());
		if (team != null) {
			// move to monster team
			team.removePlayer(player);
			monsters.add(player.getName());
			
			// check team statuses
			checkForEmptyTeams();
			checkForEndOfGame();
		}
	}
	
}
