package com.nisovin.bookworm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.packet.PacketItemName;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SpoutHandle {	
	public static void setBookName(short id, String title) {
		if (BookWorm.SPOUT_ENABLED) { 
			SpoutManager.getItemManager().setItemName(Material.BOOK, id, BookWorm.S_READ_BOOK + ": " + title);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				SpoutPlayer sp = (SpoutPlayer)p;
				if (sp.isSpoutCraftEnabled()) {
					sp.sendPacket(new PacketItemName(Material.BOOK.getId(), id, BookWorm.S_READ_BOOK + ": " + title));
				}
			}
		}
	}
}
