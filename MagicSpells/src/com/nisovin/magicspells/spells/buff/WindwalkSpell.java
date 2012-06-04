package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class WindwalkSpell extends BuffSpell {

	private int launchSpeed;
    private boolean cancelOnLand;
	private boolean cancelOnLogout;
	private boolean cancelOnTeleport;
	private boolean cancelOnDamage;
	
	private HashSet<Player> flyers;
	private HashMap<Player, Integer> tasks;
	
	public WindwalkSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		launchSpeed = getConfigInt("launch-speed", 1);
        cancelOnLand = getConfigBoolean("cancel-on-land", true);
		cancelOnLogout = getConfigBoolean("cancel-on-logout", true);
		cancelOnTeleport = getConfigBoolean("cancel-on-teleport", true);
		cancelOnDamage = getConfigBoolean("cancel-on-damage", false);
		
		flyers = new HashSet<Player>();
		if (useCostInterval > 0) {
			tasks = new HashMap<Player, Integer>();
		}
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (cancelOnLand) {
			registerEvents(new SneakListener());
		}
		if (cancelOnLogout) {
			registerEvents(new QuitListener());
		}
		if (cancelOnTeleport) {
			registerEvents(new TeleportListener());
		}
		if (cancelOnDamage) {
			registerEvents(new DamageListener());
		}
	}

	@Override
	public PostCastAction castSpell(final Player player, SpellCastState state, float power, String[] args) {
		if (flyers.contains(player)) {
			turnOff(player);
			return PostCastAction.ALREADY_HANDLED;
		} else if (state == SpellCastState.NORMAL) {
			// set flying
			flyers.add(player);
			player.setAllowFlight(true);
			player.setFlying(true);
			if (launchSpeed > 0) {
				player.setVelocity(new Vector(0,launchSpeed,0));
			}
			// set cost interval
			if (useCostInterval > 0 || numUses > 0) {
				int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						addUseAndChargeCost(player);
					}
				}, useCostInterval*20, useCostInterval*20);
				tasks.put(player, taskId);
			}
			startSpellDuration(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
    
	public class SneakListener implements Listener {
	    @EventHandler(priority=EventPriority.MONITOR)
	    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
	        if (flyers.contains(event.getPlayer())) {
	            if (event.getPlayer().getLocation().subtract(0,1,0).getBlock().getType() != Material.AIR) {
	                turnOff(event.getPlayer());
	            }
	        }
	    }
	}

	public class QuitListener implements Listener {
		@EventHandler(priority=EventPriority.MONITOR)
		public void onPlayerQuit(PlayerQuitEvent event) {
			turnOff(event.getPlayer());
		}
	}

	public class TeleportListener implements Listener {
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			if (flyers.contains(event.getPlayer())) {
				if (!event.getFrom().getWorld().getName().equals(event.getTo().getWorld().getName()) || event.getFrom().toVector().distanceSquared(event.getTo().toVector()) > 50*50) {
					turnOff(event.getPlayer());
				}
			}
		}
		
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerPortal(PlayerPortalEvent event) {
			if (flyers.contains(event.getPlayer())) {
				turnOff(event.getPlayer());
			}
		}
	}
	
	public class DamageListener implements Listener {
		@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
		public void onPlayerDamage(EntityDamageEvent event) {
			if (event.getEntity() instanceof Player && flyers.contains((Player)event.getEntity())) {
				turnOff((Player)event.getEntity());
			}
		}
	}

	@Override
	public void turnOff(final Player player) {
		super.turnOff(player);
		if (flyers.contains(player)) {
			player.setFlying(false);
			player.setAllowFlight(false);
			player.setFallDistance(0);
			flyers.remove(player);
			sendMessage(player, strFade);
		}
		if (tasks != null && tasks.containsKey(player)) {
			int taskId = tasks.remove(player);
			Bukkit.getScheduler().cancelTask(taskId);
		}
	}
	
	@Override
	protected void turnOff() {
		HashSet<Player> flyers = new HashSet<Player>(this.flyers);
		for (Player player : flyers) {
			turnOff(player);
		}
		this.flyers.clear();
	}

}
