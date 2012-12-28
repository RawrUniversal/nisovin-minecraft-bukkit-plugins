package com.nisovin.yapp.menu;

import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.TrackedNodeList;
import com.nisovin.yapp.User;

public class ModifyOptionsMore extends MenuPrompt {

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
		c.sendRawMessage(Menu.TEXT_COLOR + "  1) Check if it " + Menu.KEYLETTER_COLOR + "h" + Menu.KEYWORD_COLOR + "as " + Menu.TEXT_COLOR + "a permission");
		c.sendRawMessage(Menu.TEXT_COLOR + "  2) Check if it " + Menu.KEYLETTER_COLOR + "i" + Menu.KEYWORD_COLOR + "nherits " + Menu.TEXT_COLOR + "a group");
		c.sendRawMessage(Menu.TEXT_COLOR + "  3) List all " + Menu.KEYLETTER_COLOR + "p" + Menu.KEYWORD_COLOR + "ermission " + Menu.TEXT_COLOR + "nodes");
		c.sendRawMessage(Menu.TEXT_COLOR + "  4) List all " + Menu.KEYLETTER_COLOR + "g" + Menu.KEYWORD_COLOR + "roups");
		//c.sendRawMessage(Menu.TEXT_COLOR + "  5) Set a short " + Menu.KEYLETTER_COLOR + "d" + Menu.KEYWORD_COLOR + "escription");
		if (obj instanceof Group) {
			c.sendRawMessage(Menu.TEXT_COLOR + "  6) " + Menu.KEYLETTER_COLOR + "R" + Menu.KEYWORD_COLOR + "ename" + Menu.TEXT_COLOR + " this group");
			c.sendRawMessage(Menu.TEXT_COLOR + "  7) " + Menu.KEYWORD_COLOR + "De" + Menu.KEYLETTER_COLOR + "l" + Menu.KEYWORD_COLOR + "ete" + Menu.TEXT_COLOR + " this group");
		} else if (obj instanceof User) {
			c.sendRawMessage(Menu.TEXT_COLOR + "  6) " + Menu.KEYLETTER_COLOR + "R" + Menu.KEYWORD_COLOR + "efresh" + Menu.TEXT_COLOR + " this player's permissions");
		}
		c.sendRawMessage(Menu.TEXT_COLOR + "  0) Show " + Menu.KEYLETTER_COLOR + "m" + Menu.KEYWORD_COLOR + "ore " + Menu.TEXT_COLOR + "options");
		return MainPlugin.TEXT_COLOR + "Please type your selection:";
	}

	@Override
	public Prompt accept(ConversationContext context, String input) {
		PermissionContainer obj = getObject(context);
		input = input.toLowerCase();
		if (input.equals("1") || input.startsWith("h")) {
			return Menu.HAS_PERMISSION;
		} else if (input.equals("2") || input.startsWith("i")) {
			return Menu.HAS_GROUP;
		} else if (input.equals("3") || input.startsWith("p")) {
			listPermissions(context);
			return showMessage(context, null, this);
		} else if (input.equals("4") || input.startsWith("g")) {
			showCurrentGroupInfo(context);
			return showMessage(context, null, this);
		} else if (input.equals("5") || input.startsWith("d")) {
			return showMessage(context, Menu.ERROR_COLOR + "Not yet implemented.", this);
		} else if ((input.equals("6") || input.startsWith("r")) && obj instanceof Group) {
			return Menu.RENAME_GROUP;
		} else if ((input.equals("7") || input.equals("l") || input.startsWith("del")) && obj instanceof Group) {
			return Menu.DELETE_GROUP;
		} else if ((input.equals("6") || input.startsWith("r")) && obj instanceof User) {
			User user = (User)obj;
			if (user.isOnline()) {
				MainPlugin.yapp.loadPlayerPermissions(user.getPlayer());
				return showMessage(context, Menu.TEXT_COLOR + "Refreshed permissions for player " + Menu.HIGHLIGHT_COLOR + user.getName(), this);
			} else {
				return showMessage(context, Menu.ERROR_COLOR + "That player is not online", this);
			}
		} else if (input.equals("0") || input.startsWith("m")) {
			return Menu.MODIFY_OPTIONS;
		} else {
			context.getForWhom().sendRawMessage(Menu.ERROR_COLOR + "Invalid selection");
			return this;
		}
	}

	@Override
	public Prompt getPreviousPrompt(ConversationContext context) {
		return Menu.MODIFY_OPTIONS;
	}
	
	private void listPermissions(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		if (world == null || world.isEmpty()) {
			if (obj instanceof User) {
				User u = (User)obj;
				if (u.isOnline()) {
					world = u.getPlayer().getWorld().getName();
				}
			}
		}
		TrackedNodeList list = new TrackedNodeList(obj);
		obj.fillTrackedNodeList(list, world);
		for (TrackedNodeList.TrackedNode node : list.getTrackedNodes()) {
			context.getForWhom().sendRawMessage(node.toString());
		}
	}

}
