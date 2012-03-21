package com.nisovin.yapp.menu;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.User;

public abstract class MenuPrompt extends StringPrompt {

	@Override
	public final Prompt acceptInput(ConversationContext context, String input) {
		if (input.equals("<")) {
			return getPreviousPrompt(context);
		} else if (input.equals("!")) {
			return Menu.MAIN_MENU;
		} else if (input.toLowerCase().equals("quit")) {
			return END_OF_CONVERSATION;
		} else {
			return accept(context, input);
		}
	}
	
	public abstract Prompt accept(ConversationContext context, String input);
	
	public abstract Prompt getPreviousPrompt(ConversationContext context);
	
	protected PermissionContainer getObject(ConversationContext context) {
		Object o = context.getSessionData("obj");
		if (o == null) {
			return null;
		} else {
			return (PermissionContainer)o;
		}
	}
	
	protected void setObject(ConversationContext context, PermissionContainer obj) {
		context.setSessionData("obj", obj);
	}
	
	protected String getType(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		if (obj == null) {
			return "";
		} else if (obj instanceof User) {
			return "player";
		} else if (obj instanceof Group) {
			return "group";
		} else {
			return "";
		}
	}
	
	protected String getWorld(ConversationContext context) {
		Object o = context.getSessionData("world");
		if (o == null) {
			return null;
		} else {
			if (((String)o).isEmpty()) {
				return null;
			} else {
				return (String)o;
			}
		}
	}
	
	protected void setWorld(ConversationContext context, String world) {
		context.setSessionData("world", world);
	}

}
