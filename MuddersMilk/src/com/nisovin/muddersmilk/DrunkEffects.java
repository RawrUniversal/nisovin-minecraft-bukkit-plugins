package com.nisovin.muddersmilk;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DrunkEffects implements Runnable {

	private MuddersMilk plugin;
	private Random random;
	
	public DrunkEffects(MuddersMilk plugin) {
		this.plugin = plugin;
		random = new Random();
	}
	
	@Override
	public void run() {
		for (Object o : plugin.getDrunks().keySet().toArray()) {
			String p = (String)o;
			Player player = plugin.getServer().getPlayer(p);
			if (player == null || !player.isOnline()) {
				plugin.getDrunks().remove(p);
			} else {
				int drunkLevel = plugin.getDrunks().get(p);
				
				// stagger
				if (random.nextInt(10) < 3+drunkLevel) {
					Vector v = new Vector((random.nextDouble()-.5)/2 * drunkLevel, 0, (random.nextDouble()-.5)/2 * drunkLevel);
					player.setVelocity(v);
				}
				
				// drop item
				if (random.nextInt(100) < (10+drunkLevel*2)) {
					ItemStack item = player.getItemInHand();
					if (item != null && item.getAmount() > 0) {
						if (item.getAmount() == 1) {
							player.getWorld().dropItemNaturally(player.getLocation(), item);
							player.setItemInHand(null);
						} else {
							player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(item.getType(), item.getAmount(), item.getDurability()));
							item.setAmount(item.getAmount()-1);
							player.setItemInHand(item);
						}
					}
				}
				
				// sober up
				if (random.nextInt(100) < 15) {
					drunkLevel--;
					if (drunkLevel == 0) {
						plugin.getDrunks().remove(p);
						player.sendMessage(ChatColor.YELLOW + "You are now sober.");
						if (plugin.getDrunks().size() == 0) {
							plugin.stopEffects();
						}
					} else {
						plugin.getDrunks().put(p, drunkLevel);
					}
				}
			}
		}
	}

}
