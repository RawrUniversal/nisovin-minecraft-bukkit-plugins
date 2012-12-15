package com.nisovin.coop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Function;

public class PacketChanger extends PacketAdapter {

	CoopPlugin plugin;
	HealthPlateManager manager;
	
	public PacketChanger(CoopPlugin plugin, HealthPlateManager manager) {
		super(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, 0x14);
		this.plugin = plugin;
		this.manager = manager;
	}
	
	public void onPacketSending(PacketEvent event) {
		if (event.isCancelled()) return;
		if (event.getPlayer().hasPermission("healthplates.noview")) return;
		
		PacketContainer packet = event.getPacket();
		String name = packet.getStrings().getValues().get(0);
		Player player = Bukkit.getPlayerExact(name);
		if (player != null && !player.hasPermission("healthplates.nocolor")) {
			final ChatColor color = manager.getColor(player.getHealth());
			if (color != null) {
				packet.getStrings().modify(0, new Function<String, String>() {		
					public String apply(String name) {					
						name = color + name;
						if (name.length() > 16) {
							name = name.substring(0, 16);
						}
						return name;
					}
				});
			}
		}
	}
	
}
