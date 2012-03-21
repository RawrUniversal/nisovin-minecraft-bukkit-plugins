package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.User;

public class SelectOfflinePlayer extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the complete name of the player you would like to modify:";
	}
	
	@Override
	public Prompt accept(ConversationContext context, String input) {
		User user = MainPlugin.getPlayerUser(input.trim());
		setObject(context, user);
		return Menu.MODIFY_OPTIONS;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MAIN_MENU;
	}

}
