package com.nisovin.magicspells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.util.CastItem;
import com.nisovin.magicspells.util.ExperienceUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellReagents;
import com.nisovin.magicspells.util.Util;

public abstract class Spell implements Comparable<Spell>, Listener {

	private MagicConfig config;
	
	protected String internalName;
	protected String name;
	protected String[] aliases;	
	protected List<String> incantations;
	
	protected String description;
	protected CastItem[] castItems;
	protected boolean requireCastItemOnCommand;
	protected boolean bindable;
	protected HashSet<CastItem> bindableItems;
	protected ItemStack spellIcon;
	protected int broadcastRange;
	protected int experience;
	protected HashMap<Integer, List<String>> effects;

	protected int castTime;
	protected boolean interruptOnMove;
	protected boolean interruptOnDamage;
	
	protected SpellReagents reagents;
	protected float cooldown;
	protected HashMap<Spell, Float> sharedCooldowns;
	protected boolean ignoreGlobalCooldown;

	protected List<String> prerequisites;
	protected List<String> replaces;
	protected List<String> worldRestrictions;
	
	protected String strCost;
	protected String strCastSelf;
	protected String strCastOthers;
	protected String strOnCooldown;
	protected String strMissingReagents;
	protected String strCantCast;
	protected String strCantBind;
	protected String strWrongWorld;
	protected String strWrongCastItem;
	protected String strCastStart;
	protected String strInterrupted;
	
	private HashMap<String, Long> nextCast;
	
	public Spell(MagicConfig config, String spellName) {
		this.config = config;
		
		this.internalName = spellName;
		loadConfigData(config, spellName, "spells");
		
	}
	
