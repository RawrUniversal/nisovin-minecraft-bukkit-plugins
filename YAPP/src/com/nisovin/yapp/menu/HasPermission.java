package com.nisovin.yapp.menu;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.User;

public class HasPermission extends MenuPrompt {

	@Override
	public String getPromptText(ConversationContext context) {
		return Menu.TEXT_COLOR + "Please type the permission node you want to check:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		input = input.trim();
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		String type = getType(context);
		
		// get player if possible
		Player player = null;
		if (obj instanceof User) {
			player = ((User)obj).getPlayer();
		}
		
		boolean has = false;
		if (player != null) {
			has = player.hasPermission(input);
		} else {
			has = obj.has(world, input);
		}
		if (has) {
			context.getForWhom().sendRawMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " does have " + MainPlugin.TEXT_COLOR + "the permission " + MainPlugin.HIGHLIGHT_COLOR + input);
		} else {
			context.getForWhom().sendRawMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.RED + " does not have " + MainPlugin.TEXT_COLOR + "the permission " + MainPlugin.HIGHLIGHT_COLOR + input);			
		}
		return Menu.MODIFY_OPTIONS;
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}

}
