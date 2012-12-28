package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class AddNewGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "The group " + Menu.HIGHLIGHT_COLOR + context.getSessionData("newgroupname") + Menu.TEXT_COLOR + " does not exist, would you like to");
		return Menu.TEXT_COLOR + "create it (" + Menu.KEYLETTER_COLOR + "y" + Menu.KEYWORD_COLOR + "es" + Menu.TEXT_COLOR + "/" + Menu.KEYLETTER_COLOR + "n" + Menu.KEYWORD_COLOR + "o" + Menu.TEXT_COLOR + ")?";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.startsWith("y")) {
			PermissionContainer obj = getObject(context);
			String world = getWorld(context);
			
			String groupName = (String)context.getSessionData("setnewgroupname");
			if (groupName != null) {
				context.setSessionData("setnewgroupname", null);

				Group group = MainPlugin.newGroup(groupName);
				obj.setGroup(world, group);
				String msg = Menu.TEXT_COLOR + "Set group " + Menu.HIGHLIGHT_COLOR + group.getName() + Menu.TEXT_COLOR + " for " + getType(context) + Menu.HIGHLIGHT_COLOR + " " + obj.getName();
				if (world != null) {
					msg += "\n" + Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT_COLOR + world;
				}
				return showMessage(context, msg, Menu.MODIFY_OPTIONS);
				
			} else {
				groupName = (String)context.getSessionData("addnewgroupname");
				if (groupName != null) {
					context.setSessionData("addnewgroupname", null);
					
					Group group = MainPlugin.newGroup(groupName);
					obj.addGroup(world, group);
					String msg = Menu.TEXT_COLOR + "Added group " + Menu.HIGHLIGHT_COLOR + group.getName() + Menu.TEXT_COLOR + " for " + getType(context) + Menu.HIGHLIGHT_COLOR + " " + obj.getName();
					if (world != null) {
						msg += "\n" + Menu.TEXT_COLOR + "for world " + Menu.HIGHLIGHT_COLOR + world;
					}
					return showMessage(context, msg, Menu.MODIFY_OPTIONS);
					
				} else {
					return showMessage(context, Menu.ERROR_COLOR + "Error while creating new group.", Menu.MAIN_MENU);
				}
			}
		} else if (input.startsWith("n")) {
			context.setSessionData("setnewgroupname", null);
			context.setSessionData("addnewgroupname", null);
			return Menu.MODIFY_OPTIONS;
		} else {
			return showMessage(context, Menu.ERROR_COLOR + "That is not a valid option!", this);
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.ADD_GROUP;
	}

}
