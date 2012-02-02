package com.nisovin.worldloader;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.Button;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

public class EventListener implements Listener {

	WorldLoader plugin;
	
	public EventListener(WorldLoader plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
	}

	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerChat(PlayerChatEvent event) {
		if (event.getMessage().equalsIgnoreCase("yes")) {
			if (plugin.acceptPendingAction(event.getPlayer())) {
				event.setCancelled(true);
				return;
			}
		} else if (event.getMessage().equalsIgnoreCase("no")) {
			if (plugin.rejectPendingAction(event.getPlayer())) {
				event.setCancelled(true);
				return;
			}
		}
		if (event.getMessage().startsWith("!!")) {
			event.setFormat("[" + ChatColor.GOLD + "Global" + ChatColor.WHITE + "] " + event.getFormat());
			event.setMessage(event.getMessage().substring(2));
		} else {
			Set<Player> recips = event.getRecipients();
			recips.clear();
			for (Player p : event.getPlayer().getWorld().getPlayers()) {
				recips.add(p);
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.isOp()) {
			player.setDisplayName(ChatColor.RED + player.getName() + ChatColor.WHITE);
			player.setPlayerListName(ChatColor.RED + player.getName());
		}
		plugin.enterSavedInstance(player);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		final WorldInstance instance = plugin.getWorldInstance(player);
		if (instance != null) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					if (instance.worldLoaded() && instance.getInstanceWorld().getPlayers().size() == 0) {
						plugin.killInstance(instance, false);
					}
				}
			}, 600);
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		WorldInstance instance = plugin.getWorldInstance(event.getPlayer());
		if (instance != null) {
			event.setRespawnLocation(instance.getRespawn());
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		
		if (event.hasBlock()) {
			Block b = event.getClickedBlock();
			if (b.getType() == Material.STONE_BUTTON) {
				b = b.getRelative(((Button)b.getState().getData()).getAttachedFace());
				SignExecutor.executeSign(b.getRelative(BlockFace.DOWN), player);
				SignExecutor.executeSign(b.getRelative(BlockFace.UP), player);
				SignExecutor.executeSign(b.getRelative(BlockFace.NORTH), player);
				SignExecutor.executeSign(b.getRelative(BlockFace.SOUTH), player);
				SignExecutor.executeSign(b.getRelative(BlockFace.EAST), player);
				SignExecutor.executeSign(b.getRelative(BlockFace.WEST), player);
			} else if (event.getAction() == Action.PHYSICAL && (b.getType() == Material.STONE_PLATE || b.getType() == Material.WOOD_PLATE)) {
				SignExecutor.executeSign(b.getRelative(0,-2,0), player);
			} else if (b.getType() == Material.IRON_DOOR_BLOCK && event.hasItem()) {
				// check for dye
				ItemStack item = event.getItem();
				if (item.getType() != Material.INK_SACK) return;
				
				// check for double door
				Block b2 = null;
				if (b.getRelative(0, 0, 1).getType() == Material.IRON_DOOR_BLOCK) {
					b2 = b.getRelative(0, 0, 1);
				} else if (b.getRelative(0, 0, -1).getType() == Material.IRON_DOOR_BLOCK) {
					b2 = b.getRelative(0, 0, -1);
				} else if (b.getRelative(1, 0, 0).getType() == Material.IRON_DOOR_BLOCK) {
					b2 = b.getRelative(1, 0, 0);
				} else if (b.getRelative(-1, 0, 0).getType() == Material.IRON_DOOR_BLOCK) {
					b2 = b.getRelative(-1, 0, 0);
				}
				
				// get wool block(s) under door
				Block wool = null;
				Block wool2 = null;
				if (b.getData() >= 8) {
					wool = b.getRelative(0, -3, 0);
					if (b2 != null) {
						wool2 = b2.getRelative(0, -3, 0);
					}
				} else {
					wool = b.getRelative(0, -2, 0);
					if (b2 != null) {
						wool2 = b2.getRelative(0, -2, 0);
					}
				}
				if (wool.getType() == Material.WOOL) {
					if (((Dye)item.getData()).getColor().equals(((Wool)wool.getState().getData()).getColor())) {
						byte data = wool.getData();
						wool.setType(Material.REDSTONE_TORCH_ON);
						((CraftWorld)wool.getWorld()).getHandle().applyPhysics(wool.getX(), wool.getY()+1, wool.getZ(), Material.REDSTONE_TORCH_ON.getId());
						if (wool2 != null) {
							wool2.setTypeIdAndData(Material.WOOL.getId(), data, true);
						}
					}
				} else if (wool2.getType() == Material.WOOL) {
					if (((Dye)item.getData()).getColor().equals(((Wool)wool2.getState().getData()).getColor())) {
						byte data = wool2.getData();
						wool2.setType(Material.REDSTONE_TORCH_ON);
						((CraftWorld)wool2.getWorld()).getHandle().applyPhysics(wool2.getX(), wool2.getY()+1, wool2.getZ(), Material.REDSTONE_TORCH_ON.getId());
						wool.setTypeIdAndData(Material.WOOL.getId(), data, true);
					}
				}
			}
		}
	}
	
	static BlockFace[] dirs;
	static {
		dirs = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockRedstoneChange(BlockRedstoneEvent event) {
		Block b = event.getBlock();
		if (b.getType() == Material.REDSTONE_WIRE && event.getOldCurrent() != event.getNewCurrent()) {
			for (BlockFace face: dirs) {
				Block rel = b.getRelative(face);
				if (rel.getType() == Material.SPONGE) {
					// wireless redstone transmitted by sponge
					transmitSignal(rel, face, event.getNewCurrent() > 0);
				} else if (event.getOldCurrent() < event.getNewCurrent() && rel.getType() == Material.WALL_SIGN || rel.getType() == Material.SIGN_POST) {
					System.out.println(b);
					System.out.println(rel);
					SignExecutor.executeSign(rel, null);
				}
			}
		}
	}
	
	public void transmitSignal(Block origin, BlockFace direction, boolean on){
		Block current = origin;
		int iterations = 0;
		while (iterations < 100){
			current = current.getRelative(direction);
			if (current.getType().equals(Material.SPONGE)) {
				Block lever = current.getRelative(direction);
				if (lever.getType().equals(Material.LEVER)) {
					int data = (int)lever.getData();
					if (on) {
						if ((data & 0x08) != 0x08) {
							data |= 0x08;
						}
					} else {
						if ((data & 0x08) == 0x08) {
							data ^= 0x08;
						}
					}
					lever.setTypeIdAndData(Material.LEVER.getId(), (byte) data, true);
					Block block = lever.getRelative(((Attachable)lever.getState().getData()).getAttachedFace());
					((CraftWorld)block.getWorld()).getHandle().applyPhysics(block.getX(), block.getY(), block.getZ(), Material.LEVER.getId());
				}
			}
			iterations++;
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled() || event.getPlayer().isOp()) return;
		WorldInstance world = plugin.getWorldInstance(event.getBlock().getWorld());
		if (world != null && !world.canBreak(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled() || event.getPlayer().isOp()) return;
		WorldInstance world = plugin.getWorldInstance(event.getBlock().getWorld());
		if (world != null && !world.canPlace(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (event.isCancelled()) return;
		
		boolean pickup = plugin.getLoot(event.getItem(), event.getPlayer());
		if (!pickup) {
			event.setCancelled(true);
			return;
		}
		
		Material type = event.getItem().getItemStack().getType();
		if (type == Material.GOLD_NUGGET || type == Material.GOLD_INGOT || type == Material.GOLD_BLOCK) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new GoldScanner(event.getPlayer()), 1);
		}
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity)event.getEntity();
		
		if (entity.getKiller() == null) {
			return;
		}
		
		WorldInstance instance = plugin.getWorldInstance(entity.getWorld());
		if (instance == null) {
			return;
		}
		
		// handle exp
		int exp = event.getDroppedExp();
		event.setDroppedExp(0);
		for (Player p : entity.getWorld().getPlayers()) {
			p.giveExp(exp);
		}
		
		// handle drops
		event.getDrops().clear();
		Location location = entity.getLocation();
		if (entity instanceof Zombie) {
			plugin.dropGold(location, 5);
		} else if (entity instanceof Skeleton) {
			plugin.dropGold(location, 6);
		} else if (entity instanceof Creeper) {
			plugin.dropGold(location, 8);
		} else if (entity instanceof Spider) {
			plugin.dropGold(location, 5);
		} else if (entity instanceof CaveSpider) {
			plugin.dropGold(location, 7);
		} else if (entity instanceof Ghast) {
			plugin.dropGold(location, 20);
		} else if (entity instanceof Blaze) {
			plugin.dropGold(location, 10);
		} else if (entity instanceof Enderman) {
			plugin.dropGold(location, 15);
		} else if (entity instanceof PigZombie) {
			plugin.dropGold(location, 10);
		} else if (entity instanceof Wolf) {
			plugin.dropGold(location, 6);
		} else if (entity instanceof Silverfish) {
			plugin.dropGold(location, 2);
		}
	}
	
	private class GoldScanner implements Runnable {
		
		Player player;
		
		public GoldScanner(Player player) {
			this.player = player;
		}
		
		public void run() {
			int amt = 0;
			
			ItemStack[] inv = player.getInventory().getContents();
			ItemStack item;
			for (int i = 0; i < inv.length; i++) {
				item = inv[i];
				if (item != null) {
					if (item.getType() == Material.GOLD_NUGGET) {
						amt += item.getAmount();
						inv[i] = null;
					} else if (item.getType() == Material.GOLD_INGOT) {
						amt += (item.getAmount() * 10);
						inv[i] = null;
					} else if (item.getType() == Material.GOLD_BLOCK) {
						amt += (item.getAmount() * 100);
						inv[i] = null;
					}
				}
			}
			
			if (amt > 0) {
				boolean added = plugin.addMoney(player, amt);
				if (added) {
					player.getInventory().setContents(inv);
				}				
			}
		}
	}
	
}
