package com.nisovin.yapp.menu;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.MainPlugin;

public class Menu {

	// colors
	public static ChatColor TEXT_COLOR = MainPlugin.TEXT_COLOR;
	public static ChatColor HIGHLIGHT = MainPlugin.HIGHLIGHT_COLOR;
	public static ChatColor KEYWORD_COLOR = ChatColor.DARK_AQUA;
	public static ChatColor KEYLETTER_COLOR = ChatColor.AQUA;
	public static ChatColor ERROR_COLOR = MainPlugin.ERROR_COLOR;
	
	// prompts
	public static final Prompt MAIN_MENU = new MainMenu();
	public static final Prompt SELECT_PLAYER = new SelectPlayer();
	public static final Prompt SELECT_OFFLINE_PLAYER = new SelectOfflinePlayer();
	public static final Prompt SELECT_GROUP = new SelectGroup();
	public static final Prompt NEW_GROUP = new NewGroup();
	public static final Prompt SELECT_WORLD = new SelectWorld();
	public static final Prompt INVALID_WORLD = new InvalidWorld();
	public static final Prompt MODIFY_OPTIONS = new ModifyOptions();

	public static void sendLine(Conversable c) {
		c.sendRawMessage(Menu.TEXT_COLOR + "---------------------------------------------------");
	}
	
	public static class AbandonedListener implements ConversationAbandonedListener {

		@Override
		public void conversationAbandoned(ConversationAbandonedEvent event) {
			Conversable c = event.getContext().getForWhom();
			c.sendRawMessage(Menu.TEXT_COLOR + "Exiting YAPP menu");
			sendLine(c);
		}
		
	}
	
}
