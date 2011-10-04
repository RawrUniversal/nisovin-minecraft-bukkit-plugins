package com.nisovin.oldgods;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.oldgods.godhandlers.*;

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
			boolean success = plugin.addPrayer(player, altar.god, altar.amount);
			if (success) {
				if (altar.god == God.COOKING) {
					CookingHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.DEATH) {
					DeathHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.EXPLORATION) {
					ExplorationHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.FARMING) {
					FarmingHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.HEALING) {
					HealingHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.HUNT) {
					HuntHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.LOVE) {
					LoveHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.MINING) {
					MiningHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.OCEAN) {
					OceanHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.WAR) {
					WarHandler.pray(player, block, altar.amount);
				} else if (altar.god == God.WISDOM) {
					WisdomHandler.pray(player, block, altar.amount);
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
