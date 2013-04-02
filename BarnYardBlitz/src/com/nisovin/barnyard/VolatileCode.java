package com.nisovin.barnyard;

import java.util.Map;
import java.util.TreeSet;

import net.minecraft.server.v1_5_R2.EntityEnderDragon;
import net.minecraft.server.v1_5_R2.IScoreboardCriteria;
import net.minecraft.server.v1_5_R2.Packet24MobSpawn;
import net.minecraft.server.v1_5_R2.Packet40EntityMetadata;
import net.minecraft.server.v1_5_R2.Scoreboard;
import net.minecraft.server.v1_5_R2.ScoreboardObjective;
import net.minecraft.server.v1_5_R2.ScoreboardScore;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class VolatileCode {

	
	Scoreboard scoreboard;
	ScoreboardObjective objective;
	Map<EntityType, ScoreboardScore> scoreboardScores;
	EntityEnderDragon enderDragon;
	
	public VolatileCode() {		
		// initialize scoreboard
		scoreboard = ((CraftWorld)Bukkit.getWorlds().get(0)).getHandle().getScoreboard();
		objective = scoreboard.getObjective("Score");
		if (objective == null) {
			objective = scoreboard.registerObjective("Score", IScoreboardCriteria.b);
			objective.setDisplayName("Score");
			scoreboard.setDisplaySlot(1, objective);
		}
		
		// initialize timer bar
		enderDragon = new EntityEnderDragon(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
		enderDragon.setPositionRotation(-130, 26, 1064, 115, 0);
		enderDragon.setCustomName("Time Remaining Until Elimination");
	}
	
	public void sendEnderDragonToAllPlayers() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(new Packet24MobSpawn(enderDragon));
		}
	}
	
	public void sendEnderDragonToPlayer(Player p) {
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(new Packet24MobSpawn(enderDragon));
	}
	
	public void setDragonHealth(int health) {
		enderDragon.setHealth(health);
		enderDragon.getDataWatcher().watch(16, Integer.valueOf(health));
		Packet40EntityMetadata packet = new Packet40EntityMetadata(enderDragon.id, enderDragon.getDataWatcher(), false);
		for (Player p : Bukkit.getOnlinePlayers()) {
			((CraftPlayer)p).getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	public void updateScoreboard(TreeSet<TeamScore> scores) {
		for (TeamScore score : scores) {
			ScoreboardScore ss = scoreboard.getPlayerScoreForObjective(score.team.name().toLowerCase(), objective);
			ss.setScore(score.getScore());
		}
	}
	
	public void zeroScore(EntityType team) {
		ScoreboardScore ss = scoreboard.getPlayerScoreForObjective(team.name().toLowerCase(), objective);
		ss.setScore(0);
	}
	
}
