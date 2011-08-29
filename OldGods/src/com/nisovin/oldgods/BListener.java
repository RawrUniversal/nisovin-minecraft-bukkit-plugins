package com.nisovin.oldgods;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public class BListener extends BlockListener {

	private OldGods plugin;
	
	public BListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Monitor, plugin);
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.MINING) {
			Material inHand = event.getPlayer().getItemInHand().getType();
			if (inHand == Material.IRON_PICKAXE || inHand == Material.GOLD_PICKAXE || inHand == Material.DIAMOND_PICKAXE) {
				Block b = event.getBlock();
				if (b.getType() == Material.DIAMOND_ORE) {
					event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.DIAMOND, 1));
				} else if (b.getType() == Material.IRON_ORE) {
					event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.IRON_INGOT, 1));					
				} else if (b.getType() == Material.GOLD_ORE) {
					event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.GOLD_INGOT, 1));	
				} else if (b.getType() == Material.LAPIS_ORE) {
					event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.INK_SACK, 2, (short)4));
				}
			}
		} else if (god == God.FARMING) {
			Material inHand = event.getPlayer().getItemInHand().getType();
			if (inHand == Material.IRON_HOE || inHand == Material.GOLD_HOE || inHand == Material.DIAMOND_HOE) {
				Block b = event.getBlock();
				if (b.getType() == Material.CROPS) {
					b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.WHEAT, 1));
				}
			}
		}
	}
	
	
	
}
