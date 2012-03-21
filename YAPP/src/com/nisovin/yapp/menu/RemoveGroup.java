package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class RemoveGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the permission node you want to remove:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		Group group = MainPlugin.getGroup(input.trim());
		if (group == null) {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "That group does not exist");
			return this;
		}
		
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		boolean ok = obj.removeGroup(world, group);
		if (ok) {
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Removed group " + Menu.HIGHLIGHT_COLOR + input + Menu.TEXT_COLOR + " from " + getType(context) + Menu.HIGHLIGHT_COLOR + " " + obj.getName());
			if (world != null) {
				context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT_COLOR + world);
			}
		} else {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "Unable to remove group");
		}
		return Menu.MODIFY_OPTIONS;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