	protected void loadConfigData(MagicConfig config, String spellName, String section) {
		this.name = config.getString(section + "." + spellName + ".name", spellName);
		List<String> temp = config.getStringList(section + "." + spellName + ".aliases", null);
		if (temp != null) {
			aliases = new String[temp.size()];
			aliases = temp.toArray(aliases);
		}
		incantations = config.getStringList(section + "." + spellName + ".incantations", null);
		
		// general options
		this.description = config.getString(section + "." + spellName + ".description", "");
		String[] sItems = config.getString(section + "." + spellName + ".cast-item", "-5").trim().replace(" ", "").split(",");
		this.castItems = new CastItem[sItems.length];
		for (int i = 0; i < sItems.length; i++) {
			this.castItems[i] = new CastItem(sItems[i]);
		}
		this.requireCastItemOnCommand = config.getBoolean(section + "." + spellName + ".require-cast-item-on-command", false);
		this.bindable = config.getBoolean(section + "." + spellName + ".bindable", true);
		List<String> bindables = config.getStringList(section + "." + spellName + ".bindable-items", null);
		if (bindables != null) {
			bindableItems = new HashSet<CastItem>();
			for (String s : bindables) {
				bindableItems.add(new CastItem(s));
			}
		}
		String icontemp = config.getString(section + "." + spellName + ".spell-icon", null);
		if (icontemp == null) {
			spellIcon = null;
		} else if (icontemp.contains(":")) {
			String[] icondata = icontemp.split(":");
			spellIcon = new ItemStack(Integer.parseInt(icondata[0]), 0, Short.parseShort(icondata[1]));
		} else {
			spellIcon = new ItemStack(Integer.parseInt(icontemp), 0, (short)0);
		}
		this.broadcastRange = config.getInt(section + "." + spellName + ".broadcast-range", MagicSpells.broadcastRange);
		this.experience = config.getInt(section + "." + spellName + ".experience", 0);

		// cast time
		this.castTime = config.getInt(section + "." + spellName + ".cast-time", 0);
		this.interruptOnMove = config.getBoolean(section + "." + spellName + ".interrupt-on-move", true);
		this.interruptOnDamage = config.getBoolean(section + "." + spellName + ".interrupt-on-damage", false);
		
		// graphical effects
		List<String> effectsList = config.getStringList(section + "." + spellName + ".effects", null);
		if (effectsList != null) {
			this.effects = new HashMap<Integer, List<String>>();
			List<String> e;
			for (String eff : effectsList) {
				String[] data = eff.split(" ", 2);
				int pos = -1;
				if (data[0].equals("0") || data[0].equalsIgnoreCase("start") || data[0].equalsIgnoreCase("startcast")) {
					pos = 0;
				} else if (data[0].equals("1") || data[0].equalsIgnoreCase("pos1") || data[0].equalsIgnoreCase("position1") || data[0].equalsIgnoreCase("caster") || data[0].equalsIgnoreCase("actor")) {
					pos = 1;
				} else if (data[0].equals("2") || data[0].equalsIgnoreCase("pos2") || data[0].equalsIgnoreCase("position2") || data[0].equalsIgnoreCase("target")) {
					pos = 2;
				} else if (data[0].equals("3") || data[0].equalsIgnoreCase("line") || data[0].equalsIgnoreCase("trail")) {
					pos = 3;
				} else if (data[0].equals("4") || data[0].equalsIgnoreCase("delayed") || data[0].equalsIgnoreCase("disabled") || data[0].equalsIgnoreCase("special")) {
					pos = 4;
				}
				if (pos >= 0) {
					e = effects.get(pos);
					if (e == null) {
						e = new ArrayList<String>();
						effects.put(pos, e);
					}
					e.add(data[1]);
				}
			}
		}
		
		// cost
		reagents = new SpellReagents();
		List<String> costList = config.getStringList(section + "." + spellName + ".cost", null);
		if (costList != null && costList.size() > 0) {
			//cost = new ItemStack [costList.size()];
			String[] data, subdata;
			for (int i = 0; i < costList.size(); i++) {
				String costVal = costList.get(i);
				
				// validate cost data
				if (!costVal.matches("^([1-9][0-9]*(:[1-9][0-9]*)?|mana|health|hunger|experience|levels) [1-9][0-9]*$")) {
					MagicSpells.error("Failed to process cost value for " + spellName + " spell: " + costVal);
					continue;
				}
				
				// parse cost data
				data = costVal.split(" ");
				if (data[0].equalsIgnoreCase("health")) {
					reagents.setHealth(Integer.parseInt(data[1]));
				} else if (data[0].equalsIgnoreCase("mana")) {
					reagents.setMana(Integer.parseInt(data[1]));
				} else if (data[0].equalsIgnoreCase("hunger")) {
					reagents.setHunger(Integer.parseInt(data[1]));
				} else if (data[0].equalsIgnoreCase("experience")) {
					reagents.setExperience(Integer.parseInt(data[1]));
				} else if (data[0].equalsIgnoreCase("levels")) {
					reagents.setLevels(Integer.parseInt(data[1]));
				} else if (data[0].contains(":")) {
					subdata = data[0].split(":");
					reagents.addItem(new ItemStack(Integer.parseInt(subdata[0]), Integer.parseInt(data[1]), Short.parseShort(subdata[1])));
				} else {
					reagents.addItem(new ItemStack(Integer.parseInt(data[0]), Integer.parseInt(data[1])));
				}
			}
		}
		
		// cooldowns
		this.cooldown = (float)config.getDouble(section + "." + spellName + ".cooldown", 0);
		List<String> cooldowns = config.getStringList(section + "." + spellName + ".shared-cooldowns", null);
		if (cooldowns != null) {
			this.sharedCooldowns = new HashMap<Spell,Float>();
			for (String s : cooldowns) {
				String[] data = s.split(" ");
				Spell spell = MagicSpells.getSpellByInternalName(data[0]);
				float cd = Float.parseFloat(data[1]);
				if (spell != null) {
					this.sharedCooldowns.put(spell, cd);
				}
			}
		}
		this.ignoreGlobalCooldown = config.getBoolean(section + "." + spellName + ".ignore-global-cooldown", false);
		this.nextCast = new HashMap<String, Long>();

		// hierarchy options
		this.prerequisites = config.getStringList(section + "." + spellName + ".prerequisites", null);
		this.replaces = config.getStringList(section + "." + spellName + ".replaces", null);
		this.worldRestrictions = config.getStringList(section + "." + spellName + ".restrict-to-worlds", null);
		
		// strings
		this.strCost = config.getString(section + "." + spellName + ".str-cost", null);
		this.strCastSelf = config.getString(section + "." + spellName + ".str-cast-self", null);
		this.strCastOthers = config.getString(section + "." + spellName + ".str-cast-others", null);
		this.strOnCooldown = config.getString(section + "." + spellName + ".str-on-cooldown", MagicSpells.strOnCooldown);
		this.strMissingReagents = config.getString(section + "." + spellName + ".str-missing-reagents", MagicSpells.strMissingReagents);
		this.strCantCast = config.getString(section + "." + spellName + ".str-cant-cast", MagicSpells.strCantCast);
		this.strCantBind = config.getString(section + "." + spellName + ".str-cant-bind", null);
		this.strWrongWorld = config.getString(section + "." + spellName + ".str-wrong-world", MagicSpells.strWrongWorld);
		this.strWrongCastItem = config.getString(section + "." + spellName + ".str-wrong-cast-item", strCantCast);
		this.strCastStart = config.getString(section + "." + spellName + ".str-cast-start", null);
		this.strInterrupted = config.getString(section + "." + spellName + ".str-interrupted", null);
	}
	
