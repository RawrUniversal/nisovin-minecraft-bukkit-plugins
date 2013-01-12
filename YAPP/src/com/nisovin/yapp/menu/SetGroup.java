package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.YAPP;
import com.nisovin.yapp.PermissionContainer;

public class SetGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		showCurrentGroupInfo(context);
		showGroupList(context);
		return Menu.TEXT_COLOR + "Please type the group you want to set:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.trim();
		Group group = YAPP.getGroup(input);
		if (group == null) {
			context.setSessionData("setnewgroupname", input);
			return Menu.ADD_NEW_GROUP;
		} else {
			PermissionContainer obj = getObject(context);
			String world = getWorld(context);
			boolean set = obj.setGroup(world, group);
			String msg = "";
			if (set) {
				msg = Menu.TEXT_COLOR + "Set group " + Menu.HIGHLIGHT_COLOR + group.getName() + Menu.TEXT_COLOR + " for " + getType(context) + Menu.HIGHLIGHT_COLOR + " " + obj.getName();
				if (world != null) {
					msg += "\n" + Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT_COLOR + world;
				}
			} else {
				msg = Menu.ERROR_COLOR + "Failed to set group!";
			}
			return showMessage(context, msg, Menu.MODIFY_OPTIONS);
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}
	
}
