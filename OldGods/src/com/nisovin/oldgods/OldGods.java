package com.nisovin.oldgods;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class OldGods extends JavaPlugin {

	public static OldGods plugin;
	
	private static final int PRAYER_COOLDOWN = 60 * 1000;
	private static final ChatColor color = ChatColor.GOLD;
	private static final Random random = new Random();
	
	private static God[] gods = {
			God.COOKING, 
			God.DEATH, 
			God.EXPLORATION, 
			God.FARMING, 
			God.HEALING, 
			God.HUNT, 
			God.LOVE,
			God.MINING, 
			God.OCEAN, 
			God.WAR, 
			God.WISDOM,
			God.NOTHING
			};
	private static String[] godMessages = {
			color + "Aitseh, goddess of " + ChatColor.DARK_PURPLE + "cooking" + color + ", blesses the world.",
			color + "Sedah, god of " + ChatColor.DARK_GRAY + "death" + color + ", looks darkly upon the world.",
			color + "Semreh, god of " + ChatColor.DARK_GREEN + "exploration" + color + ", sends aid to the world.",
			color + "Retemed, god of " + ChatColor.GREEN + "farming" + color + ", blesses the growing things.",
			color + "Ollopa, god of " + ChatColor.WHITE + "healing" + color + ", smiles down upon the world.",
			color + "Simretra, goddess of the " + ChatColor.YELLOW + "hunt" + color + ", turns her gaze to the world.",
			color + "Etidorfa, goddess of " + ChatColor.RED + "love" + color + ", brings harmony to the world.",
			color + "Sutseafeh, god of " + ChatColor.AQUA + "mining" + color + ", blesses the ground.",
			color + "Nodiesop, god of the " + ChatColor.DARK_BLUE + "ocean" + color + ", blesses the waters.",
			color + "Sera, god of " + ChatColor.DARK_RED + "war" + color + ", focuses his energy on the world.",
			color + "Anetha, goddess of " + ChatColor.DARK_AQUA + "wisdom" + color + ", blesses the world with knowledge.",
			color + "Susynoid, god of " + ChatColor.GRAY + "laziness" + color + ", lets the world rest."
	};
	private static String[] devoutMessages = {
			color + "Aitseh, goddess of " + ChatColor.DARK_PURPLE + "cooking" + color + ", blesses the world.",
			color + "Sedah, god of " + ChatColor.DARK_GRAY + "death" + color + ", looks darkly upon the world.",
			color + "Semreh, god of " + ChatColor.DARK_GREEN + "exploration" + color + ", sends aid to the world.",
			color + "Retemed, god of " + ChatColor.GREEN + "farming" + color + ", increases your harvest.",
			color + "Ollopa, god of " + ChatColor.WHITE + "healing" + color + ", saves you from certain death.",
			color + "Simretra, goddess of the " + ChatColor.YELLOW + "hunt" + color + ", grants you great bounty.",
			color + "Etidorfa, goddess of " + ChatColor.RED + "love" + color + ", brings harmony to the world.",
			color + "Sutseafeh, god of " + ChatColor.AQUA + "mining" + color + ", blesses the ground.",
			color + "Nodiesop, god of the " + ChatColor.DARK_BLUE + "oceans" + color + ", blesses the waters.",
			color + "Sera, god of " + ChatColor.DARK_RED + "war" + color + ", gives you rage!",
			color + "Anetha, goddess of " + ChatColor.DARK_AQUA + "wisdom" + color + ", blesses the world with knowledge.",
			color + "Susynoid, god of " + ChatColor.GRAY + "laziness" + color + ", lets the world rest."
	};
	private int[] defaultChances = {
			10, // cooking
			10, // death
			10, // exploration
			10, // farming
			10, // healing
			10, // hunt
			10, // love
			10, // mining
			10, // ocean
			10, // war
			10, // wisdom
			10  // nothing
			};
	private int[] currentChances = defaultChances.clone();
	
	private AltarManager altars;
	private HashMap<Player,Long> prayerCooldowns = new HashMap<Player,Long>();
	
	private God currentGod;
	
	private int taskId = -1;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		altars = new AltarManager(this);
		
		new PListener(this);
		new EListener(this);
		new BListener(this);
		new IListener(this);
		//new VListener(this);
		if (getServer().getPluginManager().isPluginEnabled("MagicSpells")) new SListener(this);
		
		newGod();
		
		System.out.println("OldGods is loaded");
	}	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("oldgods.admin.altar") && args.length == 3 && args[0].equalsIgnoreCase("addaltar") && sender instanceof Player) {
			Player player = (Player)sender;
			God god = God.valueOf(args[1].toUpperCase());
			int amount = Integer.parseInt(args[2]);
			Block block = player.getTargetBlock(null, 10);
			altars.addAltar(block, god, amount);
			sender.sendMessage("Altar added.");
		} else if (sender.hasPermission("oldgods.admin.set") && args.length == 2 && args[0].equalsIgnoreCase("set")) {
			God god = God.valueOf(args[1].toUpperCase());
			if (god != null) {
				newGod(god);
			}
		} else if (sender.hasPermission("oldgods.admin.set") && args.length == 1 && args[0].equalsIgnoreCase("next")) {
			newGod();
			sender.sendMessage("New chosen god: " + currentGod.toString());
		} else if (sender.hasPermission("oldgods.admin.status") && args.length == 1 && args[0].equalsIgnoreCase("status")) {
			for (int i = 0; i < gods.length; i++) {
				sender.sendMessage(gods[i].toString() + " : " + currentChances[i]);
			}
		}
		
		return true;
	}

	public void newGod() {
		God god = getRandomGod(currentChances, gods);
		if (god == null) {
			god = God.NOTHING;
		}
		
		newGod(god);
	}
	
	public void newGod(God god) {

		currentGod = god;
		prayerCooldowns.clear();
		
		for (int i = 0; i < gods.length; i++) {
			if (gods[i] == god) {
				// set new god's chance to zero for next time
				currentChances[i] = 0;				
				// send message announcement
				getServer().broadcastMessage(godMessages[i]);
			} else {
				// increase other gods' chances
				currentChances[i] += defaultChances[i];
			}
		}
		
		// schedule next change
		if (taskId != -1) {
			getServer().getScheduler().cancelTask(taskId);
		}
		taskId = getServer().getScheduler().scheduleSyncDelayedTask(this, new GodChanger(), 24000 - getServer().getWorlds().get(0).getTime() + 10);
		
	}
	
	public void showCurrentGod(Player player) {
		for (int i = 0; i < gods.length; i++) {
			if (gods[i] == currentGod) {
				player.sendMessage(godMessages[i]);
				break;
			}
		}
	}
	
	public void addPrayer(Player player, God god, int amount) {
		if (prayerCooldowns.containsKey(player) && prayerCooldowns.get(player) > System.currentTimeMillis()) {
			player.sendMessage("You must wait to pray again.");
			return;
		}
		int index = -1;
		for (int i = 0; i < gods.length; i++) {
			if (gods[i] == god) {
				index = i;
				break;
			}
		}
		if (player.hasPermission("oldgods.disciple." + god.toString().toLowerCase())) {
			amount *= 2;
		}
		if (index != -1) {
			currentChances[index] += amount;
		}
		// set cooldown
		prayerCooldowns.put(player, System.currentTimeMillis() + PRAYER_COOLDOWN);
		player.sendMessage("You pray at the altar.");
	}
	

	public God getRandomGod(int[] chances, God[] options) {
		God result = null;
		// get random number
		int total = 0;
		for (int c : chances) {
			total += c;
		}
		int rand = new Random().nextInt(total);
		// find a random god
		int n = 0;
		for (int i = 0; i < options.length; i++) {
			if (rand < chances[i] + n) {
				result = options[i];
				break;
			} else {
				n += chances[i];
			}
		}
		return result;	
	}
	
	public void scheduleNextSwitch() {
		
	}
	
	public God currentGod() {
		return currentGod;
	}
	
	public AltarManager altars() {
		return altars;
	}

	public static int random() {
		return random.nextInt(100);
	}
	
	public static boolean isDisciple(Player player, God god) {
		return player.hasPermission("oldgods.disciple." + god.toString().toLowerCase());
	}
	
	public static String getDevoutMessage(God god) {
		for (int i = 0; i < gods.length; i++) {
			if (gods[i] == god) {
				return devoutMessages[i];
			}
		}
		return "";
	}
	
	@Override
	public void onDisable() {
	}
	
	private class GodChanger implements Runnable {
		public void run() {
			taskId = -1;
			newGod();
		}
	}

}
