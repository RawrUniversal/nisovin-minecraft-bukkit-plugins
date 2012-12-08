package com.nisovin.coop;

import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class DropListener implements Listener {

	private CoopPlugin plugin;
	
	public DropListener(CoopPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event) {
		// get killer
		Player killer = event.getEntity().getKiller();
		if (killer == null) return;
		
		// get killer's party
		Party party = plugin.getParty(killer);
		if (party == null) return;
		
		// drop the items and give xp for each party member
		Location loc = event.getEntity().getLocation();
		int xp = event.getDroppedExp();
		List<ItemStack> drops = event.getDrops();
		List<Player> members = party.getMembersInRangeOf(killer);
		for (Player p : members) {
			System.out.println("dropping for " + p.getName());
			for (ItemStack stack : drops) {
				Item item = p.getWorld().dropItem(loc, stack.clone());
				item.setMetadata("CoopPickupOwner", new FixedMetadataValue(plugin, p.getName()));
				if (item.getItemStack().getEnchantments().size() == 0) {
					item.getItemStack().addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 15);
				}
				item.setTicksLived(3000);
			}
			p.giveExp(xp);
		}
		
		// remove original drops
		drops.clear();
		event.setDroppedExp(0);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		if (item.hasMetadata("CoopPickupOwner")) {
			List<MetadataValue> values = item.getMetadata("CoopPickupOwner");
			if (!values.get(0).asString().equals(event.getPlayer().getName())) {
				event.setCancelled(true);
			} else {
				ItemStack stack = item.getItemStack();
				if (stack.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD) == 15) {
					if (stack.getEnchantments().size() == 1) {
						item.setItemStack(new ItemStack(stack.getType(), stack.getAmount(), stack.getDurability()));
					} else {
						stack.removeEnchantment(Enchantment.DAMAGE_UNDEAD);
					}
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		
		// get player's party
		Party party = plugin.getParty(player);
		if (party == null) return;
		
		Block b = event.getBlock();
		Material type = b.getType();
		if (type == Material.COAL_ORE || type == Material.IRON_ORE || type == Material.GOLD_ORE || type == Material.DIAMOND_ORE || type == Material.REDSTONE_ORE || type == Material.LAPIS_ORE) {
			// drop items for each party member
			Location loc = b.getLocation();
			int xp = event.getExpToDrop();
			Collection<ItemStack> drops = b.getDrops(event.getPlayer().getItemInHand());
			List<Player> members = party.getMembersInRangeOf(player);
			for (Player p : members) {
				// drop items
				for (ItemStack stack : drops) {
					Item item = p.getWorld().dropItem(loc, stack.clone());
					item.setMetadata("CoopPickupOwner", new FixedMetadataValue(plugin, p.getName()));
					item.getItemStack().addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 15);
					item.setTicksLived(3000);
				}
				// give exp
				p.giveExp(xp);
			}
			
			// prevent normal action
			event.setCancelled(true);
			b.setType(Material.AIR);
		}
	}
	
}