	/**
	 * Access an integer config value for this spell.
	 * 
	 * @param key The key of the config value
	 * @param defaultValue The value to return if it does not exist in the config
	 * 
	 * @return The config value, or defaultValue if it does not exist
	 */
	protected int getConfigInt(String key, int defaultValue) {
		return config.getInt("spells." + internalName + "." + key, defaultValue);
	}
	
	/**
	 * Access a boolean config value for this spell.
	 * 
	 * @param key The key of the config value
	 * @param defaultValue The value to return if it does not exist in the config
	 * 
	 * @return The config value, or defaultValue if it does not exist
	 */
	protected boolean getConfigBoolean(String key, boolean defaultValue) {
		return config.getBoolean("spells." + internalName + "." + key, defaultValue);
	}
	
	/**
	 * Access a String config value for this spell.
	 * 
	 * @param key The key of the config value
	 * @param defaultValue The value to return if it does not exist in the config
	 * 
	 * @return The config value, or defaultValue if it does not exist
	 */
	protected String getConfigString(String key, String defaultValue) {
		return config.getString("spells." + internalName + "." + key, defaultValue);
	}
	
	/**
	 * Access a float config value for this spell.
	 * 
	 * @param key The key of the config value
	 * @param defaultValue The value to return if it does not exist in the config
	 * 
	 * @return The config value, or defaultValue if it does not exist
	 */
	protected float getConfigFloat(String key, float defaultValue) {
		return (float)config.getDouble("spells." + internalName + "." + key, defaultValue);
	}
	
	protected List<Integer> getConfigIntList(String key, List<Integer> defaultValue) {
		return config.getIntList("spells." + internalName + "." + key, defaultValue);
	}
	
	protected List<String> getConfigStringList(String key, List<String> defaultValue) {
		return config.getStringList("spells." + internalName + "." + key, defaultValue);
	}

	public final SpellCastResult cast(Player player) {
		return cast(player, null);
	}
	
	public final SpellCastResult cast(Player player, String[] args) {
		MagicSpells.debug(1, "Player " + player.getName() + " is trying to cast " + internalName);
		
		// get spell state
		SpellCastState state;
		if (!MagicSpells.getSpellbook(player).canCast(this)) {
			state = SpellCastState.CANT_CAST;
		} else if (worldRestrictions != null && !worldRestrictions.contains(player.getWorld().getName())) {
			state = SpellCastState.WRONG_WORLD;
		} else if (MagicSpells.noMagicZones != null && MagicSpells.noMagicZones.willFizzle(player, this)) {
			state = SpellCastState.NO_MAGIC_ZONE;
		} else if (onCooldown(player)) {
			state = SpellCastState.ON_COOLDOWN;
		} else if (!hasReagents(player)) {
			state = SpellCastState.MISSING_REAGENTS;
		} else {
			state = SpellCastState.NORMAL;
		}
		
		MagicSpells.debug(2, "    Spell cast state: " + state);
		
		// call events
		float power = 1.0F;
		float cooldown = this.cooldown;
		int castTime = this.castTime;
		SpellReagents reagents = this.reagents.clone();
		SpellCastEvent event = new SpellCastEvent(this, player, state, power, args, cooldown, reagents, castTime);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			MagicSpells.debug(2, "    Spell canceled");
			return new SpellCastResult(SpellCastState.CANT_CAST, PostCastAction.HANDLE_NORMALLY);
		} else {
			cooldown = event.getCooldown();
			power = event.getPower();
			castTime = event.getCastTime();
			if (event.haveReagentsChanged()) {
				reagents = event.getReagents();
				if (!hasReagents(player, reagents)) {
					state = SpellCastState.MISSING_REAGENTS;
					MagicSpells.debug(2, "    Spell cast state changed: " + state);
				}
			}
			if (event.hasSpellCastStateChanged()) {
				state = event.getSpellCastState();
				MagicSpells.debug(2, "    Spell cast state changed: " + state);
			}
		}
		if (player.hasPermission("magicspells.nocasttime")) {
			castTime = 0;
		}
		
		// cast spell
		PostCastAction action;
		MagicSpells.debug(3, "    Cast time: " + castTime);
		if (castTime <= 0 || state != SpellCastState.NORMAL) {
			action = handleCast(player, state, power, cooldown, reagents, args);
		} else {
			action = PostCastAction.DELAYED;
			sendMessage(player, strCastStart);
			playSpellEffects(EffectPosition.START_CAST, player);
			if (MagicSpells.useExpBarAsCastTimeBar) {
				new DelayedSpellCastWithBar(player, this, state, power, cooldown, reagents, castTime);
			} else {
				new DelayedSpellCast(player, this, state, power, cooldown, reagents, castTime);
			}
		}
		
