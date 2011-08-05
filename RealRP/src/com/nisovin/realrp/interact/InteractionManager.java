package com.nisovin.realrp.interact;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.nisovin.realrp.RealRP;
import com.nisovin.realrp.character.AnimatableNPC;

public class InteractionManager {

	private HashMap<Player,AnimatableNPC> lastClicked = new HashMap<Player,AnimatableNPC>();
	private RequestList requestList = new RequestList();
	private HashSet<Player> availMods = new HashSet<Player>();
	private AnimationController animationController = new AnimationController();
	
	public InteractionManager() {
	}
	
	public void rightClickNpc(Player player, AnimatableNPC npc) {
		lastClicked.put(player, npc);
	}
	
	public void interactCommand(Player player, String description) {
		AnimatableNPC npc = lastClicked.get(player);
		newRequest(player, npc, description);
	}
	
	public void newRequest(Player player, AnimatableNPC npc, String description) {
		// create request
		InteractRequest request = new InteractRequest(player, npc, description);
		requestList.add(request);
		
		// announce to mods
		String ann = request.getRequestLine();
		for (Player p : availMods) {
			RealRP.sendMessage(p, ann);
		}
	}
	
	public void acceptRequest(int id, Player player) {
		InteractRequest request = requestList.get(id);
		if (request == null) {
			// no request id
			return;
		}
		
		animationController.animateNpc(player, request.getNpc());
	}
	
}
