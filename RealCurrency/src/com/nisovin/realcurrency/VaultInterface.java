package com.nisovin.realcurrency;

import java.util.List;

import org.bukkit.plugin.ServicePriority;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class VaultInterface implements Economy {

	private RealCurrency plugin;

	public VaultInterface(RealCurrency plugin) {
		this.plugin = plugin;
		plugin.getServer().getServicesManager().register(Economy.class, this, plugin, ServicePriority.Normal);
	}
	
	@Override
	public String getName() {
		return "RealCurrency";
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getBalance(String name) {
		Wallet wallet = plugin.wallets.get(name.toLowerCase());
		if (wallet != null) {
			return wallet.balance();
		} else {
			return 0;
		}
	}

	@Override
	public boolean has(String name, double amount) {
		Wallet wallet = plugin.wallets.get(name.toLowerCase());
		if (wallet != null) {
			return wallet.has(amount);
		} else {
			return false;
		}
	}

	@Override
	public EconomyResponse depositPlayer(String name, double amount) {
		Wallet wallet = plugin.wallets.get(name.toLowerCase());
		if (wallet != null) {
			double bal = wallet.balance();
			boolean added = wallet.add(amount);
			if (added) {
				return new EconomyResponse(amount, bal + amount, ResponseType.SUCCESS, "");
			} else {
				return new EconomyResponse(amount, bal, ResponseType.FAILURE, "Failed to add items.");
			}
		} else {
			return new EconomyResponse(amount, 0, ResponseType.FAILURE, "Player not online.");
		}		
	}

	@Override
	public EconomyResponse withdrawPlayer(String name, double amount) {
		Wallet wallet = plugin.wallets.get(name.toLowerCase());
		if (wallet != null) {
			double bal = wallet.balance();
			if (wallet.has(amount)) {
				boolean removed = wallet.remove(amount);
				if (removed) {
					return new EconomyResponse(amount, bal - amount, ResponseType.SUCCESS, "");
				} else {
					return new EconomyResponse(amount, bal, ResponseType.FAILURE, "Failed to remove items.");
				}
			} else {
				return new EconomyResponse(amount, bal, ResponseType.FAILURE, "Not enough currency.");
			}
		} else {
			return new EconomyResponse(amount, 0, ResponseType.FAILURE, "Player not online.");
		}		
	}

	@Override
	public String format(double amount) {
		return String.format("%.2g%n", amount) + " " + plugin.currencyName;
	}

	@Override
	public boolean hasAccount(String name) {
		return plugin.wallets.containsKey(name.toLowerCase());
	}


	@Override
	public boolean hasBankSupport() {
		return false;
	}	
	
	@Override
	public EconomyResponse bankBalance(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String arg0) {
		return true;
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return null;
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return null;
	}

}
