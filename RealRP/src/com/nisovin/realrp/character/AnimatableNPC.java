package com.nisovin.realrp.character;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkitcontrib.BukkitContrib;
import org.martin.bukkit.npclib.NPCEntity;
import org.martin.bukkit.npclib.NPCManager;

public class AnimatableNPC implements GameCharacter {

	public static NPCManager npcManager;
	
	private String name;
	private String skin;
	private Location position;
	
	private NPCEntity entity;
	
	private Player animator;
	private Location animatorLoc;
	
	public AnimatableNPC(String name, String skin, Location position) {
		this.name = name;
		this.skin = skin;
		this.position = position;
		
		show();
		
		animator = null;
		animatorLoc = null;
	}
	
	public void animate(Player player) {
		animator = player;
		animatorLoc = player.getLocation().clone();
		
		player.setDisplayName(getChatName());
		BukkitContrib.getAppearanceManager().setGlobalTitle(player, getNameplate());
		if (!skin.isEmpty()) {
			BukkitContrib.getAppearanceManager().setGlobalSkin(player, skin);
		}
		player.teleport(entity.getBukkitEntity().getLocation());
		hide();
	}
	
	public void endAnimate() {
		if (animator != null) {
			PlayerCharacter pc = PlayerCharacter.get(animator);
			pc.setUpNames();
			animator.teleport(animatorLoc);
			show();
			animator = null;
			animatorLoc = null;
		}
	}
	
	public void show() {
		entity = npcManager.spawnNPC(name, position);
		if (!skin.equals("")) {
			BukkitContrib.getAppearanceManager().setGlobalSkin((HumanEntity)entity.getBukkitEntity(), skin);
		}
	}
	
	public void hide() {
		npcManager.despawn(name);
	}

	@Override
	public String getChatName() {
		return name;
	}

	@Override
	public String getEmoteName() {
		return name;
	}

	@Override
	public String getNameplate() {
		return name;
	}
	
}
