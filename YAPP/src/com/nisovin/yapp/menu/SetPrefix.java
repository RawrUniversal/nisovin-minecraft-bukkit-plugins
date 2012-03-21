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
		return Menu.TEXT_COLOR + "Please type what you want to set it to (type a space to clear it):";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		PermissionContainer obj = getObject(context);
		if (input.trim().isEmpty()) {
			obj.setPrefix(null);
		} else {
			obj.setPrefix(input);
		}

		String prefix = obj.getActualPrefix();
		if (prefix == null || prefix.isEmpty()) {
			prefix = "(empty/inherited)";
		} else {
			prefix = ChatColor.WHITE + prefix;
		}
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Prefix has been set to: " + prefix);
		return Menu.MODIFY_OPTIONS;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
