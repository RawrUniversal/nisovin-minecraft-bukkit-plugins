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
		String type = getType(context);
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		
		Conversable c = context.getForWhom();
		c.sendRawMessage(Menu.TEXT_COLOR + "What would you like to do with the " + type + " " + Menu.HIGHLIGHT_COLOR + obj.getName());
		if (world == null) {
			c.sendRawMessage(Menu.TEXT_COLOR + "(with no world selected)?");
		} else {
			c.sendRawMessage(Menu.TEXT_COLOR + "(on world " + Menu.HIGHLIGHT_COLOR + world + Menu.TEXT_COLOR + ")?");
		}
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) Add a " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "ermission " + Menu.TEXT_COLOR + "node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) " + Menu.KEYLETTER_COLOR + "R" + Menu.KEYWORD_COLOR + "emove " + Menu.TEXT_COLOR + "a permission node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) " + Menu.KEYLETTER_COLOR + "N" + Menu.KEYWORD_COLOR + "egate " + Menu.TEXT_COLOR + "a permission node");
		c.sendRawMessage(Menu.TEXT_COLOR + "  4) Add an inherited " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roup");
		c.sendRawMessage(Menu.TEXT_COLOR + "  5) Set the " + Menu.KEYWORD_COLOR + "primar" + Menu.KEYLETTER_COLOR + "y" + Menu.TEXT_COLOR + " group");
		c.sendRawMessage(Menu.TEXT_COLOR + "  6) Remove an inherited group (" + Menu.KEYLETTER_COLOR + "u" + Menu.KEYWORD_COLOR + "ngroup" + Menu.TEXT_COLOR + ")");
		c.sendRawMessage(Menu.TEXT_COLOR + "  7) Set the " + Menu.KEYWORD_COLOR + "chat prefi" + Menu.KEYLETTER_COLOR + "x");
		c.sendRawMessage(Menu.TEXT_COLOR + "  8) Set the name " + Menu.KEYLETTER_COLOR + "c" + Menu.KEYWORD_COLOR + "olor");
		c.sendRawMessage(Menu.TEXT_COLOR + "  9) Show " + Menu.KEYLETTER_COLOR + "m" + Menu.KEYWORD_COLOR + "ore " + Menu.TEXT_COLOR + "options");
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.toLowerCase();
		if (input.equals("1") || input.equals("p") || input.startsWith("per")) {
			return Menu.ADD_PERMISSION;
		} else if (input.equals("2") || input.startsWith("r")) {
			return Menu.REMOVE_PERMISSION;
		} else if (input.equals("3") || input.startsWith("n")) {
			return Menu.NEGATE_PERMISSION;
		} else if (input.equals("4") || input.startsWith("g")) {
			return Menu.ADD_GROUP;
		} else if (input.equals("5") || input.startsWith("y") || input.startsWith("pri")) {
			return Menu.SET_PRIMARY_GROUP;
		} else if (input.equals("6") || input.startsWith("u")) {
			return Menu.REMOVE_GROUP;
		} else if (input.equals("7") || input.startsWith("ch") || input.startsWith("pre") || input.equals("x")) {
			return Menu.SET_PREFIX;
		} else if (input.equals("8") || input.startsWith("c")) {
			return Menu.SET_COLOR;
		} else if (input.equals("9") || input.startsWith("m")) {
			return Menu.MODIFY_OPTIONS_MORE;
		} else {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "Invalid selection");
			return this;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		PermissionContainer obj = getObject(context);
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
