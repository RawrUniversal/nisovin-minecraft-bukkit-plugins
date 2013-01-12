package com.nisovin.yapp.vault;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import com.nisovin.yapp.YAPP;

public class VaultHandler {

	public static void registerHooks(YAPP plugin) {
		VaultPermissionService permService = new VaultPermissionService();
		VaultChatService chatService = new VaultChatService(permService);
		Bukkit.getServer().getServicesManager().register(net.milkbowl.vault.permission.Permission.class, permService, plugin, ServicePriority.Highest);
		Bukkit.getServer().getServicesManager().register(net.milkbowl.vault.chat.Chat.class, chatService, plugin, ServicePriority.Highest);
	}
	
}
