package com.nisovin.yapp.menu;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;

public class SetColor extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		String type = getType(context);
		
		// get current color
		ChatColor currColor = obj.getActualColor();
		String colorVal;
		if (currColor == null) {
			colorVal = "(empty/inherited)";
		} else {
			colorVal = currColor + currColor.name().replace("_", " ").toLowerCase();
		}
		
		Conversable c = context.getForWhom();		
		c.sendRawMessage(Menu.TEXT_COLOR + "The " + type + " " + Menu.HIGHLIGHT_COLOR + obj.getName() + Menu.TEXT_COLOR + "'s color is: " + colorVal);
		c.sendRawMessage(Menu.TEXT_COLOR + "The available color options are:");
		
		// get all colors
		String str = "";
		for (ChatColor color : ChatColor.values()) {
			str += color + color.name().replace("_", " ").toLowerCase() + " ";
			if (str.length() > 50) {
				c.sendRawMessage("   " + str);
				str = "";
			}
		}
		if (!str.isEmpty()) {
			c.sendRawMessage("   " + str);
		}
		c.sendRawMessage(Menu.TEXT_COLOR + "Or type " + Menu.HIGHLIGHT_COLOR + "none" + Menu.TEXT_COLOR + " to remove the color");
		
		return Menu.TEXT_COLOR + "Please type the color you want:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.trim();
		ChatColor color;
		if (input.equals("-") || input.equalsIgnoreCase("none")) {
			color = null;
		} else if (input.length() == 1) {
			color = ChatColor.getByChar(input);
			if (color == null) {
				context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "Invalid color selection");
				return this;
			}
		} else {
			try {
				color = ChatColor.valueOf(input.replace(" ", "_").toUpperCase());
			} catch (IllegalArgumentException e) {
				context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "Invalid color selection");
				return this;
			}
		}
		PermissionContainer obj = getObject(context);
		obj.setColor(color);
		String val;
		if (color != null) {
			val = color + color.name().replace("_", " ").toLowerCase();
		} else {
			val = "(empty/inherited)";
		}
		return showMessage(context, MainPlugin.TEXT_COLOR + "The " + getType(context) + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + MainPlugin.TEXT_COLOR + "'s color has been set to: " + val, Menu.MODIFY_OPTIONS);
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
