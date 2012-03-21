package com.nisovin.yapp.menu;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class SelectWorld extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Please type the world you would like to work with");
		return Menu.TEXT_COLOR + "(enter a space to clear your world selection):";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.trim();
		if (input.isEmpty()) {
			setWorld(context, null);
			return Menu.MAIN_MENU;
		}
		World world = Bukkit.getWorld(input);
		if (world == null) {
			context.setSessionData("noworld", input);
			return Menu.INVALID_WORLD;
		} else {
			setWorld(context, world.getName());
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Selected world " + Menu.HIGHLIGHT_COLOR + world.getName());
			return Menu.MAIN_MENU;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MAIN_MENU;
	}

}
