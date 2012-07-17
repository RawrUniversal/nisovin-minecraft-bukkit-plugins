package com.nisovin.yapp.menu;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.PermissionContainer;

public class SetPrefix extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		String type = getType(context);
		String prefix = obj.getActualPrefix();
		if (prefix == null || prefix.isEmpty()) {
			prefix = "(empty/inherited)";
		} else {
			prefix = ChatColor.WHITE + prefix;
		}
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "The " + type + " " + Menu.HIGHLIGHT_COLOR + obj.getName() + Menu.TEXT_COLOR + "'s prefix is currently: " + prefix);
		return Menu.TEXT_COLOR + "Please type the new prefix (or a dash to clear it):";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.trim();
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		if (input.equals("-")) {
			obj.setPrefix(world, null);
		} else {
			if ((input.startsWith("\"") && input.endsWith("\"")) || (input.startsWith("'") && input.endsWith("'"))) {
				input = input.substring(1, input.length() - 1);
			}
			obj.setPrefix(world, input);
		}		

		String prefix = obj.getActualPrefix();
		if (prefix == null || prefix.isEmpty()) {
			prefix = "(empty/inherited)";
		} else {
			prefix = ChatColor.WHITE + obj.getActualPrefix();
		}
		return showMessage(context, Menu.TEXT_COLOR + "Prefix has been set to: " + prefix, Menu.MODIFY_OPTIONS);
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
