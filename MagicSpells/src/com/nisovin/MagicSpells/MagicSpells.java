package com.nisovin.MagicSpells;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.material.MaterialData;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.Events.MagicEventType;
import com.nisovin.MagicSpells.Spells.*;
import com.nisovin.MagicSpells.Util.MagicConfig;

public class MagicSpells extends JavaPlugin {

	public static MagicSpells plugin;

	protected static boolean debug;
	protected static ChatColor textColor;
	protected static int broadcastRange;
	
	protected static boolean opsHaveAllSpells;
	public static List<String> castForFree;
	protected static List<String> castNoCooldown;
	
	protected static boolean allowCycleToNoSpell;
	protected static boolean ignoreDefaultBindings;
	protected static List<Integer> losTransparentBlocks;
	
	protected static boolean enableManaBars;
	protected static int maxMana;
	protected static String manaBarPrefix;
	protected static int manaBarSize;
	protected static ChatColor manaBarColorFull;
	protected static ChatColor manaBarColorEmpty;
	protected static int manaRegenTickRate;
	protected static int manaRegenPercent;
	protected static boolean showManaOnUse;
	protected static boolean showManaOnRegen;
	protected static boolean showManaOnWoodTool;
	protected static int manaBarToolSlot;
	protected static int manaPotionCooldown;
	protected static String strManaPotionOnCooldown;
	protected static HashMap<MaterialData,Integer> manaPotions;
	
	protected static String strCastUsage;
	protected static String strUnknownSpell;
	protected static String strSpellChange;
	protected static String strSpellChangeEmpty;
	protected static String strOnCooldown;
	public static String strMissingReagents;
	protected static String strCantCast;
	protected static String strNoMagicZone;
	public static String strConsoleName;
	
	protected static HashMap<String,Spell> spells; // map internal names to spells
	protected static HashMap<String,Spell> spellNames; // map configured names to spells
	protected static HashMap<String,Spellbook> spellbooks; // player spellbooks
	
	protected static HashMap<Event.Type,HashSet<Spell>> listeners;
	protected static HashMap<MagicEventType, HashSet<Spell>> customListeners;
	
	public static ManaBarManager mana;
	protected static HashMap<Player,Long> manaPotionCooldowns;
	protected static NoMagicZoneManager noMagicZones;
	
	@Override
	public void onEnable() {
		load();
		
		// load listeners
		new MagicPlayerListener(this);
		new MagicEntityListener(this);
		new MagicBlockListener(this);
		new MagicSpellListener(this);
	}
	
