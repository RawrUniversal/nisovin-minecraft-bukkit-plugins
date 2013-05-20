package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;

public class DisguiseSpell extends TargetedEntitySpell {
	
	protected static DisguiseManager manager;
	
	private EntityType entityType;
	private boolean flag = false;
	private int var1 = 0;
	private int var2 = 0;
	private boolean showPlayerName = false;
	private String nameplateText = "";
	private boolean preventPickups = false;
	private boolean friendlyMobs = true;
	private boolean undisguiseOnDeath = true;
	private boolean undisguiseOnLogout = false;
	private int duration;
	private boolean toggle;
	private String strFade;
	
	private Map<String, Disguise> disguised;
	
	public DisguiseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		if (manager == null) {
			manager = new DisguiseManager(config);
		}
		manager.registerSpell(this);
		
		String type = getConfigString("entity-type", "zombie");
		if (type.startsWith("baby ")) {
			flag = true;
			type = type.replace("baby ", "");
		}
		if (type.equalsIgnoreCase("human") || type.equalsIgnoreCase("player")) {
			type = "player";
		} else if (type.equalsIgnoreCase("wither skeleton")) {
			type = "skeleton";
			flag = true;
		} else if (type.equalsIgnoreCase("zombie villager") || type.equalsIgnoreCase("villager zombie")) {
			type = "zombie";
			var1 = 1;
		} else if (type.equalsIgnoreCase("powered creeper")) {
			type = "creeper";
			flag = true;
		} else if (type.toLowerCase().startsWith("villager ")) {
			String prof = type.toLowerCase().replace("villager ", "");
			if (prof.matches("^[0-5]$")) {
				var1 = Integer.parseInt(prof);
			} else if (prof.toLowerCase().startsWith("green")) {
				var1 = 5;
			} else {
				try {
					var1 = Profession.valueOf(prof.toUpperCase()).getId();
				} catch (Exception e) {
					MagicSpells.error("Invalid villager profession on disguise spell '" + spellName + "'");
				}
			}
			type = "villager";
		} else if (type.toLowerCase().endsWith(" villager")) {
			String prof = type.toLowerCase().replace(" villager", "");
			if (prof.toLowerCase().startsWith("green")) {
				var1 = 5;
			} else {
				try {
					var1 = Profession.valueOf(prof.toUpperCase()).getId();
				} catch (Exception e) {
					MagicSpells.error("Invalid villager profession on disguise spell '" + spellName + "'");
				}
			}
			type = "villager";
		} else if (type.toLowerCase().endsWith(" sheep")) {
			String color = type.toLowerCase().replace(" sheep", "");
			if (color.equalsIgnoreCase("random")) {
				var1 = -1;
			} else {
				try {
					DyeColor dyeColor = DyeColor.valueOf(color.toUpperCase().replace(" ", "_"));
					if (dyeColor != null) {
						var1 = dyeColor.getWoolData();
					}
				} catch (IllegalArgumentException e) {
					MagicSpells.error("Invalid sheep color on disguise spell '" + spellName + "'");
				}
			}
			type = "sheep";
		} else if (type.toLowerCase().startsWith("wolf ")) {
			String color = type.toLowerCase().replace("wolf ", "");
			if (color.matches("[0-9a-fA-F]+")) {
				var1 = Integer.parseInt(color, 16);
			}
			type = "wolf";
		} else if (type.toLowerCase().equalsIgnoreCase("saddled pig")) {
			var1 = 1;
			type = "pig";
		} else if (type.equalsIgnoreCase("irongolem")) {
			type = "villagergolem";
		} else if (type.equalsIgnoreCase("mooshroom")) {
			type = "mushroomcow";
		} else if (type.equalsIgnoreCase("magmacube")) {
			type = "lavaslime";
		} else if (type.toLowerCase().contains("ocelot")) {
			type = type.toLowerCase().replace("ocelot", "ozelot");
		} else if (type.equalsIgnoreCase("snowgolem")) {
			type = "snowman";
		} else if (type.toLowerCase().startsWith("block") || type.toLowerCase().startsWith("fallingblock")) {
			String data = type.split(" ")[1];
			if (data.contains(":")) {
				String[] subdata = data.split(":");
				var1 = Integer.parseInt(subdata[0]);
				var2 = Integer.parseInt(subdata[1]);
			} else {
				var1 = Integer.parseInt(data);
			}
			type = "fallingsand";
		}
		if (type.toLowerCase().matches("ozelot [0-3]")) {
			var1 = Integer.parseInt(type.split(" ")[1]);
			type = "ozelot";
		} else if (type.toLowerCase().equals("ozelot random") || type.toLowerCase().equals("random ozelot")) {
			var1 = -1;
			type = "ozelot";
		}
		if (type.equals("player")) {
			entityType = EntityType.PLAYER;
		} else {
			entityType = EntityType.fromName(type);
		}
		showPlayerName = getConfigBoolean("show-player-name", false);
		nameplateText = ChatColor.translateAlternateColorCodes('&', getConfigString("nameplate-text", ""));
		preventPickups = getConfigBoolean("prevent-pickups", true);
		friendlyMobs = getConfigBoolean("friendly-mobs", true);
		undisguiseOnDeath = getConfigBoolean("undisguise-on-death", true);
		undisguiseOnLogout = getConfigBoolean("undisguise-on-logout", false);
		duration = getConfigInt("duration", 0);
		toggle = getConfigBoolean("toggle", false);
		targetSelf = getConfigBoolean("target-self", true);
		strFade = getConfigString("str-fade", "");
		
