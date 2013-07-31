package com.nisovin.magicdeathmessages;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

	// TODO: track arrow shots maybe
	// TODO: check for potion effects
	// TODO: prevent 'double' deaths
	
	Map<String, String> causeStringsWeapons = new HashMap<String, String>();
	Map<String, String> causeStringsKillers = new HashMap<String, String>();
	Map<String, String> causeStringsSuicides = new HashMap<String, String>();
	
	Map<String, AttackData> poisonedBy = new HashMap<String, AttackData>();
	Map<String, AttackData> witheredBy = new HashMap<String, AttackData>();
	Map<String, AttackData> combustedBy = new HashMap<String, AttackData>();
	Map<String, AttackData> attackedBy = new HashMap<String, AttackData>();
	
	@Override
	public void onEnable() {
		loadConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	void loadConfig() {
		causeStringsWeapons.clear();
		causeStringsKillers.clear();
		causeStringsSuicides.clear();
		
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
			
			ConfigurationSection sec = config.getConfigurationSection("cause-strings-weapons");
			Set<String> keys;
			if (sec != null) {
				keys = sec.getKeys(false);
				for (String key : keys) {
					causeStringsWeapons.put(key, ChatColor.translateAlternateColorCodes('&', sec.getString(key)));
				}
			}
			
			sec = config.getConfigurationSection("cause-strings-killers");
			if (sec != null) {
				keys = sec.getKeys(false);
				for (String key : keys) {
					causeStringsKillers.put(key, ChatColor.translateAlternateColorCodes('&', sec.getString(key)));
				}
			}
			
			sec = config.getConfigurationSection("cause-strings-suicides");
			if (sec != null) {
				keys = sec.getKeys(false);
				for (String key : keys) {
					causeStringsSuicides.put(key, ChatColor.translateAlternateColorCodes('&', sec.getString(key)));
				}
			}
			
		} catch (Exception e) {
			getLogger().severe("ERROR LOADING CONFIG");
			e.printStackTrace();
		}		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender.isOp()) {
			loadConfig();
			sender.sendMessage("MagicDeathMessages config reloaded.");
		}
		return true;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && event instanceof EntityDamageByEntityEvent && (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.ENTITY_EXPLOSION)) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
			Player attacker = null;
			if (evt.getDamager() instanceof Player) {
				attacker = (Player)evt.getDamager();
			} else if (evt.getDamager() instanceof Projectile) {
				LivingEntity shooter = ((Projectile)evt.getDamager()).getShooter();
				if (shooter != null && shooter instanceof Player) {
					attacker = (Player)shooter;
				}
			}
			if (attacker != null) {
				attackedBy.put(((Player)event.getEntity()).getName(), new AttackData(attacker));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSpellTarget(SpellTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			if (event.getSpell() instanceof PotionEffectSpell) {
				PotionEffectSpell spell = (PotionEffectSpell)event.getSpell();
				int type = spell.getType();
				if (type == 19) {
					poisonedBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster(), (spell.getDuration() * 1000 / 20) + 1000));
				} else if (type == 20) {
					witheredBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster(), (spell.getDuration() * 1000 / 20) + 1000));
					System.out.println("WITHER " + event.getCaster().getName() + " " + ((Player)event.getTarget()).getName());
				}
			} else if (event.getSpell() instanceof CombustSpell) {
				combustedBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster(), (((CombustSpell)event.getSpell()).getDuration() * 1000 / 20) + 1000));
			}
			attackedBy.put(((Player)event.getTarget()).getName(), new AttackData(event.getCaster()));
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		EntityDamageEvent dam = player.getLastDamageCause();
		DamageCause damageCause = dam != null ? dam.getCause() : DamageCause.CUSTOM;
		
		// get attacker data
		AttackData data = null;
		if (damageCause == DamageCause.POISON) {
			data = poisonedBy.get(player.getName());
		} else if (damageCause == DamageCause.WITHER) {
			data = witheredBy.get(player.getName());
		} else if (damageCause == DamageCause.FIRE_TICK) {
			data = combustedBy.get(player.getName());
		}
		if (data == null) {
			data = attackedBy.get(player.getName());
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
			if (item != null && item.getTypeId() > 0 && item.hasItemMeta()) {
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
			String cause = null;
			if (weapon != null && damageCause == DamageCause.ENTITY_ATTACK) {
				cause = causeStringsWeapons.get(ChatColor.stripColor(weapon));
			}
			if (cause == null) {
				cause = causeStringsKillers.get(damageCause.name().toLowerCase());
			}
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
			String cause = causeStringsSuicides.get(damageCause.name().toLowerCase());
			if (cause == null) {
				cause = "died";
			}
			message = player.getDisplayName() + ChatColor.WHITE + " " + cause + ChatColor.WHITE + ".";
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
			if (item != null && item.getTypeId() > 0 && item.hasItemMeta()) {
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
			return time + duration < System.currentTimeMillis();
		}
	}
	
}
