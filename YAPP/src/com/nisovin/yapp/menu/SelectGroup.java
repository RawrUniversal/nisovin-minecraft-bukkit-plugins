package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;

public class SelectGroup extends StringPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the group you would like to modify:";
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		if (input.equals("<")) {
			return Menu.MAIN_MENU;
		}
		Group group = MainPlugin.getGroup(input);
		if (group != null) {
			context.setSessionData("what", group);
			return Menu.MODIFY_OPTIONS;
		} else {
			context.setSessionData("newgroupname", input);
			return Menu.NEW_GROUP;
		}
	}

}
