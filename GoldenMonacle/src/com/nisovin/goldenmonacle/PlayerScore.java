package com.nisovin.goldenmonacle;

import org.bukkit.entity.Player;

public class PlayerScore implements Comparable<PlayerScore> {

	String playerName;
	int score;
	
	public PlayerScore(Player player, int score) {
		this.playerName = player.getName();
		this.score = score > 0 ? score : 0;
	}
	
	public PlayerScore(String name, int score) {
		this.playerName = name;
		this.score = score > 0 ? score : 0;
	}
	
	public void modifyScore(int amount) {
		int newScore = score + amount;
		if (newScore < 0) {
			score = 0;
		} else {
			score = newScore;
		}
	}
	
	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public int compareTo(PlayerScore o) {
		if (o.score > this.score) {
			return 1;
		} else if (o.score < this.score) {
			return -1;
		} else {
			return o.playerName.compareTo(this.playerName);
		}
	}
	
}
