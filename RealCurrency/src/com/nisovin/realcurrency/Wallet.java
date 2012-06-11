package com.nisovin.realcurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Wallet {

	private Player player;
	private String playerName;
	
	private boolean dirty = true;
	private double total = 0;
	private TreeMap<CurrencyItem, ArrayList<Integer>> items = new TreeMap<CurrencyItem, ArrayList<Integer>>();
	
	public Wallet(Player player, Set<CurrencyItem> items) {
		this.player = player;
		this.playerName = player.getName();
		for (CurrencyItem item : items) {
			this.items.put(item, new ArrayList<Integer>());
		}
	}
	
	private Player getPlayer() {
		if (!player.isOnline() || player.isDead()) {
			player = Bukkit.getPlayerExact(playerName);
		}
		return player;
	}
	
	private void count() {
		// clear item cache
		for (ArrayList<Integer> list : items.values()) {
			list.clear();
		}
		total = 0;
		
		// validate player
		Player player = getPlayer();
		if (player == null) return;
		
		// count and add to lists
		ItemStack[] inv = player.getInventory().getContents();
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] != null) {
				for (Map.Entry<CurrencyItem, ArrayList<Integer>> entry : items.entrySet()) {
					CurrencyItem ci = entry.getKey();
					if (ci.is(inv[i])) {
						total += ci.getValue() * inv[i].getAmount();
						entry.getValue().add(i);
						break;
					}
				}
			}
		}
		
		dirty = false;
	}
	
	public double balance() {
		if (dirty) count();
		return total;
	}
	
	public boolean has(double amount) {
		if (dirty) count();
		return total >= amount;
	}
	
	public boolean add(double amount) {
		return add(amount, false);
	}
	
	@SuppressWarnings("deprecation")
	private boolean add(final double amount, boolean secondAttempt) {
		Player player = getPlayer();
		if (player == null) return false;
		if (dirty) count();
		
		ItemStack[] inv = player.getInventory().getContents();
		double remaining = amount;
		
		// fill existing stacks
		for (Map.Entry<CurrencyItem, ArrayList<Integer>> entry : items.entrySet()) {
			CurrencyItem ci = entry.getKey();
			ArrayList<Integer> list = entry.getValue();
			// get quantity needed of this currency item
			int needed = (int)Math.floor(remaining / ci.getValue());
			if (needed > 0) {
				for (Integer slot : list) {
					ItemStack item = inv[slot];
					if (item != null && item.getTypeId() == ci.getTypeId() && item.getDurability() == ci.getData()) {
						// add as many as possible
						if (item.getAmount() + needed <= item.getMaxStackSize()) {
							// can fit all in this stack
							item.setAmount(item.getAmount() + needed);
							remaining -= needed * ci.getValue();
							needed = 0;
							break;
						} else {
							// fill up the stack and move on
							int left = item.getMaxStackSize() - item.getAmount();
							item.setAmount(item.getMaxStackSize());
							remaining -= left * ci.getValue();
							needed -= left;
						}
					} else {
						// dirty data
						if (!secondAttempt) {
							count();
							return add(amount, true);
						} else {
							return false;
						}
					}
				}
			}
		}
		
		// add new stacks
		if (remaining > 0) {
			for (Map.Entry<CurrencyItem, ArrayList<Integer>> entry : items.entrySet()) {
				CurrencyItem ci = entry.getKey();
				int needed = (int)Math.floor(remaining / ci.getValue());
				boolean full = false;
				while (needed > 0 && !full) {
					int slot = firstEmpty(inv);
					if (slot < 0) {
						full = true;
						break;
					}
					ItemStack item = new ItemStack(ci.getTypeId(), 0, ci.getData());
					int max = item.getMaxStackSize();
					if (needed > max) {
						item.setAmount(max);
						remaining -= max * ci.getValue();
						needed -= max;
					} else {
						item.setAmount(needed);
						remaining -= needed * ci.getValue();
						needed = 0;
					}
					inv[slot] = item;
					entry.getValue().add(slot);
				}
				if (full) {
					break;
				}
			}
		}
		
		// drop remainder
		if (amount > 0) {
			
		}
		
		// set inventory
		if (remaining == 0) {
			player.getInventory().setContents(inv);
			player.updateInventory();
			total += amount;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean remove(double amount) {
		return remove(amount, false);
	}
	
	@SuppressWarnings("deprecation")
	private boolean remove(final double amount, boolean secondAttempt) {
		if (dirty) count();
		if (total < amount) return false;
		Player player = getPlayer();
		if (player == null) return false;
		
		ItemStack[] inv = player.getInventory().getContents();
		double remaining = amount;
		
		// remove from inventory
		for (Map.Entry<CurrencyItem, ArrayList<Integer>> entry : items.entrySet()) {
			CurrencyItem ci = entry.getKey();
			ArrayList<Integer> list = entry.getValue();
			// get quantity needed of this currency item
			int needed = (int)Math.floor(remaining / ci.getValue());
			if (needed > 0) {
				for (Integer slot : list) {
					ItemStack item = inv[slot];
					if (item != null && ci.is(item)) {
						// remove as many as possible
						if (item.getAmount() >= needed) {
							// can pull all from this stack
							if (item.getAmount() - needed == 0) {
								inv[slot] = null;
								list.remove(slot);
							} else {
								item.setAmount(item.getAmount() - needed);
							}
							remaining -= needed * ci.getValue();
							needed = 0;
							break;
						} else {
							// not enough, take them all and move on
							remaining -= item.getAmount() * ci.getValue();
							needed -= item.getAmount();
							inv[slot] = null;
							list.remove(slot);
						}
					} else {
						// dirty data
						if (!secondAttempt) {
							count();
							return remove(amount, true);
						} else {
							return false;
						}
					}
				}
			}
		}
		
		// make change
		if (remaining > 0) {
			List<ItemStack> overflow = new ArrayList<ItemStack>();
			Stack<Map.Entry<CurrencyItem, ArrayList<Integer>>> itemstack = new Stack<Map.Entry<CurrencyItem, ArrayList<Integer>>>();
			for (Map.Entry<CurrencyItem, ArrayList<Integer>> entry : items.entrySet()) {
				itemstack.push(entry);
			}
			CurrencyItem base = itemstack.pop().getKey();
			while (!itemstack.isEmpty()) {
				Map.Entry<CurrencyItem, ArrayList<Integer>> entry = itemstack.pop();
				CurrencyItem ci = entry.getKey();
				if (remaining > ci.getValue()) {
					// amount left is greater than this currency value, which is bad
					return false;
				}
				if (entry.getValue().size() > 0) {
					int slot = entry.getValue().get(0);
					ItemStack item = inv[slot];
					if (item != null && ci.is(item)) {
						// remove one
						if (item.getAmount() > 1) {
							item.setAmount(item.getAmount() - 1);
						} else {
							inv[slot] = null;
						}
						// create change items
						int quantity = (int) (ci.getValue() / base.getValue() - remaining / base.getValue());
						while (quantity > 0) {
							// create item
							ItemStack newitem = new ItemStack(base.getTypeId(), 0, base.getData());
							if (quantity > newitem.getMaxStackSize()) {
								newitem.setAmount(newitem.getMaxStackSize());
								quantity -= newitem.getMaxStackSize();
							} else {
								newitem.setAmount(quantity);
								quantity = 0;
							}
							// add item to inventory
							int empty = firstEmpty(inv);
							if (empty >= 0) {
								inv[empty] = newitem;
							} else {
								overflow.add(newitem);
							}
						}
						if (overflow.size() > 0) {
							addOverflow(overflow);
						}
						remaining = 0;
						break;
					} else {
						// dirty data
						if (!secondAttempt) {
							count();
							return remove(amount, true);
						} else {
							return false;
						}
					}
				}
			}
		}
		
		// set inventory
		if (remaining == 0) {
			player.getInventory().setContents(inv);
			player.updateInventory();
			total -= amount;
			return true;
		} else {
			return false;
		}		
	}
	
	@SuppressWarnings("deprecation")
	public void combine() {
		count();
		Player player = getPlayer();
		if (player == null) return;
		double amt = total;
		
		ItemStack[] inv = player.getInventory().getContents();
		ItemStack[] backup = inv.clone();
		
		// remove all currency
		for (Map.Entry<CurrencyItem, ArrayList<Integer>> entry : items.entrySet()) {
			for (int slot : entry.getValue()) {
				inv[slot] = null;
			}
			entry.getValue().clear();
		}
		player.getInventory().setContents(inv);
		
		// add it back
		boolean added = add(amt);
		if (!added) {
			player.getInventory().setContents(backup);
			player.updateInventory();
		}
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	private int firstEmpty(ItemStack[] inv) {
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	private void addOverflow(List<ItemStack> items) {
		
	}
	
}
