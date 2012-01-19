package com.nisovin.worldloader;

import org.bukkit.entity.Player;

public class PendingAction {

	private Player player;
	private ActionType action;
	private String arg;
	
	public PendingAction(Player player, ActionType action, String arg) {
		this.player = player;
		this.action = action;
		this.arg = arg;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void execute() {
		if (action == ActionType.JOIN_PARTY) {
			Party party = WorldLoader.plugin.parties.get(arg);
			if (party != null) {
				WorldLoader.plugin.addPlayerToParty(player, party);
			}
		}
	}
	
	public enum ActionType {
		JOIN_PARTY
	}
	
}
