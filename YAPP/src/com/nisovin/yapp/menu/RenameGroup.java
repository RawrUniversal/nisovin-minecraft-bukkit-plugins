package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class RenameGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		return Menu.TEXT_COLOR + "Please enter the new name for the group " + Menu.HIGHLIGHT_COLOR + obj.getName() + ":";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		if (MainPlugin.getGroup(input) != null) {
			return showMessage(context, Menu.ERROR_COLOR + "That group already exists", this);
		} else {
			Group group = (Group)getObject(context);
			String oldName = group.getName();
			MainPlugin.yapp.renameOrDeleteGroup(group, input);
			return showMessage(context, Menu.TEXT_COLOR + "The group " + Menu.HIGHLIGHT_COLOR + oldName + Menu.TEXT_COLOR + " has been renamed to " + Menu.HIGHLIGHT_COLOR + input, Menu.MODIFY_OPTIONS_MORE);
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS_MORE;
	}

}