		return new SpellCastResult(state, action);
	}
	
	private PostCastAction handleCast(Player player, SpellCastState state, float power, float cooldown, SpellReagents reagents, String[] args) {
		MagicSpells.debug(3, "    Power: " + power);
		MagicSpells.debug(3, "    Cooldown: " + cooldown);
		if (MagicSpells.debug && args != null && args.length > 0) {
			MagicSpells.debug(3, "    Args: {" + Util.arrayJoin(args, ',') + "}");
		}
		PostCastAction action = castSpell(player, state, power, args);
		MagicSpells.debug(3, "    Post-cast action: " + action);

		if (action != null && action != PostCastAction.ALREADY_HANDLED) {
			if (state == SpellCastState.NORMAL) {
				if (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.COOLDOWN_ONLY || action == PostCastAction.NO_MESSAGES || action == PostCastAction.NO_REAGENTS) {
					setCooldown(player, cooldown);
				}
				if (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.REAGENTS_ONLY || action == PostCastAction.NO_MESSAGES || action == PostCastAction.NO_COOLDOWN) {
					removeReagents(player, reagents);
				}
				if (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.MESSAGES_ONLY || action == PostCastAction.NO_COOLDOWN || action == PostCastAction.NO_REAGENTS) {
					sendMessage(player, strCastSelf, "%a", player.getDisplayName());
					sendMessageNear(player, formatMessage(strCastOthers, "%a", player.getDisplayName()));
				}
				if (experience > 0) {
					player.giveExp(experience);
				}
			} else if (state == SpellCastState.ON_COOLDOWN) {
				MagicSpells.sendMessage(player, formatMessage(strOnCooldown, "%c", Math.round(getCooldown(player))+""));
			} else if (state == SpellCastState.MISSING_REAGENTS) {
				MagicSpells.sendMessage(player, strMissingReagents);
				if (MagicSpells.showStrCostOnMissingReagents && strCost != null && !strCost.isEmpty()) {
					MagicSpells.sendMessage(player, "    (" + strCost + ")");
				}
			} else if (state == SpellCastState.CANT_CAST) {
				MagicSpells.sendMessage(player, strCantCast);
			} else if (state == SpellCastState.NO_MAGIC_ZONE) {
				MagicSpells.noMagicZones.sendNoMagicMessage(player, this);
			} else if (state == SpellCastState.WRONG_WORLD) {
				MagicSpells.sendMessage(player, strWrongWorld);
			}
		}
		
		SpellCastedEvent event = new SpellCastedEvent(this, player, state, power, args, cooldown, reagents, action);
		Bukkit.getPluginManager().callEvent(event);
		
		return action;
	}

	/**
	 * This method is called when a player casts a spell, either by command, with a wand item, or otherwise.
	 * @param player the player casting the spell
	 * @param state the state of the spell cast (normal, on cooldown, missing reagents, etc)
	 * @param power the power multiplier the spell should be cast with (1.0 is normal)
	 * @param args the spell arguments, if cast by command
	 * @return the action to take after the spell is processed
	 */
	public abstract PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args);
	
	public String tabComplete(CommandSender sender, String partial) {
		return null;
	}
	
	/**
	 * This method is called when the spell is cast from the console.
	 * @param sender the console sender.
	 * @param args the command arguments
	 * @return true if the spell was handled, false otherwise
	 */
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	public abstract boolean canCastWithItem();
	
	public abstract boolean canCastByCommand();
	
	public boolean isValidItemForCastCommand(ItemStack item) {
		if (!requireCastItemOnCommand || castItems == null) {
			return true;
		} else if (item == null && castItems.length == 1 && castItems[0].getItemTypeId() == 0) {
			return true;
		} else {
			for (CastItem castItem : castItems) {
				if (castItem.equals(item)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public boolean canBind(CastItem item) {
		if (!bindable) {
			return false;
		} else if (bindableItems == null)  {
			return true;
		} else {
			return bindableItems.contains(item);
		}
	}
	
	public ItemStack getSpellIcon() {
		return spellIcon;
	}
	
	public String getCostStr() {
		if (strCost == null || strCost.equals("")) {
			return null;
		} else {
			return strCost;
		}
	}
	
	/**
	 * Check whether this spell is currently on cooldown for the specified player
	 * @param player The player to check
	 * @return whether the spell is on cooldown
	 */
	public boolean onCooldown(Player player) {
		if (player.hasPermission("magicspells.nocooldown")) {
			return false;
		}
		
		Long next = nextCast.get(player.getName());
		if (next != null) {
			if (next > System.currentTimeMillis()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get how many seconds remain on the cooldown of this spell for the specified player
	 * @param player The player to check
	 * @return The number of seconds remaining in the cooldown
	 */
	public float getCooldown(Player player) {
		Long next = nextCast.get(player.getName());
		if (next != null) {
			float c = (next - System.currentTimeMillis()) / 1000F;
			return c > 0 ? c : 0;
		} else {
			return 0;
		}
	}
	
	/**
	 * Begins the cooldown for the spell for the specified player
	 * @param player The player to set the cooldown for
	 */
	public void setCooldown(Player player, float cooldown) {
		setCooldown(player, cooldown, true);
	}
	
	/**
	 * Begins the cooldown for the spell for the specified player
	 * @param player The player to set the cooldown for
	 */
	public void setCooldown(Player player, float cooldown, boolean activateSharedCooldowns) {
		if (cooldown > 0) {
			nextCast.put(player.getName(), System.currentTimeMillis() + (int)(cooldown * 1000));
		}
		if (activateSharedCooldowns && sharedCooldowns != null) {
			for (Map.Entry<Spell, Float> scd : sharedCooldowns.entrySet()) {
				scd.getKey().setCooldown(player, scd.getValue(), false);
			}
		}
	}
	
	/**
	 * Checks if a player has the reagents required to cast this spell
	 * @param player the player to check
	 * @return true if the player has the reagents, false otherwise
	 */
	protected boolean hasReagents(Player player) {
		return hasReagents(player, reagents);
	}
	
	/**
	 * Checks if a player has the reagents required to cast this spell
	 * @param player the player to check
	 * @param reagents the reagents to check for
	 * @return true if the player has the reagents, false otherwise
	 */
	protected boolean hasReagents(Player player, SpellReagents reagents) {
		return hasReagents(player, reagents.getItemsAsArray(), reagents.getHealth(), reagents.getMana(), reagents.getHunger(), reagents.getExperience(), reagents.getLevels());
	}
	
	/**
	 * Checks if a player has the specified reagents
	 * @param player the player to check
	 * @param cost the reagents to look for
	 * @return true if the player has the reagents, false otherwise
	 */
	protected boolean hasReagents(Player player, ItemStack[] cost) {
		return hasReagents(player, cost, 0, 0, 0, 0, 0);
	}
	
	/**
	 * Checks if a player has the specified reagents, including health and mana
	 * @param player the player to check
	 * @param reagents the inventory item reagents to look for
	 * @param healthCost the health cost, in half-hearts
	 * @param manaCost the mana cost
	 * @return true if the player has all the reagents, false otherwise
	 */
	private boolean hasReagents(Player player, ItemStack[] reagents, int healthCost, int manaCost, int hungerCost, int experienceCost, int levelsCost) {
		if (player.hasPermission("magicspells.noreagents")) {
			return true;
		}
		if (reagents == null && healthCost <= 0 && manaCost <= 0 && hungerCost <= 0) {
			return true;
		}
		if (healthCost > 0 && player.getHealth() <= healthCost) {
			return false;
		}
		if (manaCost > 0 && !MagicSpells.mana.hasMana(player, manaCost)) {
			return false;
		}
		if (hungerCost > 0 && player.getFoodLevel() < hungerCost) {
			return false;
		}
		if (experienceCost > 0 && !ExperienceUtils.hasExp(player, experienceCost)) {
			return false;
		}
		if (levelsCost > 0 && player.getLevel() < levelsCost) {
			return false;
		}
		for (ItemStack item : reagents) {
			if (item != null && !inventoryContains(player.getInventory(), item)) {
				return false;
			}
		}
		return true;		
	}
	
	/**
	 * Removes the reagent cost of this spell from the player's inventoryy.
	 * This does not check if the player has the reagents, use hasReagents() for that.
	 * @param player the player to remove reagents from
	 */
	protected void removeReagents(Player player) {
		removeReagents(player, reagents);
	}
	
	/**
	 * Removes the specified reagents from the player's inventoryy.
	 * This does not check if the player has the reagents, use hasReagents() for that.
	 * @param player the player to remove the reagents from
	 * @param reagents the inventory item reagents to remove
	 */
	protected void removeReagents(Player player, ItemStack[] reagents) {
		removeReagents(player, reagents, 0, 0, 0, 0, 0);
	}
	
	protected void removeReagents(Player player, SpellReagents reagents) {
		removeReagents(player, reagents.getItemsAsArray(), reagents.getHealth(), reagents.getMana(), reagents.getHunger(), reagents.getExperience(), reagents.getLevels());
	}
	
	/**
	 * Removes the specified reagents, including health and mana, from the player's inventory.
	 * This does not check if the player has the reagents, use hasReagents() for that.
	 * @param player the player to remove the reagents from
	 * @param reagents the inventory item reagents to remove
	 * @param healthCost the health to remove
	 * @param manaCost the mana to remove
	 */
	private void removeReagents(Player player, ItemStack[] reagents, int healthCost, int manaCost, int hungerCost, int experienceCost, int levelsCost) {
		if (player.hasPermission("magicspells.noreagents")) {
			return;
		}
		if (reagents != null) {
			for (ItemStack item : reagents) {
				if (item != null) {
					removeFromInventory(player.getInventory(), item);
				}
			}
		}
		if (healthCost > 0) {
			player.setHealth(player.getHealth() - healthCost);
		}
		if (manaCost > 0) {
			MagicSpells.mana.removeMana(player, manaCost, ManaChangeReason.SPELL_COST);
		}
		if (hungerCost > 0) {
			player.setFoodLevel(player.getFoodLevel() - hungerCost);
		}
		if (experienceCost > 0) {
			ExperienceUtils.changeExp(player, -experienceCost);
		}
		if (levelsCost > 0) {
			int lvl = player.getLevel() - levelsCost;
			if (lvl < 0) lvl = 0;
			player.setLevel(lvl);
		}
	}
	
	private boolean inventoryContains(Inventory inventory, ItemStack item) {
		int count = 0;
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
				count += items[i].getAmount();
			}
			if (count >= item.getAmount()) {
				return true;
			}
		}
		return false;
	}
	
	private void removeFromInventory(Inventory inventory, ItemStack item) {
		int amt = item.getAmount();
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
				if (items[i].getAmount() > amt) {
					items[i].setAmount(items[i].getAmount() - amt);
					break;
				} else if (items[i].getAmount() == amt) {
					items[i] = null;
					break;
				} else {
					amt -= items[i].getAmount();
					items[i] = null;
				}
			}
		}
		inventory.setContents(items);
	}
	
	protected void playSpellEffects(Entity pos1, Entity pos2) {
		playSpellEffects(EffectPosition.CASTER, pos1);
		playSpellEffects(EffectPosition.TARGET, pos2);
		playSpellEffectsTrail(pos1.getLocation(), pos2.getLocation(), null);
	}
	
	protected void playSpellEffects(Entity pos1, Location pos2) {
		playSpellEffects(EffectPosition.CASTER, pos1);
		playSpellEffects(EffectPosition.TARGET, pos2);
		playSpellEffectsTrail(pos1.getLocation(), pos2, null);
	}
	
	protected void playSpellEffects(Location pos1, Entity pos2) {
		playSpellEffects(EffectPosition.CASTER, pos1);
		playSpellEffects(EffectPosition.TARGET, pos2);
		playSpellEffectsTrail(pos1, pos2.getLocation(), null);
	}
	
	protected void playSpellEffects(Location pos1, Location pos2) {
		playSpellEffects(EffectPosition.CASTER, pos1);
		playSpellEffects(EffectPosition.TARGET, pos2);
		playSpellEffectsTrail(pos1, pos2, null);
	}
	
	protected void playSpellEffects(EffectPosition pos, Entity entity) {
		playSpellEffects(pos, entity, null);
	}
	
	protected void playSpellEffects(EffectPosition pos, Entity entity, String param) {
		if (effects != null) {
			List<String> effectsList = effects.get(pos.getId());
			if (effectsList != null) {
				for (String eff : effectsList) {
					SpellEffect effect = null;
					if (eff.contains(" ")) {
						String[] data = eff.split(" ", 2);
						effect = SpellEffect.getEffectByName(data[0]);
						param = data[1];
					} else {
						effect = SpellEffect.getEffectByName(eff);
					}
					if (effect != null) {
						effect.playEffect(entity, param);
					}
				}
			}
		}
	}
	
	protected void playSpellEffects(EffectPosition pos, Location location) {
		playSpellEffects(pos, location, null);
	}
	
	protected void playSpellEffects(EffectPosition pos, Location location, String param) {
		if (effects != null) {
			List<String> effectsList = effects.get(pos.getId());
			if (effectsList != null) {
				for (String eff : effectsList) {
					SpellEffect effect = null;
					if (eff.contains(" ")) {
						String[] data = eff.split(" ", 2);
						effect = SpellEffect.getEffectByName(data[0]);
						param = data[1];
					} else {
						effect = SpellEffect.getEffectByName(eff);
					}
					if (effect != null) {
						effect.playEffect(location, param);
					}
				}
			}
		}
	}
	
	protected void playSpellEffectsTrail(Location loc1, Location loc2, String param) {
		if (effects != null) {
			List<String> effectsList = effects.get(EffectPosition.TRAIL.getId());
			if (effectsList != null) {
				for (String eff : effectsList) {
					SpellEffect effect = null;
					if (eff.contains(" ")) {
						String[] data = eff.split(" ", 2);
						effect = SpellEffect.getEffectByName(data[0]);
						param = data[1];
					} else {
						effect = SpellEffect.getEffectByName(eff);
					}
					if (effect != null) {
						effect.playEffect(loc1, loc2, param);
					}
				}
			}
		}
	}
	
	protected void registerEvents() {
		registerEvents(this);
	}
	
	protected void registerEvents(Listener listener) {
		Bukkit.getPluginManager().registerEvents(listener, MagicSpells.plugin);
	}
	
	protected void unregisterEvents() {
		unregisterEvents(this);
	}
	
	protected void unregisterEvents(Listener listener) {
		HandlerList.unregisterAll(listener);
	}
	
	/**
	 * Formats a string by performing the specified replacements.
	 * @param message the string to format
	 * @param replacements the replacements to make, in pairs.
	 * @return the formatted string
	 */
	protected String formatMessage(String message, String... replacements) {
		return MagicSpells.formatMessage(message, replacements);
	}
	
	/**
	 * Sends a message to a player, first making the specified replacements. This method also does color replacement and has multi-line functionality.
	 * @param player the player to send the message to
	 * @param message the message to send
	 * @param replacements the replacements to be made, in pairs
	 */
	protected void sendMessage(Player player, String message, String... replacements) {
		sendMessage(player, formatMessage(message, replacements));
	}
	
	/**
	 * Sends a message to a player. This method also does color replacement and has multi-line functionality.
	 * @param player the player to send the message to
	 * @param message the message to send
	 */
	protected void sendMessage(Player player, String message) {
		MagicSpells.sendMessage(player, message);
	}
	
	/**
	 * Sends a message to all players near the specified player, within the configured broadcast range.
	 * @param player the "center" player used to find nearby players
	 * @param message the message to send
	 */
	protected void sendMessageNear(Player player, String message) {
		sendMessageNear(player, null, message, broadcastRange);
	}
	
	/**
	 * Sends a message to all players near the specified player, within the specified broadcast range.
	 * @param player the "center" player used to find nearby players
	 * @param message the message to send
	 * @param range the broadcast range
	 */
	protected void sendMessageNear(Player player, Player ignore, String message, int range) {
		if (message != null && !message.equals("") && !player.hasPermission("magicspells.silent")) {
			String [] msgs = message.replaceAll("&([0-9a-f])", "\u00A7$1").split("\n");
			List<Entity> entities = player.getNearbyEntities(range*2, range*2, range*2);
			for (Entity entity : entities) {
				if (entity instanceof Player && entity != player && entity != ignore) {
					for (String msg : msgs) {
						if (!msg.equals("")) {
							((Player)entity).sendMessage(MagicSpells.textColor + msg);
						}
					}
				}
			}
		}
	}
	
	public String getInternalName() {
		return this.internalName;
	}
	
	public String getName() {
		if (this.name != null && !this.name.isEmpty()) {
			return this.name;
		} else {
			return this.internalName;
		}
	}
	
	public String getCantBindError() {
		return strCantBind;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public List<String> getIncantations() {
		return this.incantations;
	}
	
	public CastItem getCastItem() {
		if (this.castItems.length == 1) {
			return this.castItems[0];
		} else {
			return null;
		}
	}
	
	public CastItem[] getCastItems() {
		return this.castItems;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public SpellReagents getReagents() {
		return this.reagents;
	}
	
	public String getConsoleName() {
		return MagicSpells.strConsoleName;
	}
	
	public String getStrWrongCastItem() {
		return strWrongCastItem;
	}
	
	/**
	 * This method is called immediately after all spells have been loaded.
	 */
	protected void initialize() {
		registerEvents();
	}
	
	/**
	 * This method is called when the plugin is being disabled, for any reason.
	 */
	protected void turnOff() {
	}
	
	@Override
	public int compareTo(Spell spell) {
		return this.name.compareTo(spell.name);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Spell && ((Spell)o).internalName.equals(this.internalName)) {
			return true;
		} else {
			return false;
		}		
	}
	
	@Override
	public int hashCode() {
		return internalName.hashCode();
	}
	
	public enum SpellCastState {
		NORMAL,
		ON_COOLDOWN,
		MISSING_REAGENTS,
		CANT_CAST,
		NO_MAGIC_ZONE,
		WRONG_WORLD
	}
	
	public enum PostCastAction {
		HANDLE_NORMALLY(true, true, true),
		ALREADY_HANDLED(false, false, false),
		NO_MESSAGES(true, true, false),
		NO_REAGENTS(true, false, true),
		NO_COOLDOWN(false, true, true),
		MESSAGES_ONLY(false, false, true),
		REAGENTS_ONLY(false, true, false),
		COOLDOWN_ONLY(true, false, false),
		DELAYED(false, false, false);
		
		private boolean cooldown;
		private boolean reagents;
		private boolean messages;
		private PostCastAction(boolean cooldown, boolean reagents, boolean messages) {
			this.cooldown = cooldown;
			this.reagents = reagents;
			this.messages = messages;
		}
		
		public boolean setCooldown() {
			return cooldown;
		}
		
		public boolean chargeReagents() {
			return reagents;
		}
		
		public boolean sendMessages() {
			return messages;
		}
	}
	
	public class SpellCastResult {
		public SpellCastState state;
		public PostCastAction action;
		public SpellCastResult(SpellCastState state, PostCastAction action) {
			this.state = state;
			this.action = action;
		}
	}
	
	public class DelayedSpellCast implements Runnable, Listener {
		private Player player;
		private Location prevLoc;
		private Spell spell;
		private SpellCastState state;
		private float power;
		private float cooldown;
		private SpellReagents reagents;
		private int taskId;
		private boolean cancelled = false;
		
		public DelayedSpellCast(Player player, Spell spell, SpellCastState state, float power, float cooldown, SpellReagents reagents, int castTime) {
			this.player = player;
			this.prevLoc = player.getLocation().clone();
			this.spell = spell;
			this.state = state;
			this.power = power;
			this.cooldown = cooldown;
			this.reagents = reagents;
			
			taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, this, castTime);
			if (interruptOnDamage) {
				registerEvents(this);
			}
		}
		
		@Override
		public void run() {
			if (!cancelled && player.isOnline() && !player.isDead()) {
				Location currLoc = player.getLocation();
				if (!interruptOnMove || (Math.abs(currLoc.getX() - prevLoc.getX()) < .01 && Math.abs(currLoc.getY() - prevLoc.getY()) < .01 && Math.abs(currLoc.getZ() - prevLoc.getZ()) < .01)) {
					if (!hasReagents(player)) {
						state = SpellCastState.MISSING_REAGENTS;
					}
					spell.handleCast(player, state, power, cooldown, reagents, null);
				} else {
					sendMessage(player, strInterrupted);
				}
			}
			HandlerList.unregisterAll(this);
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onDamage(EntityDamageEvent event) {
			if (event.getEntity().equals(player)) {
				cancelled = true;
				Bukkit.getScheduler().cancelTask(taskId);
				sendMessage(player, strInterrupted);
			}
		}
	}
	
	public class DelayedSpellCastWithBar implements Runnable, Listener {
		private Player player;
		private Location prevLoc;
		private Spell spell;
		private SpellCastState state;
		private float power;
		private float cooldown;
		private SpellReagents reagents;
		private int castTime;
		private int taskId;
		private boolean cancelled = false;
		
		private int interval = 5;
		private int elapsed = 0;
		
		public DelayedSpellCastWithBar(Player player, Spell spell, SpellCastState state, float power, float cooldown, SpellReagents reagents, int castTime) {
			this.player = player;
			this.prevLoc = player.getLocation().clone();
			this.spell = spell;
			this.state = state;
			this.power = power;
			this.cooldown = cooldown;
			this.reagents = reagents;
			this.castTime = castTime;
			
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, interval, interval);
			if (interruptOnDamage) {
				registerEvents(this);
			}
		}
		
		@Override
		public void run() {
			if (!cancelled && player.isOnline() && !player.isDead()) {
				elapsed += interval;
				Location currLoc = player.getLocation();
				if (!interruptOnMove || (Math.abs(currLoc.getX() - prevLoc.getX()) < .01 && Math.abs(currLoc.getY() - prevLoc.getY()) < .01 && Math.abs(currLoc.getZ() - prevLoc.getZ()) < .01)) {
					if (elapsed >= castTime) {
						if (!hasReagents(player)) {
							state = SpellCastState.MISSING_REAGENTS;
						}
						spell.handleCast(player, state, power, cooldown, reagents, null);
						cancelled = true;
					}
					MagicSpells.getVolatileCodeHandler().setExperienceBar(player, 0, ((float)elapsed / (float)castTime));
				} else {
					sendMessage(player, strInterrupted);
					end();
				}
			} else {
				end();
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onDamage(EntityDamageEvent event) {
			if (!cancelled && event.getEntity().equals(player)) {
				cancelled = true;
				sendMessage(player, strInterrupted);
			}
		}
		
		private void end() {
			cancelled = true;
			Bukkit.getScheduler().cancelTask(taskId);
			HandlerList.unregisterAll(this);
			MagicSpells.getVolatileCodeHandler().setExperienceBar(player, player.getLevel(), player.getExp());
		}
	}

}
