package com.nisovin.brucesgym;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class BrucesGym extends JavaPlugin {

	private static BrucesGym plugin;
	public BrucesGym getInstance() {
		return plugin;
	}
	
	private Database database;
	
	private GymGameMode gameMode;
	private Queue<DatabaseUpdate> updates = new ConcurrentLinkedQueue<DatabaseUpdate>();
	private BukkitTask updateTask;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		// get config file
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (Exception e) {
			getLogger().severe("FAILED TO LOAD CONFIG FILE");
			e.printStackTrace();
			setEnabled(false);
			return;
		}
		
		// load database
		database = new Database(this);
		boolean connected = database.connect(config.getString("database.host"), config.getString("database.user"), config.getString("database.pass"), config.getString("database.db"));
		if (!connected) {
			getLogger().severe("DATABASE CONNECTION ERROR, STATS WILL NOT BE SAVED");
			return;
		}
		
		// start updater
		updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
			public void run() {
				database.performUpdates(updates);
			}
		}, 30*20, 30*20);
	}
	
	@Override
	public void onDisable() {
		updateTask.cancel();
		
		if (updates.size() > 0) {
			database.performUpdates(updates);
		}
	}
	
	public static void initializeGameMode(GymGameMode gameMode) {
		plugin.gameMode = gameMode;
	}
	
	public static void registerStatistic(String statistic, StatisticType type) {
		registerStatistic(statistic, type, plugin.gameMode);
	}
	
	public static void registerStatistic(String statistic, StatisticType type, GymGameMode gameMode) {
		plugin.database.registerStatistic(gameMode, statistic, type);
	}
	
	public static void updateStatistic(String playerName, String statistic, int amount) {
		plugin.updates.add(new StatisticUpdate(playerName, statistic, amount));
	}
	
	public static void addKill(String killerName, String killedName, String weaponName) {
		plugin.updates.add(new KillUpdate(killerName, killedName, plugin.gameMode, weaponName));
	}
	
}
