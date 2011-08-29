package com.nisovin.GraveyardSpawn;

import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class GYEntityListener extends EntityListener {
	
	private GraveyardSpawn plugin;
	
	//private HashMap<String, String> lastDamage = new HashMap<String, String>();
	//private HashMap<String, String> graveSigns = new HashMap<String, String>();
	
	public GYEntityListener(GraveyardSpawn plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			plugin.deathLocation.put(((Player)event.getEntity()).getName(), event.getEntity().getLocation());
		}
	}
	
	/*public void onEntityDamage(EntityDamageEvent evt) {
		if (!evt.isCancelled() && evt.getEntity() instanceof Player) {
			Player defender = (Player)evt.getEntity();
			
			if (evt.getDamage() >= defender.getHealth()) {
				// going to die
				if ((evt.getCause() == DamageCause.ENTITY_ATTACK || evt.getCause() == DamageCause.ENTITY_EXPLOSION) && evt instanceof EntityDamageByEntityEvent) {
					// attacked by an entity
					EntityDamageByEntityEvent event = (EntityDamageByEntityEvent)evt;
					Entity attacker = event.getDamager();
					
					if (attacker instanceof Player) {
						lastDamage.put(defender.getName(), "killed by|" + ((Player)attacker).getName());
					} else if (attacker instanceof Zombie) {
						lastDamage.put(defender.getName(), "mauled by|a zombie");
					} else if (attacker instanceof Skeleton) {
						lastDamage.put(defender.getName(), "shot by|a skeleton");
					} else if (attacker instanceof Spider) {
						lastDamage.put(defender.getName(), "bitten by|a spider");
					} else if (attacker instanceof Creeper) {
						lastDamage.put(defender.getName(), "blown up by|a creeper");
					} else if (attacker instanceof PigZombie) {
						lastDamage.put(defender.getName(), "mauled by|a pig zombie");
					} else if (attacker instanceof Giant) {
						lastDamage.put(defender.getName(), "smashed by|a giant");
					} else {
						lastDamage.put(defender.getName(), "");
					}
				} else if (evt.getCause() == DamageCause.FALL) {
					// fell
					lastDamage.put(defender.getName(), "crushed by|the ground");
				} else if (evt.getCause() == DamageCause.FIRE || evt.getCause() == DamageCause.FIRE_TICK) {
					lastDamage.put(defender.getName(), "died while|on fire");
				} else if (evt.getCause() == DamageCause.LAVA) {
					lastDamage.put(defender.getName(), "melted by|lava");
				} else {
					lastDamage.put(defender.getName(), "|");
				}
			}
		}
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			
			Location loc = player.getLocation();
			
			Block ground = loc.getBlock();
			while (ground.getType() == Material.AIR && ground.getY() > 0) {
				ground = ground.getRelative(BlockFace.DOWN);
			}
			
			if (ground.getType() != Material.AIR && ground.getRelative(0,1,0).getType() == Material.AIR && ground.getRelative(0,2,0).getType() == Material.AIR) {
				Block signBlock = ground.getRelative(0,1,0);
				signBlock.setType(Material.SIGN);
				Sign sign = (Sign)signBlock.getState();
				sign.setLine(0, "R.I.P.");
				sign.setLine(1, player.getName());
				String text = lastDamage.get(player.getName());
				if (text != null) {
					String [] lines = text.split("|");
					sign.setLine(2, lines[0]);
					sign.setLine(3, lines[1]);
					lastDamage.remove(player.getName());
				}
				sign.update();
				
				graveSigns.put(signBlock.getWorld()+","+signBlock.getX()+","+signBlock.getY()+","+signBlock.getZ(), player.getName()+"|"+System.currentTimeMillis());
			}
			
		}
	}
	
	public class GraveSignRemover implements Runnable {
		public void run() {
			for (String s : graveSigns.keySet()) {
				String [] loc = s.split(",");
				String [] data = graveSigns.get(s).split(":");
				if (Long.parseLong(data[1]) + plugin.SIGN_REMOVE_DELAY*1000 > System.currentTimeMillis()) {
					graveSigns.remove(s);
					Block sign = plugin.getServer().getWorld(loc[0]).getBlockAt(Integer.parseInt(loc[1]), Integer.parseInt(loc[2]), Integer.parseInt(loc[3]));
					if (sign != null) {
						sign.setType(Material.AIR);
					}
				}
			}
		}
	}*/

}