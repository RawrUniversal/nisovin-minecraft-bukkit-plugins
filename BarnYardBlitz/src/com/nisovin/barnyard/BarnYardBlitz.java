package com.nisovin.barnyard;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BarnYardBlitz extends JavaPlugin implements Listener {
	
	static final EntityType[] validTeams = new EntityType[] { EntityType.CHICKEN, EntityType.COW, EntityType.OCELOT, EntityType.PIG, EntityType.SHEEP, EntityType.SQUID, EntityType.WOLF };
	static BlockUpdateQueue blockQueue;
	
	int uncapturedType1 = Material.DIRT.getId();
	byte uncapturedData1 = 0;
	int deadType1 = Material.BEDROCK.getId();
	byte deadData1 = 0;
	
	int chickenType1 = Material.GOLD_BLOCK.getId();
	byte chickenData1 = 0;
	int chickenType2 = 0;
	byte chickenData2 = 0;
	
	int cowType1 = Material.STEP.getId();
	byte cowData1 = 4;
	int cowType2 = 0;
	byte cowData2 = 0;
	
	int ocelotType1 = Material.GRASS.getId();
	byte ocelotData1 = 0;
	int ocelotType2 = Material.LONG_GRASS.getId();
	byte ocelotData2 = 1;
	
	int pigType1 = Material.SOUL_SAND.getId();
	byte pigData1 = 0;
	int pigType2 = 0;
	byte pigData2 = 0;
	
	int sheepType1 = Material.WOOL.getId();
	byte sheepData1 = 6;
	int sheepType2 = 0;
	byte sheepData2 = 0;
	
	int squidType1 = Material.STATIONARY_WATER.getId();
	byte squidData1 = 0;
	int squidType2 = 0;
	byte squidData2 = 0;
	
	int wolfType1 = Material.ICE.getId();
	byte wolfData1 = 0;
	int wolfType2 = 0;
	byte wolfData2 = 0;
	
	Random random = new Random();	

	static int areaSize = 64;
	int fieldY = 3;
	int buffInterval = 100;
	int foodInterval = 200;
	int foodIntervalBasedOnBuffInterval = 2;
	int captureInterval = 200;
	float captureRatio = 0.9F;
	static int noCaptureBorder = 16;
	int blockUpdatesPerTick = 150;
	static int[] replaceableLayer1 = { 2, 3, 8, 9, 35, 41, 43, 44, 88, 79 };
	static int[] replaceableLayer2 = { 0, 31 };
	int[] eliminationIntervals = { 600, 240, 240, 240, 240, 240 };
	CapturedArea barnArea;
	float pointsPerKill = 0.25F;
	
	boolean gameStarted = false;
	World world;
	String pigCaptain = null;
	Eliminator eliminator = null;
	int foodIntervalCounter = 0;
	
	Map<CapturedArea, String> capturedAreaNames;
	Map<CapturedArea, Integer> capturedAreaValues;
	Map<CapturedArea, List<Location>> capturedAreaSpawns;

	Map<EntityType, CapturedArea> teamDefaultSpawns;
	Map<EntityType, Integer> teamColors;
	Map<EntityType, String> teamNames;
	Map<EntityType, String[]> initCommands;
	Map<EntityType, String[]> spawnCommands;
	String[] pigCaptainCommands = new String[] {};
	
	Map<String, EntityType> playersToTeams;
	Map<EntityType, Set<String>> teamsToPlayers;
	Map<CapturedArea, EntityType> capturedAreasToTeams;
	Map<EntityType, Set<CapturedArea>> teamsToCapturedAreas;
	Map<EntityType, Integer> teamKills;
	
	long buffTime = 0;
	int buffIterations = 0;
	long captureTime1 = 0;
	long captureTime2 = 0;
	int captureIterations = 0;
	long spawnTime = 0;
	long spawnIterations = 0;
	
	VolatileCode volatileCode = null;
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);		
		world = getServer().getWorlds().get(0);
		blockQueue = new BlockUpdateQueue(this);

		// initialize containers
		capturedAreaNames = new HashMap<CapturedArea, String>();
		capturedAreaValues = new HashMap<CapturedArea, Integer>();
		capturedAreaSpawns = new HashMap<CapturedArea, List<Location>>();
		
		teamDefaultSpawns = new HashMap<EntityType, CapturedArea>();
		teamNames = new HashMap<EntityType, String>();
		teamColors = new HashMap<EntityType, Integer>();
		initCommands = new HashMap<EntityType, String[]>();
		spawnCommands = new HashMap<EntityType, String[]>();
		
		playersToTeams = new HashMap<String, EntityType>();
		teamsToPlayers = new HashMap<EntityType, Set<String>>();
		capturedAreasToTeams = new HashMap<CapturedArea, EntityType>();
		teamsToCapturedAreas = new HashMap<EntityType, Set<CapturedArea>>();
		teamKills = new HashMap<EntityType, Integer>();
		
		for (EntityType team : validTeams) {
			teamsToPlayers.put(team, new HashSet<String>());
			teamsToCapturedAreas.put(team, new HashSet<CapturedArea>());
		}
		
		// get config file
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (Exception e) {
			getLogger().severe("ERROR LOADING CONFIG FILE");
			e.printStackTrace();
			this.setEnabled(false);
			return;
		}
		
		// get basic options
		areaSize = config.getInt("area-size", areaSize);
		fieldY = config.getInt("field-y", fieldY);
		buffInterval = config.getInt("buff-interval", buffInterval);
		foodInterval = config.getInt("food-interval", foodInterval);
		foodIntervalBasedOnBuffInterval = foodInterval / buffInterval;
		captureInterval = config.getInt("capture-interval", captureInterval);
		captureRatio = (float)config.getDouble("capture-ratio", captureRatio);
		noCaptureBorder = config.getInt("no-capture-border", noCaptureBorder);
		blockUpdatesPerTick = config.getInt("block-updates-per-tick", blockUpdatesPerTick);
		List<Integer> list = config.getIntegerList("replaceable-layer1");
		if (list != null) {
			replaceableLayer1 = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				replaceableLayer1[i] = list.get(i);
			}
		}
		Arrays.sort(replaceableLayer1);
		list = config.getIntegerList("replaceable-layer2");
		if (list != null) {
			replaceableLayer2 = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				replaceableLayer2[i] = list.get(i);
			}
		}
		Arrays.sort(replaceableLayer2);
		list = config.getIntegerList("elimination-intervals");
		if (list != null && list.size() > 0) {
			eliminationIntervals = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				eliminationIntervals[i] = list.get(i);
			}
		}
		uncapturedType1 = config.getInt("uncaptured-type", uncapturedType1);
		uncapturedData1 = (byte)config.getInt("uncaptured-data", uncapturedData1);
		deadType1 = config.getInt("dead-type", deadType1);
		deadData1 = (byte)config.getInt("dead-data", deadData1);
		
		// get capture areas
		List<String> capturable = config.getStringList("capturable");
		if (capturable != null) {
			for (String s : capturable) {
				String[] data = s.split(" ", 3);
				CapturedArea area = new CapturedArea(data[0]);
				capturedAreaNames.put(area, data[2]);
				capturedAreaValues.put(area, Integer.parseInt(data[1]));
			}
		}
		barnArea = new CapturedArea(config.getString("barn", "0,0"));
		
		// get animal team spawns and commands and colors
		for (EntityType team : validTeams) {
			List<String> temp = config.getStringList(team.name().toLowerCase() + ".init-commands");
			if (temp != null && temp.size() > 0) {
				initCommands.put(team, temp.toArray(new String[temp.size()]));
			}
			temp = config.getStringList(team.name().toLowerCase() + ".spawn-commands");
			if (temp != null && temp.size() > 0) {
				spawnCommands.put(team, temp.toArray(new String[temp.size()]));
			}
			String color = config.getString(team.name().toLowerCase() + ".color", "9900FF");
			teamColors.put(team, Integer.parseInt(color, 16));
		}
		List<String> temp = config.getStringList("pig.captain-commands");
		if (temp != null && temp.size() > 0) {
			pigCaptainCommands = temp.toArray(new String[temp.size()]);
		}
		
		// get other animal team data
		chickenType1 = config.getInt("chicken.type1", chickenType1);
		chickenData1 = (byte)config.getInt("chicken.data1", chickenData1);
		chickenType2 = config.getInt("chicken.type2", chickenType2);
		chickenData2 = (byte)config.getInt("chicken.data2", chickenData2);
		cowType1 = config.getInt("cow.type1", cowType1);
		cowData1 = (byte)config.getInt("cow.data1", cowData1);
		cowType2 = config.getInt("cow.type2", cowType2);
		cowData2 = (byte)config.getInt("cow.data2", cowData2);
		ocelotType1 = config.getInt("ocelot.type1", ocelotType1);
		ocelotData1 = (byte)config.getInt("ocelot.data1", ocelotData1);
		ocelotType2 = config.getInt("ocelot.type2", ocelotType2);
		ocelotData2 = (byte)config.getInt("ocelot.data2", ocelotData2);
		pigType1 = config.getInt("pig.type1", pigType1);
		pigData1 = (byte)config.getInt("pig.data1", pigData1);
		pigType2 = config.getInt("pig.type2", pigType2);
		pigData2 = (byte)config.getInt("pig.data2", pigData2);
		sheepType1 = config.getInt("sheep.type1", sheepType1);
		sheepData1 = (byte)config.getInt("sheep.data1", sheepData1);
		sheepType2 = config.getInt("sheep.type2", sheepType2);
		sheepData2 = (byte)config.getInt("sheep.data2", sheepData2);
		squidType1 = config.getInt("squid.type1", squidType1);
		squidData1 = (byte)config.getInt("squid.data1", squidData1);
		squidType2 = config.getInt("squid.type2", squidType2);
		squidData2 = (byte)config.getInt("squid.data2", squidData2);
		wolfType1 = config.getInt("wolf.type1", wolfType1);
		wolfData1 = (byte)config.getInt("wolf.data1", wolfData1);
		wolfType2 = config.getInt("wolf.type2", wolfType2);
		wolfData2 = (byte)config.getInt("wolf.data2", wolfData2);
		
		// set team names
		teamNames.put(EntityType.CHICKEN, ChatColor.YELLOW + "Chicken");
		teamNames.put(EntityType.COW, ChatColor.RED + "Cow");
		teamNames.put(EntityType.OCELOT, ChatColor.GREEN + "Cat");
		teamNames.put(EntityType.PIG, ChatColor.DARK_RED + "Pig");
		teamNames.put(EntityType.SHEEP, ChatColor.LIGHT_PURPLE + "Sheep");
		teamNames.put(EntityType.SQUID, ChatColor.BLUE + "Squid");
		teamNames.put(EntityType.WOLF, ChatColor.GRAY + "Wolf");
		
		// reset playing field
		for (CapturedArea area : capturedAreaNames.keySet()) {
			area.setAreaTypeNoQueue(world, uncapturedType1, uncapturedData1, 0, (byte)0, fieldY);
		}
		
		// generate random spawn areas
		List<CapturedArea> areas = new ArrayList<CapturedArea>(capturedAreaNames.keySet());
		for (EntityType team : teamsToPlayers.keySet()) {
			CapturedArea area = areas.remove(random.nextInt(areas.size()));
			teamDefaultSpawns.put(team, area);
			captureArea(area, team);
			getLogger().info("Starting area for " + team.name().toLowerCase() + " is " + capturedAreaNames.get(area));
		}
		
		// load spawn points
		try {
			File spawnFile = new File(getDataFolder(), "spawnpoints.txt");
			if (spawnFile.exists()) {
				Scanner scanner = new Scanner(spawnFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine().trim();
					if (!line.isEmpty() && !line.startsWith("#")) {
						String[] data = line.split(",");
						Location location = new Location(world, Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Float.parseFloat(data[3]), 0F);
						CapturedArea area = new CapturedArea(location);
						if (capturedAreaNames.containsKey(area)) {
							List<Location> locs = capturedAreaSpawns.get(area);
							if (locs == null) {
								locs = new ArrayList<Location>();
								capturedAreaSpawns.put(area, locs);
							}
							locs.add(location);
						}
					}
				}
			}
		} catch (Exception e) {
			getLogger().severe("ERROR READING SPAWN POINTS FILE");
			e.printStackTrace();
		}

		try {
			Class.forName("net.minecraft.server.v1_5_R2.MinecraftServer");
			volatileCode = new VolatileCode();
		} catch (ClassNotFoundException e) {
			getLogger().warning("BarnYardBlitz needs an update! Scoreboard and dragon timer are disabled.");
			volatileCode = null;
		}
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String commandName = command.getName().toLowerCase();
		if (commandName.equals("score")) {
			if (gameStarted) {
				sender.sendMessage(ChatColor.GOLD + "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
				TreeSet<TeamScore> scores = getScores();
				for (TeamScore score : scores.descendingSet()) {
					sender.sendMessage(ChatColor.GOLD + "  Team " + teamNames.get(score.getTeam()) + ChatColor.GOLD + ": " + score.getScore() + " (captures: " + score.getAreaScore() + ", kills: " + Math.round(score.getKillScore()) + ")");
				}
				if (teamsToCapturedAreas.containsKey(EntityType.OCELOT) && teamsToCapturedAreas.get(EntityType.OCELOT).contains(barnArea)) {
					sender.sendMessage(ChatColor.GOLD + "  The cats have " + capturedAreaNames.get(barnArea) + "!");
				}
				if (eliminator != null) {
					long seconds = eliminator.timeToNextElimination();
					if (seconds > 100) {
						int minutes = (int)(seconds / 60);
						int secs = (int)(seconds % 60);
						sender.sendMessage(ChatColor.GOLD + "  Time to next elimination: " + minutes + " minutes and " + secs + " seconds");
					} else {
						sender.sendMessage(ChatColor.GOLD + "  Time to next elimination: " + seconds + " seconds");
					}
				}
				sender.sendMessage(ChatColor.GOLD + "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+");
			} else {
				sender.sendMessage(ChatColor.RED + "The game hasn't started yet.");
			}
		} else if (commandName.equals("setteamspawn")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					EntityType team = getTeamByName(args[0]);
					if (team == null || !teamsToPlayers.containsKey(team)) {
						sender.sendMessage(ChatColor.RED + "Invalid team or team already eliminated.");
					} else {
						teamDefaultSpawns.put(team, new CapturedArea(((Player)sender).getLocation()));
						sender.sendMessage(ChatColor.GOLD + "Default spawn for team " + team.name().toLowerCase() + " set to your location.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You must do this as a player");
				}
			} else if (args.length > 1) {
				EntityType team = getTeamByName(args[0]);
				if (team == null || !teamsToPlayers.containsKey(team)) {
					sender.sendMessage(ChatColor.RED + "Invalid team or team already eliminated.");
				} else {
					String name = args[1];
					for (int i = 2; i < args.length; i++) {
						name += " " + args[i];
					}
					for (CapturedArea area : capturedAreaNames.keySet()) {
						if (capturedAreaNames.get(area).equalsIgnoreCase(name)) {
							teamDefaultSpawns.put(team, area);
							sender.sendMessage(ChatColor.GOLD + "Default spawn for team " + team.name().toLowerCase() + " set to area " + name);
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Invalid area name specified.");
				}				
			} else {
				sender.sendMessage(ChatColor.RED + "Usage: /setteamspawn <team> [area name]");
			}
		} else if (commandName.equals("randomteamspawns")) {
			List<CapturedArea> areas = new ArrayList<CapturedArea>(capturedAreaNames.keySet());
			for (EntityType team : teamsToPlayers.keySet()) {
				CapturedArea area = areas.remove(random.nextInt(areas.size()));
				teamDefaultSpawns.put(team, area);
				sender.sendMessage(ChatColor.GOLD + "Default spawn for team " + team.name().toLowerCase() + " set to area " + capturedAreaNames.get(area));
			}
		} else if (commandName.equals("addspawnpoint")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				CapturedArea area = new CapturedArea(player.getLocation());
				if (capturedAreaNames.containsKey(area)) {
					List<Location> locs = capturedAreaSpawns.get(area);
					if (locs == null) {
						locs = new ArrayList<Location>();
						capturedAreaSpawns.put(area, locs);
					}
					locs.add(player.getLocation().add(0, 1, 0));
					sender.sendMessage(ChatColor.GOLD + "This location has been added as a spawn point.");
					saveSpawnPoints(sender);
				} else {
					sender.sendMessage(ChatColor.RED + "This is not a valid capture area.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		} else if (commandName.equals("removespawnpoints")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				CapturedArea area = new CapturedArea(player.getLocation());
				List<Location> locs = capturedAreaSpawns.remove(area);
				if (locs != null) {
					sender.sendMessage(ChatColor.GOLD + "Removed " + locs.size() + " spawn points in this area.");
					saveSpawnPoints(sender);
					for (Location loc : locs) {
						Block b = loc.clone().subtract(0, 1, 0).getBlock();
						player.sendBlockChange(b.getLocation(), b.getType(), b.getData());
					}
				} else {
					sender.sendMessage(ChatColor.RED + "There are no spawn points in this area.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		} else if (commandName.equals("viewspawnpoints")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				CapturedArea area = new CapturedArea(player.getLocation());
				if (capturedAreaSpawns.containsKey(area)) {
					List<Location> locs = capturedAreaSpawns.get(area);
					for (Location loc : locs) {
						player.sendBlockChange(loc.clone().subtract(0, 1, 0), Material.GLOWSTONE, (byte)0);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "There are no spawn points in this area.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}			
		} else if (commandName.equals("startgame")) {
			if (gameStarted) {
				sender.sendMessage(ChatColor.RED + "The game has already started.");
			} else {
				sender.sendMessage(ChatColor.GOLD + "The game has started.");
				startGame();
			}
		} else if (commandName.equals("setteam")) {
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "You must specify a player and team.");
				sender.sendMessage(ChatColor.RED + "Usage: /setteam <player> <team>");
			} else {
				Player player = Bukkit.getPlayer(args[0]);
				if (player == null) {
					sender.sendMessage(ChatColor.RED + "Player not found.");
				} else {
					EntityType team = getTeamByName(args[1]);
					if (team == null || !teamsToPlayers.containsKey(team)) {
						sender.sendMessage(ChatColor.RED + "Invalid team or team already eliminated.");
					} else {
						putPlayerInTeam(player, team);
						sender.sendMessage(ChatColor.GOLD + "Player " + player.getName() + " put in team " + team.name().toLowerCase());
					}
				}
			}
		} else if (commandName.equals("pigcaptain")) {
			choosePigCaptain();
			sender.sendMessage(ChatColor.GOLD + "A new pig captain has been chosen: " + pigCaptain);
		} else if (commandName.equals("capture")) {
			if (args.length == 2) {
				if (!args[0].matches("-?[0-9]+,-?[0-9]+")) {
					sender.sendMessage(ChatColor.RED + "Invalid capture id.");
				} else {
					String[] coords = args[0].split(",");
					CapturedArea area = new CapturedArea(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));
					if (args[1].equalsIgnoreCase("nobody")) {
						captureArea(area, null);
						sender.sendMessage(ChatColor.GOLD + "Capture area " + area.getId() + " is now owned by nobody");
					} else if (args[1].equalsIgnoreCase("dead")) {
						setAreaUncapturable(area);
						sender.sendMessage(ChatColor.GOLD + "Capture area " + area.getId() + " is now uncaptureable");
					} else {
						EntityType team = getTeamByName(args[1]);
						if (team == null || !teamsToPlayers.containsKey(team)) {
							sender.sendMessage(ChatColor.RED + "Invalid team or team already eliminated.");
						} else {
							captureArea(area, team);
							sender.sendMessage(ChatColor.GOLD + "Capture area " + area.getId() + " is now owned by team " + team.name().toLowerCase());
						}
					}
				}
			} else if (args.length == 1) {
				if (sender instanceof Player) {
					if (args[0].equalsIgnoreCase("nobody")) {
						CapturedArea area = new CapturedArea(((Player)sender).getLocation());
						captureArea(area, null);
						sender.sendMessage(ChatColor.GOLD + "Capture area " + area.getId() + " is now owned by nobody");
					} else if (args[0].equalsIgnoreCase("dead")) {
						CapturedArea area = new CapturedArea(((Player)sender).getLocation());
						setAreaUncapturable(area);
						sender.sendMessage(ChatColor.GOLD + "Capture area " + area.getId() + " is now uncaptureable");
					} else {
						EntityType team = getTeamByName(args[0]);
						if (team == null || !teamsToPlayers.containsKey(team)) {
							sender.sendMessage(ChatColor.RED + "Invalid team or team already eliminated.");
						} else {
							CapturedArea area = new CapturedArea(((Player)sender).getLocation());
							captureArea(area, team);
							sender.sendMessage(ChatColor.GOLD + "Capture area " + area.getId() + " is now owned by team " + team.name().toLowerCase());
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You must do this as a player, or specify the capture id.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Usage: /capture [id] <team>");
			}
		} else if (commandName.equals("captureid")) {
			if (sender instanceof Player) {
				CapturedArea area = new CapturedArea(((Player)sender).getLocation());
				sender.sendMessage(ChatColor.GOLD + "Capture id: " + area.getId());
			} else {
				sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
			}
		} else if (commandName.equals("eliminate")) {
			if (args.length == 1) {
				EntityType team = getTeamByName(args[0]);
				if (team == null || !teamsToPlayers.containsKey(team)) {
					sender.sendMessage(ChatColor.RED + "Invalid team or team already eliminated.");
				} else {
					eliminateTeam(team, null);
					sender.sendMessage(ChatColor.GOLD + "Team " + team.name().toLowerCase() + " has been eliminated.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Usage: /eliminate <team>");
			}
		} else if (commandName.equals("stopgame")) {
			if (gameStarted) {
				stopGame();
				sender.sendMessage(ChatColor.GOLD + "The game is now stopped.");
			} else {
				sender.sendMessage(ChatColor.RED + "The game has not started.");
			}
		} else if (commandName.equals("reloadgame")) {
			onDisable();
			onEnable();
			sender.sendMessage(ChatColor.GOLD + "The game and config have been reloaded.");
		} else if (commandName.equals("gametimings")) {
			sender.sendMessage(ChatColor.GOLD + "TIMINGS:");
			if (captureIterations > 0) {
				sender.sendMessage(ChatColor.GOLD + "  Capture phase 1: " + (captureTime1/captureIterations) + " (" + captureTime1 + "/" + captureIterations + ")");
				sender.sendMessage(ChatColor.GOLD + "  Capture phase 2: " + (captureTime2/captureIterations) + " (" + captureTime2 + "/" + captureIterations + ")");
			}
			if (buffIterations > 0) {
				sender.sendMessage(ChatColor.GOLD + "  Buff processing: " + (buffTime/buffIterations) + " (" + buffTime + "/" + buffIterations + ")");
			}
			if (spawnIterations > 0) {
				sender.sendMessage(ChatColor.GOLD + "  Spawn calculation: " + (spawnTime/spawnIterations) + " (" + spawnTime + "/" + spawnIterations + ")");
			}
			sender.sendMessage(ChatColor.GOLD + "Current block update queue: " + blockQueue.size());
		} else {
			sender.sendMessage("HUH?");
		}
		return true;
	}
	
	private void saveSpawnPoints(CommandSender sender) {
		try {
			File file = new File(getDataFolder(), "spawnpoints.txt");
			if (file.exists()) file.delete();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (List<Location> locactions : capturedAreaSpawns.values()) {
				for (Location loc : locactions) {
					writer.write(loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw());
					writer.newLine();
				}
			}
			writer.close();
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "ERROR SAVING SPAWN POINTS FILE");
			e.printStackTrace();
		}
	}
	
	private EntityType getTeamByName(String name) {
		return name.equalsIgnoreCase("cat") ? EntityType.OCELOT : EntityType.fromName(name);
	}
	
	public void startGame() {
		// initial captures
		for (EntityType team : teamDefaultSpawns.keySet()) {
			CapturedArea area = teamDefaultSpawns.get(team);
			captureArea(area, team);
		}
		
		// choose barn
		//chooseRandomHotZone();
		
		// get players
		List<Player> players = Arrays.asList(getServer().getOnlinePlayers());
		Collections.shuffle(players);
		float playersPerTeam = players.size() / 8F;
		
		// split players
		int i = 0;
		for (; i < playersPerTeam * 2; i++) {
			putPlayerInTeam(players.get(i), EntityType.CHICKEN);
		}
		for (; i < playersPerTeam * 3; i++) {
			putPlayerInTeam(players.get(i), EntityType.COW);
		}
		for (; i < playersPerTeam * 4; i++) {
			putPlayerInTeam(players.get(i), EntityType.OCELOT);
		}
		for (; i < playersPerTeam * 5; i++) {
			putPlayerInTeam(players.get(i), EntityType.PIG);
		}
		for (; i < playersPerTeam * 6; i++) {
			putPlayerInTeam(players.get(i), EntityType.SHEEP);
		}
		for (; i < playersPerTeam * 7; i++) {
			putPlayerInTeam(players.get(i), EntityType.SQUID);
		}
		for (; i < players.size(); i++) {
			putPlayerInTeam(players.get(i), EntityType.WOLF);
		}
		
		// choose pig captain
		choosePigCaptain();
		
		// start buff applicator
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				long start = System.currentTimeMillis();
				for (Player player : Bukkit.getOnlinePlayers()) {
					EntityType team = playersToTeams.get(player.getName().toLowerCase());
					if (team != null) {
						if (team == EntityType.COW) {
							player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, buffInterval + 10, -10), true);
						} else if (team == EntityType.WOLF) {
							long time = player.getWorld().getTime();
							if (time > 13000 && time < 23000) {
								player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, buffInterval + 10, 0), true);
								player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, buffInterval + 10, 1), true);
							}
						} else if (team == EntityType.SQUID) {
							Material type = player.getLocation().getBlock().getType();
							if (type == Material.WATER || type == Material.STATIONARY_WATER) {
								player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, buffInterval + 10, 2), true);
							} else {
								player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, buffInterval + 10, 1), true);
							}
						} else if (team == EntityType.PIG) {
							if (player.getLocation().getBlock().getType() == Material.SOUL_SAND) {
								player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, buffInterval + 10, 2), true);
							}
						}
						EntityType areaTeam = capturedAreasToTeams.get(new CapturedArea(player.getLocation()));
						if (areaTeam != null) {
							if (areaTeam == team) {
								player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, buffInterval + 10, 0), true);
							}
							if (areaTeam == EntityType.COW) {
								player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, buffInterval + 10, -10), true);
							}
							if (foodIntervalCounter++ % foodIntervalBasedOnBuffInterval == 0 && areaTeam == team && player.getFoodLevel() < 20) {
								player.setFoodLevel(player.getFoodLevel() + (team == EntityType.COW ? 10 : 6));
							}
						}
					}
				}
				buffTime += (System.currentTimeMillis() - start);
				buffIterations++;
			}
		}, 10, buffInterval);
		
		// start capture monitor
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				calculateCaptures();
			}
		}, captureInterval * 2, captureInterval);
		
		// elimination timer
		eliminator = new Eliminator(this);

		// send enderdragon
		if (volatileCode != null) {
			volatileCode.sendEnderDragonToAllPlayers();
		}
		
		// initialize scores
		if (volatileCode != null) {
			volatileCode.updateScoreboard(getScores(), teamNames);
		}
		
		gameStarted = true;
	}
	
	/*public void chooseRandomHotZone() {
		List<CapturedArea> areas = new ArrayList<CapturedArea>(capturedAreaValues.keySet());
		if (barnArea != null) areas.remove(barnArea);
		if (areas.size() > 0) {
			CapturedArea area = areas.get(random.nextInt(areas.size()));
			setHotZone(area);
			getServer().broadcastMessage(ChatColor.GOLD + capturedAreaNames.get(area) + " is the new hot zone!");
		} else {
			setHotZone(null);
		}
	}
	
	private BlockFace[] faces = new BlockFace[] { BlockFace.SELF, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };
	public void setHotZone(CapturedArea area) {
		if (barnArea != null) {
			Location loc = barnArea.getHighCenter(world);
			if (loc.getBlockY() < fieldY + 20) {
				loc.setY(loc.getY() + 10);
			}
			Block b = loc.getBlock();
			for (BlockFace face : faces) {
				b.getRelative(face).setType(Material.AIR);
			}
			b.getRelative(BlockFace.UP).setType(Material.AIR);
		}
		if (area != null) {
			Location loc = area.getHighCenter(world);
			if (loc.getBlockY() < fieldY + 20) {
				loc.setY(loc.getY() + 10);
			}
			Block b = loc.getBlock();
			for (BlockFace face : faces) {
				b.getRelative(face).setType(Material.DIAMOND_BLOCK);
			}
			b.getRelative(BlockFace.UP).setType(Material.BEACON);
		}
		barnArea = area;
	}*/
	
	public void putPlayerInTeam(Player player, EntityType team) {
		putPlayerInTeam(player, team, true);
	}
	
	public void putPlayerInTeam(Player player, EntityType team, boolean teleportAndRunSpawnCommands) {
		// remove old team
		EntityType oldTeam = playersToTeams.remove(player.getName().toLowerCase());
		if (oldTeam != null && teamsToPlayers.containsKey(oldTeam)) {
			teamsToPlayers.get(oldTeam).remove(player.getName().toLowerCase());
		}
		
		// add to collections
		teamsToPlayers.get(team).add(player.getName().toLowerCase());
		playersToTeams.put(player.getName().toLowerCase(), team);

		runInitCommands(player, team);
		
		if (teleportAndRunSpawnCommands) {
			// teleport player
			Location loc = getSpawnLocation(team);
			player.teleport(loc);
		
			// run commands
			runSpawnCommands(player, team);
		}
		
		player.sendMessage(ChatColor.GOLD + "You are on team " + teamNames.get(team) + ChatColor.GOLD + ".");
	}
	
	public void putPlayerInRandomTeam(Player player) {
		EntityType[] teams = teamsToPlayers.keySet().toArray(new EntityType[]{});
		int r = random.nextInt(teams.length);
		putPlayerInTeam(player, teams[r]);
	}
	
	public void choosePigCaptain() {
		Set<String> pigs = teamsToPlayers.get(EntityType.PIG);
		if (pigs != null && pigs.size() > 0) {
			String oldPigCaptain = pigCaptain != null ? pigCaptain : "";
			pigCaptain = null;
			int attempts = 0;
			while (pigCaptain == null && attempts < 10) {
				pigCaptain = pigs.toArray(new String[pigs.size()])[random.nextInt(pigs.size())];
				Player player = Bukkit.getPlayerExact(pigCaptain);
				if (player != null && player.isOnline() && !player.isDead() && !player.getName().equalsIgnoreCase(oldPigCaptain)) {
					for (String comm : pigCaptainCommands) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comm.replace("%p", pigCaptain));
					}
					player.sendMessage(ChatColor.GOLD + "You are the pig captain!");
					System.out.println("THE PIG CAPTAIN IS " + player.getName());
				} else {
					pigCaptain = null;
					attempts++;
				}
			}
			if (!oldPigCaptain.isEmpty()) {
				Player player = Bukkit.getPlayerExact(oldPigCaptain);
				if (player != null && player.isOnline() && !player.isDead()) {
					putPlayerInTeam(player, EntityType.PIG);
				}
			}
		}
	}
	
	public void runInitCommands(Player player, EntityType team) {
		String[] commands = initCommands.get(team);
		if (commands != null) {
			for (String command : commands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%p", player.getName()));
			}
		}
	}
	
	public void runSpawnCommands(Player player, EntityType team) {
		String[] commands = spawnCommands.get(team);
		if (commands != null) {
			for (String command : commands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%p", player.getName()));
			}
		}
	}
	
	public Location getSpawnLocation(EntityType team) {
		long start = System.currentTimeMillis();
		spawnIterations++;
		if (team == EntityType.WOLF) {
			CapturedArea area = teamDefaultSpawns.get(EntityType.WOLF);
			List<Location> locs = capturedAreaSpawns.get(area);
			if (locs != null) {
				spawnTime += (System.currentTimeMillis() - start);
				return locs.get(random.nextInt(locs.size()));
			} else {
				spawnTime += (System.currentTimeMillis() - start);
				return area.getCenter(world, fieldY);
			}
		}
		if (team == EntityType.PIG && pigCaptain != null) {
			Player captain = Bukkit.getPlayerExact(pigCaptain);
			if (captain != null) {
				CapturedArea area = new CapturedArea(captain.getLocation());
				List<Location> locs = capturedAreaSpawns.get(area);
				if (locs != null) {
					spawnTime += (System.currentTimeMillis() - start);
					return locs.get(random.nextInt(locs.size()));
				} else {
					spawnTime += (System.currentTimeMillis() - start);
					return captain.getLocation().add(0, 1, 0);
				}
			}
		}
		if (team != null) {
			Set<CapturedArea> areas = teamsToCapturedAreas.get(team);
			if (areas != null && areas.size() > 0) {
				CapturedArea area = areas.toArray(new CapturedArea[areas.size()])[random.nextInt(areas.size())];
				List<Location> locs = capturedAreaSpawns.get(area);
				if (locs != null) {
					spawnTime += (System.currentTimeMillis() - start);
					return locs.get(random.nextInt(locs.size()));
				} else {
					spawnTime += (System.currentTimeMillis() - start);
					return area.getCenter(world, fieldY);
				}
			} else {
				CapturedArea area = teamDefaultSpawns.get(team);
				if (area != null) {
					List<Location> locs = capturedAreaSpawns.get(area);
					if (locs != null) {
						spawnTime += (System.currentTimeMillis() - start);
						return locs.get(random.nextInt(locs.size()));
					} else {
						spawnTime += (System.currentTimeMillis() - start);
						return area.getCenter(world, fieldY);
					}
				}
			}
		}
		spawnTime += (System.currentTimeMillis() - start);
		return world.getSpawnLocation();
	}
	
	public void calculateCaptures() {
		final Map<CapturedArea, Map<EntityType, Integer>> counts = new HashMap<CapturedArea, Map<EntityType,Integer>>();
		final Map<CapturedArea, List<Player>> playersInAreas = new HashMap<CapturedArea, List<Player>>();
		
		// count em up
		long start = System.currentTimeMillis();
		for (Player player : Bukkit.getOnlinePlayers()) {
			EntityType team = playersToTeams.get(player.getName().toLowerCase());
			if (team == null) continue;
			CapturedArea area = new CapturedArea(player.getLocation());
			if (!capturedAreaValues.containsKey(area)) continue;
			if (!area.inCaptureArea(player.getLocation())) continue;
			Map<EntityType, Integer> areaCounts = counts.get(area);
			if (areaCounts == null) {
				areaCounts = new HashMap<EntityType, Integer>();
				areaCounts.put(team, team == EntityType.COW ? 2 : 1);
				counts.put(area, areaCounts);
			} else if (areaCounts.containsKey(team)) {
				int count = areaCounts.get(team);
				areaCounts.put(team, count + (team == EntityType.COW ? 2 : 1));
			} else {
				areaCounts.put(team, 1);
			}
			List<Player> players = playersInAreas.get(area);
			if (players == null) {
				players = new ArrayList<Player>();
				playersInAreas.put(area, players);
			}
			players.add(player);
		}
		captureTime1 += (System.currentTimeMillis() - start);
		captureIterations++;
		
		// check for captures
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				long start = System.currentTimeMillis();
				Iterator<Map.Entry<CapturedArea, Map<EntityType, Integer>>> iter = counts.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<CapturedArea, Map<EntityType, Integer>> entry = iter.next();
					Map<EntityType, Integer> areaCounts = entry.getValue();
					EntityType highestTeam = null;
					int highestCount = 0;
					int total = 0;
					for (EntityType team : areaCounts.keySet()) {
						int c = areaCounts.get(team);
						total += c;
						if (c > highestCount) {
							highestTeam = team;
							highestCount = c;
						}
					}
					if ((float)highestCount / (float)total > captureRatio) {
						boolean capped = captureArea(entry.getKey(), highestTeam);
						if (capped) {
							for (Player player : playersInAreas.get(entry.getKey())) {
								player.playSound(player.getLocation(), Sound.NOTE_PLING, 2.0F, 2.0F);
							}
						}
					} else {
						captureArea(entry.getKey(), null);
					}
				}
				captureTime2 += (System.currentTimeMillis() - start);
			}
		}, 7);
		
		// do scoreboard
		if (volatileCode != null) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					volatileCode.updateScoreboard(getScores(), teamNames);
				}
			}, 14);
		}
		
		// do game timer
		if (volatileCode != null) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					int health = 200 - (int) (eliminator.getPercentElapsedTime() * 200);
					volatileCode.setDragonHealth(health);
				}
			}, 21);
		}
	}
	
	public boolean captureArea(CapturedArea area, EntityType team) {
		if (team != null && !teamsToCapturedAreas.containsKey(team)) return false;
		// get area name
		String areaName = capturedAreaNames.get(area);
		if (areaName == null) areaName = "a zone";
		// remove previous team owner
		EntityType previousTeam = capturedAreasToTeams.get(area);
		if (previousTeam != null) {
			if (previousTeam == team) return false;
			teamsToCapturedAreas.get(previousTeam).remove(area);
			sendMessageToTeam(previousTeam, ChatColor.GOLD + "Your team has LOST " + areaName + "!");
		} else if (team == null) return false;
		// set captured
		if (team != null) {
			teamsToCapturedAreas.get(team).add(area);
			capturedAreasToTeams.put(area, team);
			sendMessageToTeam(team, ChatColor.GOLD + "Your team has CAPTURED " + areaName + "!");
		} else {
			capturedAreasToTeams.remove(area);
		}
		// change ground
		if (team == null) {
			area.setAreaType(world, uncapturedType1, uncapturedData1, fieldY);
		} else if (team == EntityType.CHICKEN) {
			area.setAreaType(world, chickenType1, chickenData1, chickenType2, chickenData2, fieldY);
		} else if (team == EntityType.COW) {
			area.setAreaType(world, cowType1, cowData1, cowType2, cowData2, fieldY);
		} else if (team == EntityType.OCELOT) {
			area.setAreaType(world, ocelotType1, ocelotData1, ocelotType2, ocelotData2, fieldY);
		} else if (team == EntityType.PIG) {
			area.setAreaType(world, pigType1, pigData1, pigType2, pigData2, fieldY);
		} else if (team == EntityType.SHEEP) {
			area.setAreaType(world, sheepType1, sheepData1, sheepType2, sheepData2, fieldY);
		} else if (team == EntityType.SQUID) {
			area.setAreaType(world, squidType1, squidData1, squidType2, squidData2, fieldY);
		} else if (team == EntityType.WOLF) {
			area.setAreaType(world, wolfType1, wolfData1, wolfType2, wolfData2, fieldY);
		}
		// launch firework
		if (team != null) {
			int color = 0x9900FF;
			if (teamColors.containsKey(team)) color = teamColors.get(team);
			Firework firework = world.spawn(area.getHighCenter(world), Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.setPower(1);
			meta.addEffect(FireworkEffect.builder().with(Type.BALL_LARGE).withColor(Color.fromRGB(color)).build());
			meta.addEffect(FireworkEffect.builder().with(Type.BURST).withColor(Color.fromRGB(color)).withFlicker().build());
			firework.setFireworkMeta(meta);
		}
		return true;
	}
	
	public void setAreaUncapturable(CapturedArea area) {
		EntityType previousTeam = capturedAreasToTeams.remove(area);
		if (previousTeam != null) {
			teamsToCapturedAreas.get(previousTeam).remove(area);
		}
		capturedAreaValues.remove(area);
		area.setAreaType(world, deadType1, deadData1, fieldY);
	}
	
	public void calculateScoresAndDoElimination() {
		TreeSet<TeamScore> scores = getScores();
		EntityType winning = scores.last().getTeam();
		for (TeamScore score : scores) {
			EntityType team = score.getTeam();
			//if (team == EntityType.OCELOT && barnArea != null && teamsToCapturedAreas.get(team).contains(barnArea)) {
			//	continue;
			//} else {
				eliminateTeam(team, winning);
				Bukkit.broadcastMessage(ChatColor.GOLD + "TEAM " + teamNames.get(team).toUpperCase() + ChatColor.GOLD + " HAS BEEN ELIMINATED!");
				break;
			//}
		}
		//teamKills.clear();
	}
	
	public TreeSet<TeamScore> getScores() {
		TreeSet<TeamScore> scores = new TreeSet<TeamScore>();
		
		for (EntityType team : teamsToCapturedAreas.keySet()) {
			int areaScore = 0;
			float killScore = 0;
			Set<CapturedArea> areas = teamsToCapturedAreas.get(team);
			for (CapturedArea area : areas) {
				if (capturedAreaValues.containsKey(area)) {
					areaScore += capturedAreaValues.get(area);
				}
			}
			if (team == EntityType.OCELOT && barnArea != null && teamsToCapturedAreas.get(team).contains(barnArea)) {
				areaScore *= 2;
			}
			if (teamKills.containsKey(team)) {
				killScore = teamKills.get(team).floatValue() * pointsPerKill;
			}
			scores.add(new TeamScore(team, areaScore, killScore));
		}
		
		return scores;
	}
	
	public void eliminateTeam(EntityType team, EntityType winning) {
		if (winning == team) winning = null;
		
		// remove captured areas
		Set<CapturedArea> areas = teamsToCapturedAreas.remove(team);
		for (CapturedArea area : areas) {
			//area.setAreaType(world, uncapturedType1, uncapturedData1, fieldY);
			capturedAreasToTeams.remove(area);
			setAreaUncapturable(area);
		}
		
		// move players to other teams
		Set<String> playersInTeam = teamsToPlayers.remove(team);
		List<EntityType> teams = new ArrayList<EntityType>(teamsToPlayers.keySet());
		if (winning != null && teams.size() > 1) teams.remove(winning); // don't move to winning team
		if (teams.contains(EntityType.CHICKEN)) teams.add(EntityType.CHICKEN); // chickens get double players
		for (String playerName : playersInTeam) {
			Player player = Bukkit.getPlayerExact(playerName);
			if (player == null) {
				playersToTeams.remove(playerName);
			} else {
				putPlayerInTeam(player, teams.get(random.nextInt(teams.size())), false);
				player.damage(50);
			}
		}
		
		// remove score
		if (volatileCode != null) {
			volatileCode.zeroScore(team, teamNames);
		}
	}
	
	public void sendMessageToTeam(EntityType team, String message) {
		Set<String> names = teamsToPlayers.get(team);
		if (names != null) {
			for (String name : names) {
				Player player = Bukkit.getPlayerExact(name);
				if (player != null && player.isOnline()) {
					player.sendMessage(message);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (gameStarted) {
			final Player player = event.getPlayer();
			EntityType team = playersToTeams.get(player.getName().toLowerCase());
			if (team == null || !teamsToPlayers.containsKey(team)) {
				putPlayerInRandomTeam(player);
				if (volatileCode != null) {
					volatileCode.sendEnderDragonToPlayer(player);
				}
			} else {
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						player.setHealth(0);
					}
				});
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (gameStarted) {
			final Player player = event.getPlayer();
			final EntityType team = playersToTeams.get(player.getName().toLowerCase());
			
			// set spawn location
			if (team != null) {
				event.setRespawnLocation(getSpawnLocation(team));
			}
						
			// run commands (and get new team if necessary)
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					if (team == null || !teamsToPlayers.containsKey(team)) {
						putPlayerInRandomTeam(player);
					} else {
						runSpawnCommands(player, team);
						player.sendMessage(ChatColor.GOLD + "You are on team " + team.name().toLowerCase());
					}
					if (volatileCode != null) {
						volatileCode.sendEnderDragonToPlayer(player);
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.getDrops().clear();
		event.setDroppedExp(0);
		event.setDeathMessage(null);
		
		// add kill
		Player killer = event.getEntity().getKiller();
		if (killer != null) {
			EntityType team = playersToTeams.get(killer.getName().toLowerCase());
			if (team != null) {
				int kills = 1;
				if (teamKills.containsKey(team)) {
					kills = teamKills.get(team) + 1;
				}
				teamKills.put(team, kills);
			}
		}
		
		// choose new pig captain
		if (event.getEntity().getName().equalsIgnoreCase(pigCaptain)) {
			choosePigCaptain();
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (event.getPlayer().getName().equalsIgnoreCase(pigCaptain)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					choosePigCaptain();
				}
			});
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().isOp()) return;
		event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onExplode(EntityExplodeEvent event) {
		Iterator<Block> iter = event.blockList().iterator();
		while (iter.hasNext()) {
			if (iter.next().getY() <= fieldY) {
				iter.remove();
			}
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		if (event.getPlayer().isOp()) return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPickupItem(PlayerPickupItemEvent event) {
		if (event.getPlayer().isOp()) return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockFade(BlockFadeEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event) {
		event.setCancelled(true);
	}
	
	public void stopGame() {
		if (gameStarted) {
			eliminator.stop();
			gameStarted = false;
		}
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	@Override
	public void onDisable() {
		stopGame();
		HandlerList.unregisterAll((Plugin)this);
	}
	
}
