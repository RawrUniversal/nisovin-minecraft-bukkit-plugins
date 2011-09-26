package com.nisovin.realrp.character;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.nisovin.realrp.npc.NPC;

public class AnimatableNPC implements GameCharacter {

	private String name;
	private String skin;
	private Location position;
	
	private NPC npc;
	
	private Player animator;
	private Location animatorLoc;
	
	public AnimatableNPC(String name, String skin, Location position) {		
		this.name = name;
		this.skin = skin;
		this.position = position;
		
		animator = null;
		animatorLoc = null;
	}
	
	public void animate(Player player) {
		animator = player;
		animatorLoc = player.getLocation().clone();
		
		player.setDisplayName(getChatName());
		SpoutManager.getAppearanceManager().setGlobalTitle(player, getNameplate());
		if (!skin.isEmpty()) {
			SpoutManager.getAppearanceManager().setGlobalSkin(player, skin);
		}
		player.teleport(npc.getLocation());
		hide();
	}
	
	public void endAnimate() {
		if (animator != null) {
			PlayerCharacter pc = PlayerCharacter.get(animator);
			if (pc == null) {
				// severe error
				return;
			}
			
			pc.setUpNames();
			animator.teleport(animatorLoc);
			show();
			animator = null;
			animatorLoc = null;
		}
	}
	
	public void show() {
		if (npc == null) {
			npc = new NPC(name, position);
			if (!skin.equals("")) {
				SpoutManager.getAppearanceManager().setGlobalSkin((HumanEntity)npc.getEntity().getBukkitEntity(), skin);
			}
		}
	}
	
	public void hide() {
		if (npc != null) {
			npc.despawn();
			npc = null;
		}
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
	
	@Override
	public Sex getSex() {
		return Sex.Unknown;
	}
	
	public Location getLocation() {
		return position;
	}
	
}
