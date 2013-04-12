package com.nisovin.goldenmonacle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nisovin.magicspells.util.Util;

public class GoldenMonocle extends JavaPlugin implements Listener {

	//   start and end time
	// X control spawn points
	// X give out items
	// X top 10 scoreboard
	// X prevent drops, block break, block place
	// X special drop spawn points

	Random random = new Random();
	
	List<Location> spawnPoints;
	List<Location> dropPoints;
	Location deadSpawn;
	int spawnDelay = 10*20;
	
	List<ItemStack> weaponItems;
	List<ItemStack> healItems;
	int weaponItemCount = 2;
	int healItemCount = 1;
	
	List<ItemStack> dropItems;
	int dropItemInterval = 30*20;
	int dropsSpawned = 0;
	int maxDropCount = 3;
	
	int autoStartTime = 0;
	int gameDuration = 0;
	
	boolean gameStarted = false;
	long gameStartedTime = 0;
	
	ScoreboardHandler scoreboardHandler;
	VolatileCode volatileCode;
	
	Map<String, String> lastDamagers;
	Set<String> deadPlayers;
	
	@Override
	public void onEnable() {

		World world = Bukkit.getWorlds().get(0);
		
		getServer().getPluginManager().registerEvents(this, this);
		
		// get config
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
			setEnabled(false);
			return;
		}
		
		// load config values
		spawnDelay = config.getInt("spawn-delay", spawnDelay);
		weaponItemCount = config.getInt("weapon-item-count", weaponItemCount);
		healItemCount = config.getInt("heal-item-count", healItemCount);
		dropItemInterval = config.getInt("drop-item-interval", dropItemInterval);
		maxDropCount = config.getInt("max-drop-count", maxDropCount);
		autoStartTime = config.getInt("auto-start-time", autoStartTime);
		gameDuration = config.getInt("game-duration", gameDuration) * 1000;
		
		// load weapon and healing items
		weaponItems = new ArrayList<ItemStack>();
		List<String> weaponData = config.getStringList("weapon-items");
		for (String weaponStr : weaponData) {
			ItemStack item = Util.getItemStackFromString(weaponStr);
			if (item != null) {
				item.setAmount(1);
				weaponItems.add(item);
			} else {
				getLogger().warning("Invalid weapon item: " + weaponStr);
			}
		}
		healItems = new ArrayList<ItemStack>();
		List<String> healData = config.getStringList("heal-items");
		for (String healStr : healData) {
			ItemStack item = Util.getItemStackFromString(healStr);
			if (item != null) {
				item.setAmount(1);
				healItems.add(item);
			} else {
				getLogger().warning("Invalid heal item: " + healStr);
			}
		}
		
		// load drop items
		dropItems = new ArrayList<ItemStack>();
		List<String> dropData = config.getStringList("drop-items");
		for (String dropStr : dropData) {
			ItemStack item = Util.getItemStackFromString(dropStr);
			if (item != null) {
				item.setAmount(1);
				dropItems.add(item);
			} else {
				getLogger().warning("Invalid drop item: " + dropStr);
			}
		}
		
		// initialize stuff
		scoreboardHandler = new ScoreboardHandler(this);		
		lastDamagers = new HashMap<String, String>();
		deadPlayers = new HashSet<String>();
		
		// load spawn points
		deadSpawn = world.getSpawnLocation();
		spawnPoints = new ArrayList<Location>();
		try {
			File spawnFile = new File(getDataFolder(), "spawnpoints.txt");
			if (spawnFile.exists()) {
				Scanner scanner = new Scanner(spawnFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine().trim();
					if (!line.isEmpty() && !line.startsWith("#")) {
						String[] data = line.split(",");
						Location location = new Location(world, Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Float.parseFloat(data[3]), 0F);
						spawnPoints.add(location);
					}
				}
			}
		} catch (Exception e) {
			getLogger().severe("ERROR READING SPAWN POINTS FILE");
			e.printStackTrace();
		}
		
		// load drop points
		dropPoints = new ArrayList<Location>();
		try {
			File spawnFile = new File(getDataFolder(), "droppoints.txt");
			if (spawnFile.exists()) {
				Scanner scanner = new Scanner(spawnFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine().trim();
					if (!line.isEmpty() && !line.startsWith("#")) {
						String[] data = line.split(",");
						Location location = new Location(world, Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Float.parseFloat(data[3]), 0F);
						dropPoints.add(location);
					}
				}
			}
		} catch (Exception e) {
			getLogger().severe("ERROR READING DROP POINTS FILE");
			e.printStackTrace();
		}
		
