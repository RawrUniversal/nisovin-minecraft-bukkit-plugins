package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class InvalidWorld extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "The world " + Menu.HIGHLIGHT + context.getSessionData("noworld") + Menu.TEXT_COLOR + " is not loaded, would you like to");
		return Menu.TEXT_COLOR + "select it anyway (" + Menu.KEYLETTER_COLOR + "y" + Menu.KEYWORD_COLOR + "es" + Menu.TEXT_COLOR + "/" + Menu.KEYLETTER_COLOR + "n" + Menu.KEYWORD_COLOR + "o" + Menu.TEXT_COLOR + ")?";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.startsWith("y")) {
			String world = (String)context.getSessionData("noworld");
			context.setSessionData("world", world);
			context.setSessionData("noworld", null);
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Selected world " + Menu.HIGHLIGHT + world);
			return Menu.MAIN_MENU;
		} else if (input.startsWith("n")) {
			return Menu.SELECT_WORLD;
		} else {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "That is not a valid option!");
			return this;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.SELECT_WORLD;
	}

}
