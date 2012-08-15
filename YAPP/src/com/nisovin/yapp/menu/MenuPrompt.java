package com.nisovin.yapp.menu;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.User;

public abstract class MenuPrompt extends StringPrompt {

	@Override
	public final Prompt acceptInput(final ConversationContext context, final String input) {
		if (input.startsWith("/")) {
			// ignore slash commands
			return this;
		} else if (input.equals("<")) {
			cleanup(context);
			return getPreviousPrompt(context);
		} else if (input.equals("!")) {
			cleanup(context);
			return Menu.MAIN_MENU;
		} else if (input.equals("?")) {
			return showHelp(context);
		} else if (input.toLowerCase().equals("quit")) {
			return END_OF_CONVERSATION;
		} else {
			if (Thread.currentThread().getId() == MainPlugin.mainThreadId) {
				// we're on main thread, just handle normally
				return accept(context, input);
			} else {
				// not on main thread, so pass to main thread and wait for response
				Future<Prompt> future = Bukkit.getScheduler().callSyncMethod(MainPlugin.yapp, new Callable<Prompt> () {
					@Override
					public Prompt call() throws Exception {
						return accept(context, input);
					}
				});
				while (!future.isDone()){}
				try {
					return future.get();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return END_OF_CONVERSATION;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return END_OF_CONVERSATION;
				}
			}
		}
	}
	
	public abstract Prompt accept(ConversationContext context, String input);
	
	public abstract Prompt getPreviousPrompt(ConversationContext context);
	
	public void cleanup(ConversationContext context) {
	}
	
	public String getHelp(ConversationContext context) {
		return null;
	}
	
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
	
	private Prompt showHelp(ConversationContext context) {
		// get data
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		
		// prepare message
		String msg;
		if (obj != null) {
			msg = Menu.TEXT_COLOR + "You have currently selected the " + getType(context) + " " + Menu.HIGHLIGHT_COLOR + obj.getName() + "\n";
			if (world == null) {
				msg += Menu.TEXT_COLOR + "with no world selected";
			} else {
				msg += Menu.TEXT_COLOR + "with the world " + Menu.HIGHLIGHT_COLOR + world + Menu.TEXT_COLOR + " selected";
			}
		} else if (world != null) {
			msg = Menu.TEXT_COLOR + "You have selected world " + Menu.HIGHLIGHT_COLOR + world;
		} else {
			msg = Menu.TEXT_COLOR + "You have nothing selected";
		}
		String helpMsg = getHelp(context);
		if (helpMsg != null && !helpMsg.isEmpty()) {
			msg += "\n" + helpMsg;
		}
		
		return showMessage(context, msg, this);
	}
	
	protected Prompt showMessage(ConversationContext context, String message, Prompt nextPrompt) {
		context.setSessionData("message", message);
		context.setSessionData("nextprompt", nextPrompt);
		return Menu.MESSAGE;
	}
	
	protected void showCurrentGroupInfo(ConversationContext context) {
		PermissionContainer obj = getObject(context);
		String world = getWorld(context);
		String type = getType(context);
		List<Group> groups = obj.getActualGroupList(world);
		
		Conversable c = context.getForWhom();
		if (groups == null) {
			c.sendRawMessage(Menu.TEXT_COLOR + "The " + type + " " + Menu.HIGHLIGHT_COLOR + obj.getName() + Menu.TEXT_COLOR + " currently has no groups defined" + (world != null ? " on world " + Menu.HIGHLIGHT_COLOR + world + Menu.TEXT_COLOR : ""));
		} else {
			c.sendRawMessage(Menu.TEXT_COLOR + "The " + type + " " + Menu.HIGHLIGHT_COLOR + obj.getName() + Menu.TEXT_COLOR + " currently inherits the following groups" + (world != null ? " on world " + Menu.HIGHLIGHT_COLOR + world + Menu.TEXT_COLOR : "") + ":");
			String s = "";
			for (Group g : groups) {
				if (!s.isEmpty()) {
					s += Menu.TEXT_COLOR + ", ";					
				}
				s += Menu.HIGHLIGHT_COLOR + g.getName();
				if (s.length() > 40) {
					c.sendRawMessage("   " + s);
					s = "";
				}
			}
			if (s.length() > 0) {
				c.sendRawMessage("   " + s);
			}
			c.sendRawMessage(Menu.TEXT_COLOR + "The primary group is " + Menu.HIGHLIGHT_COLOR + groups.get(0).getName());
		}
	}

}
