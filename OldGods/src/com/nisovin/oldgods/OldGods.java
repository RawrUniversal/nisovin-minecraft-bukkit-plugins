package com.nisovin.oldgods;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffect;
import net.minecraft.server.Packet42RemoveMobEffect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nisovin.oldgods.godhandlers.CookingHandler;
import com.nisovin.oldgods.godhandlers.DeathHandler;
import com.nisovin.oldgods.godhandlers.ExplorationHandler;
import com.nisovin.oldgods.godhandlers.FarmingHandler;
import com.nisovin.oldgods.godhandlers.HealingHandler;
import com.nisovin.oldgods.godhandlers.HuntHandler;
import com.nisovin.oldgods.godhandlers.LoveHandler;
import com.nisovin.oldgods.godhandlers.MiningHandler;
import com.nisovin.oldgods.godhandlers.OceanHandler;
import com.nisovin.oldgods.godhandlers.WarHandler;
import com.nisovin.oldgods.godhandlers.WisdomHandler;

public class OldGods extends JavaPlugin {

	public static OldGods plugin;
	
	private static final int PRAYER_COOLDOWN = 300 * 1000;
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
	private static String[] godNames = {
		"Aitseh",
		"Sedah",
		"Semreh",
		"Retemed",
		"Ollopa",
		"Simetra",
		"Etidorfa",
		"Sutseafeh",
		"Nodiesop",
		"Sera",
		"Anetha",
		"Susynoid"
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
			color + "Sutseafeh, god of " + ChatColor.AQUA + "mining" + color + ", grants you great bounty.",
			color + "Nodiesop, god of the " + ChatColor.DARK_BLUE + "oceans" + color + ", blesses the waters.",
			color + "Sera, god of " + ChatColor.DARK_RED + "war" + color + ", gives you rage!",
			color + "Anetha, goddess of " + ChatColor.DARK_AQUA + "wisdom" + color + ", blesses the world with knowledge.",
			color + "Susynoid, god of " + ChatColor.GRAY + "laziness" + color + ", lets the world rest."
	};
	private int[] defaultChances = {
			10, // cooking
			5, // death
			10, // exploration
			10, // farming
			10, // healing
			10, // hunt
			10, // love
			10, // mining
			10, // ocean
			10, // war
			10, // wisdom
			15  // nothing
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

		// load state
		File file = new File(getDataFolder(), "saved_state.yml");
		if (file.exists()) {
			// load chances
			Configuration config = new Configuration(file);
			config.load();
			for (int i = 0; i < gods.length; i++) {
				currentChances[i] = config.getInt(gods[i].name(), defaultChances[i]);
			}
		}
		
		// get first god
		newGod();
		
		System.out.println("OldGods is loaded");
	}	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equals("pray")) {
			if (!(sender instanceof Player)) return true;
			
			Player player = (Player)sender;
			
			if (args.length != 1) {
				return false;
			}
			
			// get god
			God god = null;
			String godName = "a god";
			for (int i = 0; i < gods.length; i++) {
				if (args[0].equalsIgnoreCase(gods[i].name()) || args[0].equalsIgnoreCase(godNames[i])) {
					if (player.hasPermission("oldgods.pray." + gods[i].name().toLowerCase())) {
						god = gods[i];
						godName = godNames[i];
					} else {
						player.sendMessage("You cannot pray to " + godNames[i] + ", you must go to an altar.");
					}
					break;
				}
			}
			if (god == null) {
				player.sendMessage("There is no god by that name.");
				return true;
			}
			
			// add prayer
			boolean success = addPrayer(player, god, player.getLocation().add(0,1,0), 10);
			if (success) {
				player.sendMessage("You have prayed to " + godName + ".");
			}
			
		} else if (command.getName().equals("oldgods")) {
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
				for (Player p : getServer().getOnlinePlayers()) {
					p.sendMessage(godMessages[i]);
				}
				getServer().getLogger().info(godMessages[i]);
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
	
	public boolean addPrayer(Player player, God god, Location location, int amount) {
		if (prayerCooldowns.containsKey(player) && prayerCooldowns.get(player) > System.currentTimeMillis()) {
			player.sendMessage("You must wait to pray again.");
			return false;
		}
		
		// get the god's index
		int index = -1;
		for (int i = 0; i < gods.length; i++) {
			if (gods[i] == god) {
				index = i;
				break;
			}
		}
		
		// add amount to god chances
		if (player.hasPermission("oldgods.disciple." + god.toString().toLowerCase())) {
			amount *= 2;
		}
		if (index != -1) {
			currentChances[index] += amount;
		}
		
		// set cooldown
		prayerCooldowns.put(player, System.currentTimeMillis() + PRAYER_COOLDOWN);
		
		// grant god blessing
		if (god == God.COOKING) {
			CookingHandler.pray(player, location, amount);
		} else if (god == God.DEATH) {
			DeathHandler.pray(player, location, amount);
		} else if (god == God.EXPLORATION) {
			ExplorationHandler.pray(player, location, amount);
		} else if (god == God.FARMING) {
			FarmingHandler.pray(player, location, amount);
		} else if (god == God.HEALING) {
			HealingHandler.pray(player, location, amount);
		} else if (god == God.HUNT) {
			HuntHandler.pray(player, location, amount);
		} else if (god == God.LOVE) {
			LoveHandler.pray(player, location, amount);
		} else if (god == God.MINING) {
			MiningHandler.pray(player, location, amount);
		} else if (god == God.OCEAN) {
			OceanHandler.pray(player, location, amount);
		} else if (god == God.WAR) {
			WarHandler.pray(player, location, amount);
		} else if (god == God.WISDOM) {
			WisdomHandler.pray(player, location, amount);
		}
		
		return true;
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
		return random(100);
	}
	
	public static int random(int r) {
		return random.nextInt(r);
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
		// save state
		File file = new File(getDataFolder(), "saved_state.yml");
		if (file.exists()) {
			file.delete();
		}
		Configuration config = new Configuration(file);
		for (int i = 0; i < gods.length; i++) {
			config.setProperty(gods[i].name(), currentChances[i]);
		}
		config.save();
	}
	
	private class GodChanger implements Runnable {
		public void run() {
			taskId = -1;
			newGod();
		}
	}
	
	public static void setMobEffect(LivingEntity entity, int type, int duration, int amplifier) {		
		((CraftLivingEntity)entity).getHandle().addEffect(new MobEffect(type, duration, amplifier));
	}
	
	@SuppressWarnings("rawtypes")
	public static void removeMobEffect(LivingEntity entity, int type) {
		try {
			if (entity instanceof Player) {
				EntityPlayer player = ((CraftPlayer)entity).getHandle();
				player.netServerHandler.sendPacket(new Packet42RemoveMobEffect(player.id, new MobEffect(type, 0, 0)));
			}
			Field field = EntityLiving.class.getDeclaredField("effects");
			field.setAccessible(true);
			HashMap effects = (HashMap)field.get(((CraftLivingEntity)entity).getHandle());
			effects.remove(type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
