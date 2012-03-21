package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;

public class SelectGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the group you would like to modify:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		Group group = MainPlugin.getGroup(input.trim());
		if (group != null) {
			setObject(context, group);
			return Menu.MODIFY_OPTIONS;
		} else {
			context.setSessionData("newgroupname", input.trim());
			return Menu.NEW_GROUP;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MAIN_MENU;
	}

}
