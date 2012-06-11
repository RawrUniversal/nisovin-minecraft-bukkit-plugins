package com.nisovin.realcurrency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RealCurrency extends JavaPlugin implements Listener {

	String currencyName = "gold";
	Set<CurrencyItem> currencyItems = new HashSet<CurrencyItem>();
	double smallestCurrency = 0;
	HashMap<String, Wallet> wallets = new HashMap<String, Wallet>();
	
	@Override
	public void onEnable() {
		currencyItems.add(new CurrencyItem(371, (short) 0, 1));
		currencyItems.add(new CurrencyItem(266, (short) 0, 10));
		currencyItems.add(new CurrencyItem(41, (short) 0, 100));
		
		// validate currency
		for (CurrencyItem ci : currencyItems) {
			if (smallestCurrency == 0 || ci.getValue() < smallestCurrency) {
				smallestCurrency = ci.getValue();
			}
		}
		for (CurrencyItem ci : currencyItems) {
			if (ci.getValue() % smallestCurrency != 0) {
				getLogger().severe("CURRENCY ERROR: " + ci.getValue() + " is not divisible by " + smallestCurrency);
			}
		}
		
		for (Player p : getServer().getOnlinePlayers()) {
			getWallet(p);
		}
		
		getServer().getPluginManager().registerEvents(this,this);
		if (getServer().getPluginManager().isPluginEnabled("Vault")) {
			new VaultInterface(this);
		}
	}
	
	public Wallet getWallet(Player player) {
		Wallet wallet = wallets.get(player.getName().toLowerCase());
		if (wallet == null) {
			wallet = new Wallet(player, currencyItems);
			wallets.put(player.getName().toLowerCase(), wallet);
		}
		return wallet;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		getWallet(event.getPlayer());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (command.getName().equals("addcurrency")) {
				wallets.get(player.getName()).add(Float.parseFloat(args[0]));
			} else if (command.getName().equals("remcurrency")) {
				wallets.get(player.getName()).remove(Float.parseFloat(args[0]));
			} else if (command.getName().equals("combinecurrency")) {
				wallets.get(player.getName()).combine();
			}
		}
		return true;
	}
	
}
