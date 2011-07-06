package com.nisovin.MagicSpells;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.Spells.*;

public class MagicSpells extends JavaPlugin {

	public static MagicSpells plugin;

	public static boolean debug;
	public static ChatColor textColor;
	public static int broadcastRange;
	
	public static boolean opsHaveAllSpells;
	public static List<String> castForFree;
	public static boolean freecastNoCooldown;
	
	public static boolean ignoreDefaultBindings;
	public static List<Integer> losTransparentBlocks;
	
	public static boolean enableManaBars;
	public static int maxMana;
	public static String manaBarPrefix;
	public static int manaBarSize;
	public static ChatColor manaBarColorFull;
	public static ChatColor manaBarColorEmpty;
	public static int manaRegenTickRate;
	public static int manaRegenPercent;
	public static boolean showManaOnUse;
	public static boolean showManaOnRegen;
	public static boolean showManaOnWoodTool;
	public static int manaBarToolSlot;
	public static HashMap<Integer,Integer> manaPotions;
	
	public static String strCastUsage;
	public static String strUnknownSpell;
	public static String strSpellChange;
	public static String strOnCooldown;
	public static String strMissingReagents;
	public static String strCantCast;
	public static String strConsoleName;
	
	public static HashMap<String,Spell> spells; // map internal names to spells
	public static HashMap<String,Spell> spellNames; // map configured names to spells
	public static HashMap<String,Spellbook> spellbooks; // player spellbooks
	
	public static HashMap<Event.Type,HashSet<Spell>> listeners;
	
	public static ManaBarManager mana;
	
	@Override
	public void onEnable() {
		load();
		
		// load listeners
		new MagicPlayerListener(this);
		new MagicEntityListener(this);
		new MagicBlockListener(this);
	}
	
