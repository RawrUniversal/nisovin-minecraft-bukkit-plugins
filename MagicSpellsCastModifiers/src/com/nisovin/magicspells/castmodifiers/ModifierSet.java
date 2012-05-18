package com.nisovin.magicspells.castmodifiers;

import java.util.ArrayList;
import java.util.List;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellCastEvent;

public class ModifierSet {

	private List<Modifier> modifiers;
	private String message;
	
	public ModifierSet(List<String> data, String message) {
		this.message = message;
		modifiers = new ArrayList<Modifier>();
		for (String s : data) {
			modifiers.add(new Modifier(s));
			MagicSpells.debug(3, "    Modifier added: " + s);
		}
	}
	
	public void apply(SpellCastEvent event) {
		for (Modifier modifier : modifiers) {
			System.out.println("Applying modifier");
			boolean cont = modifier.apply(event);
			if (!cont) {
				MagicSpells.sendMessage(event.getCaster(), message);
				break;
			}
		}
	}
	
}
