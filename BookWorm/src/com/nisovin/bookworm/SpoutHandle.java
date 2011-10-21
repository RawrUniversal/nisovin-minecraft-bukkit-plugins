package com.nisovin.bookworm;

import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.Label;
import org.getspout.spoutapi.gui.PopupScreen;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Widget;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class SpoutHandle {	
	public static void setBookName(short id, String title) {
		new SpoutBookItem(id).setName(BookWorm.S_READ_BOOK + ": " + title);
	}
	
	public static boolean hasSpoutCraft(Player player) {
		return ((SpoutPlayer)player).isSpoutCraftEnabled();
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
		Label textLabel = null;
		Button prev = null, next = null;
		Label pageNumLabel = null;
		if (popup == null || !popup.getPlugin().equals(BookWorm.plugin)) {
			// new popup
			popup = new GenericPopup();
			popup.setPlugin(BookWorm.plugin);

			// create book text label
			textLabel = new GenericLabel();
			textLabel.setX(75);
			textLabel.setY(5);
			textLabel.setHeight(110);
			textLabel.setPriority(RenderPriority.High);
			popup.attachWidget(BookWorm.plugin, textLabel);
			
			// create prev button
			prev = new GenericButton("<--");
			prev.setX(127);
			prev.setY(170);
			prev.setWidth(50);
			prev.setHeight(20);
			prev.setPriority(RenderPriority.Low);
			popup.attachWidget(BookWorm.plugin, prev);
			
			// create next button
			next = new GenericButton("-->");
			next.setX(248);
			next.setY(170);
			next.setWidth(50);
			next.setHeight(20);
			next.setPriority(RenderPriority.Low);
			popup.attachWidget(BookWorm.plugin, next);	
			
			// create page num label
			pageNumLabel = new GenericLabel();
			pageNumLabel.setX(212).setY(180).setWidth(50).setHeight(20);
			pageNumLabel.setAlign(WidgetAnchor.CENTER_CENTER);
			pageNumLabel.setPriority(RenderPriority.Low);
			popup.attachWidget(BookWorm.plugin, pageNumLabel);
			
			// create close button
			Button close = new GenericButton("x");
			close.setX(3).setY(3).setWidth(20).setHeight(20);
			close.setPriority(RenderPriority.Low);
			popup.attachWidget(BookWorm.plugin, close);

			sp.getMainScreen().attachPopupScreen(popup);
		} else {
			// popup is already displayed
			Widget[] widgets = popup.getAttachedWidgets();
			for (Widget widget : widgets) {
				if (widget instanceof Label && ((Label)widget).getY() == 5) {
					textLabel = (Label)widget;
				} else if (widget instanceof Button && ((Button)widget).getText().equals("<--")) {
					prev = (Button)widget;
				} else if (widget instanceof Button && ((Button)widget).getText().equals("-->")) {
					next = (Button)widget;
				} else if (widget instanceof Label && ((Label)widget).getY() == 180) {
					pageNumLabel = (Label)widget;
				}
			}
		}
		
		// create page text
		String text = book.getTitle() + "\n    " + BookWorm.S_READ_BY + " " + book.getAuthor() + "\n\n";
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
		textLabel.setText(text);
		textLabel.setDirty(true);
		
		// set page num
		int pages = (int)Math.ceil(book.pageCount() / 2.0);
		pageNumLabel.setText((page+1) + "/" + pages);
		pageNumLabel.setDirty(true);
		
		// check buttons
		if (page == 0 && prev != null && prev.isEnabled()) {
			prev.setEnabled(false);
			prev.setDirty(true);
		} else if (prev != null && !prev.isEnabled()) {
			prev.setEnabled(true);
			prev.setDirty(true);
		}
		if (pages <= page+1 && next != null && next.isEnabled()) {
			next.setEnabled(false);
			next.setDirty(true);
		} else if (next != null && !next.isEnabled()) {
			next.setEnabled(true);
			next.setDirty(true);
		}
		
		return true;
	}
}
