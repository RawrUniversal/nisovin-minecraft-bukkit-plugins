package com.nisovin.magicspells.materials;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public abstract class MagicMaterial {
	
	public Material getMaterial() { return null; }
	
	public MaterialData getMaterialData() { return null; }
	
	public final void setBlock(Block block) {
		setBlock(block, true);
	}
	
	public void setBlock(Block block, boolean applyPhysics) {}
	
	public final ItemStack toItemStack() {
		return toItemStack(1);
	}
	
	public abstract ItemStack toItemStack(int quantity);
	
	public final boolean equals(Block block) {
		return equals(block.getState().getData());
	}
	
	public boolean equals(MaterialData matData) {
		MaterialData d = getMaterialData();
		if (d != null) {
			return d.equals(matData);
		} else {
			return false;
		}
	}
	
	public boolean equals(ItemStack itemStack) {
		MaterialData d = getMaterialData();
		if (d != null) {
			ItemStack i = d.toItemStack();
			return i.getType() == itemStack.getType() && i.getDurability() == itemStack.getDurability();
		} else {
			return false;
		}
	}
}