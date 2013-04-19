package com.nisovin.brucesgym;

public class StatisticUpdate implements DatabaseUpdate {

	String playerName;
	String statistic;
	int amount;
	
	public StatisticUpdate(String playerName, String statistic, int amount) {
		this.playerName = playerName;
		this.statistic = statistic;
		this.amount = amount;
	}
	
}
