package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;

public class Disguise {

	private Player player;
	private EntityType entityType;
	private String nameplateText;
	private boolean flag;
	private int var;
	private DisguiseSpell spell;
	
	private int taskId;
	
	public Disguise(Player player, EntityType entityType, String nameplateText, boolean flag, int var, int duration, DisguiseSpell spell) {
		this.player = player;
		this.entityType = entityType;
		this.nameplateText = nameplateText;
		this.flag = flag;
		this.var = var;
		if (duration > 0) {
			startDuration(duration);
		}
		this.spell = spell;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public EntityType getEntityType() {
		return entityType;
	}
	
	public String getNameplateText() {
		return nameplateText;
	}
	
	public boolean getFlag() {
		return flag;
	}
	
	public int getVar() {
		return var;
	}
	
	private void startDuration(int duration) {
		taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
			public void run() {
				DisguiseSpell.manager.removeDisguise(player);
			}
		}, duration);
	}
	
	public void cancelDuration() {
		if (taskId > 0) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = 0;
		}
	}
	
	public DisguiseSpell getSpell() {
		return spell;
	}
	
}
