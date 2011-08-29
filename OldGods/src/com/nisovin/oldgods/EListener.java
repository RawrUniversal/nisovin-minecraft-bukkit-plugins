package com.nisovin.oldgods;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public class EListener extends EntityListener {

	OldGods plugin;
	
	public EListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.ENTITY_TARGET, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.ENTITY_COMBUST, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this, Event.Priority.Normal, plugin);
	}

	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.DEATH) {
			if (event.getSpawnReason() == SpawnReason.NATURAL) {
				CreatureType c = event.getCreatureType();
				if (c == CreatureType.CREEPER || c == CreatureType.PIG_ZOMBIE || c == CreatureType.SKELETON || c == CreatureType.SPIDER || c == CreatureType.ZOMBIE) {
					event.getEntity().getWorld().spawnCreature(event.getEntity().getLocation(), c);
				}
			}
		}
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		
		if (event instanceof EntityDamageByEntityEvent) {
			onEntityDamageByEntity((EntityDamageByEntityEvent)event);
		}
		
		God god = plugin.currentGod();
		
		if (god == God.HEALING) {
			if (event.getEntity() instanceof Player) {
				event.setDamage(event.getDamage() / 2);
			}
		} else if (god == God.EXPLORATION) {
			if (event.getCause() == DamageCause.FALL) {
				event.setDamage(event.getDamage() / 2);				
			}
		} else if (god == God.MINING) {
			if (event.getCause() == DamageCause.LAVA) {
				event.setDamage(1);
			}
		} else if (god == God.OCEAN) {
			if (event.getCause() == DamageCause.DROWNING) {
				event.setCancelled(true);
			}
		}
	}
	
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		God god = plugin.currentGod();
		
		if (god == God.DEATH) {
			if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
				event.setDamage(event.getDamage() * 2);
			}
		} else if (god == God.HUNT) {
			if (event.getDamager() instanceof Arrow && ((Arrow)event.getDamager()).getShooter() instanceof Player) {
				event.setDamage(event.getDamage() * 2);
			}
		} else if (god == God.WAR) {
			if (event.getDamager() instanceof Player) {
				String itemName = ((Player)event.getDamager()).getItemInHand().getType().name();
				if (itemName.contains("SWORD") || itemName.contains("AXE")) {
					event.setDamage(event.getDamage() * 2);					
				}
			} else if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
				event.setDamage(event.getDamage() / 2);
			}
		}
		
	}

	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.LOVE) {
			if (event.getTarget() instanceof Player && event.getReason() == TargetReason.CLOSEST_PLAYER) {
				event.setCancelled(true);
			}
		}
	}

	@Override
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.DEATH) {
			event.setCancelled(true);
		}
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		God god = plugin.currentGod();
		
		if (god == God.HUNT) {
			if (event.getEntity() instanceof Creature) {
				List<ItemStack> drops = event.getDrops();
				int max = drops.size();
				for (int i = 0; i < max; i++) {
					drops.add(drops.get(i).clone());
				}
			}
		} else if (god == God.COOKING) {
			if (event.getEntity() instanceof Creature) {
				List<ItemStack> drops = event.getDrops();
				for (ItemStack i : drops) {
					if (i.getType() == Material.PORK) {
						i.setType(Material.GRILLED_PORK);
					}
				}
			}
		}
	}
	
	
	
}
