package com.nisovin.bookworm;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenListener;
import org.getspout.spoutapi.player.SpoutPlayer;

public class BookWormSpoutScreenListener extends ScreenListener {

	private BookWorm plugin;
	
	public BookWormSpoutScreenListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Monitor, plugin);
	}

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		if (event.getButton().getPlugin().equals(BookWorm.plugin)) { 
			String btnText = event.getButton().getText();
			
			Bookmark bookmark = plugin.bookmarks.get(event.getPlayer().getName());
			if (bookmark != null) {
				Player player = event.getPlayer();
				if (btnText.equals("-->")) {
					bookmark.nextPage(player);
				} else if (btnText.equals("<--")) {
					bookmark.previousPage(player);
				} else if (btnText.equals("x")) {
					((SpoutPlayer)player).closeActiveWindow();
				}
			}
		}
	}
	
	
	
}
