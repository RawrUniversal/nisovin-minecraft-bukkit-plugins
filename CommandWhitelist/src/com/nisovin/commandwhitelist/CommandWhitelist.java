package com.nisovin.commandwhitelist;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandWhitelist extends JavaPlugin implements Listener {

	Set<String> allowedCommands = new HashSet<String>();
	Set<String> noReportCommands = new HashSet<String>();
	
	@Override
	public void onEnable() {
		loadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	void loadConfig() {
		allowedCommands.clear();
		noReportCommands.clear();
		
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
			
			List<String> list = config.getStringList("commands");
			if (list != null) {
				for (String s : list) {
					allowedCommands.add(s.toLowerCase());
				}
			}
			
			list = config.getStringList("no-report");
			if (list != null) {
				for (String s : list) {
					noReportCommands.add(s.toLowerCase());
				}
			}
		} catch (Exception e) {
			getLogger().severe("CONFIG FAILED TO LOAD");
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		loadConfig();
		sender.sendMessage("Command whitelist reloaded");
		return true;
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (player.isOp() || player.hasPermission("commandwhitelist.admin")) {
			return;
		}
		
		String command = event.getMessage().split(" ")[0].substring(1).toLowerCase();
		
		if (!noReportCommands.contains(command)) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.isOp() || p.hasPermission("commandwhitelist.admin")) {
					p.sendMessage(ChatColor.RED + "[Cmd] " + ChatColor.AQUA + player.getName() + ": " + ChatColor.GREEN + event.getMessage());
				}
			}
		}
		
		if (!allowedCommands.contains(command)) {
			event.setCancelled(true);
		}
	}
	
}
