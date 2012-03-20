package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;

public class NewGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "The group " + Menu.HIGHLIGHT + context.getSessionData("newgroupname") + Menu.TEXT_COLOR + " does not exist, would you like to");
		return Menu.TEXT_COLOR + "create it (" + Menu.KEYLETTER_COLOR + "y" + Menu.KEYWORD_COLOR + "es" + Menu.TEXT_COLOR + "/" + Menu.KEYLETTER_COLOR + "n" + Menu.KEYWORD_COLOR + "o" + Menu.TEXT_COLOR + ")?";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.startsWith("y")) {
			String groupName = (String)context.getSessionData("newgroupname");
			Group group = new Group(groupName);
			MainPlugin.addGroup(group);
			context.setSessionData("what", group);
			return Menu.MODIFY_OPTIONS;
		} else if (input.startsWith("n")) {
			return Menu.SELECT_GROUP;
		} else {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "That is not a valid option!");
			return this;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.SELECT_GROUP;
	}

}
