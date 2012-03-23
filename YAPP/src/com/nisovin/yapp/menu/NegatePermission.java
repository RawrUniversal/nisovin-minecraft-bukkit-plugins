package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.PermissionContainer;

public class NegatePermission extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the permission node you want to negate:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		input = input.trim();
		obj.addPermission(world, "-" + input);
		String msg = Menu.TEXT_COLOR + "Negated permission " + Menu.HIGHLIGHT_COLOR + input + Menu.TEXT_COLOR + " for " + getType(context) + Menu.HIGHLIGHT_COLOR + " " + obj.getName();
		if (world != null) {
			msg += "\n" + Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT_COLOR + world;
		}
		return showMessage(context, msg, Menu.MODIFY_OPTIONS);
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
