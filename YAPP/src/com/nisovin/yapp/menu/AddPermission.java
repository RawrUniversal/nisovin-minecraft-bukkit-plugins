package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.PermissionContainer;

public class AddPermission extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the permission node you want to add:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		input = input.trim();
		obj.addPermission(world, input);
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Added permission " + Menu.HIGHLIGHT_COLOR + input + Menu.TEXT_COLOR + " to " + getType(context) + Menu.HIGHLIGHT_COLOR + " " + obj.getName());
		if (world != null) {
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT_COLOR + world);
		}
		return Menu.MODIFY_OPTIONS;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
