package com.nisovin.MineCal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MineCal extends JavaPlugin implements Listener {

	private long day = 0;
	private long yearOffset = 0;
	private String worldName;
	
	private String[] monthNames;
	private int[] monthDays;
	private String[] weekdays;
	private String[] ordinals;
	private int yearLength;
	private String yearTag;
	
	private String dateStr;
	private boolean announce;
	private int offset;
	private boolean ignoreBigChanges;
	private int tickInterval = 1000;
	private boolean debug = false;
	
	private int task;
	
	String year;
	String weekday;
	int dayOfMonth;
	String month;
	String dayName;
	
	private SignHandler signHandler;

	@Override
	public void onEnable() {
		loadConfig();
		loadDay();
		calcDateData();
		
		World world = getServer().getWorld(worldName);
		
		signHandler = new SignHandler(this);
		signHandler.updateAllSigns();
		
		DayWatcher watcher = new DayWatcher(this, world, offset, ignoreBigChanges, tickInterval);
		task = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, tickInterval, tickInterval);
	}
	
	@Override
	public void onDisable() {
		signHandler.disable();
		Bukkit.getScheduler().cancelTask(task);
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (command.getName().equals("today")) {
			sender.sendMessage(getDateString());
			return true;
		} else if (command.getName().equals("roleplaycalendar")) {
			if (args.length == 0) {
				return false;
			} else if (args[0].equalsIgnoreCase("reload")) {
				onDisable();
				onEnable();
				sender.sendMessage("RolePlayCalendar reloaded.");
				return true;
			} else if (args[0].equalsIgnoreCase("advance")) {
				advanceDay();
				return true;
			}
		}
		return false;
	}
	
	public void advanceDay() {
		day++;
		saveDay();
		
		calcDateData();
		
		if (announce) {
			String s = getDateString();
			Player[] players = getServer().getOnlinePlayers();
			for (Player p : players) {
				if (p.getWorld().getName().equals(worldName)) {
					p.sendMessage(s);
				}
			}
		}
		
		signHandler.updateAllSigns();
	}
	
	private void calcDateData() {
		year = ((int)(day / yearLength) + yearOffset) + (yearTag.equals("")?"":" "+yearTag);
		weekday = weekdays[(int)(day % weekdays.length)];
		
		dayOfMonth = (int)(day % yearLength);
		month = "";
		for (int i = 0; i < monthDays.length; i++) {
			if (dayOfMonth - monthDays[i] < 0) {
				month = monthNames[i];
				break;
			} else {
				dayOfMonth -= monthDays[i];
			}
		}
		dayOfMonth++;
		if (month.equals("")) {
			month = monthNames[monthNames.length-1];
		}
		dayName = ordinals[dayOfMonth-1];
	}
	
	public String getDateString() {
		String str = dateStr.replace("%y", year).replace("%m", month).replace("%w", weekday).replace("%o", dayName).replace("%d", dayOfMonth+"");
		str = ChatColor.translateAlternateColorCodes('&', str);
		return str;
	}
	
	private void loadDay() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "day.txt");
		try {
			Scanner scanner = new Scanner(file);
			if (scanner.hasNext()) {
				String line = scanner.nextLine();
				day = Integer.parseInt(line);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			day = 0;
		}		
	}
	
	private void saveDay() {
		File file = new File(getDataFolder(), "day.txt");
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			writer.append(day+"");
			writer.close();			
		} catch (IOException e) {
		}	
	}
	
	private void loadConfig() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "config.yml");
		if (!file.exists()) {
			saveDefaultConfig();
		}
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
			
			dateStr = config.getString("datestr", "Today is %w, %m %d, %y");
			announce = config.getBoolean("dayannounce", true);
			monthNames = config.getString("monthnames", "January,February,March,April,May,June,July,August,September,October,November,December").split(",");
			String[] temp = config.getString("monthdays", "31,28,31,30,31,30,31,31,30,31,30,31").split(",");
			monthDays = new int[temp.length];
			ordinals = config.getString("ordinals", "1st,2nd,3rd,4th,5th,6th,7th,8th,9th,10th,11th,12th,13th,14th,15th,16th,17th,18th,19th,20th,21st,22nd,23rd,24th,25th,26th,27th,28th,29th,30th,31st").split(",");
			yearLength = 0;
			for (int i = 0; i < temp.length; i++) {
				monthDays[i] = Integer.parseInt(temp[i]);
				yearLength += monthDays[i];
			}
			weekdays = config.getString("weekdays", "Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday").split(",");
			yearOffset = config.getInt("yearoffset", 2000);
			yearTag = config.getString("yeartag", "A.D.");
			worldName = config.getString("world", getServer().getWorlds().get(0).getName());
			offset = config.getInt("daychangeoffset", 0) * 1000;
			tickInterval = config.getInt("timecheckinterval", 1000);
			ignoreBigChanges = config.getBoolean("ignorebigchanges", false);
			debug = config.getBoolean("debug", false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void debug(String message) {
		if (debug) {
			getLogger().info("DEBUG: " + message);
		}
	}
	
}
