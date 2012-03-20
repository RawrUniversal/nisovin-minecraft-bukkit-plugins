package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.User;

public class ModifyOptions extends StringPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		String type = (String)context.getSessionData("type");
		PermissionContainer obj = (PermissionContainer)context.getSessionData("what");
		
		Conversable c = context.getForWhom();
		c.sendRawMessage(Menu.TEXT_COLOR + "What would you like to do with the " + type + " " + Menu.HIGHLIGHT + obj.getName() + Menu.TEXT_COLOR + "?");
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) Add a " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "ermission " + Menu.TEXT_COLOR + "node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) " + Menu.KEYLETTER_COLOR + "R" + Menu.KEYWORD_COLOR + "emove " + Menu.TEXT_COLOR + "a permission node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) " + Menu.KEYLETTER_COLOR + "N" + Menu.KEYWORD_COLOR + "egate " + Menu.TEXT_COLOR + "a permission node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  4) Add an inherited " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roup");
		c.sendRawMessage(Menu.TEXT_COLOR + "  5) Remove an inherited group (" + Menu.KEYLETTER_COLOR + "u" + Menu.KEYWORD_COLOR + "ngroup" + Menu.TEXT_COLOR + ")");
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt acceptInput(ConversationContext context, String input) {
		PermissionContainer obj = (PermissionContainer)context.getSessionData("what");
		if (input.equals("<")) {
			if (obj instanceof User) {
				return Menu.SELECT_PLAYER;
			} else if (obj instanceof Group) {
				return Menu.SELECT_GROUP;
			} else {
				return END_OF_CONVERSATION;
			}
		}
		return END_OF_CONVERSATION;
	}

}
