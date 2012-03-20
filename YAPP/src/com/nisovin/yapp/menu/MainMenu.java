package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.nisovin.yapp.MainPlugin;

public class MainMenu extends StringPrompt {
	
	@Override
	public String getPromptText(ConversationContext context) {
		Conversable c = context.getForWhom();
		c.sendRawMessage(Menu.TEXT_COLOR + "---------------------------------------------------");
		c.sendRawMessage(Menu.TEXT_COLOR + "Welcome to the YAPP menu! At any time you can type " + Menu.KEYLETTER_COLOR + "<" + Menu.TEXT_COLOR + " to");
		c.sendRawMessage(Menu.TEXT_COLOR + "return to the previous menu, or " + Menu.KEYLETTER_COLOR + "q" + Menu.TEXT_COLOR + " to exit the menu.");
		c.sendRawMessage(Menu.TEXT_COLOR + "What would you like to do?");
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) Modify a " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "layer");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) Modify a " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roup");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) Select the " + Menu.KEYLETTER_COLOR + "w" + Menu.KEYWORD_COLOR + "orld" + Menu.TEXT_COLOR + " to modify");
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.equals("1") || input.startsWith("p")) {
			context.setSessionData("type", "player");
			return Menu.SELECT_PLAYER;
		} else if (input.equals("2") || input.startsWith("g")) {
			context.setSessionData("type", "group");
			return Menu.SELECT_GROUP;
		} else if (input.equals("3") || input.startsWith("w")) {
			return Menu.SELECT_WORLD;
		} else {
			return END_OF_CONVERSATION;
		}
	}

}
