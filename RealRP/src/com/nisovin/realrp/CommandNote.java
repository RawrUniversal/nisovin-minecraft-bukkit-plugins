package com.nisovin.realrp;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.realrp.character.PlayerCharacter;
import com.nisovin.realrp.character.PlayerCharacter.CharacterNote;

public class CommandNote implements CommandExecutor {

	public RealRP plugin;	

	private HashMap<CommandSender,PlayerCharacter> notesInProgress = new HashMap<CommandSender,PlayerCharacter>();
	
	public CommandNote(RealRP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("realrp.notes")) {
			
		} else if (args.length == 0) {
			// show usage
		} else if (args[0].toLowerCase().startsWith("l")) {
			list(sender, label, args);
		} else if (args[0].toLowerCase().startsWith("r")) {
			read(sender, args);
		} else if (args[0].toLowerCase().startsWith("n")) {
			neww(sender, args);
		} else if (args[0].toLowerCase().startsWith("w")) {
			write(sender, args);
		} else if (args[0].toLowerCase().startsWith("s")) {
			save(sender, args);
		} else if (args[0].toLowerCase().startsWith("d")) {
			discard(sender, args);
		} else {
			return false;
		}
		
		return true;
	}
	
	public void list(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			// usage
			sender.sendMessage("/" + label + " list <player> [page]");
		} else {
			PlayerCharacter pc = PlayerCharacter.match(args[1]);
			if (pc == null) {
				// no char found
				sender.sendMessage("No char found");
			} else {
				int page = 0;
				if (args.length == 3) {
					page = Integer.parseInt(args[2]);
				}
				int start = page*5;
				int end = page*5+5;
				ArrayList<CharacterNote> notes = pc.getNotes();
				if (start > notes.size()) {
					sender.sendMessage("No such page");
					return;
				}
				sender.sendMessage("Notes for " + pc.getChatName() + " (page " + (page+1) + "/" + (notes.size()/5+1) + "):");
				for (int i = start; i < end && i < notes.size(); i++) {
					CharacterNote note = notes.get(i);
					sender.sendMessage("  " + (i+1) + ": " + note.getBrief());
				}
			}
		}
	}
	
	public void read(CommandSender sender, String[] args) {
		if (args.length != 3) {
			// usage
		} else {
			PlayerCharacter pc = PlayerCharacter.match(args[1]);
			if (pc == null) {
				// no char found
				sender.sendMessage("No char found");
			} else {
				int id = Integer.parseInt(args[2]) - 1;
				RealRP.sendMessage(sender, pc.getNotes().get(id).getNote());
			}			
		}
	}
	
	public void neww(CommandSender sender, String[] args) {
		if (notesInProgress.containsKey(sender)) {
			sender.sendMessage("Note already in progress");
		} else if (args.length < 2) {
			// usage
			sender.sendMessage("Invalid");
		} else {
			PlayerCharacter pc = PlayerCharacter.match(args[1]);
			if (pc == null) {
				// no char found
				sender.sendMessage("No char found");
			} else {
				pc.startNote((Player)sender);
				notesInProgress.put(sender, pc);
				sender.sendMessage("Note started for " + pc.getChatName());
			}
		}
	}
	
	public void write(CommandSender sender, String[] args) {
		if (!notesInProgress.containsKey(sender)) {
			sender.sendMessage("No note in progress");
		} else if (args.length < 2) {
			// usage
			sender.sendMessage("Invalid");
		} else {
			PlayerCharacter pc = notesInProgress.get(sender);			
			StringBuilder s = new StringBuilder();
			for (int i = 1; i < args.length; i++) {
				s.append(args[i]);
				s.append(' ');
			}
			pc.addNoteText((Player)sender, s.toString());
			sender.sendMessage("Wrote: " + s.toString());
		}
	}
	
	public void save(CommandSender sender, String[] args) {
		if (!notesInProgress.containsKey(sender)) {
			sender.sendMessage("No note in progress");
		} else {
			PlayerCharacter pc = notesInProgress.get(sender);	
			pc.saveNote((Player)sender);
			sender.sendMessage("Note saved");
			notesInProgress.remove(sender);
		}
	}
	
	public void discard(CommandSender sender, String[] args) {
		if (!notesInProgress.containsKey(sender)) {
			sender.sendMessage("No note in progress");
		} else {
			PlayerCharacter pc = notesInProgress.get(sender);	
			pc.discardNote((Player)sender);
			sender.sendMessage("Note discarded");
			notesInProgress.remove(sender);
		}		
	}

}
