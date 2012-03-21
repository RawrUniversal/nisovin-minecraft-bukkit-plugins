package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class AddGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the group you want to add:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.trim();
		Group group = MainPlugin.getGroup(input);
		if (group == null) {
			context.setSessionData("addnewgroupname", input);
			return Menu.ADD_NEW_GROUP;
		} else {
			PermissionContainer obj = getObject(context);
			String world = getWorld(context);
			obj.addGroup(world, group);
			context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Added group " + Menu.HIGHLIGHT + group.getName() + Menu.TEXT_COLOR + " for " + getType(context) + Menu.HIGHLIGHT + " " + obj.getName());
			if (world != null) {
				context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT + world);
			}
			return Menu.MODIFY_OPTIONS;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
