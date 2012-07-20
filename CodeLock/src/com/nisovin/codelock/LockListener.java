package com.nisovin.codelock;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class LockListener implements Listener {
	
	CodeLock plugin;
	
	private HashMap<Player, PlayerStatus> playerStatuses = new HashMap<Player, PlayerStatus>();
	
	public LockListener(CodeLock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasBlock()) return;
		
		Block block = event.getClickedBlock();
		Material mat = block.getType();
		if (mat == Material.CHEST || mat == Material.DISPENSER || mat == Material.FURNACE || mat == Material.BREWING_STAND || mat == Material.ENCHANTMENT_TABLE || mat == Material.TRAP_DOOR || mat == Material.WOODEN_DOOR || mat == Material.IRON_DOOR_BLOCK) {
			Player player = event.getPlayer();
			PlayerAction action = null;
			if (plugin.isLocked(block) && (!(mat == Material.TRAP_DOOR || mat == Material.WOODEN_DOOR || mat == Material.IRON_DOOR_BLOCK) || Utilities.isDoorClosed(block) )) {
				// it's locked
				boolean bypass = player.hasPermission("codelock.bypass");
				if (player.isSneaking()) {
					action = PlayerAction.REMOVING;
				} else if (!bypass) {
					action = PlayerAction.UNLOCKING;
				}
				if (bypass) {
					String code = plugin.getCode(block);
					player.sendMessage(Settings.strLocked + code);
				}
			} else if (player.isSneaking()) {
				// trying to lock
				if (Settings.checkBuildPerms) {
					BlockPlaceEvent evt = new BlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), player.getItemInHand(), player, true);
					Bukkit.getPluginManager().callEvent(evt);
					if (!evt.isCancelled()) {
						action = PlayerAction.LOCKING;
					}
				} else {
					action = PlayerAction.LOCKING;
				}
			}
			if (action != null) {
				event.setCancelled(true);
				Inventory inv = Bukkit.createInventory(player, Settings.lockInventorySize, Settings.lockTitle);
				ItemStack[] contents = new ItemStack[Settings.lockInventorySize];
				for (int i = 0; i < Settings.buttons.length; i++) {
					contents[Settings.buttonPositions[i]] = new ItemStack(Settings.buttons[i]);
				}
				inv.setContents(contents);
				PlayerStatus status = new PlayerStatus(player, action, inv, block, plugin.getCode(block));
				playerStatuses.put(player, status);
				player.openInventory(inv);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onInventoryClick(final InventoryClickEvent event) {
		PlayerStatus status = playerStatuses.get(event.getWhoClicked());
		if (status != null) {
			event.setCancelled(true);
			status.handleClick(event);
			if (status.getAction() != PlayerAction.LOCKING && status.isCodeComplete()) {
				// code is complete
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						event.getWhoClicked().closeInventory();
					}
				});
				playerStatuses.remove(event.getWhoClicked());
				if (status.getAction() == PlayerAction.UNLOCKING) {
					final Block block = status.getBlock();
					if (block.getState() instanceof InventoryHolder) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								Inventory inv = ((InventoryHolder)block.getState()).getInventory();
								event.getWhoClicked().openInventory(inv);
							}
						}, 3);
					} else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK || block.getType() == Material.TRAP_DOOR) {
						Utilities.openDoor(block);
						if (Settings.autoDoorClose > 0) {
							Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								public void run() {
									Utilities.closeDoor(block);
								}
							}, Settings.autoDoorClose);
						}
					}
				} else if (status.getAction() == PlayerAction.REMOVING) {
					plugin.removeLock(status.getBlock());
					((Player)event.getWhoClicked()).sendMessage(Settings.strRemoved);
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		PlayerStatus status = playerStatuses.get(event.getPlayer());
		if (status != null) {
			if (status.getAction() == PlayerAction.LOCKING) {
				String code = status.getCurrentCode();
				if (code != null && !code.isEmpty()) {
					plugin.addLock(status.getBlock(), code);
					((Player)event.getPlayer()).sendMessage(Settings.strLocked + code);
				}
			}
			playerStatuses.remove(event.getPlayer());
		}
	}
	
}
