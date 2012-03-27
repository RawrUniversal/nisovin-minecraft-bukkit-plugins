package com.nisovin.bookworm;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class BookMapRenderer extends MapRenderer {

	
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		BookWorm.plugin.bookmarks.get(player.getName());
	}

}
