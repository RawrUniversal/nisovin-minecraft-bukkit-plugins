package com.nisovin.magicspells.shop;

import java.util.HashMap;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

public class CurrencyHandler {

	private HashMap<String,String> currencies;
	private String defaultCurrency;
	private Economy economy;
	
	public CurrencyHandler(Configuration config) {
		ConfigurationSection sec = config.getConfigurationSection("currencies");
		if (sec == null) {
			defaultCurrency = "money";
			currencies.put("money", "vault");
		}
		Set<String> keys = sec.getKeys(false);
		for (String key : keys) {
			if (defaultCurrency == null) {
				defaultCurrency = key;
			}
			currencies.put(key, sec.getString(key));
		}		

		// set up vault hook
		RegisteredServiceProvider<Economy> provider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if (provider != null) {
			economy = provider.getProvider();
		}
	}
	
	public boolean has(Player player, double amount) {
		return has(player, amount, defaultCurrency);
	}
	
	public boolean has(Player player, double amount, String currency) {
		String c = currencies.get(currency);
		if (c == null) c = defaultCurrency;
		
		if (c.equalsIgnoreCase("vault") && economy != null) {
			return economy.has(player.getName(), amount);
		} else if (c.matches("^[0-9]+$")) {
			return player.getInventory().contains(Material.getMaterial(Integer.parseInt(c)), (int)amount);
		} else if (c.matches("^[0-9]+:[0-9]+$")) {
			String[] s = c.split(":");
			int type = Integer.parseInt(s[0]);
			short data = Short.parseShort(s[1]);
			return player.getInventory().contains(new ItemStack(type, (int)amount, data), (int)amount);
		} else {
			return false;
		}
	}
	
	public void remove(Player player, double amount) {
		remove(player, amount, defaultCurrency);
	}
	
	public void remove(Player player, double amount, String currency) {
		String c = currencies.get(currency);
		if (c == null) c = defaultCurrency;
		
		if (c.equalsIgnoreCase("vault") && economy != null) {
			economy.withdrawPlayer(player.getName(), amount);
		} else if (c.matches("^[0-9]+$")) {
			player.getInventory().remove(new ItemStack(Material.getMaterial(Integer.parseInt(c)), (int)amount));
		} else if (c.matches("^[0-9]+:[0-9]+$")) {
			String[] s = c.split(":");
			int type = Integer.parseInt(s[0]);
			short data = Short.parseShort(s[1]);
			player.getInventory().remove(new ItemStack(type, (int)amount, data));
		}
	}
	
	public boolean isValidCurrency(String currency) {
		return currencies.containsKey(currency);
	}
	
}
