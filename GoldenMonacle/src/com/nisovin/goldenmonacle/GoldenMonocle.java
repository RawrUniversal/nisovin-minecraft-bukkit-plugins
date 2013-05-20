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
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nisovin.brucesgym.BrucesGym;
import com.nisovin.brucesgym.GymGameMode;
import com.nisovin.brucesgym.StatisticType;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.util.Util;

public class GoldenMonocle extends JavaPlugin implements Listener {

	Random random = new Random();
	
	List<Location> spawnPoints;
	List<Location> dropPoints;
	Location deadSpawn;
	int spawnDelay = 10*20;
	Spell spawnSpell = null;
	
	int pointsGainedPerKill = 1;
	int pointsLostPerDeath = 8;
	int pointsStolenPerKill = 8;
	
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
	Map<String, Location> deathLocations;
	
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
		pointsGainedPerKill = config.getInt("points-gained-per-kill", pointsGainedPerKill);
		pointsStolenPerKill = config.getInt("points-stolen-per-kill", pointsStolenPerKill);
		pointsLostPerDeath = config.getInt("points-lost-per-death", pointsLostPerDeath);
		String spawnSpellName = config.getString("spawn-spell", "");
		if (spawnSpellName != null && !spawnSpellName.isEmpty()) {
			spawnSpell = MagicSpells.getSpellByInternalName(spawnSpellName);
		}
		
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
		deathLocations = new HashMap<String, Location>();
		
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
			Class.forName("net.minecraft.server.v1_5_R3.MinecraftServer");
			volatileCode = new VolatileCode();
		} catch (ClassNotFoundException e) {
			getLogger().warning("GoldenMonocle needs an update! Dragon timer is disabled.");
			volatileCode = null;
		}
		
		// register stats
		BrucesGym.initializeGameMode(GymGameMode.GOLDEN_MONOCLE);
		BrucesGym.registerStatistic("global_xp", StatisticType.XP, GymGameMode.GLOBAL);
		BrucesGym.registerStatistic("gm_xp", StatisticType.XP);
		BrucesGym.registerStatistic("gm_kills", StatisticType.TOTAL);
		BrucesGym.registerStatistic("gm_deaths", StatisticType.TOTAL);
		BrucesGym.registerStatistic("gm_first_place", StatisticType.TOTAL);
		BrucesGym.registerStatistic("gm_second_place", StatisticType.TOTAL);
		BrucesGym.registerStatistic("gm_third_place", StatisticType.TOTAL);
		BrucesGym.registerStatistic("gm_top_ten", StatisticType.TOTAL);
		BrucesGym.registerStatistic("gm_highest_score", StatisticType.MAX);
		
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
			if (p.hasPermission("monocle.ignore")) continue;
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
		if (gameStarted) {
			gameStarted = false;
			
			giveEndOfGamePoints();
			
			Bukkit.broadcastMessage(ChatColor.GOLD + "GAME OVER!");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.hasPermission("monocle.ignore")) continue;
				player.getInventory().clear();
				if (player.isValid()) {
					for (int i = 0; i < 3; i++) {
						player.getInventory().addItem(generateRandomFirework());
					}
				}
			}
			
			Bukkit.getScheduler().cancelTasks(this);
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					System.out.println("Golden Monocle End: asdf8907sdfbn3lkasdf83");
				}
			}, 200);
			
		}
	}
	
	public ItemStack generateRandomFirework() {
		ItemStack item = new ItemStack(Material.FIREWORK);
		FireworkMeta meta = (FireworkMeta)item.getItemMeta();
		FireworkEffect.Type[] types = FireworkEffect.Type.values();
		meta.addEffect(FireworkEffect.builder()
			.with(types[random.nextInt(types.length)])
			.flicker(random.nextInt(2) == 1)
			.trail(random.nextInt(2) == 1)
			.withColor(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
			.withFade(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
			.build());
		meta.setPower(random.nextInt(3) + 1);
		item.setItemMeta(meta);
		return item;
	}
	
	public void giveEndOfGamePoints() {
		Player[] players = Bukkit.getOnlinePlayers();
				
		// score points
		int place = 1;
		TreeSet<PlayerScore> scores = scoreboardHandler.getScores();
		for (PlayerScore score : scores) {
			// regular score
			int s = 1000 + score.score * 25;
			// top scorers
			if (players.length > 40) {
				if (place == 1) {
					s += 9000;
					BrucesGym.updateStatistic(score.playerName, "gm_first_place", 1);
				} else if (place == 2) {
					s += 3000;
					BrucesGym.updateStatistic(score.playerName, "gm_second_place", 1);
				} else if (place == 3) {
					s += 2000;
					BrucesGym.updateStatistic(score.playerName, "gm_third_place", 1);
				} else if (place >= 10) {
					s += 1000;
					BrucesGym.updateStatistic(score.playerName, "gm_top_ten", 1);
				}
			}
			place++;
			// update stats
			BrucesGym.updateStatistic(score.playerName, "global_xp", s);
			BrucesGym.updateStatistic(score.playerName, "gm_xp", s);
			BrucesGym.updateStatistic(score.playerName, "gm_highest_score", s);
		}
	}
	
	public void respawnPlayer(final Player player) {
		player.sendMessage(ChatColor.GOLD + "You are " + ChatColor.RED + "dead" + ChatColor.GOLD + ". Please wait to respawn.");
		deadPlayers.add(player.getName());
		player.getInventory().clear();
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5000, 0));
		//player.teleport(deadSpawn);
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
		player.setFireTicks(0);
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		if (spawnSpell != null) {
			spawnSpell.cast(player);
		}
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
		inv.setItem(8, new ItemStack(Material.COMPASS, 1));
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
			if (player.hasPermission("monocle.ignore")) return;
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
		
		Player killed = event.getEntity();
		if (gameStarted && !deathLocations.containsKey(killed.getName()) && !deadPlayers.contains(killed.getName())) {
			if (killed.hasPermission("monocle.ignore")) return;
			
			Player killer = killed.getKiller();
			if (killer == null) {
				String lastDamager = lastDamagers.get(killed.getName());
				if (lastDamager != null) {
					killer = Bukkit.getPlayerExact(lastDamager);
				}
			}
			if (killer != null && killer.hasPermission("monocle.ignore")) return;
			
			// give scores
			if (killer != null) {
				int killedScore = scoreboardHandler.getScore(killed);
				int killerScore = scoreboardHandler.getScore(killer);
				if (killerScore <= killedScore) {
					int stolen = pointsStolenPerKill;
					if (pointsStolenPerKill > killedScore) stolen = killedScore;
					scoreboardHandler.modifyScore(killed, -stolen);
					scoreboardHandler.modifyScore(killer, stolen);
				}
				scoreboardHandler.modifyScore(killer, pointsGainedPerKill);
			}
			scoreboardHandler.modifyScore(killed, -pointsLostPerDeath);
			
			// add stats
			BrucesGym.updateStatistic(killed.getName(), "gm_deaths", 1);
			if (killer != null) {
				BrucesGym.updateStatistic(killer.getName(), "gm_kills", 1);
				BrucesGym.addKill(killer.getName(), killed.getName(), getWeaponName(killer.getItemInHand()));
			}
			
			// save death location
			deathLocations.put(killed.getName(), killer != null ? killer.getLocation() : killed.getLocation());
		}
	}
	
	private String getWeaponName(ItemStack item) {
		if (item != null) {
			if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
				return ChatColor.stripColor(item.getItemMeta().getDisplayName());
			}
		}
		return "";
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		scoreboardHandler.modifyScore(event.getPlayer(), -pointsLostPerDeath - pointsStolenPerKill);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!gameStarted) {
			event.setCancelled(true);
			return;
		}
		Entity damaged = event.getEntity();
		if (damaged.getType() == EntityType.PLAYER && event.getDamager().getType() == EntityType.PLAYER) {
			Player pDamaged = (Player)damaged;
			Player pDamager = (Player)event.getDamager();
			if (deadPlayers.contains(pDamaged.getName()) || deadPlayers.contains(pDamager.getName())) {
				event.setCancelled(true);
			} else if (pDamaged.hasPermission("monocle.ignore") || pDamager.hasPermission("monocle.ignore")) {
				event.setCancelled(true);
			} else {
				lastDamagers.put(((Player)damaged).getName(), ((Player)event.getDamager()).getName());
			}
		} else if (damaged.getType() == EntityType.ENDER_CRYSTAL) {
			event.setCancelled(true);
			if (event.getDamager().getType() == EntityType.PLAYER) {
				Player player = (Player)event.getDamager();
				if (player.hasPermission("monocle.ignore")) {
					event.setCancelled(true);
					return;
				}
				if (deadPlayers.contains(player.getName())) {
					return;
				}
				event.getEntity().remove();
				player.getInventory().addItem(dropItems.get(random.nextInt(dropItems.size())));
				dropsSpawned--;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (!gameStarted || (event.getEntity() instanceof Player && deadPlayers.contains(((Player)event.getEntity()).getName()))) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onSpellTarget(SpellTargetEvent event) {
		if (event.getTarget() instanceof Player && !event.getTarget().equals(event.getCaster())) {
			lastDamagers.put(((Player)event.getTarget()).getName(), event.getCaster().getName());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (gameStarted) {
			final Player player = event.getPlayer();
			if (player.hasPermission("monocle.ignore")) return;
			Location loc = deathLocations.remove(player.getName());
			if (loc != null) {
				event.setRespawnLocation(loc);
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					respawnPlayer(player);
					if (volatileCode != null) {
						volatileCode.sendEnderDragonToPlayer(player);
					}
				}
			});
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
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent event) {
		event.setFoodLevel(20);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event) {
		if (!event.getPlayer().isOp() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (!gameStarted && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && event.getItem().getType() == Material.FIREWORK) {
				// allowed
			} else {
				event.setCancelled(true);
			}
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