	private void load() {
		plugin = this;
		
		// create storage stuff
		spells = new HashMap<String,Spell>();
		spellNames = new HashMap<String,Spell>();
		spellbooks = new HashMap<String,Spellbook>();
		listeners = new HashMap<Event.Type,HashSet<Spell>>();
		customListeners = new HashMap<MagicEventType, HashSet<Spell>>();
		
		// make sure directories are created
		this.getDataFolder().mkdir();
		new File(this.getDataFolder(), "spellbooks").mkdir();
		
		// load config
		loadConfigFromJar();
		Configuration config = new MagicConfig(new File(this.getDataFolder(), "config.yml"));
		debug = config.getBoolean("general.debug", false);
		textColor = ChatColor.getByCode(config.getInt("general.text-color", ChatColor.DARK_AQUA.getCode()));
		broadcastRange = config.getInt("general.broadcast-range", 20);
		allowCycleToNoSpell = config.getBoolean("general.allow-cycle-to-no-spell", false);
		opsHaveAllSpells = config.getBoolean("general.ops-have-all-spells", true);
		strCastUsage = config.getString("general.str-cast-usage", "Usage: /cast <spell>. Use /cast list to see a list of spells.");
		strUnknownSpell = config.getString("general.str-unknown-spell", "You do not know a spell with that name.");
		strSpellChange = config.getString("general.str-spell-change", "You are now using the %s spell.");
		strSpellChangeEmpty = config.getString("general.str-spell-change-empty", "You are no longer using a spell.");
		strOnCooldown = config.getString("general.str-on-cooldown", "That spell is on cooldown.");
		strMissingReagents = config.getString("general.str-missing-reagents", "You do not have the reagents for that spell.");
		strCantCast = config.getString("general.str-cant-cast", "You can't cast that spell right now.");
		strNoMagicZone = config.getString("general.str-no-magic-zone", "An anti-magic aura makes your spell fizzle.");
		strConsoleName = config.getString("general.console-name", "Admin");
		castForFree = config.getStringList("general.cast-for-free", null);
		if (castForFree != null) {
			for (int i = 0; i < castForFree.size(); i++) {
				castForFree.set(i, castForFree.get(i).toLowerCase());
			}
		}
		castNoCooldown = config.getStringList("general.cast-no-cooldown", null);
		if (castForFree != null) {
			for (int i = 0; i < castNoCooldown.size(); i++) {
				castNoCooldown.set(i, castNoCooldown.get(i).toLowerCase());
			}
		}
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
		enableManaBars = config.getBoolean("general.mana.enable-mana-bars", true);
		maxMana = config.getInt("general.mana.max-mana", 100);
		manaBarPrefix = config.getString("general.mana.mana-bar-prefix", "Mana:");
		manaBarSize = config.getInt("general.mana.mana-bar-size", 35);
		manaBarColorFull = ChatColor.getByCode(config.getInt("general.mana.color-full", ChatColor.GREEN.getCode()));
		manaBarColorEmpty = ChatColor.getByCode(config.getInt("general.mana.color-empty", ChatColor.BLACK.getCode()));
		manaRegenTickRate = config.getInt("general.mana.regen-tick-seconds", 5) * 1000;
		manaRegenPercent = config.getInt("general.mana.regen-percent", 5);
		showManaOnUse = config.getBoolean("general.mana.show-mana-on-use", false);
		showManaOnRegen = config.getBoolean("general.mana.show-mana-on-regen", false);
		showManaOnWoodTool = config.getBoolean("general.mana.show-mana-on-wood-tool", true);
		manaBarToolSlot = config.getInt("general.mana.tool-slot", 8);
		manaPotionCooldown = config.getInt("general.mana.mana-potion-cooldown", 30);
		strManaPotionOnCooldown = config.getString("general.mana.str-mana-potion-on-cooldown", "You cannot use another mana potion yet.");
		
		// setup mana bar manager
		if (enableManaBars) {
			mana = new ManaBarManager();
			for (Player p : getServer().getOnlinePlayers()) {
				mana.createManaBar(p);
			}
		}
		
		// load mana potions
		List<String> manaPots = config.getStringList("general.mana.mana-potions", null);
		if (manaPots != null && manaPots.size() > 0) {
			manaPotions = new HashMap<MaterialData,Integer>();
			for (int i = 0; i < manaPots.size(); i++) {
				String[] data = manaPots.get(i).split(" ");
				MaterialData mat;
				if (data[0].contains(":")) {
					String[] data2 = data[0].split(":");
					mat = new MaterialData(Integer.parseInt(data2[0]), Byte.parseByte(data2[1]));
				} else {
					mat = new MaterialData(Integer.parseInt(data[0]));					
				}
				manaPotions.put(mat, Integer.parseInt(data[1]));
			}
			manaPotionCooldowns = new HashMap<Player,Long>();
		}
		
		// load no-magic zones
		noMagicZones = new NoMagicZoneManager(config);
		if (noMagicZones.zoneCount() == 0) {
			noMagicZones = null;
		}
		
		// load permissions
		PluginManager pm = getServer().getPluginManager();
		HashMap<String, Boolean> permGrantChildren = new HashMap<String,Boolean>();
		HashMap<String, Boolean> permLearnChildren = new HashMap<String,Boolean>();
		HashMap<String, Boolean> permCastChildren = new HashMap<String,Boolean>();
		HashMap<String, Boolean> permTeachChildren = new HashMap<String,Boolean>();
		
		// load spells
		loadNormalSpells(config, pm, permGrantChildren, permLearnChildren, permCastChildren, permTeachChildren);
		loadCustomSpells(config, pm, permGrantChildren, permLearnChildren, permCastChildren, permTeachChildren);
		loadSpellCopies(config, pm, permGrantChildren, permLearnChildren, permCastChildren, permTeachChildren);
		loadMultiSpells(config, pm, permGrantChildren, permLearnChildren, permCastChildren, permTeachChildren);
		
		// finalize permissions
		addPermission(pm, "grant.*", PermissionDefault.FALSE, permGrantChildren);
		addPermission(pm, "learn.*", PermissionDefault.TRUE, permLearnChildren);
		addPermission(pm, "cast.*", PermissionDefault.TRUE, permCastChildren);
		addPermission(pm, "teach.*", PermissionDefault.TRUE, permTeachChildren);
		HashMap<String,Boolean> permAllChildren = new HashMap<String,Boolean>();
		permAllChildren.putAll(permGrantChildren);
		permAllChildren.putAll(permLearnChildren);
		permAllChildren.putAll(permCastChildren);
		permAllChildren.putAll(permTeachChildren);
		addPermission(pm, "*", PermissionDefault.FALSE, permAllChildren);
		
		// load in-game spell names and initialize spells
		for (Spell spell : spells.values()) {
			spellNames.put(spell.getName(), spell);
			String[] aliases = spell.getAliases();
			if (aliases != null && aliases.length > 0) {
				for (String alias : aliases) {
					if (!spellNames.containsKey(alias)) {
						spellNames.put(alias, spell);
					}
				}
			}
			spell.initialize();
		}
		
		// load online player spellbooks
		for (Player p : getServer().getOnlinePlayers()) {
			spellbooks.put(p.getName(), new Spellbook(p, this));
		}
		
		getServer().getLogger().info("MagicSpells v" + this.getDescription().getVersion() + " loaded!");
		
	}
	
