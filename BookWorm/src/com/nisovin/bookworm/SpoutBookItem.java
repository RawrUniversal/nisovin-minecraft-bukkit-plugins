package com.nisovin.bookworm;

import org.getspout.spoutapi.material.MaterialData;
import org.getspout.spoutapi.material.item.GenericItem;

public class SpoutBookItem extends GenericItem {

	private short id;
	
	public SpoutBookItem(short id) {
		super("Book", MaterialData.book.getRawId());
		this.id = id;
	}

	@Override
	public int getRawData() {
		return id;
	}	
	

}
