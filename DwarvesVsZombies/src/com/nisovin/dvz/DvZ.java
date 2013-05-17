package com.nisovin.dvz;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.nisovin.magicspells.util.BoundingBox;

public class DvZ extends JavaPlugin implements Listener {

	// TODO: special players
	
	boolean gameStarted = false;
	boolean monstersReleased = false;
	boolean override = false;

	int autoStartTime;
	int monsterReleaseTime;
	int scoreInterval;
	int monsterSpecialInterval;
	int percentMonsters;
	boolean killDwarvesOnJoin;
	int startEndTimerAtPercent;
	int endTimerDuration;
	Spell becomeDwarfSpell;
	Spell becomeMonsterSpell;
	Spell monsterSpecialSpell;
	List<String> startCommands;
	List<String> monsterReleaseCommands;
	
	Location shrineCenter;
	BoundingBox shrine;
	int dwarfValue;
	int monsterValue;
	int shrinePower = 100;
	
	Set<String> dwarves;
	Set<String> monsters;
	
	Scoreboard scoreboard;
	Objective objective;
	
	Score remainingDwarvesScore;
	Score monsterKillsScore;
	Score omwKillsScore;
	
	Map<String, Long> deathTimes;
	
	BukkitTask startTask;
	BukkitTask counterTask;
	BukkitTask endTask;
	BukkitTask monsterSpecialTask;
	
	VolatileCode volatileCode;
	
