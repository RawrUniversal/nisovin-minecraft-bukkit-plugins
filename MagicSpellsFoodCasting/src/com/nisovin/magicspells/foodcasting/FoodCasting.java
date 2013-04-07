package com.nisovin.magicspells.foodcasting;

import net.minecraft.server.v1_5_R2.Packet14BlockDig;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class FoodCasting {


	
	class PacketListener extends PacketAdapter {
		
		public PacketListener(Plugin plugin) {
			super(plugin, ConnectionSide.BOTH, ListenerPriority.NORMAL, 14);
		}
		
		@Override
		public void onPacketReceiving(PacketEvent event) {
			Packet14BlockDig packet = (Packet14BlockDig)event.getPacket().getHandle();
			System.out.println("dig packet " + packet.e + " x" + packet.a + " y" + packet.b + " z" + packet.c + " f" + packet.face);
			//Packet15Place packet = (Packet15Place)event.getPacket().getHandle();
			//System.out.println("place packet x:" + packet.d() + " y:" + packet.f() + " z:" + packet.g() + " d:" + packet.getFace() + " cx:" + packet.j() + " cy:" + packet.k() + " cz:" + packet.l());
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			//Packet15Place packet = (Packet15Place)event.getPacket().getHandle();
			//System.out.println("place packet send ");
		}
		
	}
	
}
