package com.nisovin.realrp.character;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.realrp.RealRP;
import com.nisovin.realrp.character.GameCharacter.Sex;

public class CharacterCreator {
	
	private static ArrayList<State> stateOrder = new ArrayList<State>();
	static {
		stateOrder.add(State.GET_FIRST_NAME);
		stateOrder.add(State.GET_LAST_NAME);
		stateOrder.add(State.GET_AGE);
		stateOrder.add(State.GET_SEX);
		stateOrder.add(State.GET_DESCRIPTION);
	}
	
	private Player player;
	private State state;
	private String firstName;
	private String lastName;
	private PlayerCharacter.Sex sex;
	private int age;
	private String description;
	
	public CharacterCreator(Player player) {
		this.player = player;
		this.state = stateOrder.get(0);
		askForInformation();
	}
	
	public void nextState() {
		int i = stateOrder.indexOf(state) + 1;
		if (i == stateOrder.size()) {
			// done
			player.sendMessage("Character creation done.");
			// finish up the char
			PlayerCharacter pc = new PlayerCharacter(player, firstName, lastName, age, sex, description);
			pc.save();
			RealRP.getPlugin().finishCharacterCreator(player);
			// give items
			String sItems = RealRP.settings().ccItemsGranted;
			if (sItems != null && !sItems.isEmpty()) {
				String[] items = sItems.split(";");
				for (String s : items) {
					String[] itemAndQuantity = s.trim().split(" ");
					int quantity = 1;
					if (itemAndQuantity.length == 2) {
						quantity = Integer.parseInt(itemAndQuantity[1]);
					}
					int type, data;
					if (itemAndQuantity[0].contains(":")) {
						String[] typeData = itemAndQuantity[0].split(":");
						type = Integer.parseInt(typeData[0]);
						data = Integer.parseInt(typeData[1]);
					} else {
						type = Integer.parseInt(itemAndQuantity[0]);
						data = 0;
					}
					ItemStack item = new ItemStack(type, quantity, (short)data);
					player.getInventory().addItem(item);
				}
			}
		} else {
			state = stateOrder.get(i);
			askForInformation();
		}
	}
	
	public void askForInformation() {
		if (state == State.GET_FIRST_NAME) {
			RealRP.sendMessage(player, RealRP.settings().ccFirstNameGet);
		} else if (state == State.GET_LAST_NAME) {
			RealRP.sendMessage(player, RealRP.settings().ccLastNameGet);
		} else if (state == State.GET_AGE) {
			RealRP.sendMessage(player, RealRP.settings().ccAgeGet);
		} else if (state == State.GET_SEX) {
			RealRP.sendMessage(player, RealRP.settings().ccSexGet);		
		} else if (state == State.GET_DESCRIPTION) {
			RealRP.sendMessage(player, RealRP.settings().ccDescriptionGet);		
		}
	}
	
	public void onChat(String message) {
		if (state == State.GET_FIRST_NAME) {
			if (message.matches(RealRP.settings().ccFirstNameRegex)) {
				firstName = message;
				RealRP.sendMessage(player, RealRP.settings().ccFirstNameOk, "%v", firstName);
				nextState();
			} else {
				RealRP.sendMessage(player, RealRP.settings().ccFirstNameInvalid);
				askForInformation();
			}
		} else if (state == State.GET_LAST_NAME) {
			if (message.matches(RealRP.settings().ccLastNameRegex)) {
				lastName = message;
				RealRP.sendMessage(player, RealRP.settings().ccLastNameOk, "%v", lastName);
				nextState();
			} else {
				RealRP.sendMessage(player, RealRP.settings().ccLastNameInvalid);
				askForInformation();
			}
		} else if (state == State.GET_AGE) {
			if (message.matches(RealRP.settings().ccAgeRegex)) {
				age = Integer.parseInt(message);
				RealRP.sendMessage(player, RealRP.settings().ccAgeOk, "%v", age+" years");
				nextState();
			} else {
				RealRP.sendMessage(player, RealRP.settings().ccAgeInvalid);
				askForInformation();
			}
		} else if (state == State.GET_SEX) {
			if (message.matches(RealRP.settings().ccSexRegex)) {
				String s = message.substring(0,1).toLowerCase();
				if (s.equals("m") || s.equals("b")) {
					sex = Sex.Male;
				} else if (s.equals("f") || s.equals("w") || s.equals("g")) {
					sex = Sex.Female;
				} else {
					sex = Sex.Unknown;
				}
				RealRP.sendMessage(player, RealRP.settings().ccSexOk, "%v", sex.toString());
				nextState();
			} else {
				RealRP.sendMessage(player, RealRP.settings().ccSexInvalid);
				askForInformation();
			}
		} else if (state == State.GET_DESCRIPTION) {
			if (message.matches(RealRP.settings().ccDescriptionRegex)) {
				description = message;
				RealRP.sendMessage(player, RealRP.settings().ccDescriptionOk);
				nextState();
			} else {
				RealRP.sendMessage(player, RealRP.settings().ccDescriptionInvalid);
				askForInformation();
			}
		}
	}
	
	private enum State {
		GET_FIRST_NAME,
		GET_LAST_NAME,
		GET_AGE,
		GET_SEX,
		GET_DESCRIPTION
	}
	
}
