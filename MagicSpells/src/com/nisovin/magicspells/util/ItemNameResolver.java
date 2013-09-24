package com.nisovin.magicspells.util;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public interface ItemNameResolver {

	static Random rand = new Random();

	@Deprecated
	public ItemTypeAndData resolve(String string);
	
	public MagicMaterial resolveItem(String string);
	
	public MagicMaterial resolveBlock(String string);
	
	public class ItemTypeAndData {
		public int id = 0;
		public short data = 0;
	}
	
	public abstract class MagicMaterial {
		public Material getMaterial() { return null; }
		public MaterialData getMaterialData() { return null; }
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
		public Material getMaterial() {
			return data.getItemType();
		}
		
		@Override
		public MaterialData getMaterialData() {
			return data;
		}
		
		@Override
		public void setBlock(Block block) {
			BlockState state = block.getState();
			state.setType(getMaterial());
			state.setData(getMaterialData());
			state.update(true);
		}

		@Override
		public ItemStack toItemStack() {
			return getMaterialData().toItemStack();
		}

		@Override
		public ItemStack toItemStack(int quantity) {
			return getMaterialData().toItemStack(quantity);
		}
	}
	
	public class MagicRandomBlockMaterial extends MagicBlockMaterial {
		MaterialData[] datas;
		
		public MagicRandomBlockMaterial(MaterialData[] datas) {
			super(null);
			this.datas = datas;
		}
		
		@Override
		public Material getMaterial() {
			return datas[rand.nextInt(datas.length)].getItemType();
		}
		
		@Override
		public MaterialData getMaterialData() {
			return datas[rand.nextInt(datas.length)];
		}		
	}
	
	public class MagicItemMaterial extends MagicMaterial {
		Material type;
		MaterialData matData;
		short duraData;

		public MagicItemMaterial(Material type, short data) {
			this.type = type;
			this.duraData = data;
		}
		
		public MagicItemMaterial(MaterialData data) {
			type = data.getItemType();
			matData = data;
		}
		
		public short getDurability() {
			return duraData;
		}
		
		@Override
		public Material getMaterial() {
			return type;
		}
		
		@Override
		public MaterialData getMaterialData() {
			if (matData != null) {
				return matData;
			} else {
				return null;
			}
		}
		
		@Override
		public ItemStack toItemStack() {
			MaterialData matData = getMaterialData();
			if (matData != null) {
				return matData.toItemStack(1);
			}
			return new ItemStack(getMaterial(), 1, getDurability());
		}

		@Override
		public ItemStack toItemStack(int quantity) {
			MaterialData matData = getMaterialData();
			if (matData != null) {
				return matData.toItemStack(quantity);
			}
			return new ItemStack(getMaterial(), quantity, getDurability());
		}
	}
	
	public class MagicUnknownMaterial extends MagicMaterial {
		int type;
		short data;
		
		public MagicUnknownMaterial(int type, short data) {
			this.type = type;
			this.data = data;
		}
		
		@Override
		public Material getMaterial() {
			return Material.getMaterial(type);
		}
		
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
