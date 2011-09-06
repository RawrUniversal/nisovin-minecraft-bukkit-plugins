package com.nisovin.realrp.character;

import org.bukkit.entity.Player;

public class EmptyPlayerCharacter implements GameCharacter {

	private Player player;
	
	public EmptyPlayerCharacter(Player player) {
		this.player = player;
	}
	
	@Override
	public String getChatName() {
		return player.getDisplayName();
	}

	@Override
	public String getEmoteName() {
		return player.getDisplayName();
	}

	@Override
	public String getNameplate() {
		return player.getName();
	}

	@Override
	public Sex getSex() {
		return Sex.Unknown;
	}

}
