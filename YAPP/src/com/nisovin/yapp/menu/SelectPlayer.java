package com.nisovin.yapp.menu;

import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.nisovin.yapp.YAPP;
import com.nisovin.yapp.User;

public class SelectPlayer extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the player you would like to modify:";
	}
	
	@Override
	public Prompt accept(ConversationContext context, String input) {
		Player player = Bukkit.getPlayer(input);
		if (player == null) {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "That player could not be found");
			return this;
		} else {
			User user = YAPP.getPlayerUser(player.getName());
			setObject(context, user);
			return Menu.MODIFY_OPTIONS;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MAIN_MENU;
	}

}
