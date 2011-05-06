package com.nisovin.MagicSystem;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

public abstract class Spell {

	protected String internalName;
	protected String name;
	protected String description;
	protected int castItem;
	protected ItemStack[] cost;
	protected int healthCost = 0;
	protected int cooldown;
	protected int broadcastRange;
	protected String strCost;
	protected String strCastSelf;
	protected String strCastOthers;
	
	private HashMap<String, Long> lastCast;
	
	public Spell(Configuration config, String spellName) {
		this.internalName = spellName;
		this.name = config.getString("spells." + spellName + ".name", spellName);
		this.description = config.getString("spells." + spellName + ".description", "");
		this.castItem = config.getInt("spells." + spellName + ".cast-item", 280);
		List<String> costList = config.getStringList("spells." + spellName + ".cost", null);
		if (costList != null && costList.size() > 0) {
			cost = new ItemStack [costList.size()];
			for (int i = 0; i < costList.size(); i++) {
				if (costList.get(i).contains(" ")) {
					String [] data = costList.get(i).split(" ");
					if (data[0].equalsIgnoreCase("health")) {
						healthCost = Integer.parseInt(data[1]);
					} else if (data[0].contains(":")) {
						String [] subdata = data[0].split(":");
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
		this.broadcastRange = config.getInt("spells." + spellName + ".broadcast-range", MagicSystem.broadcastRange);
		this.strCost = config.getString("spells." + spellName + ".str-cost", null);
		this.strCastSelf = config.getString("spells." + spellName + ".str-cast-self", null);
		this.strCastOthers = config.getString("spells." + spellName + ".str-cast-others", null);
		
		if (cooldown > 0) {
			lastCast = new HashMap<String, Long>();
		}
	}

	public final void cast(Player player) {
		cast(player, null);
	}
	
	public final void cast(Player player, String[] args) {
		SpellCastState state;
		if (onCooldown(player)) {
			state = SpellCastState.ON_COOLDOWN;
		} else if (!hasReagents(player)) {
			state = SpellCastState.MISSING_REAGENTS;
		} else {
			state = SpellCastState.NORMAL;
		}
		
		boolean handled = castSpell(player, state, args);
		if (!handled) {
			if (state == SpellCastState.NORMAL) {
				setCooldown(player);
				removeReagents(player);
				sendMessage(player, strCastSelf);
				sendMessageNear(player, strCastOthers);
			} else if (state == SpellCastState.ON_COOLDOWN) {
				sendMessage(player, MagicSystem.strOnCooldown);
			} else if (state == SpellCastState.MISSING_REAGENTS) {
				sendMessage(player, MagicSystem.strMissingReagents);
			}
		}
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
		if (cooldown == 0) {
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
	
	protected void setCooldown(Player player) {
		if (cooldown > 0) {
			lastCast.put(player.getName(), System.currentTimeMillis());
		}
	}
	
	protected boolean hasReagents(Player player, ItemStack[] reagents) {
		if (reagents == null && healthCost <= 0) {
			return true;
		}
		if (player.getHealth() <= healthCost) { // TODO: add option to allow death from health cost
			return false;
		}
		for (ItemStack item : reagents) {
			if (!player.getInventory().contains(item)) {
				return false;
			}
		}
		return true;		
	}
	
	protected boolean hasReagents(Player player) {
		return hasReagents(player, cost);
	}
	
	protected void removeReagents(Player player) {
		removeReagents(player, cost);
	}
	
	protected void removeReagents(Player player, ItemStack[] reagents) {
		if (reagents != null) {
			for (ItemStack item : reagents) {
				player.getInventory().remove(item);
			}
		}
		if (healthCost > 0) {
			player.setHealth(player.getHealth() - healthCost);
		}
	}
	
	protected void sendMessage(Player player, String message) {
		if (message != null && message != "") {
			player.sendMessage(message);
		}
	}
	
	protected void sendMessageNear(Player player, String message) {
		sendMessageNear(player, message, MagicSystem.broadcastRange);
	}
	
	protected void sendMessageNear(Player player, String message, int range) {
		List<Entity> entities = player.getNearbyEntities(range/2, range/2, range/2);
		for (Entity entity : entities) {
			if (entity instanceof Player && entity != player) {
				((Player)entity).sendMessage(message);
			}
		}
	}
	
	protected abstract boolean castSpell(Player player, SpellCastState state, String[] args);

	public String getInternalName() {
		return this.internalName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getCastItem() {
		return this.castItem;
	}
	
	protected void addListener(Event.Type eventType) {
		MagicSystem.addSpellListener(eventType, this);
	}
	
	protected void removeListener(Event.Type eventType) {
		MagicSystem.removeSpellListener(eventType, this);
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {}
	public void onPlayerQuit(PlayerQuitEvent event) {}
	public void onEntityDamage(EntityDamageEvent event) {}	
	public void onEntityTarget(EntityTargetEvent event) {}	
	public void onBlockBreak(BlockBreakEvent event) {}
	public void onBlockPlace(BlockPlaceEvent event) {}
	
	protected enum SpellCastState {
		NORMAL,
		ON_COOLDOWN,
		MISSING_REAGENTS
	}

}
