package com.nisovin.realcurrency;

import org.bukkit.inventory.ItemStack;

public class CurrencyItem implements Comparable<CurrencyItem> {

	int typeId;
	short data;
	double value;
	
	public CurrencyItem(int typeId, short data, double value) {
		this.typeId = typeId;
		this.data = data;
		this.value = value;
	}
	
	public int getTypeId() {
		return typeId;
	}
	
	public short getData() {
		return data;
	}
	
	public double getValue() {
		return value;
	}
	
	public boolean is(ItemStack item) {
		return item.getTypeId() == typeId && item.getDurability() == data;
	}

	@Override
	public int compareTo(CurrencyItem o) {
		if (o.value > this.value) {
			return 1;
		} else if (o.value < this.value) {
			return -1;
		} else {
			return 0;
		}
	}
	
	
	
}
