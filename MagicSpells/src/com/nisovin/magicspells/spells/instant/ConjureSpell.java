package com.nisovin.magicspells.spells.instant;

import java.util.List;

import org.bukkit.entity.Player;
import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ConjureSpell extends InstantSpell {

	private int[] itemTypes;
	private int[] itemDatas;
	private int[] itemMinQuantities;
	private int[] itemMaxQuantities;
	
	public ConjureSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		List<String> list = getConfigStringList("items", null);
		if (list != null && list.size() > 0) {
			itemTypes = new int[list.size()];
			itemDatas = new int[list.size()];
			itemMinQuantities = new int[list.size()];
			itemMaxQuantities = new int[list.size()];
			
			for (int i = 0; i < list.size(); i++) {
				String[] data = list.get(i).split(" ");
				String[] typeData = data[0].split(":");
				String[] quantityData = data[1].split("-");
				
				if (typeData.length == 1) {
					itemTypes[i] = Integer.parseInt(typeData[0]);
					itemDatas[i] = 0;
				} else {
					itemTypes[i] = Integer.parseInt(typeData[0]);
					itemDatas[i] = Integer.parseInt(typeData[1]);
				}
				
				if (quantityData.length == 1) {
					itemMinQuantities[i] = Integer.parseInt(quantityData[0]);
					itemMaxQuantities[i] = itemMinQuantities[i];
				} else {
					itemMinQuantities[i] = Integer.parseInt(quantityData[0]);
					itemMaxQuantities[i] = Integer.parseInt(quantityData[1]);	
				}
			}
		}
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			
		}		
		return PostCastAction.HANDLE_NORMALLY;
		
	}
	

}