	@Override
	public void onEnable() {
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
		
		autoStartTime = config.getInt("auto-start-time", 0);
		monsterReleaseTime = config.getInt("monster-release-time", 0);
		scoreInterval = config.getInt("score-interval", 30);
		monsterSpecialInterval = config.getInt("monster-special-interval", 480);
		percentMonsters = config.getInt("percent-monsters", 30);
		killDwarvesOnJoin = config.getBoolean("kill-dwarves-on-join", false);
		startEndTimerAtPercent = config.getInt("start-end-timer-at-percent", 20);
		endTimerDuration = config.getInt("end-timer-duration", 300);
		
		becomeDwarfSpell = MagicSpells.getSpellByInternalName(config.getString("become-dwarf-spell", "become_dwarf"));
		becomeMonsterSpell = MagicSpells.getSpellByInternalName(config.getString("become-monster-spell", "become_monster"));
		monsterSpecialSpell = MagicSpells.getSpellByInternalName(config.getString("monster-special-spell", "monster_special"));
		startCommands = config.getStringList("start-commands");
		monsterReleaseCommands = config.getStringList("monster-release-commands");
		
		int shrineRadius = config.getInt("shrine-radius", 20);
		String[] coords = config.getString("shrine", "0,0,0").split(",");
		shrineCenter = new Location(Bukkit.getWorlds().get(0), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
		shrine = new BoundingBox(shrineCenter, shrineRadius);
		dwarfValue = config.getInt("dwarf-value", 10);
		monsterValue = config.getInt("monster-value", 1);
		
		dwarves = new HashSet<String>();
		monsters = new HashSet<String>();
		
		if (becomeDwarfSpell == null) {
			getLogger().severe("BECOME-DWARF-SPELL IS INVALID!");
			setEnabled(false);
			return;
		} else if (becomeMonsterSpell == null) {
			getLogger().severe("BECOME-MONSTER-SPELL IS INVALID!");
			setEnabled(false);
			return;
		}
		if (monsterSpecialSpell == null) {
			getLogger().warning("MONSTER-SPECIAL-SPELL IS INVALID!");
		}
		
		getServer().getPluginManager().registerEvents(this, this);
				
		// initialize volatile code
		try {
			Class.forName("net.minecraft.server.v1_5_R3.MinecraftServer");
			volatileCode = new VolatileCode(shrineCenter);
		} catch (ClassNotFoundException e) {
			getLogger().warning("DvZ needs an update! Dragon bar is disabled.");
			volatileCode = null;
		}
		
		if (autoStartTime > 0) {
			startTask = Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					startGame();
				}
			}, autoStartTime * 20);
		}
	}
	
	@Override
	public void onDisable() {
		endGame();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("startgame")) {
			if (!gameStarted) {
				if (startTask != null) {
					startTask.cancel();
				}
				startGame();
			}
		} else if (command.getName().equalsIgnoreCase("override")) {
			if (!gameStarted) {
				if (startTask != null) {
					startTask.cancel();
				}
				override = true;
			}
		}
		return true;
	}
	
	public void startGame() {
		gameStarted = true;
		
		// run start commands
		if (startCommands != null) {
			for (String comm : startCommands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comm);
			}
		}
		
		// make everyone a dwarf
		for (Player player : Bukkit.getOnlinePlayers()) {
			becomeDwarfSpell.castSpell(player, SpellCastState.NORMAL, 1.0F, null);
			dwarves.add(player.getName());
		}
		
		// create scoreboard
		initializeScoreboard();
		shrinePower = 100;
		
		// start monster release task
		if (monsterReleaseTime > 0 && monsterReleaseCommands != null && monsterReleaseCommands.size() > 0) {
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					System.out.println("BEGIN MONSTER RELEASE");
					monstersReleased = true;
					
					// run commands
					for (String comm : monsterReleaseCommands) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comm);
					}
					
					// make monsters
					List<Player> players = Arrays.asList(Bukkit.getOnlinePlayers());
					Collections.shuffle(players);
					int monsterCount = Math.round(players.size() * (percentMonsters / 100F));
					if (override) monsterCount = 0;
					System.out.println("  Initial monster count: " + monsterCount);
					if (monsterCount > 0) {
						// adjust for already existing monsters
						monsterCount -= monsters.size();
						System.out.println("  Adjusted monster count: " + monsterCount);
						// make some new monsters
						if (monsterCount > 0) {
							int c = 0;
							for (int i = 0; i < players.size(); i++) {
								Player player = players.get(i);
								if (dwarves.contains(player.getName())) {
									System.out.println("  Player " + player.getName() + " has been given the plague");
									player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Short.MAX_VALUE - 1, 2));
									player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Short.MAX_VALUE - 1, 2));
									player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, Short.MAX_VALUE - 1, 0));
									player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Short.MAX_VALUE - 1, 2));
									MagicSpells.getManaHandler().addMana(player, -1000, ManaChangeReason.OTHER);
									player.sendMessage(ChatColor.DARK_RED + "You have contracted the " + ChatColor.GREEN + "Zombie " + ChatColor.LIGHT_PURPLE + "Plague" + ChatColor.DARK_RED + "!");
									//becomeMonsterSpell.castSpell(players.get(i), SpellCastState.NORMAL, 1.0F, null);
									if (++c >= monsterCount) {
										System.out.println("  Plague completed " + c);
										break;
									}
								} else {
									System.out.println("  Tried to plague " + player.getName() + ", but that player is not a dwarf");
								}
							}
						}
					}
					
					// start special task
					startMonsterSpecialTask();
					
					System.out.println("END MONSTER RELEASE");
				}
			}, monsterReleaseTime*20);
		}
		
		// start recount task
		counterTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				recount();
			}
		}, scoreInterval*20, scoreInterval*20);
		
		// initialize power bar
		if (volatileCode != null) {
			volatileCode.sendEnderDragonToAllPlayers();
		}
	}
	
	private void startMonsterSpecialTask() {
		if (monsterSpecialInterval > 0 && monsterSpecialSpell != null) {
			monsterSpecialTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
				public void run() {
					runMonsterSpecial();
				}
			}, monsterSpecialInterval * 20, monsterSpecialInterval * 20);
		}
	}
	
	public void endGame() {
		if (gameStarted) {
			gameStarted = false;
			
			if (counterTask != null) {
				counterTask.cancel();
				counterTask = null;
			}
			if (endTask != null) {
				endTask.cancel();
				endTask = null;
			}
			if (monsterSpecialTask != null) {
				monsterSpecialTask.cancel();
				monsterSpecialTask = null;
			}
			
			if (volatileCode != null) {
				volatileCode.removeEnderDragonForAllPlayers();
			}
			
			// destroy shrine
			World world = shrineCenter.getWorld();
			world.createExplosion(shrineCenter, 3.0F);
			world.createExplosion(shrineCenter.clone().add(3, 0, 0), 3.0F);
			world.createExplosion(shrineCenter.clone().add(0, 0, 3), 3.0F);
			world.createExplosion(shrineCenter.clone().add(-3, 0, 0), 3.0F);
			world.createExplosion(shrineCenter.clone().add(0, 0, -3), 3.0F);
			world.createExplosion(shrineCenter.clone().add(3, 0, 3), 3.0F);
			world.createExplosion(shrineCenter.clone().add(3, 0, -3), 3.0F);
			world.createExplosion(shrineCenter.clone().add(-3, 0, 3), 3.0F);
			world.createExplosion(shrineCenter.clone().add(-3, 0, -3), 3.0F);
			
			Bukkit.broadcastMessage(ChatColor.DARK_RED + "The dwarven shrine has fallen!");
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					System.out.println("DvZ End: asdf8907sdfbn3lkasdf83");
				}
			}, 200);
		}
	}
	
	private void initializeScoreboard() {
		// get server scoreboard instance
		scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		// get dwarf team info objective
		objective = scoreboard.getObjective("Dwarves");
		if (objective == null) {
			// not created, so create it
			objective = scoreboard.registerNewObjective("Dwarves", "DvZ");
			objective.setDisplayName(ChatColor.AQUA + "Dwarves");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		// get/create score counters
		remainingDwarvesScore = objective.getScore(Bukkit.getOfflinePlayer((ChatColor.GREEN + "Remaining")));
		monsterKillsScore = objective.getScore(Bukkit.getOfflinePlayer((ChatColor.RED + "Kills")));
		omwKillsScore = objective.getScore(Bukkit.getOfflinePlayer((ChatColor.GOLD + "Old Man Kills")));
		
		// create death time tracker
		deathTimes = new HashMap<String, Long>();
	}
	
	private void recount() {
		System.out.println("SCORE TICK!");
		long start = System.currentTimeMillis();
		
		// do dwarf count and shrine power
		int oldShrinePower = shrinePower;
		int c = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (dwarves.contains(p.getName())) {
				c++;
				if (shrine.contains(p)) {
					shrinePower += dwarfValue;
				}
			} else if (monsters.contains(p.getName()) && shrine.contains(p)) {
				shrinePower -= monsterValue;
			}
		}
		if (remainingDwarvesScore.getScore() != c) {
			remainingDwarvesScore.setScore(c);
		}
		if (shrinePower > 100) {
			shrinePower = 100;
		} else if (shrinePower < 0) {
			shrinePower = 0;
		}
		if (oldShrinePower != shrinePower && volatileCode != null) {
			volatileCode.setDragonHealth(shrinePower * 2);
		}
		
		// check end game conditions
		if (c == 0 || shrinePower == 0) {
			endGame();
		} else if (endTask == null && startEndTimerAtPercent > 0 && c < Math.round(Bukkit.getOnlinePlayers().length * (startEndTimerAtPercent / 100F))) {
			endTask = Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				public void run() {
					endGame();
				}
			}, endTimerDuration * 20);
		}
		
		System.out.println("    DURATION: " + (System.currentTimeMillis() - start) + "ms");
	}
	
	private void runMonsterSpecial() {
		if (monsterSpecialSpell != null) {
			List<Player> players = Arrays.asList(Bukkit.getOnlinePlayers());
			Collections.shuffle(players);
			for (Player p : players) {
				if (monsters.contains(p.getName()) && p.isValid() && !p.isDead()) {
					monsterSpecialSpell.castSpell(p, SpellCastState.NORMAL, 1.0F, null);
					break;
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDeath(PlayerDeathEvent event) {
		if (gameStarted) {
			Player p = event.getEntity();
			p.setSneaking(false);
			
			// check last death
			if (deathTimes.containsKey(p.getName())) {
				long time = deathTimes.get(p.getName());
				if (time > System.currentTimeMillis() - 5000) {
					return;
				}
			}
			deathTimes.put(p.getName(), System.currentTimeMillis());
			
			// adjust scores
			if (dwarves.contains(p.getName())) {
				int s = remainingDwarvesScore.getScore() - 1;
				if (s < 0) s = 0;
				remainingDwarvesScore.setScore(s);
				if (s == 0) {
					recount();
				}
			} else if (monsters.contains(p.getName())) {
				monsterKillsScore.setScore(monsterKillsScore.getScore() + 1);
			}
			
			// add to old man score
			if (p.getKiller() != null && p.getKiller().getName().equals("OldManWillakers")) {
				omwKillsScore.setScore(omwKillsScore.getScore() + 1);
			}
		}
	}
	
	@EventHandler
	public void onRespawn(final PlayerRespawnEvent event) {
		if (gameStarted) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					Player player = event.getPlayer();
					dwarves.remove(player.getName());
					monsters.add(player.getName());
					becomeMonsterSpell.castSpell(player, SpellCastState.NORMAL, 1.0F, null);
					if (volatileCode != null) {
						volatileCode.sendEnderDragonToPlayer(event.getPlayer());
					}
				}
			});
		}
	}
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		if (gameStarted) {
			final Player player = event.getPlayer();
			if (monstersReleased) {
				if (!dwarves.contains(player.getName()) || killDwarvesOnJoin) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							player.setHealth(0);
						}
					});
				}
			} else if (!dwarves.contains(player.getName()) && !monsters.contains(player.getName())) {
				becomeDwarfSpell.castSpell(player, SpellCastState.NORMAL, 1.0F, null);
				dwarves.add(player.getName());
			}
			if (volatileCode != null) {
				volatileCode.sendEnderDragonToPlayer(event.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CAKE_BLOCK) {
			if (event.getClickedBlock().getData() >= 4) {
				event.getClickedBlock().setData((byte)0);
			}
		}
	}
	
}