		// initialize volatile code
		try {
			Class.forName("net.minecraft.server.v1_5_R2.MinecraftServer");
			volatileCode = new VolatileCode();
		} catch (ClassNotFoundException e) {
			getLogger().warning("GoldenMonocle needs an update! Dragon timer is disabled.");
			volatileCode = null;
		}
		
		// auto start game
		if (autoStartTime > 0) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					startGame();
				}
			}, autoStartTime * 20);
		}
	}
	
	@Override
	public void onDisable() {
		if (volatileCode != null) {
			volatileCode.removeEnderDragonForAllPlayers();
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String commandName = command.getName();
		if (commandName.equals("startgame")) {
			startGame();
			sender.sendMessage("Game started!");
		} else if (commandName.equals("stopgame")) {
			endGame();
		} else if (commandName.equals("addspawnpoint")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				spawnPoints.add(player.getLocation());
				sender.sendMessage(ChatColor.GOLD + "This location has been added as a spawn point.");
				saveSpawnPoints(sender);
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		} else if (commandName.equals("removespawnpoint")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				double distance = 999;
				Location nearestLoc = null;
				for (Location loc : spawnPoints) {
					double d = loc.distance(player.getLocation());
					if (d < distance) {
						distance = d;
						nearestLoc = loc;
					}
				}
				if (distance < 5 && nearestLoc != null) {
					spawnPoints.remove(nearestLoc);
					Location under = nearestLoc.clone().subtract(0, 1, 0);
					player.sendBlockChange(under, under.getBlock().getType(), under.getBlock().getData());
					sender.sendMessage(ChatColor.GOLD + "This spawn point has been removed.");
					saveSpawnPoints(sender);
				} else {
					sender.sendMessage(ChatColor.RED + "There are no spawn points nearby.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		} else if (commandName.equals("viewspawnpoints")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				for (Location loc : spawnPoints) {
					player.sendBlockChange(loc.clone().subtract(0, 1, 0), Material.GLOWSTONE, (byte)0);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
			
		} else if (commandName.equals("adddroppoint")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				dropPoints.add(player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5));
				sender.sendMessage(ChatColor.GOLD + "This location has been added as a drop point.");
				saveDropPoints(sender);
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		} else if (commandName.equals("removedroppoint")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				double distance = 999;
				Location nearestLoc = null;
				for (Location loc : dropPoints) {
					double d = loc.distance(player.getLocation());
					if (d < distance) {
						distance = d;
						nearestLoc = loc;
					}
				}
				if (distance < 5 && nearestLoc != null) {
					dropPoints.remove(nearestLoc);
					player.sendBlockChange(nearestLoc, nearestLoc.getBlock().getType(), nearestLoc.getBlock().getData());
					sender.sendMessage(ChatColor.GOLD + "This drop point has been removed.");
					saveDropPoints(sender);
				} else {
					sender.sendMessage(ChatColor.RED + "There are no drop points nearby.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		} else if (commandName.equals("viewdroppoints")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				for (Location loc : dropPoints) {
					player.sendBlockChange(loc, Material.REDSTONE_BLOCK, (byte)0);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You must do this as a player.");
			}
		}
		return true;
	}
	
	public void startGame() {
		gameStarted = true;
		gameStartedTime = System.currentTimeMillis();
		
		// teleport players
		List<Location> locs = new ArrayList<Location>(spawnPoints);
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);			
			giveRandomItems(p);
			if (locs.size() == 0) {
				locs = new ArrayList<Location>(spawnPoints);
			}
			Location loc = locs.remove(random.nextInt(locs.size()));
			p.teleport(loc);
		}
		
		// set up drop spawn timer
		if (dropPoints.size() > 0) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					if (dropsSpawned < maxDropCount) {
						Location loc = dropPoints.get(random.nextInt(dropPoints.size()));
						loc.getWorld().spawn(loc, EnderCrystal.class);
						dropsSpawned++;
					}
				}
			}, dropItemInterval, dropItemInterval);
		}
		
		// run game timer
		if (gameDuration > 0) {
			if (volatileCode != null) {
				volatileCode.sendEnderDragonToAllPlayers();
			}
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					long elapsed = System.currentTimeMillis() - gameStartedTime;
					double remainingPct = 1.0 - ((double)elapsed / (double)gameDuration);
					if (volatileCode != null) {
						volatileCode.setDragonHealth((int)(remainingPct * 200));
					}
					if (elapsed >= gameDuration) {
						endGame();
					}
				}
			}, 0, 200);
		}
	}
	
	public void endGame() {
		gameStarted = false;
		Bukkit.broadcastMessage(ChatColor.GOLD + "GAME OVER!");
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.getInventory().clear();
		}
		Bukkit.getScheduler().cancelTasks(this);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				System.out.println("Golden Monocle End: asdf8907sdfbn3lkasdf83");
			}
		}, 200);
	}
	
	public void respawnPlayer(final Player player) {
		deadPlayers.add(player.getName());
		player.getInventory().clear();
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5000, 0));
		player.teleport(deadSpawn);
		player.setAllowFlight(true);
		player.setFlying(true);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				spawnPlayer(player);
			}
		}, spawnDelay);
	}
	
	public void spawnPlayer(Player player) {
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		giveRandomItems(player);
		teleportToRandomSpawn(player);
		deadPlayers.remove(player.getName());
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
	}
	
	public void giveRandomItems(Player player) {
		ItemStack item1 = weaponItems.get(random.nextInt(weaponItems.size()));
		ItemStack item2 = weaponItems.get(random.nextInt(weaponItems.size()));
		while (item1.equals(item2)) {
			item2 = weaponItems.get(random.nextInt(weaponItems.size()));
		}
		ItemStack item3 = healItems.get(random.nextInt(healItems.size()));
		PlayerInventory inv = player.getInventory();
		inv.clear();
		inv.addItem(item1);
		inv.addItem(item2);
		inv.addItem(item3);
		inv.setHeldItemSlot(0);
	}
	
	public void teleportToRandomSpawn(Player player) {
		Location spawnPoint = spawnPoints.get(random.nextInt(spawnPoints.size())).clone();
		while (spawnPoint.getBlock().getType() == Material.AIR && spawnPoint.getY() > 1) {
			spawnPoint.subtract(0, 1, 0);
		}
		spawnPoint.add(0, 1.5, 0);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.teleport(spawnPoint);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (gameStarted) {
			final Player player = event.getPlayer();
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					respawnPlayer(player);
				}
			}, 1);
			if (volatileCode != null) {
				volatileCode.sendEnderDragonToPlayer(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		// disallow drops
		event.setDroppedExp(0);
		event.getDrops().clear();
		
		// give scores
		Player killed = event.getEntity();
		Player killer = killed.getKiller();
		if (killer == null) {
			String lastDamager = lastDamagers.get(killed.getName());
			if (lastDamager != null) {
				killer = Bukkit.getPlayerExact(lastDamager);
			}
		}
		int score = scoreboardHandler.getScore(killed);
		scoreboardHandler.setScore(killed, 0);
		if (killer != null) {
			scoreboardHandler.modifyScore(killer, score);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!gameStarted) {
			event.setCancelled(true);
			return;
		}
		Entity damaged = event.getEntity();
		if (damaged.getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
			if (deadPlayers.contains(((Player)damaged).getName()) || deadPlayers.contains(((Player)event.getDamager()).getName())) {
				event.setCancelled(true);
			} else {
				lastDamagers.put(((Player)damaged).getName(), ((Player)event.getDamager()).getName());
			}
		} else if (damaged.getType() == EntityType.ENDER_CRYSTAL) {
			event.setCancelled(true);
			if (event.getDamager().getType() == EntityType.PLAYER) {
				Player player = (Player)event.getDamager();
				if (deadPlayers.contains(player.getName())) {
					return;
				}
				event.getEntity().remove();
				player.getInventory().addItem(dropItems.get(random.nextInt(dropItems.size())));
				dropsSpawned--;
			}
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (gameStarted) {
			final Player player = event.getPlayer();
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					respawnPlayer(player);
					if (volatileCode != null) {
						volatileCode.sendEnderDragonToPlayer(player);
					}
				}
			}, 1);
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		event.setCancelled(true);
		event.getEntity().remove();
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event) {
		if (!event.getPlayer().isOp() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			event.setCancelled(true);
		}
	}
	
	private void saveSpawnPoints(CommandSender sender) {
		try {
			File file = new File(getDataFolder(), "spawnpoints.txt");
			if (file.exists()) file.delete();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Location loc : spawnPoints) {
				writer.write(loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "ERROR SAVING SPAWN POINTS FILE");
			e.printStackTrace();
		}
	}
	
	private void saveDropPoints(CommandSender sender) {
		try {
			File file = new File(getDataFolder(), "droppoints.txt");
			if (file.exists()) file.delete();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Location loc : dropPoints) {
				writer.write(loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "ERROR SAVING DROP POINTS FILE");
			e.printStackTrace();
		}
	}
	
}
