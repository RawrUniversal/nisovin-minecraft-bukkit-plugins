package com.nisovin.magicspells.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public interface ItemNameResolver {

	public ItemTypeAndData resolve(String string);
	
	public MagicMaterial resolve2(String string);
	
	public class ItemTypeAndData {
		public int id = 0;
		public short data = 0;
	}
	
	public abstract class MagicMaterial {
		public void setBlock(Block block) {}
		public abstract ItemStack toItemStack();
		public abstract ItemStack toItemStack(int quantity);
	}
	
	public class MagicBlockMaterial extends MagicMaterial {
		MaterialData data;
		
		public MagicBlockMaterial(MaterialData data) {
			this.data = data;
		}
		
		@Override
		public void setBlock(Block block) {
			BlockState state = block.getState();
			state.setType(data.getItemType());
			state.setData(data);
			state.update(true);
		}

		@Override
		public ItemStack toItemStack() {
			return data.toItemStack();
		}

		@Override
		public ItemStack toItemStack(int quantity) {
			return data.toItemStack(quantity);
		}
	}
	
	public class MagicItemMaterial extends MagicMaterial {
		Material type;
		short data;

		public MagicItemMaterial(Material type, short data) {
			this.type = type;
			this.data = data;
		}
		
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(type, 1, data);
		}

		@Override
		public ItemStack toItemStack(int quantity) {
			return new ItemStack(type, quantity, data);
		}
	}
	
	public class MagicUnknownMaterial extends MagicMaterial {
		int type;
		short data;
		
		@Override
		public void setBlock(Block block) {
			if (data < 16) {
				block.setTypeIdAndData(type, (byte)data, true);
			}
		}		
		
		@Override
		public ItemStack toItemStack() {
			return new ItemStack(type, 1, data);
		}
		
		@Override
		public ItemStack toItemStack(int quantity) {
			return new ItemStack(type, quantity, data);
		}
	}
	
}
