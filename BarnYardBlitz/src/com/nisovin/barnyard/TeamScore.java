package com.nisovin.barnyard;

import org.bukkit.entity.EntityType;

public class TeamScore implements Comparable<TeamScore> {

	EntityType team;
	int score;
	
	public TeamScore(EntityType team) {
		this.team = team;
	}
	
	public TeamScore(EntityType team, int score) {
		this.team = team;
		this.score = score;
	}
	
	public EntityType getTeam() {
		return team;
	}
	
	public int getScore() {
		return score;
	}
	
	public void add(int amt) {
		score += amt;
	}
	
	public void set(int amt) {
		score = amt;
	}

	@Override
	public int compareTo(TeamScore obj) {
		if (this.score > obj.score) {
			return 1;
		} else if (this.score < obj.score) { 
			return -1;
		} else {
			return this.team.name().compareTo(obj.team.name());
		}
	}
	
}