	public void load() {
		plugin = this;
		
		// create storage stuff
		spells = new HashMap<String,Spell>();
		spellNames = new HashMap<String,Spell>();
		spellbooks = new HashMap<String,Spellbook>();
		listeners = new HashMap<Event.Type,HashSet<Spell>>();
		
		// make sure directories are created
		this.getDataFolder().mkdir();
		new File(this.getDataFolder(), "spellbooks").mkdir();
		
		// load config
		loadConfigFromJar();
		Configuration config = this.getConfiguration();
		config.load();
		debug = config.getBoolean("general.debug", false);
		textColor = ChatColor.getByCode(config.getInt("general.text-color", ChatColor.DARK_AQUA.getCode()));
		broadcastRange = config.getInt("general.broadcast-range", 20);
		opsHaveAllSpells = config.getBoolean("general.ops-have-all-spells", true);
		strCastUsage = config.getString("general.str-cast-usage", "Usage: /cast <spell>. Use /cast list to see a list of spells.");
		strUnknownSpell = config.getString("general.str-unknown-spell", "You do not know a spell with that name.");
		strSpellChange = config.getString("general.str-spell-change", "You are now using the %s spell.");
		strOnCooldown = config.getString("general.str-on-cooldown", "That spell is on cooldown.");
		strMissingReagents = config.getString("general.str-missing-reagents", "You do not have the reagents for that spell.");
		strCantCast = config.getString("general.str-cant-cast", "You can't cast that spell right now.");
		strConsoleName = config.getString("general.console-name", "Admin");
		castForFree = config.getStringList("general.cast-for-free", null);
		if (castForFree != null) {
			for (int i = 0; i < castForFree.size(); i++) {
				castForFree.set(i, castForFree.get(i).toLowerCase());
			}
		}
		freecastNoCooldown = config.getBoolean("general.freecast-no-cooldown", true);
		ignoreDefaultBindings = config.getBoolean("general.ignore-default-bindings", false);
		losTransparentBlocks = config.getIntList("general.los-transparent-blocks", null);
		if (losTransparentBlocks == null || losTransparentBlocks.size() == 0) {
			losTransparentBlocks = new ArrayList<Integer>();
			losTransparentBlocks.add(Material.AIR.getId());
			losTransparentBlocks.add(Material.TORCH.getId());
			losTransparentBlocks.add(Material.REDSTONE_WIRE.getId());
			losTransparentBlocks.add(Material.REDSTONE_TORCH_ON.getId());
			losTransparentBlocks.add(Material.REDSTONE_TORCH_OFF.getId());
			losTransparentBlocks.add(Material.YELLOW_FLOWER.getId());
			losTransparentBlocks.add(Material.RED_ROSE.getId());
			losTransparentBlocks.add(Material.BROWN_MUSHROOM.getId());
			losTransparentBlocks.add(Material.RED_MUSHROOM.getId());
			losTransparentBlocks.add(Material.LONG_GRASS.getId());
			losTransparentBlocks.add(Material.DEAD_BUSH.getId());
			losTransparentBlocks.add(Material.DIODE_BLOCK_ON.getId());
			losTransparentBlocks.add(Material.DIODE_BLOCK_OFF.getId());
		}		
		
		// setup mana bar manager		
		enableManaBars = config.getBoolean("general.mana.enable-mana-bars", true);
		maxMana = config.getInt("general.mana.max-mana", 100);
		manaBarPrefix = config.getString("general.mana.mana-bar-prefix", "Mana:");
		manaBarSize = config.getInt("general.mana.mana-bar-size", 35);
		manaBarColorFull = ChatColor.getByCode(config.getInt("general.mana.color-full", ChatColor.GREEN.getCode()));
		manaBarColorEmpty = ChatColor.getByCode(config.getInt("general.mana.color-empty", ChatColor.BLACK.getCode()));
		manaRegenTickRate = config.getInt("general.mana.regen-tick-seconds", 5) * 20;
		manaRegenPercent = config.getInt("general.mana.regen-percent", 5);
		showManaOnUse = config.getBoolean("general.mana.show-mana-on-use", false);
		showManaOnRegen = config.getBoolean("general.mana.show-mana-on-regen", false);
		showManaOnWoodTool = config.getBoolean("general.mana.show-mana-on-wood-tool", true);
		manaBarToolSlot = config.getInt("general.mana.tool-slot", 8);
		if (enableManaBars) {
			mana = new ManaBarManager();
			for (Player p : getServer().getOnlinePlayers()) {
				mana.createManaBar(p);
			}
		}
		List<String> manaPots = config.getStringList("general.mana.mana-potions", null);
		if (manaPots != null && manaPots.size() > 0) {
			manaPotions = new HashMap<Integer,Integer>();
			for (int i = 0; i < manaPots.size(); i++) {
				String[] data = manaPots.get(i).split(" ");
				manaPotions.put(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
			}
		}
		
		// load permissions
		if (config.getBoolean("general.use-permissions", false)) {
			Spellbook.initPermissions();
		}
		
		// load spells
		BindSpell.load(config);
		BlinkSpell.load(config);
		BuildSpell.load(config);
		CombustSpell.load(config);
		EntombSpell.load(config);
		ExplodeSpell.load(config);
		ExternalCommandSpell.load(config);
		FireballSpell.load(config);
		FirenovaSpell.load(config);
		FlamewalkSpell.load(config);
		ForcepushSpell.load(config);
		ForgetSpell.load(config);
		FrostwalkSpell.load(config);
		GateSpell.load(config);
		GillsSpell.load(config);
		HealSpell.load(config);
		HelpSpell.load(config);
		InvulnerabilitySpell.load(config);
		LifewalkSpell.load(config);
		LightningSpell.load(config);
		LightwalkSpell.load(config);
		ListSpell.load(config);
		MarkSpell.load(config);
		MinionSpell.load(config);
		PrayerSpell.load(config);
		PurgeSpell.load(config);
		RecallSpell.load(config);
		SafefallSpell.load(config);
		SpellbookSpell.load(config);
		StealthSpell.load(config);
		StonevisionSpell.load(config);
		TeachSpell.load(config);
		TelekinesisSpell.load(config);
		//if (getServer().getPluginManager().isPluginEnabled("BookWorm")) TomeSpell.load(config);
		VolleySpell.load(config);
		WallSpell.load(config);
		WindwalkSpell.load(config);
		ZapSpell.load(config);
		
		// load spell copies
		List<String> copies = config.getStringList("spellcopies", null);
		if (copies != null && copies.size() > 0) {
			for (String copy : copies) {
				String[] data = copy.split("=");
				Spell spell = spells.get(data[1]);
				if (spell != null) {
					try {
						spell.getClass().getMethod("load", Configuration.class, String.class).invoke(null, config, data[0]);
					} catch (Exception e) {
						getServer().getLogger().severe("Unable to create spell copy: " + copy);
					}
				}
			}
		}
		
		// load in-game spell names
		for (Spell spell : spells.values()) {
			spellNames.put(spell.getName(), spell);
		}
		
		// load online player spellbooks
		for (Player p : getServer().getOnlinePlayers()) {
			spellbooks.put(p.getName(), new Spellbook(p, this));
		}
		
		getServer().getLogger().info("MagicSpells v" + this.getDescription().getVersion() + " loaded!");
		
	}
	
	public static Spellbook getSpellbook(Player player) {
		Spellbook spellbook = spellbooks.get(player.getName());
		if (spellbook == null) {
			spellbooks.put(player.getName(), new Spellbook(player, plugin));
		}
		return spellbook;
	}
	
	public static void addSpellListener(Event.Type eventType, Spell spell) {
		HashSet<Spell> spells = listeners.get(eventType);
		if (spells == null) {
			spells = new HashSet<Spell>();
			listeners.put(eventType, spells);
		}
		spells.add(spell);
	}
	
	public static void removeSpellListener(Event.Type eventType, Spell spell) {
		HashSet<Spell> spells = listeners.get(eventType);
		if (spells != null) {
			spells.remove(spell);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (command.getName().equalsIgnoreCase("cast")) {
			if (args == null || args.length == 0) {
				if (sender instanceof Player) {
					Spell.sendMessage((Player)sender, strCastUsage);
				} else {
					sender.sendMessage(textColor + strCastUsage);
				}
			} else if (sender.isOp() && args[0].equals("reload")) {
				if (args.length == 1) {
					onDisable();
					load();
					sender.sendMessage(textColor + "MagicSpells config reloaded.");
				} else {
					List<Player> players = getServer().matchPlayer(args[1]);
					if (players.size() != 1) {
						sender.sendMessage(textColor + "Player not found.");
					} else {
						Player player = players.get(0);
						spellbooks.put(player.getName(), new Spellbook(player, this));
						sender.sendMessage(textColor + player.getName() + "'s spellbook reloaded.");
					}
				}
			} else if (sender instanceof Player) {
				Player player = (Player)sender;
				Spellbook spellbook = getSpellbook(player);
				Spell spell = spellbook.getSpellByName(args[0]);
				if (spell != null && spell.canCastByCommand()) {
					String[] spellArgs = null;
					if (args.length > 1) {
						spellArgs = new String[args.length-1];
						for (int i = 1; i < args.length; i++) {
							spellArgs[i-1] = args[i];
						}
					}
					spell.cast(player, spellArgs);
				} else {
					Spell.sendMessage(player, strUnknownSpell);
				}
			} else { // not a player
				Spell spell = spellNames.get(args[0]);
				if (spell == null) {
					sender.sendMessage("Unknown spell.");
				} else if (spell instanceof CommandSpell) {
					String[] spellArgs = null;
					if (args.length > 1) {
						spellArgs = new String[args.length-1];
						for (int i = 1; i < args.length; i++) {
							spellArgs[i-1] = args[i];
						}
					}
					boolean ok = ((CommandSpell)spell).castFromConsole(sender, spellArgs);
					if (!ok) {
						sender.sendMessage("Cannot cast that spell from console.");
					}
				} else {
					sender.sendMessage("Cannot cast that spell from console.");					
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("mana")) {
			if (enableManaBars && sender instanceof Player) {
				Player player = (Player)sender;
				mana.showMana(player);
			}
			return true;
		}
		return false;
	}
	
	public static void debug(String message) {
		if (MagicSpells.debug) {
			plugin.getServer().getLogger().info("MagicSpells: " + message);
		}
	}
	
	public static boolean teachSpell(Player player, String spellName) {
		Spell spell = spellNames.get(spellName);
		if (spell == null) {
			spell = spells.get(spellName);
			if (spell == null) {
				return false;
			}
		}
		
		Spellbook spellbook = getSpellbook(player);
		
		if (spellbook == null || spellbook.hasSpell(spell) || !spellbook.canLearn(spell)) {
			return false;
		} else {
			spellbook.addSpell(spell);
			spellbook.save();
			return true;
		}
	}
	
	public void loadConfigFromJar() {
		File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            InputStream fis = getClass().getResourceAsStream("/config.yml");
            FileOutputStream fos = null;
            try {
            	fos = new FileOutputStream(configFile);
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
            	getServer().getLogger().log(Level.SEVERE, "Failed to load config from JAR", e);
            } finally {
            	try {
	                if (fis != null) {
	                    fis.close();
	                }
	                if (fos != null) {
	                    fos.close();
	                }
            	} catch (Exception e) {            		
            	}
            }
        }
	}
	
	@Override
	public void onDisable() {
		for (Spell spell : spells.values()) {
			spell.turnOff();
		}
		spells = null;
		spellNames = null;
		spellbooks = null;
		listeners = null;
		if (mana != null) {
			mana.stopRegenerator();
		}
	}
	
}
