package com.nisovin.MineCal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MineCal extends JavaPlugin implements Listener {

	private long day = 0;
	private long yearOffset = 0;
	private String worldName;
	
	private String[] monthNames;
	private int[] monthDays;
	private String[] weekdays;
	private int yearLength;
	private String yeartag;
	
	private String dateStr;
	private boolean announce;
	private boolean allowCommand;
	private int offset;
	private boolean ignoreBigChanges;
	private int tickInterval = 1000;
	
	private int task;
	
	private String year;
	private String weekday;
	private int dayOfMonth;
	private String month;
	
	// TODO: save signs by chunk rather than location
	public Hashtable<Location,String[]> signs = new Hashtable<Location,String[]>();

	@Override
	public void onEnable() {
		// TODO: create yaml config file
		loadConfig();
		loadDay();
		loadSigns();
		calcDateData();
		
		World world = getServer().getWorld(worldName);
		
		DayWatcher watcher = new DayWatcher(this, world, offset, ignoreBigChanges, tickInterval);
		task = getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, tickInterval, tickInterval);
		
		getServer().getPluginManager().registerEvents(this, this);
		
		getServer().getLogger().info("MineCal v" + this.getDescription().getVersion() + " loaded!");
	}
	
	// TODO: move listeners to new SignListener class
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onChunkLoad(final ChunkLoadEvent event) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				Chunk chunk = event.getChunk();
				for (Location loc : signs.keySet()) {
					if (loc.getBlockX() >> 4 == chunk.getX() && loc.getBlockZ() >> 4 == chunk.getZ()) {
						updateSign(loc);
					}
				}
			}
		}, 2);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onSignChange(SignChangeEvent event) {
		if (!event.isCancelled()) {
			boolean isCalSign = false;
			for (int i = 0; i < 4; i++) {
				if (event.getLine(i).contains("[cal]")) {
					isCalSign = true;
					break;
				}
			}
			if (isCalSign) {
				signs.put(event.getBlock().getLocation(), event.getLines().clone());
				initSign(event);
				saveSigns();
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (!event.isCancelled() && (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			if (signs.containsKey(block.getLocation())) {
				signs.remove(block.getLocation());
				saveSigns();
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if ((allowCommand || sender.isOp()) && command.getName().equals("today")) {
			sendDate(sender);
			return true;
		}
		return false;
	}
	
	public void advanceDay() {
		day++;
		saveDay();
		
		calcDateData();
		
		if (announce) {
			Player[] players = getServer().getOnlinePlayers();
			for (Player p : players) {
				if (p.getWorld().getName().equals(worldName)) {
					sendDate(p);
				}
			}
		}
		
		for (Location loc : signs.keySet()) {
			if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
				// TODO: check if block is sign, remove from list if it's not
				updateSign(loc);
			}
		}
	}
	
	public void updateSign(Location loc) {
		Material mat = loc.getBlock().getType();
		if (mat == Material.SIGN_POST || mat == Material.WALL_SIGN) { 
			Sign sign = (Sign)loc.getBlock().getState();
			String[] lines = signs.get(loc);
			for (int i = 0; i < lines.length; i++) {
				sign.setLine(i, lines[i].replace("[cal]","").replace("%y", year).replace("%m", month).replace("%w", weekday).replace("%d", dayOfMonth+""));
			}
			sign.update();
		}
	}
	
	public void initSign(SignChangeEvent event) {
		for (int i = 0; i < 4; i++) {
			event.setLine(i, event.getLine(i).replace("[cal]","").replace("%y", year).replace("%m", month).replace("%w", weekday).replace("%d", dayOfMonth+""));
		}		
	}
	
	private void calcDateData() {
		year = ((int)(day / yearLength) + yearOffset) + (yeartag.equals("")?"":" "+yeartag);
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
	}
	
	public void sendDate(CommandSender player) {		
		String str = dateStr.replace("%y", year).replace("%m", month).replace("%w", weekday).replace("%d", dayOfMonth+"");
		for (ChatColor color : ChatColor.values()) {
			str = str.replace("&" + color.getChar(), color.toString());
		}
		player.sendMessage(str);
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(task);		
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
	
	private void loadSigns() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "signs.txt");
		signs.clear();
		
		try {
			Scanner scanner = new Scanner(file);
			if (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					String[] data = line.split("\\|",8);
					System.out.println("len:"+data.length+"");
					Location loc = new Location(getServer().getWorld(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));
					String[] lines = {data[4],data[5],data[6],data[7]};
					signs.put(loc, lines);
				}
				
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}		
	}
	
	public void saveSigns() {
		File file = new File(getDataFolder(), "signs.txt");
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			for (Location loc : signs.keySet()) {
				String[] lines = signs.get(loc);
				writer.append(loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ());
				for (int i = 0; i < lines.length; i++) {
					writer.append("|" + lines[i]);
				}
				writer.newLine();
			}
			writer.close();			
		} catch (IOException e) {
		}	
	}
	
	private void loadConfig() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "config.txt");
		if (!file.exists()) {
			createDefaultConfig();
		}
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("") && !line.startsWith("#")) {
					String[] keyvalue = line.split("=");
					if (keyvalue[0].equals("datestr")) {
						dateStr = keyvalue[1];
					} else if (keyvalue[0].equals("dayannounce")) {
						if (keyvalue[1].equals("true") || keyvalue[1].equals("yes")) {
							announce = true;
						} else {
							announce = false;
						}
					} else if (keyvalue[0].equals("allowcommand")) {
						if (keyvalue[1].equals("true") || keyvalue[1].equals("yes")) {
							allowCommand = true;
						} else {
							allowCommand = false;
						}
					} else if (keyvalue[0].equals("monthnames")) {
						monthNames = keyvalue[1].split(",");
					} else if (keyvalue[0].equals("monthdays")) {
						String[] temp = keyvalue[1].split(",");
						monthDays = new int [temp.length];
						yearLength = 0;
						for (int i = 0; i < temp.length; i++) {
							monthDays[i] = Integer.parseInt(temp[i]);
							yearLength += monthDays[i];
						}
					} else if (keyvalue[0].equals("weekdays")) {
						weekdays = keyvalue[1].split(",");
					} else if (keyvalue[0].equals("yearoffset")) {
						yearOffset = Integer.parseInt(keyvalue[1]);
					} else if (keyvalue[0].equals("yeartag")) {
						yeartag = keyvalue[1];
					} else if (keyvalue[0].equals("world")) {
						worldName = keyvalue[1];
					} else if (keyvalue[0].equals("daychangeoffset")) {
						offset = Integer.parseInt(keyvalue[1]) * 1000;
					} else if (keyvalue[0].equals("timecheckinterval")) {
						tickInterval = Integer.parseInt(keyvalue[1]);
					} else if (keyvalue[0].equals("ignorebigchanges")) {
						if (keyvalue[1].equals("true") || keyvalue[1].equals("yes")) {
							ignoreBigChanges = true;
						} else {
							ignoreBigChanges = false;
						}
					}
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {			
		}
	}
	
	private void createDefaultConfig() {
		File file = new File(getDataFolder(), "config.txt");
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			
			writer.append("datestr=Today is %w, %m %d, %y");
			writer.newLine();
			writer.append("dayannounce=true");
			writer.newLine();
			writer.append("allowcommand=true");
			writer.newLine();
			writer.append("monthnames=January,February,March,April,May,June,July,August,September,October,November,December");
			writer.newLine();
			writer.append("monthdays=31,28,31,30,31,30,31,31,30,31,30,31");
			writer.newLine();
			writer.append("weekdays=Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday");
			writer.newLine();
			writer.append("yearoffset=2000");
			writer.newLine();
			writer.append("yeartag=A.D.");
			writer.newLine();
			writer.append("world=" + getServer().getWorlds().get(0).getName());
			writer.newLine();
			writer.append("daychangeoffset=0");
			writer.newLine();
			writer.append("timecheckinterval=1000");
			writer.newLine();
			writer.append("ignorebigchanges=false");
			writer.newLine();
			
			writer.close();
			
		} catch (IOException e) {
		}		
	}
	
}
