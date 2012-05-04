package com.nisovin.codelock;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CodeLock extends JavaPlugin implements Listener {

	private int lockInventorySize = 27;
	private String lockTitle = "Code Entry";
	
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
	
	private int autoDoorClose = 100;
	private boolean checkBuildPerms = true;
	
	private String strLocked = "Locked with code: ";
	private String strRemoved = "Removed lock.";
	
	private HashMap<String,String> locks = new HashMap<String,String>();
	
	private HashMap<Player,PlayerStatus> playerStatuses = new HashMap<Player,PlayerStatus>();
	
	@Override
	public void onEnable() {
		
		// load config
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.saveDefaultConfig();
		}
		Configuration config = getConfig();
		
		lockInventorySize = config.getInt("lock-inventory-size", lockInventorySize);
		lockTitle = config.getString("lock-title", lockTitle);
		if (config.contains("buttons")) {
			List<Integer> list = config.getIntegerList("buttons");
			buttons = new Material[list.size()];
			for (int i = 0; i < buttons.length; i++) {
				buttons[i] = Material.getMaterial(list.get(i).intValue());
			}
		}
		if (config.contains("button-positions")) {
			List<Integer> list = config.getIntegerList("button-positions");
			buttonPositions = new int[list.size()];
			for (int i = 0; i < buttonPositions.length; i++) {
				buttonPositions[i] = list.get(i).intValue();
			}
		}
		if (config.contains("letter-codes")) {
			List<String> list = config.getStringList("letter-codes");
			letterCodes = new char[list.size()];
			for (int i = 0; i < letterCodes.length; i++) {
				letterCodes[i] = list.get(i).charAt(0);
			}
		}
		autoDoorClose = config.getInt("aut-door-close", autoDoorClose);
		checkBuildPerms = config.getBoolean("check-build-perms", checkBuildPerms);
		strLocked = config.getString("str-locked", strLocked);
		strRemoved = config.getString("str-removed", strRemoved);
		
		// load locks
		load();
		
		// register events
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
			if (locks.containsKey(getLocStr(block)) && (!(mat == Material.WOODEN_DOOR || mat == Material.IRON_DOOR_BLOCK) || isDoorClosed(block) )) {
				// it's locked
				boolean bypass = player.hasPermission("codelock.bypass");
				if (player.isSneaking()) {
					action = PlayerAction.REMOVING;
				} else if (!bypass) {
					action = PlayerAction.UNLOCKING;
				}
				if (bypass) {
					String code = locks.get(getLocStr(block));
					player.sendMessage(strLocked + code);
				}
			} else if (player.isSneaking()) {
				// trying to lock
				if (checkBuildPerms) {
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
				Inventory inv = Bukkit.createInventory(player, lockInventorySize, lockTitle);
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
	public void onInventoryClick(final InventoryClickEvent event) {
		PlayerStatus status = playerStatuses.get(event.getWhoClicked());
		if (status != null) {
			event.setCancelled(true);
			status.handleClick(event);
			if (status.getAction() != PlayerAction.LOCKING && status.isCodeComplete()) {
				// code is complete
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						event.getWhoClicked().closeInventory();
					}
				});
				playerStatuses.remove(event.getWhoClicked());
				if (status.getAction() == PlayerAction.UNLOCKING) {
					final Block block = status.getBlock();
					if (block.getState() instanceof InventoryHolder) {
						Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
							public void run() {
								Inventory inv = ((InventoryHolder)block.getState()).getInventory();
								event.getWhoClicked().openInventory(inv);
							}
						}, 3);
					} else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK) {
						openDoor(block);
						if (autoDoorClose > 0) {
							Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
								public void run() {
									closeDoor(block);
								}
							}, autoDoorClose);
						}
					}
				} else if (status.getAction() == PlayerAction.REMOVING) {
					removeLock(status.getBlock());
					((Player)event.getWhoClicked()).sendMessage(strRemoved);
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
					((Player)event.getPlayer()).sendMessage(strLocked + code);
				}
			}
			playerStatuses.remove(event.getPlayer());
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().hasPermission("codelock.bypass") && locks.containsKey(getLocStr(event.getBlock()))) {
			event.setCancelled(true);
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
				}
			}
		} else if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK) {
			if (block.getRelative(BlockFace.UP).getType() == type) {
				locks.put(getLocStr(block, 0, 1, 0), code);
			} else if (block.getRelative(BlockFace.DOWN).getType() == type) {
				locks.put(getLocStr(block, 0, -1, 0), code);
			}
		}
		save();
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
		save();
	}
	
	private String getLocStr(Block block) {
		return getLocStr(block, 0, 0, 0);
	}
	
	private String getLocStr(Block block, int offsetX, int offsetY, int offsetZ) {
		return block.getWorld().getName() + "," + (block.getX() + offsetX) + "," + (block.getY() + offsetY) + "," + (block.getZ() + offsetZ);
	}
	
	private boolean isDoorClosed(Block block) {
		byte data = block.getData();
		if ((data & 0x8) == 0x8) {
			block = block.getRelative(BlockFace.DOWN);
			data = block.getData();
		}
		return ((data & 0x4) == 0);
	}
	
	private void openDoor(Block block) {
		byte data = block.getData();
		if ((data & 0x8) == 0x8) {
			block = block.getRelative(BlockFace.DOWN);
			data = block.getData();
		}
		if (isDoorClosed(block)) {
			data = (byte) (data | 0x4);
			block.setData(data, true);
			block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
		}
	}
	
	private void closeDoor(Block block) {
		byte data = block.getData();
		if ((data & 0x8) == 0x8) {
			block = block.getRelative(BlockFace.DOWN);
			data = block.getData();
		}
		if (!isDoorClosed(block)) {
			data = (byte) (data & 0xb);
			block.setData(data, true);
			block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
		}
	}
	
	private void load() {
		File file = new File(getDataFolder(), "data.yml");
		if (file.exists()) {
			YamlConfiguration yaml = new YamlConfiguration();
			try {
				yaml.load(file);
				for (String s : yaml.getKeys(false)) {
					locks.put(s, yaml.getString(s));
				}
			} catch (Exception e) {
				getLogger().severe("Failed to load data!");
				e.printStackTrace();
				this.setEnabled(false);
			}
		}
	}
	
	private void save() {
		File file = new File(getDataFolder(), "data.yml");
		if (file.exists()) {
			file.delete();
		}
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			for (String s : locks.keySet()) {
				yaml.set(s, locks.get(s));
			}
			yaml.save(file);
		} catch (Exception e) {
			getLogger().severe("Failed to save data!");
			e.printStackTrace();
		}
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