	private void loadNormalSpells(Configuration config, PluginManager pm, HashMap<String, Boolean> permGrantChildren, HashMap<String, Boolean> permLearnChildren, HashMap<String, Boolean> permCastChildren, HashMap<String, Boolean> permTeachChildren) {
		// create list of spells
		ArrayList<Class<? extends Spell>> spellClasses = new ArrayList<Class<? extends Spell>>();
		spellClasses.add(BindSpell.class);
		spellClasses.add(BlinkSpell.class);
		spellClasses.add(BuildSpell.class);
		spellClasses.add(CarpetSpell.class);
		spellClasses.add(CombustSpell.class);
		spellClasses.add(ConfusionSpell.class);
		spellClasses.add(DrainlifeSpell.class);
		spellClasses.add(EntombSpell.class);
		spellClasses.add(ExplodeSpell.class);
		spellClasses.add(ExternalCommandSpell.class);
		spellClasses.add(FireballSpell.class);
		spellClasses.add(FirenovaSpell.class);
		spellClasses.add(FlamewalkSpell.class);
		spellClasses.add(ForcepushSpell.class);
		spellClasses.add(ForcetossSpell.class);
		spellClasses.add(ForgetSpell.class);
		spellClasses.add(FrostwalkSpell.class);
		spellClasses.add(GateSpell.class);
		spellClasses.add(GeyserSpell.class);
		spellClasses.add(GillsSpell.class);
		spellClasses.add(HealSpell.class);
		spellClasses.add(HelpSpell.class);
		spellClasses.add(InvulnerabilitySpell.class);
		spellClasses.add(LeapSpell.class);
		spellClasses.add(LifewalkSpell.class);
		spellClasses.add(LightningSpell.class);
		spellClasses.add(LightwalkSpell.class);
		spellClasses.add(ListSpell.class);
		spellClasses.add(ManaSpell.class);
		spellClasses.add(MarkSpell.class);
		spellClasses.add(MinionSpell.class);
		spellClasses.add(PainSpell.class);
		spellClasses.add(PrayerSpell.class);
		spellClasses.add(PurgeSpell.class);
		spellClasses.add(RecallSpell.class);
		spellClasses.add(RepairSpell.class);
		spellClasses.add(SafefallSpell.class);
		spellClasses.add(ScrollSpell.class);
		spellClasses.add(SpellbookSpell.class);
		spellClasses.add(StealthSpell.class);
		spellClasses.add(StonevisionSpell.class);
		spellClasses.add(SummonSpell.class);
		spellClasses.add(SunSpell.class);
		spellClasses.add(TeachSpell.class);
		spellClasses.add(TelekinesisSpell.class);
		if (getServer().getPluginManager().isPluginEnabled("BookWorm")) spellClasses.add(TomeSpell.class);
		spellClasses.add(VolleySpell.class);
		spellClasses.add(WalkwaySpell.class);
		spellClasses.add(WallSpell.class);
		spellClasses.add(ZapSpell.class);
		// load the spells
		for (Class<? extends Spell> c : spellClasses) {
			try {
				// get spell name
				String spellName;
				try {
					Field spellNameField = c.getDeclaredField("SPELL_NAME");
					spellNameField.setAccessible(true);
					spellName = (String)spellNameField.get(null);
				} catch (NoSuchFieldException e) {
					spellName = c.getSimpleName().replace("Spell", "").toLowerCase(); 
				} catch (IllegalAccessException e) {
					spellName = c.getSimpleName().replace("Spell", "").toLowerCase(); 	
				}
				// check enabled
				if (config.getBoolean("spells." + spellName + ".enabled", true)) {
					// initialize spell
					Constructor<? extends Spell> constructor = c.getConstructor(Configuration.class, String.class);
					Spell spell = constructor.newInstance(config, spellName);
					spells.put(spellName, spell);
					// add permissions
					addPermission(pm, "grant." + spellName, PermissionDefault.OP);
					addPermission(pm, "learn." + spellName, PermissionDefault.TRUE);
					addPermission(pm, "cast." + spellName, PermissionDefault.TRUE);
					addPermission(pm, "teach." + spellName, PermissionDefault.TRUE);
					permGrantChildren.put("magicspells.grant." + spellName, true);
					permLearnChildren.put("magicspells.learn." + spellName, true);
					permCastChildren.put("magicspells.cast." + spellName, true);
					permTeachChildren.put("magicspells.teach." + spellName, true);
					// spell load complete
					debug("Loaded spell: " + spellName);
				}
			} catch (Exception e) {
				getServer().getLogger().severe("MagicSpells: Failed to load spell: " + c.getName());
				e.printStackTrace();
			}
		}
		
	}
	
