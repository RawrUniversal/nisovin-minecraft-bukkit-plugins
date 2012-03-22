package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class MessagePrompt extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		Conversable c = context.getForWhom();
		String msg = (String)context.getSessionData("message");
		if (msg != null) {
			context.setSessionData("message", null);
			String[] msgs = msg.split("\\n");
			for (String m : msgs) {
				c.sendRawMessage(m);
			}
		}
		return Menu.TEXT_COLOR + "  (type anything to continue)";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		Prompt next = (Prompt)context.getSessionData("nextprompt");
		if (next != null) {
			context.setSessionData("nextprompt", null);
			return next;
		} else {
			return Menu.MAIN_MENU;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MAIN_MENU;
	}

}
