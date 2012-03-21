package com.nisovin.yapp.menu;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

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
		int count = 0;
		for (ChatColor color : ChatColor.values()) {
			str += color + color.name().replace("_", " ").toLowerCase() + " ";
			if (count++ == 5) {
				c.sendRawMessage("   " + str);
				str = "";
				count = 0;
			}
		}
		if (!str.isEmpty()) {
			c.sendRawMessage("   " + str);
		}
		
		return Menu.TEXT_COLOR + "Please type the color you want:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
