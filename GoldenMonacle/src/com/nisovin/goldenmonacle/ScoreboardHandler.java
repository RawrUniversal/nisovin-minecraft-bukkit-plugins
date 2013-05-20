package com.nisovin.goldenmonacle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardHandler {
	
	final int numPlayersOnScoreboard = 10;
	final int scoreboardUpdateInterval = 50;

	Map<String, PlayerScore> scoresMap;
	TreeSet<PlayerScore> scoresSorted;
	Set<String> currentTopPlayers;
	
	Scoreboard scoreboard;
	Objective objective;
	
	BukkitTask updater;
	
	public ScoreboardHandler(GoldenMonocle plugin) {
		scoresMap = new HashMap<String, PlayerScore>();
		scoresSorted = new TreeSet<PlayerScore>();
		currentTopPlayers = new HashSet<String>();
		
		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		objective = scoreboard.getObjective("Score");
		if (objective == null) {
			objective = scoreboard.registerNewObjective("Score", "Score");
			objective.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "Top Ten");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		updater = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			public void run() {
				updateScoreboard();
			}
		}, scoreboardUpdateInterval, scoreboardUpdateInterval);
	}
	
	public void modifyScore(Player player, int amount) {
		modifyScore(player.getName(), amount);
	}
	
	public void modifyScore(String name, int amount) {
		PlayerScore score = scoresMap.get(name);
		if (score != null) {
			scoresSorted.remove(score);
			score.modifyScore(amount);
		} else {
			score = new PlayerScore(name, amount);
			scoresMap.put(name, score);
		}
		scoresSorted.add(score);
	}
	
	public void setScore(Player player, int amount) {
		setScore(player.getName(), amount);
	}
	
	public void setScore(String name, int amount) {
		PlayerScore score = scoresMap.get(name);
		if (score != null) {
			scoresSorted.remove(score);
			score.setScore(amount);
		} else {
			score = new PlayerScore(name, amount);
			scoresMap.put(name, score);
		}
		scoresSorted.add(score);
	}
	
	public TreeSet<PlayerScore> getScores() {
		return scoresSorted;
	}
	
	public int getScore(Player player) {
		return getScore(player.getName());
	}
	
	public int getScore(String name) {
		PlayerScore score = scoresMap.get(name);
		if (score != null) {
			return score.score;
		} else {
			return 0;
		}
	}
	
	public void updateScoreboard() {
		String[] players = new String[numPlayersOnScoreboard];
		int[] scores = new int[numPlayersOnScoreboard];
		Set<String> newTopPlayers = new HashSet<String>();
		
		// get top scores
		int i = 0;
		String firstPlace = null;
		for (PlayerScore score : scoresSorted) {
			if (i == 0) {
				firstPlace = score.playerName;
			}
			players[i] = score.playerName;
			scores[i] = score.score;
			newTopPlayers.add(score.playerName);
			if (++i >= numPlayersOnScoreboard) {
				break;
			}
		}
		
		// remove players who are no longer in top score list
		for (String name : currentTopPlayers) {
			if (!newTopPlayers.contains(name)) {
				scoreboard.resetScores(Bukkit.getOfflinePlayer(name));
			}
		}
		
		// add/update top scores
		for (i = 0; i < players.length; i++) {
			if (players[i] != null) {
				Score score = objective.getScore(Bukkit.getOfflinePlayer(players[i]));
				score.setScore(scores[i]);
			}
		}
		currentTopPlayers = newTopPlayers;
		
		// update compasses
		if (firstPlace != null) {
			Player first = Bukkit.getPlayerExact(firstPlace);
			if (first != null) {
				Location loc = first.getLocation();
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.isValid()) {
						p.setCompassTarget(loc);
					}
				}
			}
		}
	}
	
	public void stopUpdater() {
		if (updater != null) {
			updater.cancel();
			updater = null;
		}
	}
	
}
