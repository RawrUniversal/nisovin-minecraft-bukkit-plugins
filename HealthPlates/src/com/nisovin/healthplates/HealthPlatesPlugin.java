package com.nisovin.healthplates;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_5_R2.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HealthPlatesPlugin extends JavaPlugin implements Listener {
		
	Scoreboard scoreboard;
	
	Map<String, ScoreboardTeam> healthTeams;
	ScoreboardTeam teamWhite;
	ScoreboardTeam teamYellow;
	ScoreboardTeam teamGold;
	ScoreboardTeam teamRed;
	
	@Override
	public void onEnable() {
		scoreboard = ((CraftWorld)Bukkit.getWorlds().get(0)).getHandle().getScoreboard();
		teamWhite = scoreboard.createTeam("w");
		teamWhite.setPrefix(ChatColor.WHITE.toString());
		teamYellow = scoreboard.createTeam("y");
		teamYellow.setPrefix(ChatColor.YELLOW.toString());
		teamGold = scoreboard.createTeam("g");
		teamGold.setPrefix(ChatColor.GOLD.toString());
		teamRed = scoreboard.createTeam("r");
		teamRed.setPrefix(ChatColor.RED.toString());
		ScoreboardTeam[] teams = { teamWhite, teamYellow, teamGold, teamRed };
		for (ScoreboardTeam team : teams) {
			team.setAllowFriendlyFire(true);
			team.setCanSeeFriendlyInvisibles(false);
			team.setDisplayName("");
		}
		
		healthTeams = new HashMap<String, ScoreboardTeam>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			updateHealthTeam(player);
		}
		
		getServer().getPluginManager().registerEvents(this, this);
		
	}
	
	@Override
	public void onDisable() {
		ScoreboardTeam[] teams = { teamWhite, teamYellow, teamGold, teamRed };
		for (ScoreboardTeam team : teams) {
			scoreboard.removeTeam(team);
		}
		
		HandlerList.unregisterAll((Listener)this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender.isOp()) {
			if (args.length == 0) return false;
			if (args[0].equalsIgnoreCase("disable")) {
				onDisable();
				sender.sendMessage("Health Plates disabled.");
			} else if (args[0].equalsIgnoreCase("enable")) {
				onDisable();
				onEnable();
				sender.sendMessage("Health Plates enabled.");
			} else if (args[0].equalsIgnoreCase("update") && args.length == 2) {
				Player player = Bukkit.getPlayer(args[1]);
				if (player != null) {
					updateHealthTeam(player);
				} else {
					sender.sendMessage("No such player.");
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	public void updateHealthTeam(Player player) {
		String name = player.getName();
		ScoreboardTeam currTeam = healthTeams.get(name);
		ScoreboardTeam newTeam = getTeamByHealth(player.getHealth());
		if (currTeam != newTeam) {
			scoreboard.addPlayerToTeam(name, newTeam);
			healthTeams.put(name, newTeam);
		}
	}
	
	public ScoreboardTeam getTeamByHealth(int health) {
		if (health >= 16) {
			return teamWhite;
		} else if (health >= 11) {
			return teamYellow;
		} else if (health >= 6) {
			return teamGold;
		} else {
			return teamRed;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		
		final Player player = (Player)event.getEntity();
		//if (player.hasPermission("healthplates.nocolor")) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				if (player.isValid()) {
					updateHealthTeam(player);
				}
			}
		}, 1);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGainHealth(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		
		final Player player = (Player)event.getEntity();
		//if (player.hasPermission("healthplates.nocolor")) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				updateHealthTeam(player);
			}
		}, 1);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		//if (player.hasPermission("healthplates.nocolor")) return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				updateHealthTeam(player);
			}
		}, 1);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		//if (player.hasPermission("healthplates.nocolor")) return;
		
		scoreboard.addPlayerToTeam(event.getPlayer().getName(), teamWhite);
		healthTeams.put(player.getName().toLowerCase(), teamWhite);
	}
	
	public void debug(String string) {
		System.out.println(string);
	}

}
