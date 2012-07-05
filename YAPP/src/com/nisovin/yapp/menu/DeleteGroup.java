package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class DeleteGroup extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		context.getForWhom().sendRawMessage(Menu.TEXT_COLOR + "Are you sure you want to delete the group " + Menu.HIGHLIGHT_COLOR + obj.getName() + "?");
		return Menu.TEXT_COLOR + "This cannot be undone! (" + Menu.HIGHLIGHT_COLOR + "y" + Menu.TEXT_COLOR + "es/" + Menu.HIGHLIGHT_COLOR + "n" + Menu.TEXT_COLOR + "o)";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
			Group group = (Group)getObject(context);
			String name = group.getName();
			MainPlugin.yapp.renameOrDeleteGroup(group, null);
			setObject(context, null);
			return showMessage(context, Menu.TEXT_COLOR + "The group " + Menu.HIGHLIGHT_COLOR + name + Menu.TEXT_COLOR + " has been deleted", Menu.MAIN_MENU);
		} else {
			return Menu.MODIFY_OPTIONS_MORE;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS_MORE;
	}

}
