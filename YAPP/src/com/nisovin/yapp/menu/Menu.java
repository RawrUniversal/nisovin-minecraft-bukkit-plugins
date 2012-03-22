package com.nisovin.yapp.menu;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.MainPlugin;

public class Menu {

	// colors
	public static final ChatColor TEXT_COLOR = MainPlugin.TEXT_COLOR;
	public static final ChatColor HIGHLIGHT_COLOR = MainPlugin.HIGHLIGHT_COLOR;
	public static final ChatColor KEYWORD_COLOR = ChatColor.DARK_AQUA;
	public static final ChatColor KEYLETTER_COLOR = ChatColor.AQUA;
	public static final ChatColor ERROR_COLOR = MainPlugin.ERROR_COLOR;
	
	// prompts
	public static final Prompt MESSAGE = new MessagePrompt();
	public static final Prompt MAIN_MENU = new MainMenu();
	public static final Prompt SELECT_PLAYER = new SelectPlayer();
	public static final Prompt SELECT_OFFLINE_PLAYER = new SelectOfflinePlayer();
	public static final Prompt SELECT_GROUP = new SelectGroup();
	public static final Prompt NEW_GROUP = new NewGroup();
	public static final Prompt SELECT_WORLD = new SelectWorld();
	public static final Prompt INVALID_WORLD = new InvalidWorld();
	
	public static final Prompt MODIFY_OPTIONS = new ModifyOptions();
	public static final Prompt MODIFY_OPTIONS_MORE = new ModifyOptionsMore();
	
	public static final Prompt ADD_PERMISSION = new AddPermission();
	public static final Prompt REMOVE_PERMISSION = new RemovePermission();
	public static final Prompt NEGATE_PERMISSION = new NegatePermission();
	public static final Prompt ADD_GROUP = new AddGroup();
	public static final Prompt ADD_NEW_GROUP = new AddNewGroup();
	public static final Prompt REMOVE_GROUP = new RemoveGroup();
	public static final Prompt SET_COLOR = new SetColor();
	public static final Prompt SET_PREFIX = new SetPrefix();
	
	public static final Prompt HAS_PERMISSION = new HasPermission();
	public static final Prompt HAS_GROUP = new HasGroup();
	
	// conversation management
	private static Map<Conversable,Conversation> conversations = new HashMap<Conversable, Conversation>();
	private static ConversationFactory menuFactory;
	
	public static void initializeFactory(MainPlugin plugin, boolean modal) {
		menuFactory = new ConversationFactory(plugin)
			.withFirstPrompt(Menu.MAIN_MENU)
			.withModality(modal)
			.withEscapeSequence("q")
			.withTimeout(60)
			.withLocalEcho(true)
			.addConversationAbandonedListener(new AbandonedListener());
	}
	
	public static void openMenu(Conversable conversable) {
		sendLine(conversable);
		Conversation conversation = menuFactory.buildConversation(conversable);
		conversation.begin();
		conversations.put(conversable, conversation);
	}
	
	public static void closeAllMenus() {
		for (Conversation conversation : conversations.values()) {
			conversation.abandon();
		}
		conversations.clear();
	}

	public static void sendLine(Conversable c) {
		c.sendRawMessage(TEXT_COLOR + "---------------------------------------------------");
	}
	
	public static class AbandonedListener implements ConversationAbandonedListener {

		@Override
		public void conversationAbandoned(ConversationAbandonedEvent event) {
			Conversable c = event.getContext().getForWhom();
			c.sendRawMessage(Menu.TEXT_COLOR + "Exiting YAPP menu");
			sendLine(c);
			conversations.remove(c);
		}
		
	}
	
}
