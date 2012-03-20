package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.User;

public class ModifyOptions extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		String type = (String)context.getSessionData("type");
		PermissionContainer obj = (PermissionContainer)context.getSessionData("obj");
		String world = (String)context.getSessionData("world");
		if (world != null && world.isEmpty()) {
			world = null;
		}
		
		Conversable c = context.getForWhom();
		c.sendRawMessage(Menu.TEXT_COLOR + "What would you like to do with the " + type + " " + Menu.HIGHLIGHT + obj.getName() + Menu.TEXT_COLOR + 
				(world != null ? "(on world " + Menu.HIGHLIGHT + world + Menu.TEXT_COLOR + ")" : "") + "?");
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) Add a " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "ermission " + Menu.TEXT_COLOR + "node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) " + Menu.KEYLETTER_COLOR + "R" + Menu.KEYWORD_COLOR + "emove " + Menu.TEXT_COLOR + "a permission node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) " + Menu.KEYLETTER_COLOR + "N" + Menu.KEYWORD_COLOR + "egate " + Menu.TEXT_COLOR + "a permission node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  4) Add an inherited " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roup");
		c.sendRawMessage(Menu.TEXT_COLOR + "  5) Remove an inherited group (" + Menu.KEYLETTER_COLOR + "u" + Menu.KEYWORD_COLOR + "ngroup" + Menu.TEXT_COLOR + ")");
		c.sendRawMessage(Menu.TEXT_COLOR + "  6) " + Menu.KEYLETTER_COLOR + "C" + Menu.KEYWORD_COLOR + "heck" + Menu.TEXT_COLOR + " if it has a permission");
		c.sendRawMessage(Menu.TEXT_COLOR + "  7) Check if it " + Menu.KEYLETTER_COLOR + "i" + Menu.KEYWORD_COLOR + "nherits " + Menu.TEXT_COLOR + "a group");
		c.sendRawMessage(Menu.TEXT_COLOR + "  8) " + Menu.KEYLETTER_COLOR + "D" + Menu.KEYWORD_COLOR + "elete" + Menu.TEXT_COLOR + " this " + type);
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		//PermissionContainer obj = (PermissionContainer)context.getSessionData("obj");
		return END_OF_CONVERSATION;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		PermissionContainer obj = (PermissionContainer)context.getSessionData("obj");
		if (obj == null) {
			return Menu.MAIN_MENU;
		} else if (obj instanceof User) {
			return Menu.SELECT_PLAYER;
		} else if (obj instanceof Group) {
			return Menu.SELECT_GROUP;
		} else {
			return Menu.MAIN_MENU;
		}
	}

}
