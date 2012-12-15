package com.nisovin.accesscontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;

public class AccessControl extends JavaPlugin implements Listener {

	Map<String, Set<String>> votekicks;
	Set<String> votekicked;
	
	@Override
	public void onEnable() {
		votekicks = new HashMap<String, Set<String>>();
		votekicked = new HashSet<String>();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if (event.getResult() == Result.KICK_FULL) {
			if (player.isOp()) {
				event.setResult(Result.ALLOWED);
				return;
			}
			if (player.hasPermission("access.whitelist")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (!p.isOp() && !p.hasPermission("access.whitelist")) {
						p.kickPlayer("The server is full!");
						event.setResult(Result.ALLOWED);
						return;
					}
				}
			}
			if (player.hasPermission("access.admin")) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (!p.isOp() && !p.hasPermission("access.admin")) {
						p.kickPlayer("The server is full!");
						event.setResult(Result.ALLOWED);
						return;
					}
				}
			}
		} else if (!player.hasPermission("access.whitelist") && votekicked.contains(player.getName().toLowerCase())) {
			event.setResult(Result.KICK_WHITELIST);
			event.setKickMessage("You are not on the whitelist.");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender instanceof Player && sender.hasPermission("access.whitelist")) {
			final Player player = (Player)sender;
			
			if (args == null || args.length != 1) {
				sender.sendMessage("You must specify a player to vote kick.");
				return true;
			}
			
			final Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sender.sendMessage("Player not found.");
				return true;
			} else if (target.hasPermission("access.whitelist")) {
				sender.sendMessage("You cannot vote kick that player.");
				return true;
			}
			
			List<Player> whitelisted = new ArrayList<Player>();
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("access.whitelist")) {
					whitelisted.add(p);
				}
			}
			
			Set<String> votes = votekicks.get(target.getName().toLowerCase());
			if (votes == null) {
				votes = new HashSet<String>();
				votekicks.put(target.getName().toLowerCase(), votes);
				for (Player p : whitelisted) {
					if (!p.equals(player)) {
						p.sendMessage("A vote has been initiated to kick " + target.getName() + ".");
						p.sendMessage("To vote to kick, type: /votekick " + target.getName());
					}
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						Set<String> votes = votekicks.remove(target.getName().toLowerCase());
						if (votes != null) {
							for (String name : votes) {
								Player p = Bukkit.getPlayerExact(name);
								if (p != null) {
									p.sendMessage("The vote to kick " + target.getName() + " has failed.");
								}
							}
						}
					}
				}, 60);
			}
			
			if (votes.contains(player.getName().toLowerCase())) {
				player.sendMessage("You have already voted to kick that player.");
				return true;
			}
			
			votes.add(player.getName().toLowerCase());
			
			if ((double)votes.size() / (double)whitelisted.size() > .39) {
				target.kickPlayer("You have been vote kicked.");
				votekicked.add(target.getName().toLowerCase());
				votekicks.remove(target.getName().toLowerCase());
			}
		}
		
		return true;
	}
	
}
