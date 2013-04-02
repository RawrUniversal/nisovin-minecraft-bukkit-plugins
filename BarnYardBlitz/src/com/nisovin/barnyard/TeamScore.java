package com.nisovin.barnyard;

import org.bukkit.entity.EntityType;

public class TeamScore implements Comparable<TeamScore> {

	EntityType team;
	int areaScore;
	float killScore;
	
	public TeamScore(EntityType team) {
		this.team = team;
	}
	
	public TeamScore(EntityType team, int areaScore, float killScore) {
		this.team = team;
		this.areaScore = areaScore;
		this.killScore = killScore;
	}
	
	public EntityType getTeam() {
		return team;
	}
	
	public int getScore() {
		return areaScore + Math.round(killScore);
	}
	
	public int getAreaScore() {
		return areaScore;
	}
	
	public float getKillScore() {
		return killScore;
	}

	@Override
	public int compareTo(TeamScore obj) {
		if (this.getScore() > obj.getScore()) {
			return 1;
		} else if (this.getScore() < obj.getScore()) {
			return -1;
		} else {
			return this.team.name().compareTo(obj.team.name());
		}
	}
	
}
