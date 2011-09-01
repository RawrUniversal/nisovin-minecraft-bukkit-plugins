package com.nisovin.bookworm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.gui.Widget;
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
					sp.sendPacket(new PacketItemName(Material.BOOK.getId(), (short) 0, BookWorm.S_READ_BOOK)); // don't know why this is needed
				}
			}
		}
	}
	
	public static boolean showBook(Player player, Book book, int page) {
		SpoutPlayer sp = (SpoutPlayer)player;
		if (!sp.isSpoutCraftEnabled()) {
			return false;
		}
				
		if (page < 0) page = 0;
		String[] pageText = book.getPage(page*2);
		String[] pageText2 = book.getPage(page*2+1);
		if (pageText == null) {
			return true;
		}
		
		PopupScreen popup = sp.getMainScreen().getActivePopup();
		Label pageLabel = null;
		if (popup == null || !popup.getPlugin().equals(BookWorm.plugin)) {
			// new popup
			popup = new GenericPopup();
			popup.setPlugin(BookWorm.plugin);
			sp.getMainScreen().attachPopupScreen(popup);			

			// create book text label
			pageLabel = new GenericLabel();
			pageLabel.setX(75);
			pageLabel.setY(5);
			popup.attachWidget(BookWorm.plugin, pageLabel);
			
			// create buttons
			GenericButton prev = new GenericButton("<--");
			prev.setX(157);
			prev.setY(150);
			prev.setWidth(50);
			prev.setHeight(20);
			popup.attachWidget(BookWorm.plugin, prev);
			GenericButton next = new GenericButton("-->");
			next.setX(218);
			next.setY(150);
			next.setWidth(50);
			next.setHeight(20);
			
			// show popup
			popup.attachWidget(BookWorm.plugin, next);
		} else {
			// popup is already displayed
			Widget[] widgets = popup.getAttachedWidgets();
			for (Widget widget : widgets) {
				if (widget instanceof Label && ((Label)widget).getY() == 5) {
					pageLabel = (Label)widget;
				}
			}
		}
		
		// create page text
		String text = "";
		for (String s : pageText) {
			if (s != null && !s.isEmpty()) {
				text += s + "\n";
			}
		}
		if (pageText2 != null) {
			for (String s : pageText2) {
				if (s != null && !s.isEmpty()) {
					text += s + "\n";
				}
			}
		}
		pageLabel.setText(text);
		pageLabel.setDirty(true);
		
		System.out.println(((SpoutPlayer)player).getMainScreen().getWidth() + "," + ((SpoutPlayer)player).getMainScreen().getHeight());
		
		return true;
	}
}
