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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CodeLock extends JavaPlugin implements Listener {

	private int lockInventorySize = 27;
	
	private Material[] buttons = new Material[] {
		Material.APPLE, Material.BOOK, Material.COAL,
		Material.DIAMOND, Material.EGG, Material.FURNACE,
		Material.GLASS, Material.STONE_HOE, Material.IRON_INGOT
	};
	private int[] buttonPositions = new int[] {
		3, 4, 5,
		12, 13, 14, 
		21, 22, 23
	};
	private char[] letterCodes = new char[] {
		'A', 'B', 'C',
		'D', 'E', 'F',
		'G', 'H', 'I'
	};
	
	private HashMap<String,String> locks = new HashMap<String,String>();
	
	private HashMap<Player,PlayerStatus> playerStatuses = new HashMap<Player,PlayerStatus>();
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this,this);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.hasBlock()) return;
		
		Block block = event.getClickedBlock();
		Material mat = block.getType();
		if (mat == Material.CHEST || mat == Material.DISPENSER || mat == Material.FURNACE || mat == Material.BREWING_STAND || mat == Material.WOODEN_DOOR || mat == Material.IRON_DOOR_BLOCK) {
			Player player = event.getPlayer();
			PlayerAction action = null;
			if (locks.containsKey(getLocStr(block))) {
				// it's locked
				//if (player.isSneaking()) {
					action = PlayerAction.REMOVING;
				//} else {
				//	action = PlayerAction.UNLOCKING;
				//}
			} else if (player.isSneaking()) {
				// trying to lock
				action = PlayerAction.LOCKING;
			}
			if (action != null) {
				event.setCancelled(true);
				Inventory inv = Bukkit.createInventory(player, lockInventorySize, "Code Entry");
				ItemStack[] contents = new ItemStack[lockInventorySize];
				for (int i = 0; i < buttons.length; i++) {
					contents[buttonPositions[i]] = new ItemStack(buttons[i]);
				}
				inv.setContents(contents);
				PlayerStatus status = new PlayerStatus(player, action, inv, block);
				playerStatuses.put(player, status);
				player.openInventory(inv);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onInventoryClick(InventoryClickEvent event) {
		PlayerStatus status = playerStatuses.get(event.getWhoClicked());
		if (status != null) {
			event.setCancelled(true);
			status.handleClick(event);
			if (status.getAction() != PlayerAction.LOCKING && status.isCodeComplete()) {
				// code is complete
				event.getWhoClicked().closeInventory();
				playerStatuses.remove(event.getWhoClicked());
				if (status.getAction() == PlayerAction.UNLOCKING) {
					Block block = status.getBlock();
					if (block.getState() instanceof InventoryHolder) {
						Inventory inv = ((InventoryHolder)block.getState()).getInventory();
						event.getWhoClicked().openInventory(inv);
					}
				} else if (status.getAction() == PlayerAction.REMOVING) {
					removeLock(status.getBlock());
					((Player)event.getWhoClicked()).sendMessage("Removed lock.");
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
					addLock(status.getBlock(), code);
					((Player)event.getPlayer()).sendMessage("Locked with code: " + code);
				}
			}
			playerStatuses.remove(event.getPlayer());
		}
	}
	
	public boolean isLocked(Block block) {
		return locks.containsKey(getLocStr(block));
	}
	
	public void addLock(Block block, String code) {
		Material type = block.getType();
		locks.put(getLocStr(block), code);
		if (type == Material.CHEST) {
			int[] xoff = new int[] {-1, 0, 1, 0};
			int[] zoff = new int[] {0, -1, 0, 1};
			for (int i = 0; i < 4; i++) {
				if (block.getRelative(xoff[i], 0, zoff[i]).getType() == Material.CHEST) {
					locks.put(getLocStr(block, xoff[i], 0 , zoff[i]), code);
					return;
				}
			}
		} else if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK) {
			if (block.getRelative(BlockFace.UP).getType() == type) {
				locks.put(getLocStr(block, 0, 1, 0), code);
			} else if (block.getRelative(BlockFace.DOWN).getType() == type) {
				locks.put(getLocStr(block, 0, -1, 0), code);
			}
		}
	}
	
	public void removeLock(Block block) {
		locks.remove(getLocStr(block));
		if (block.getType() == Material.CHEST) {
			locks.remove(getLocStr(block, -1, 0, 0));
			locks.remove(getLocStr(block, 1, 0, 0));
			locks.remove(getLocStr(block, 0, 0, 1));
			locks.remove(getLocStr(block, 0, 0, -1));
		} else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK) {
			locks.remove(getLocStr(block, 0, 1, 0));
			locks.remove(getLocStr(block, 0, -1, 0));
		}
	}
	
	private String getLocStr(Block block) {
		return getLocStr(block, 0, 0, 0);
	}
	
	private String getLocStr(Block block, int offsetX, int offsetY, int offsetZ) {
		return block.getWorld() + "," + (block.getX() + offsetX) + "," + (block.getY() + offsetY) + "," + (block.getZ() + offsetZ);
	}
	
	public class PlayerStatus {
		private PlayerAction action;
		private Block block;
		private String locStr;
		
		private StringBuilder code;
		
		public PlayerStatus(Player player, PlayerAction action, Inventory inventory, Block block) {
			this.action = action;
			this.block = block;
			this.locStr = getLocStr(block);
			this.code = new StringBuilder(10);
		}
		
		public void handleClick(InventoryClickEvent event) {			
			int idx = indexOf(buttonPositions, event.getSlot());
			if (idx >= 0) {
				code.append(letterCodes[idx]);
			}
		}
		
		public boolean isCodeComplete() {
			String realCode = locks.get(locStr);
			if (realCode != null) {
				return realCode.equals(code.toString());
			} else {
				return false;
			}
		}
		
		public String getCurrentCode() {
			return code.toString();
		}
		
		public PlayerAction getAction() {
			return action;
		}
		
		public Block getBlock() {
			return block;
		}
		
		private int indexOf(int[] array, int val) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] == val) {
					return i;
				}
			}
			return -1;
		}
	}
	
	public enum PlayerAction {
		LOCKING,
		UNLOCKING,
		REMOVING
	}
	
}
