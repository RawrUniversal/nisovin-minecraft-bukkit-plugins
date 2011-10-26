package com.nisovin.oldgods;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class AltarManager {

	private OldGods plugin;
	private HashMap<String,Altar> altars = new HashMap<String, AltarManager.Altar>();
	
	public AltarManager(OldGods plugin) {
		this.plugin = plugin;
		
		Configuration config = new Configuration(new File(plugin.getDataFolder(), "altars.yml"));
		config.load();
		List<String> list = config.getStringList("altars", null);
		if (list != null && list.size() > 0) {
			for (String s : list) {
				String[] data = s.split(":");
				String coords = data[0];
				God god = God.valueOf(data[1]);
				int amount = Integer.parseInt(data[2]);
				altars.put(coords, new Altar(god,amount));
			}
		}
	}
	
	public void addAltar(Block block, God god, int amount) {
		String s = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
		altars.put(s, new Altar(god,amount));
		saveAll();
	}
	
	public boolean pray(Player player, Block block) {
		String s = block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
		Altar altar = altars.get(s);
		if (altar != null) {
			boolean success = plugin.addPrayer(player, altar.god, block.getRelative(BlockFace.UP).getLocation(), altar.amount);
			if (success) {
				player.sendMessage("You pray at the altar.");
				
				if (altar.amount >= 25) {
					// it's a temple altar, grant pray permission
					String godName = altar.god.name().toLowerCase();
					if (player.hasPermission("oldgods.disciple." + godName) && !player.hasPermission("oldgods.pray." + godName)) {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "perms player setperm " + player.getName() + " oldgods.pray." + godName + " true");
					}
				}
				
				return true;
			}
		}
		return false;
	}
	
	private void saveAll() {
		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, Altar> entry : altars.entrySet()) {
			String s = entry.getKey() + ":" + entry.getValue().god.name() + ":" + entry.getValue().amount;
			list.add(s);
		}
		Configuration config = new Configuration(new File(plugin.getDataFolder(), "altars.yml"));
		config.setProperty("altars", list);
		config.save();
	}
	
	private class Altar {
		public God god;
		public int amount;
		public Altar(God god, int amount) {
			this.god = god;
			this.amount = amount;
		}
	}
	
}
