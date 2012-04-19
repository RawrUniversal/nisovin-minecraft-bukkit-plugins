package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class SetPrimaryGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		showCurrentGroupInfo(context);
		return Menu.TEXT_COLOR + "Please type the new primary group: ";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		Group group = MainPlugin.getGroup(input);
		if (group == null) {
			return showMessage(context, Menu.ERROR_COLOR + "That group does not exist", this);
		} else {
			PermissionContainer obj = getObject(context);
			String world = getWorld(context);
			String type = getType(context);
			boolean ok = obj.setPrimaryGroup(world, group);
			if (!ok) {
				return showMessage(context, Menu.ERROR_COLOR + "Cannot set the primary group to that group", this);
			} else {
				return showMessage(context, Menu.TEXT_COLOR + "The primary group for " + type + " " + Menu.HIGHLIGHT_COLOR + obj.getName() + Menu.TEXT_COLOR + " has been set to " + Menu.HIGHLIGHT_COLOR + group.getName(), Menu.MODIFY_OPTIONS);
			}
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
