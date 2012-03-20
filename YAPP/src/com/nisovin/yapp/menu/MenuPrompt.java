package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public abstract class MenuPrompt extends StringPrompt {

	@Override
	public final Prompt acceptInput(ConversationContext context, String input) {
		if (input.equals("<")) {
			return getPreviousPrompt(context);
		} else if (input.equals("!")) {
			return Menu.MAIN_MENU;
		} else if (input.toLowerCase().equals("quit")) {
			return END_OF_CONVERSATION;
		} else {
			return accept(context, input);
		}
	}
	
	public abstract Prompt accept(ConversationContext context, String input);
	
	public abstract Prompt getPreviousPrompt(ConversationContext context);

}
