package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class ModifyOptionsMore extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		String type = getType(context);
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		
		Conversable c = context.getForWhom();
		c.sendRawMessage(Menu.TEXT_COLOR + "What would you like to do with the " + type + " " + Menu.HIGHLIGHT + obj.getName());
		if (world == null) {
			c.sendRawMessage(Menu.TEXT_COLOR + "(with no world selected)?");
		} else {
			c.sendRawMessage(Menu.TEXT_COLOR + "(on world " + Menu.HIGHLIGHT + world + Menu.TEXT_COLOR + ")?");
		}
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) List all " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "ermission " + Menu.TEXT_COLOR + "nodes");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) List all " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roups");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) Show " + Menu.KEYLETTER_COLOR + "m" + Menu.KEYWORD_COLOR + "ore " + Menu.TEXT_COLOR + "options");
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.equals("1") || input.startsWith("p")) {
			return END_OF_CONVERSATION;
		} else if (input.equals("2") || input.startsWith("g")) {
			return END_OF_CONVERSATION;
		} else if (input.equals("3") || input.startsWith("m")) {
			return Menu.MODIFY_OPTIONS;
		}
		return END_OF_CONVERSATION;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