	private void loadCustomSpells(Configuration config, PluginManager pm, HashMap<String, Boolean> permGrantChildren, HashMap<String, Boolean> permLearnChildren, HashMap<String, Boolean> permCastChildren, HashMap<String, Boolean> permTeachChildren) {
		// load spells from plugin folder
		File[] classFiles = getDataFolder().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".class")) {
					return true;
				} else {
					return false;
				}
			}			
		});
		try {
			URLClassLoader ucl = new URLClassLoader(new URL[]{getDataFolder().toURI().toURL()}, getClassLoader());
			for (File file : classFiles) {
				try {
					// load spell from class file
					Class<? extends Spell> c = ucl.loadClass(file.getName().replace(".class", "")).asSubclass(Spell.class);
					// get spell name
					String spellName;
					try {
						Field spellNameField = c.getDeclaredField("SPELL_NAME");
						spellNameField.setAccessible(true);
						spellName = (String)spellNameField.get(null);
					} catch (NoSuchFieldException e) {
						spellName = c.getSimpleName().replace("Spell", "").toLowerCase(); 
					} catch (IllegalAccessException e) {
						spellName = c.getSimpleName().replace("Spell", "").toLowerCase(); 						
					}
					if (config.getBoolean("spells." + spellName + ".enabled", true)) {
						// initialize spell
						Spell spell = c.getConstructor(Configuration.class, String.class).newInstance(config, spellName);
						spells.put(spellName, spell);
						// add permissions
						addPermission(pm, "grant." + spellName, PermissionDefault.FALSE);
						addPermission(pm, "learn." + spellName, PermissionDefault.TRUE);
						addPermission(pm, "cast." + spellName, PermissionDefault.TRUE);
						addPermission(pm, "teach." + spellName, PermissionDefault.TRUE);
						permGrantChildren.put("magicspells.grant." + spellName, true);
						permLearnChildren.put("magicspells.learn." + spellName, true);
						permCastChildren.put("magicspells.cast." + spellName, true);
						permTeachChildren.put("magicspells.teach." + spellName, true);
						// spell load complete
						debug("Loaded external spell: " + spellName);
					}
				} catch (Exception e) {
					getServer().getLogger().severe("MagicSpells: Failed to load external spell: " + file.getName());
				}
			}
		} catch (Exception e) {
			getServer().getLogger().severe("MagicSpells: Failed to create external spells");
		}
	}
	
	private void loadSpellCopies(Configuration config, PluginManager pm, HashMap<String, Boolean> permGrantChildren, HashMap<String, Boolean> permLearnChildren, HashMap<String, Boolean> permCastChildren, HashMap<String, Boolean> permTeachChildren) {
		// load spell copies
		List<String> copies = config.getStringList("spellcopies", new ArrayList<String>());
		List<String> moreCopies = config.getStringList("spells.spellcopies", null);
		if (moreCopies != null && copies != null) {
			copies.addAll(moreCopies);
		}
		if (copies != null && copies.size() > 0) {
			for (String copy : copies) {
				String[] data = copy.split("=");
				Spell spell = spells.get(data[1]);
				String spellName = data[0];
				if (spell != null) {
					try {
						// check enabled
						if (config.getBoolean("spells." + spellName + ".enabled", true)) {
							// initialize spell
							Spell spellCopy = spell.getClass().getConstructor(Configuration.class, String.class).newInstance(config, spellName);
							spells.put(spellName, spellCopy);
							// add permissions
							addPermission(pm, "grant." + spellName, PermissionDefault.FALSE);
							addPermission(pm, "learn." + spellName, PermissionDefault.TRUE);
							addPermission(pm, "cast." + spellName, PermissionDefault.TRUE);
							addPermission(pm, "teach." + spellName, PermissionDefault.TRUE);
							permGrantChildren.put("magicspells.grant." + spellName, true);
							permLearnChildren.put("magicspells.learn." + spellName, true);
							permCastChildren.put("magicspells.cast." + spellName, true);
							permTeachChildren.put("magicspells.teach." + spellName, true);
							// load complete
							debug("Loaded spell copy: " + data[0] + " (copy of " + data[1] + ")");
						}
					} catch (Exception e) {
						getServer().getLogger().severe("MagicSpells: Failed to create spell copy: " + copy);
					}
				}
			}
		}
	}
	
	private void loadMultiSpells(Configuration config, PluginManager pm, HashMap<String, Boolean> permGrantChildren, HashMap<String, Boolean> permLearnChildren, HashMap<String, Boolean> permCastChildren, HashMap<String, Boolean> permTeachChildren) {
		// load multi-spells
		List<String> multiSpells = config.getStringList("multispells", null);
		if (multiSpells != null) {
			for (String spellName : multiSpells) {
				// initialize spell
				MultiSpell multiSpell = new MultiSpell(config, spellName);
				spells.put(spellName, multiSpell);
				// add permissions
				addPermission(pm, "grant." + spellName, PermissionDefault.FALSE);
				addPermission(pm, "learn." + spellName, PermissionDefault.TRUE);
				addPermission(pm, "cast." + spellName, PermissionDefault.TRUE);
				addPermission(pm, "teach." + spellName, PermissionDefault.TRUE);
				permGrantChildren.put("magicspells.grant." + spellName, true);
				permLearnChildren.put("magicspells.learn." + spellName, true);
				permCastChildren.put("magicspells.cast." + spellName, true);
				permTeachChildren.put("magicspells.teach." + spellName, true);
				// load complete
				debug("Loaded multi-spell: " + spellName);
			}
		}
	}
	
	private void addPermission(PluginManager pm, String perm, PermissionDefault permDefault) {
		addPermission(pm, perm, permDefault, null);
	}
	
	private void addPermission(PluginManager pm, String perm, PermissionDefault permDefault, Map<String,Boolean> children) {
		if (pm.getPermission("magicspells." + perm) == null) {
			pm.addPermission(new Permission("magicspells." + perm, permDefault, children));
		}
	}
	
	protected static void addSpellListener(Event.Type eventType, Spell spell) {
		HashSet<Spell> spells = listeners.get(eventType);
		if (spells == null) {
			spells = new HashSet<Spell>();
			listeners.put(eventType, spells);
		}
		spells.add(spell);
	}
	
	protected static void addSpellListener(MagicEventType eventType, Spell spell) {
		HashSet<Spell> spells = customListeners.get(eventType);
		if (spells == null) {
			spells = new HashSet<Spell>();
			customListeners.put(eventType, spells);
		}
		spells.add(spell);		
	}
	
	protected static void removeSpellListener(Event.Type eventType, Spell spell) {
		HashSet<Spell> spells = listeners.get(eventType);
		if (spells != null) {
			spells.remove(spell);
			if (spells.size() == 0) {
				listeners.remove(eventType);
			}
		}
	}
	
	protected static void removeSpellListener(MagicEventType eventType, Spell spell) {
		HashSet<Spell> spells = customListeners.get(eventType);
		if (spells != null) {
			spells.remove(spell);
			if (spells.size() == 0) {
				customListeners.remove(eventType);
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (command.getName().equalsIgnoreCase("magicspellcast")) {
			if (args == null || args.length == 0) {
				if (sender instanceof Player) {
					sendMessage((Player)sender, strCastUsage);
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
				Spell spell = getSpellByInGameName(args[0]);
				if (spell != null && spell.canCastByCommand() && spellbook.hasSpell(spell)) {
					String[] spellArgs = null;
					if (args.length > 1) {
						spellArgs = new String[args.length-1];
						for (int i = 1; i < args.length; i++) {
							spellArgs[i-1] = args[i];
						}
					}
					spell.cast(player, spellArgs);
				} else {
					sendMessage(player, strUnknownSpell);
				}
			} else { // not a player
				Spell spell = spellNames.get(args[0]);
				if (spell == null) {
					sender.sendMessage("Unknown spell.");
				} else {
					String[] spellArgs = null;
					if (args.length > 1) {
						spellArgs = new String[args.length-1];
						for (int i = 1; i < args.length; i++) {
							spellArgs[i-1] = args[i];
						}
					}
					boolean ok = spell.castFromConsole(sender, spellArgs);
					if (!ok) {
						sender.sendMessage("Cannot cast that spell from console.");
					}
				}
			}
			return true;
		} else if (command.getName().equalsIgnoreCase("magicspellmana")) {
			if (enableManaBars && sender instanceof Player) {
				Player player = (Player)sender;
				mana.showMana(player);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the instance of the MagicSpells plugin
	 * @return the MagicSpells plugin
	 */
	public static MagicSpells getInstance() {
		return plugin;
	}
	
	/**
	 * Gets a spell by its internal name (the key name in the config file)
	 * @param spellName the internal name of the spell to find
	 * @return the Spell found, or null if no spell with that name was found
	 */
	public static Spell getSpellByInternalName(String spellName) {
		return spells.get(spellName);
	}
	
	/**
	 * Gets a spell by its in-game name (the name specified with the 'name' config option)
	 * @param spellName the in-game name of the spell to find
	 * @return the Spell found, or null if no spell with that name was found
	 */
	public static Spell getSpellByInGameName(String spellName) {
		return spellNames.get(spellName);
	}
	
	/**
	 * Gets a player's spellbook, which contains known spells and handles spell permissions. 
	 * If a player does not have a spellbook, one will be created.
	 * @param player the player to get a spellbook for
	 * @return the player's spellbook
	 */
	public static Spellbook getSpellbook(Player player) {
		Spellbook spellbook = spellbooks.get(player.getName());
		if (spellbook == null) {
			spellbook = new Spellbook(player, plugin);
			spellbooks.put(player.getName(), spellbook);
		}
		return spellbook;
	}
	
	/**
	 * Formats a string by performing the specified replacements.
	 * @param message the string to format
	 * @param replacements the replacements to make, in pairs.
	 * @return the formatted string
	 */
	static public String formatMessage(String message, String... replacements) {
		if (message == null) return null;
		
		String msg = message;
		for (int i = 0; i < replacements.length; i+=2) {
			msg = msg.replace(replacements[i], replacements[i+1]);
		}
		return msg;
	}
	
	/**
	 * Sends a message to a player, first making the specified replacements. This method also does color replacement and has multi-line functionality.
	 * @param player the player to send the message to
	 * @param message the message to send
	 * @param replacements the replacements to be made, in pairs
	 */
	static public void sendMessage(Player player, String message, String... replacements) {
		sendMessage(player, formatMessage(message, replacements));
	}
	
	/**
	 * Sends a message to a player. This method also does color replacement and has multi-line functionality.
	 * @param player the player to send the message to
	 * @param message the message to send
	 */
	static public void sendMessage(Player player, String message) {
		if (message != null && !message.equals("")) {
			String [] msgs = message.replaceAll("&([0-9a-f])", "\u00A7$1").split("\n");
			for (String msg : msgs) {
				if (!msg.equals("")) {
					player.sendMessage(MagicSpells.textColor + msg);
				}
			}
		}
	}
	
	/**
	 * Writes a debug message to the console if the debug option is enabled.
	 * @param message the message to write to the console
	 */
	public static void debug(String message) {
		if (MagicSpells.debug) {
			plugin.getServer().getLogger().info("MagicSpells: " + message);
		}
	}
	
	/**
	 * Teaches a player a spell (adds it to their spellbook)
	 * @param player the player to teach
	 * @param spellName the spell name, either the in-game name or the internal name
	 * @return whether the spell was taught to the player
	 */
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
	
	private void loadConfigFromJar() {
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
