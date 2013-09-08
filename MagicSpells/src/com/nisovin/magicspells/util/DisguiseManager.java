package com.nisovin.magicspells.util;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.targeted.DisguiseSpell;

public interface DisguiseManager {

	public void registerSpell(DisguiseSpell spell);
	
	public void unregisterSpell(DisguiseSpell spell);
	
	public int registeredSpellsCount();
	
	public void addDisguise(Player player, DisguiseSpell.Disguise disguise);
	
	public void removeDisguise(Player player);
	
	public boolean isDisguised(Player player);
	
	public DisguiseSpell.Disguise getDisguise(Player player);
	
	public void destroy();
	
}
