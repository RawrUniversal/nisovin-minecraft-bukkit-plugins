package com.nisovin.magicdeathmessages;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.targeted.CombustSpell;
import com.nisovin.magicspells.spells.targeted.PotionEffectSpell;

public class MagicDeathMessages extends JavaPlugin implements Listener {

	Map<String, String> causeStringsKillers;
	Map<String, String> causeStringsSuicides;
	
	Map<String, AttackData> poisonedBy;
	Map<String, AttackData> witheredBy;
	Map<String, AttackData> combustedBy;
	Map<String, AttackData> targetedBy;
	
	@Override
	public void onEnable() {
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSpellTarget(SpellTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			if (event.getSpell() instanceof PotionEffectSpell) {
				PotionEffectSpell spell = (PotionEffectSpell)event.getSpell();
				int type = spell.getType();
				if (type == 19) {
					poisonedBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster(), spell.getDuration() * 1000 / 20));
				} else if (type == 20) {
					witheredBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster(), spell.getDuration() * 1000 / 20));
				}
			} else if (event.getSpell() instanceof CombustSpell) {
				combustedBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster(), ((CombustSpell)event.getSpell()).getDuration() * 1000 / 20));
			}
			targetedBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster()));
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		EntityDamageEvent dam = player.getLastDamageCause();
		
		// get attacker data
		AttackData data = null;
		if (dam.getCause() == DamageCause.POISON) {
			data = poisonedBy.get(player.getName());
		} else if (dam.getCause() == DamageCause.WITHER) {
			data = witheredBy.get(player.getName());
		} else if (dam.getCause() == DamageCause.FIRE_TICK) {
			data = combustedBy.get(player.getName());
		}
		if (data == null) {
			data = targetedBy.get(player.getName());
		}
		if (data != null && data.isExpired()) {
			data = null;
		}
		
		// get killer
		Player killer = null;
		String killerName = null;
		String killerDisplayName = null;
		if (data != null) {
			killer = Bukkit.getPlayerExact(data.attackerName);
			if (killer == null) {
				killer = player.getKiller();
			}
			if (killer == null) {
				killerName = data.attackerName;
				killerDisplayName = data.attackerName;
			} else {
				killerName = killer.getName();
				killerDisplayName = killer.getDisplayName();
			}
		} else {
			killer = player.getKiller();
			if (killer != null) {
				killerName = killer.getName();
				killerDisplayName = killer.getDisplayName();
			}
		}
		
		// get weapon name
		String weapon = null;
		if (data != null) {
			weapon = data.weapon;
		} else if (killer != null) {
			ItemStack item = killer.getItemInHand();
			if (item != null) {
				weapon = item.getItemMeta().getDisplayName();
			}
		}
		if (weapon != null && weapon.isEmpty()) {
			weapon = null;
		}
		
		// set metadata
		if (killerName != null) {
			player.setMetadata("KILLER", new FixedMetadataValue(this, killerName));
		}
		if (weapon != null) {
			player.setMetadata("KILLER_WEAPON", new FixedMetadataValue(this, weapon));
		}
		
		// set death message
		String message = null;
		if (killerDisplayName != null) {
			String cause = causeStringsKillers.get(dam.getCause().name().toLowerCase());
			if (cause == null) {
				cause = "was killed by";
			}
			message = player.getDisplayName() + ChatColor.WHITE + " " + cause + " " + killerDisplayName;
			if (weapon != null) {
				message += ChatColor.WHITE + " using " + weapon + ChatColor.WHITE + ".";
			} else {
				message += ChatColor.WHITE + ".";
			}
		} else {
			String cause = causeStringsSuicides.get(dam.getCause().name().toLowerCase());
			if (cause == null) {
				cause = "died";
			}
			message = player.getDisplayName() + ChatColor.WHITE + cause + ChatColor.WHITE + ".";
		}
		event.setDeathMessage(message);
	}
	
	class AttackData {
		String attackerName;
		String weapon;
		int duration;
		long time;
		
		public AttackData(Player player) {
			attackerName = player.getName();
			ItemStack item = player.getItemInHand();
			if (item != null) {
				weapon = item.getItemMeta().getDisplayName();
			}
			duration = 10000;
			time = System.currentTimeMillis();
		}
		
		public AttackData(Player player, int duration) {
			this(player);
			this.duration = duration;
		}
		
		public boolean isExpired() {
			return time + duration + 2000 < System.currentTimeMillis();
		}
	}
	
}