		disguised = new HashMap<String, Disguise>();
		
		if (entityType == null) {
			MagicSpells.error("Invalid entity-type specified for disguise spell '" + spellName + "'");
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Disguise oldDisguise = disguised.remove(player.getName().toLowerCase());
			manager.removeDisguise(player);
			if (oldDisguise != null && toggle) {
				sendMessage(player, strFade);
				return PostCastAction.ALREADY_HANDLED;
			}
			if (state == SpellCastState.NORMAL) {
				Player target = getTargetPlayer(player);
				if (target != null) {
					disguise(target);
					sendMessages(player, target);
					return PostCastAction.NO_MESSAGES;
				} else {
					return noTarget(player);
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	private void disguise(Player player) {
		String nameplate = nameplateText;
		if (showPlayerName) nameplate = player.getDisplayName();
		Disguise disguise = new Disguise(player, entityType, nameplate, flag, var1, var2, duration, this);
		manager.addDisguise(player, disguise);
		disguised.put(player.getName().toLowerCase(), disguise);
	}
	
	public void undisguise(Player player) {
		Disguise disguise = disguised.remove(player.getName().toLowerCase());
		if (disguise != null) {
			disguise.cancelDuration();
			sendMessage(player, strFade);
		}
	}
	
	@Override
	public boolean castAtEntity(Player player, LivingEntity target, float power) {
		if (target instanceof Player) {
			disguise((Player)target);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		if (preventPickups && disguised.containsKey(event.getPlayer().getName().toLowerCase())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if (undisguiseOnDeath && disguised.containsKey(event.getEntity().getName().toLowerCase())) {
			manager.removeDisguise(event.getEntity());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (undisguiseOnLogout && disguised.containsKey(event.getPlayer().getName().toLowerCase())) {
			manager.removeDisguise(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onTarget(EntityTargetEvent event) {
		if (friendlyMobs && event.getTarget() instanceof Player && disguised.containsKey(((Player)event.getTarget()).getName().toLowerCase())) {
			event.setCancelled(true);
		}
	}
	
	@Override
	public void turnOff() {
		for (String name : new ArrayList<String>(disguised.keySet())) {
			Player player = Bukkit.getPlayerExact(name);
			if (player != null) {
				manager.removeDisguise(player);
			}
		}
		manager.unregisterSpell(this);
		if (manager.registeredSpellsCount() == 0) {
			manager.destroy();
			manager = null;
		}
	}

}
