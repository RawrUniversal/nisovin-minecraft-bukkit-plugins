package com.nisovin.MagicSpells;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

public abstract class Spell implements Comparable<Spell> {

	private Configuration config;
	protected String internalName;
	protected String name;
	protected String description;
	protected int castItem;
	protected ItemStack[] cost;
	protected int healthCost = 0;
	protected int manaCost = 0;
	protected int cooldown;
	protected int broadcastRange;
	protected String strCost;
	protected String strCastSelf;
	protected String strCastOthers;
	
	private HashMap<String, Long> lastCast;
	
	public Spell(Configuration config, String spellName) {
		this.config = config;
		this.internalName = spellName;
		this.name = config.getString("spells." + spellName + ".name", spellName);
		this.description = config.getString("spells." + spellName + ".description", "");
		this.castItem = config.getInt("spells." + spellName + ".cast-item", 280);
		List<String> costList = config.getStringList("spells." + spellName + ".cost", null);
		if (costList != null && costList.size() > 0) {
			cost = new ItemStack [costList.size()];
			String[] data, subdata;
			for (int i = 0; i < costList.size(); i++) {
				if (costList.get(i).contains(" ")) {
					data = costList.get(i).split(" ");
					if (data[0].equalsIgnoreCase("health")) {
						healthCost = Integer.parseInt(data[1]);
					} else if (data[0].equalsIgnoreCase("mana")) {
						manaCost = Integer.parseInt(data[1]);
					} else if (data[0].contains(":")) {
						subdata = data[0].split(":");
						cost[i] = new ItemStack(Integer.parseInt(subdata[0]), Integer.parseInt(data[1]), Short.parseShort(subdata[1]));
					} else {
						cost[i] = new ItemStack(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
					}
				} else {
					cost[i] = new ItemStack(Integer.parseInt(costList.get(i)));
				}
			}
		} else {
			cost = null;
		}
		this.cooldown = config.getInt("spells." + spellName + ".cooldown", 0);
		this.broadcastRange = config.getInt("spells." + spellName + ".broadcast-range", MagicSpells.broadcastRange);
		this.strCost = config.getString("spells." + spellName + ".str-cost", null);
		this.strCastSelf = config.getString("spells." + spellName + ".str-cast-self", null);
		this.strCastOthers = config.getString("spells." + spellName + ".str-cast-others", null);
		
		if (cooldown > 0) {
			lastCast = new HashMap<String, Long>();
		}
	}
	
	protected int getConfigInt(String key, int defaultValue) {
		return config.getInt("spells." + internalName + "." + key, defaultValue);
	}
	
	protected boolean getConfigBoolean(String key, boolean defaultValue) {
		return config.getBoolean("spells." + internalName + "." + key, defaultValue);
	}
	
	protected String getConfigString(String key, String defaultValue) {
		return config.getString("spells." + internalName + "." + key, defaultValue);
	}
	
	protected List<Integer> getConfigIntList(String key, List<Integer> defaultValue) {
		return config.getIntList("spells." + internalName + "." + key, defaultValue);
	}
	
	protected List<String> getConfigStringList(String key, List<String> defaultValue) {
		return config.getStringList("spells." + internalName + "." + key, defaultValue);
	}

	public final SpellCastState cast(Player player) {
		return cast(player, null);
	}
	
	public final SpellCastState cast(Player player, String[] args) {
		MagicSpells.debug("Player " + player.getName() + " is trying to cast " + internalName);
		SpellCastState state;
		if (!MagicSpells.getSpellbook(player).canCast(this)) {
			state = SpellCastState.CANT_CAST;
		} else if (MagicSpells.noMagicZones != null && MagicSpells.noMagicZones.inNoMagicZone(player)) {
			state = SpellCastState.NO_MAGIC_ZONE;
		} else if (onCooldown(player)) {
			state = SpellCastState.ON_COOLDOWN;
		} else if (!hasReagents(player)) {
			state = SpellCastState.MISSING_REAGENTS;
		} else {
			state = SpellCastState.NORMAL;
		}
		
		PostCastAction action = castSpell(player, state, args);
		if (action != null && action != PostCastAction.ALREADY_HANDLED) {
			if (state == SpellCastState.NORMAL) {
				if (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.COOLDOWN_ONLY || action == PostCastAction.NO_MESSAGES || action == PostCastAction.NO_REAGENTS) {
					setCooldown(player);
				}
				if (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.REAGENTS_ONLY || action == PostCastAction.NO_MESSAGES || action == PostCastAction.NO_COOLDOWN) {
					removeReagents(player);
				}
				if (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.MESSAGES_ONLY || action == PostCastAction.NO_COOLDOWN || action == PostCastAction.NO_REAGENTS) {
					sendMessage(player, strCastSelf);
					sendMessageNear(player, formatMessage(strCastOthers, "%a", player.getName()));
				}
			} else if (state == SpellCastState.ON_COOLDOWN) {
				sendMessage(player, formatMessage(MagicSpells.strOnCooldown, "%c", getCooldown(player)+""));
			} else if (state == SpellCastState.MISSING_REAGENTS) {
				sendMessage(player, MagicSpells.strMissingReagents);
			} else if (state == SpellCastState.CANT_CAST) {
				sendMessage(player, MagicSpells.strCantCast);
			} else if (state == SpellCastState.NO_MAGIC_ZONE) {
				sendMessage(player, MagicSpells.strNoMagicZone);
			}
		}
		
		return state;
	}
	
	protected abstract PostCastAction castSpell(Player player, SpellCastState state, String[] args);

	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	public abstract boolean canCastWithItem();
	
	public abstract boolean canCastByCommand();
	
	public String getCostStr() {
		if (strCost == null || strCost.equals("")) {
			return null;
		} else {
			return strCost;
		}
	}
	
	protected boolean onCooldown(Player player) {
		if (cooldown == 0 || (MagicSpells.castNoCooldown.contains(player.getName().toLowerCase()))) {
			return false;
		}
		
		Long casted = lastCast.get(player.getName());
		if (casted != null) {
			if (casted + (cooldown*1000) > System.currentTimeMillis()) {
				return true;
			}
		}
		return false;
	}
	
	protected int getCooldown(Player player) {
		if (cooldown <= 0) {
			return 0;
		}
		
		Long casted = lastCast.get(player.getName());
		if (casted != null) {
			return (int)(cooldown - ((System.currentTimeMillis()-casted)/1000));
		} else {
			return 0;
		}
	}
	
	protected void setCooldown(Player player) {
		if (cooldown > 0) {
			lastCast.put(player.getName(), System.currentTimeMillis());
		}
	}
	
	protected boolean hasReagents(Player player) {
		return hasReagents(player, cost, healthCost, manaCost);
	}
	
	protected boolean hasReagents(Player player, ItemStack[] cost) {
		return hasReagents(player, cost, 0, 0);
	}
	
	protected boolean hasReagents(Player player, ItemStack[] reagents, int healthCost, int manaCost) {
		if (MagicSpells.castForFree != null && MagicSpells.castForFree.contains(player.getName().toLowerCase())) {
			return true;
		}
		if (reagents == null && healthCost <= 0) {
			return true;
		}
		if (player.getHealth() <= healthCost) { // TODO: add option to allow death from health cost
			return false;
		}
		if (manaCost > 0 && !MagicSpells.mana.hasMana(player, manaCost)) {
			return false;
		}
		for (ItemStack item : reagents) {
			if (item != null && !inventoryContains(player.getInventory(), item)) {
				return false;
			}
		}
		return true;		
	}
	
	protected void removeReagents(Player player) {
		removeReagents(player, cost, healthCost, manaCost);
	}
	
	protected void removeReagents(Player player, ItemStack[] reagents) {
		removeReagents(player, reagents, 0, 0);
	}
	
	protected void removeReagents(Player player, ItemStack[] reagents, int healthCost, int manaCost) {
		if (MagicSpells.castForFree != null && MagicSpells.castForFree.contains(player.getName().toLowerCase())) {
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
			MagicSpells.mana.removeMana(player, manaCost);
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
	
	public void removeFromInventory(Inventory inventory, ItemStack item) {
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
	
	static protected String formatMessage(String message, String... replacements) {
		if (message == null) return null;
		
		String msg = message;
		for (int i = 0; i < replacements.length; i+=2) {
			msg = msg.replace(replacements[i], replacements[i+1]);
		}
		return msg;
	}
	
	static protected void sendMessage(Player player, String message, String... replacements) {
		sendMessage(player, formatMessage(message, replacements));
	}
	
	static protected void sendMessage(Player player, String message) {
		if (message != null && !message.equals("")) {
			String [] msgs = message.replaceAll("&([0-9a-f])", "\u00A7$1").split("\n");
			for (String msg : msgs) {
				if (!msg.equals("")) {
					player.sendMessage(MagicSpells.textColor + msg);
				}
			}
		}
	}
	
	protected void sendMessageNear(Player player, String message) {
		sendMessageNear(player, message, broadcastRange);
	}
	
	protected void sendMessageNear(Player player, String message, int range) {
		if (message != null && !message.equals("")) {
			String [] msgs = message.replaceAll("&([0-9a-f])", "\u00A7$1").split("\n");
			List<Entity> entities = player.getNearbyEntities(range*2, range*2, range*2);
			for (Entity entity : entities) {
				if (entity instanceof Player && entity != player) {
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
		return this.name;
	}
	
	public int getCastItem() {
		return this.castItem;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public ItemStack[] getReagentCost() {
		return this.cost;
	}
	
	public int getManaCost() {
		return this.manaCost;
	}
	
	public int getHealthCost() {
		return this.healthCost;
	}
	
	protected void addListener(Event.Type eventType) {
		MagicSpells.addSpellListener(eventType, this);
	}
	
	protected void removeListener(Event.Type eventType) {
		MagicSpells.removeSpellListener(eventType, this);
	}
	
	protected void initialize() {		
	}
	
	protected void turnOff() {		
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {}
	public void onPlayerQuit(PlayerQuitEvent event) {}
	public void onPlayerInteract(PlayerInteractEvent event) {}
	public void onItemHeldChange(PlayerItemHeldEvent event) {}
	public void onPlayerMove(PlayerMoveEvent event) {}
	public void onPlayerTeleport(PlayerTeleportEvent event) {}
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {}
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {}
	public void onBlockBreak(BlockBreakEvent event) {}
	public void onBlockPlace(BlockPlaceEvent event) {}
	public void onEntityDamage(EntityDamageEvent event) {}	
	public void onEntityTarget(EntityTargetEvent event) {}	
	public void onEntityCombust(EntityCombustEvent event) {}	
	public void onExplosionPrime(ExplosionPrimeEvent event) {}	
	
	protected enum SpellCastState {
		NORMAL,
		ON_COOLDOWN,
		MISSING_REAGENTS,
		CANT_CAST,
		NO_MAGIC_ZONE
	}
	
	protected enum PostCastAction {
		HANDLE_NORMALLY,
		ALREADY_HANDLED,
		NO_MESSAGES,
		NO_REAGENTS,
		NO_COOLDOWN,
		MESSAGES_ONLY,
		REAGENTS_ONLY,
		COOLDOWN_ONLY
	}
	
	@Override
	public int compareTo(Spell spell) {
		return this.name.compareTo(spell.name);
	}

}
