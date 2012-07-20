package com.nisovin.codelock;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class PlayerStatus {
	private PlayerAction action;
	private Block block;
	private String realCode;
	
	private StringBuilder code;
	
	public PlayerStatus(Player player, PlayerAction action, Inventory inventory, Block block, String realCode) {
		this.action = action;
		this.block = block;
		this.realCode = realCode;
		this.code = new StringBuilder(10);
	}
	
	public void handleClick(InventoryClickEvent event) {			
		int idx = indexOf(Settings.buttonPositions, event.getSlot());
		if (idx >= 0) {
			code.append(Settings.letterCodes[idx]);
		}
	}
	
	public boolean isCodeComplete() {
		//String realCode = locks.get(locStr);
		if (realCode != null) {
			return realCode.equals(code.toString());
		} else {
			return false;
		}
	}
	
	public String getCurrentCode() {
		return code.toString();
	}
	
	public PlayerAction getAction() {
		return action;
	}
	
	public Block getBlock() {
		return block;
	}
	
	private int indexOf(int[] array, int val) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == val) {
				return i;
			}
		}
		return -1;
	}
}