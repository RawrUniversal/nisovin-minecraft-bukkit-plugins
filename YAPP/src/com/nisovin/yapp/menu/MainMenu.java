package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.MainPlugin;

public class MainMenu extends MenuPrompt {
	
	@Override
	public String getPromptText(ConversationContext context) {
		Conversable c = context.getForWhom();
		c.sendRawMessage(Menu.TEXT_COLOR + "Welcome to the YAPP menu! At any time you can type " + Menu.KEYLETTER_COLOR + "<" + Menu.TEXT_COLOR + " to");
		c.sendRawMessage(Menu.TEXT_COLOR + "return to the previous menu, " + Menu.KEYLETTER_COLOR + "!" + Menu.TEXT_COLOR + " to return to this menu, or");
		c.sendRawMessage(Menu.KEYLETTER_COLOR + "q" + Menu.KEYWORD_COLOR + "uit" + Menu.TEXT_COLOR + " to exit the YAPP menu. What would you like to do?");
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) Modify a " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "layer");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) Modify an " + Menu.KEYLETTER_COLOR + "o" + Menu.KEYWORD_COLOR + "ffline player");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) Modify a " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roup");
		c.sendRawMessage(Menu.TEXT_COLOR + "  4) Select the " + Menu.KEYLETTER_COLOR + "w" + Menu.KEYWORD_COLOR + "orld" + Menu.TEXT_COLOR + " to modify");
		c.sendRawMessage(Menu.TEXT_COLOR + "  5) " + Menu.KEYLETTER_COLOR + "S" + Menu.KEYWORD_COLOR + "ave" + Menu.TEXT_COLOR + " all changes and " + Menu.KEYLETTER_COLOR + "r" + Menu.KEYWORD_COLOR + "eload");
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.equals("1") || input.startsWith("p")) {
			return Menu.SELECT_PLAYER;
		} else if (input.equals("2") || input.startsWith("o")) {
			return Menu.SELECT_OFFLINE_PLAYER;
		} else if (input.equals("3") || input.startsWith("g")) {
			return Menu.SELECT_GROUP;
		} else if (input.equals("4") || input.startsWith("w")) {
			return Menu.SELECT_WORLD;
		} else if (input.equals("5") || input.startsWith("s") || input.startsWith("r")) {
			MainPlugin.yapp.reload();
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Permission data has been saved and reloaded");
			return this;
		} else {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "Invalid selection");
			return this;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return END_OF_CONVERSATION;
	}

}
