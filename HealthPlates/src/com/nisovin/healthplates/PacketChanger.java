package com.nisovin.healthplates;

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

	HealthPlatesPlugin plugin;
	
	public PacketChanger(HealthPlatesPlugin plugin) {
		super(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, 0x14);
		this.plugin = plugin;
	}
	
	public void onPacketSending(PacketEvent event) {
		System.out.println("packet " + event.getPlayer().getName() + " test 1");
		if (event.isCancelled()) return;
		System.out.println("packet " + event.getPlayer().getName() + " test 2");
		if (event.getPlayer().hasPermission("healthplates.noview")) return;
		System.out.println("packet " + event.getPlayer().getName() + " test 3");
		
		PacketContainer packet = event.getPacket();
		String name = packet.getStrings().getValues().get(0);
		Player player = Bukkit.getPlayerExact(name);
		if (player != null && !player.hasPermission("healthplates.nocolor")) {
			System.out.println("packet " + event.getPlayer().getName() + " test 4");
			final ChatColor color = plugin.getColor(player.getHealth());
			if (color != null) {
				System.out.println("packet " + event.getPlayer().getName() + " test 5");
				plugin.debug("Adding color to packet 20: sending to " + event.getPlayer().getName() + " about " + name);
				packet.getStrings().modify(0, new Function<String, String>() {		
					public String apply(String name) {
						System.out.println("packet test 6");						
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
