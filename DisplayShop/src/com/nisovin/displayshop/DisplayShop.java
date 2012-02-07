package com.nisovin.displayshop;

import java.util.Collection;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class DisplayShop extends JavaPlugin implements Listener {

	private Economy economy;
	
	private String firstLine = "[FOR SALE]";
	
	@Override
	public void onEnable() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equals("enchant")) {
			if (!(sender instanceof Player || !sender.isOp())) {
				return true;
			}
			Player player = (Player)sender;
			
			if (args.length < 2 || !args[0].matches("[0-9]+") || !args[1].matches("[0-9]+")) {
				return false;
			}
			
			Enchantment enchant = Enchantment.getById(Integer.parseInt(args[0]));
			int level = Integer.parseInt(args[1]);
			
			ItemStack item = player.getItemInHand();
			try {
				item.addEnchantment(enchant, level);
				player.setItemInHand(item);
			} catch (IllegalArgumentException e) {
				player.sendMessage("Failed to enchant.");
			}
		}
		
		return true;
	}

	@EventHandler(event=SignChangeEvent.class, priority=EventPriority.NORMAL)
	public void onSignChange(SignChangeEvent event) {
		String[] lines = event.getLines();
		if (!lines[0].equals(firstLine)) {
			return;
		}
		
		if (!event.getPlayer().isOp()) {
			event.setCancelled(true);
			return;
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(event=PlayerInteractEvent.class, priority=EventPriority.NORMAL)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		if (!event.hasBlock()) return;
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		Block block = event.getClickedBlock();
		
		// check for shop creation
		if (block.getType() == Material.STEP && event.hasItem() && event.getPlayer().isOp()) {
			// it's a step - check for a shop sign			
			if (isShop(block)) {
				// it's a shop - create the display item
				ItemStack item = event.getItem().clone();
				final Location location = block.getLocation().clone().add(.5, 1, .5);
				final Item it = block.getWorld().dropItem(location.clone(), item);
				it.setVelocity(new Vector(0, 0, 0));
				event.setCancelled(true);
			}
		}
		
		// check for purchases
		if (block.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign)block.getState();
			String[] lines = sign.getLines();
			if (!lines[0].equals(firstLine)) {
				return;
			}
			
			Block itemBlock = block.getRelative(((Attachable)block.getState().getData()).getAttachedFace()).getRelative(BlockFace.UP);
			
			String samt = lines[3].replaceAll("[^0-9.]", "");
			if (samt.isEmpty()) {
				return;
			}
			double amount = Double.parseDouble(samt);
			
			ItemStack item = null;
			@SuppressWarnings("unchecked")
			Collection<Item> items = block.getWorld().getEntitiesByClass(Item.class);
			for (Item i : items) {
				if (i.getLocation().getBlock().equals(itemBlock)) {
					item = i.getItemStack().clone();
					break;
				}
			}
			if (item == null) {
				return;
			}
			
			Player player = event.getPlayer();
			EconomyResponse response = economy.withdrawPlayer(player.getName(), amount);
			if (response.transactionSuccess()) {
				int slot = player.getInventory().firstEmpty();
				if (slot >= 0) {
					player.getInventory().setItem(slot, item);
					player.updateInventory();
				} else {
					player.getWorld().dropItemNaturally(player.getLocation(), item);
				}
				player.sendMessage(ChatColor.GREEN + "Purchase successful!");
			} else {
				player.sendMessage(ChatColor.GREEN + "You cannot afford that item.");
			}
			
			event.setCancelled(true);
		}
	}
	
	@EventHandler(event=PlayerPickupItemEvent.class, priority=EventPriority.NORMAL)
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (event.isCancelled()) return;
		
		Item item = event.getItem();
		Block b = item.getLocation().getBlock();
		if (b.getType() != Material.STEP) {
			return;
		}
		if (isShop(b)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(event=ItemDespawnEvent.class, priority=EventPriority.NORMAL)
	public void onItemDespawn(ItemDespawnEvent event) {
		if (event.isCancelled()) return;
		
		Item item = (Item)event.getEntity();
		Block b = item.getLocation().getBlock();
		if (b.getType() != Material.STEP) {
			return;
		}
		if (isShop(b)) {
			event.setCancelled(true);
		}
	}
	
	public boolean isShop(Block step) {
		Block[] blocks = new Block[4];
		blocks[0] = step.getRelative(1, -1, 0);
		blocks[1] = step.getRelative(-1, -1, 0);
		blocks[2] = step.getRelative(0, -1, 1);
		blocks[3] = step.getRelative(0, -1, -1);
		for (Block b : blocks) {
			if (b.getType() == Material.WALL_SIGN) {
				Sign sign = (Sign)b.getState();
				if (sign.getLines()[0].equals(firstLine)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void onDisable() {
	}

}
