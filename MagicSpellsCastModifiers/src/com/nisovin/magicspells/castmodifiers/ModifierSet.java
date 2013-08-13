package com.nisovin.magicspells.castmodifiers;

import java.util.ArrayList;
import java.util.List;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;

public class ModifierSet {

	private List<Modifier> modifiers;
	private String message;
	
	public ModifierSet(List<String> data, String message) {
		this.message = message;
		modifiers = new ArrayList<Modifier>();
		for (String s : data) {
			Modifier m = Modifier.factory(s);
			if (m != null) {
				modifiers.add(m);
				MagicSpells.debug(3, "    Modifier added: " + s);
			} else {
				MagicSpells.error("Problem with modifier: " + s);
			}
		}
	}
	
	public void apply(SpellCastEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				MagicSpells.sendMessage(event.getCaster(), message);
				break;
			}
		}
	}
	
	public void apply(ManaChangeEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				break;
			}
		}
	}
	
	public void apply(SpellTargetEvent event) {
		for (Modifier modifier : modifiers) {
			boolean cont = modifier.apply(event);
			if (!cont) {
				break;
			}
		}
	}
	
}
