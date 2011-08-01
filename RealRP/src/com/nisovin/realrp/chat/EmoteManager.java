package com.nisovin.realrp.chat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.nisovin.realrp.RealRP;
import com.nisovin.realrp.character.GameCharacter;
import com.nisovin.realrp.character.PlayerCharacter;
import com.nisovin.realrp.character.PlayerCharacter.Sex;

public class EmoteManager {

	private HashMap<String,Emote> emotes;
	
	//pronouns
	private static String proSubjMale = "he";
	private static String proSubjFemale = "she";
	private static String proSubjUnknown = "he/she";
	private static String proObjMale = "him";
	private static String proObjFemale = "her";
	private static String proObjUnknown = "him/her";
	private static String proPossMale = "his";
	private static String proPossFemale = "her";
	private static String proPossUnknown = "his/her";
	private static String proPoss2Male = "his";
	private static String proPoss2Female = "hers";
	private static String proPoss2Unknown = "his/hers";
	private static String proReflMale = "himself";
	private static String proReflFemale = "herself";
	private static String proReflUnknown = "him/herself";
	
	public EmoteManager() {
		emotes = new HashMap<String,Emote>();
		
		Configuration config = new Configuration(new File(RealRP.getPlugin().getDataFolder(), "emotes.yml"));
		config.load();
		
		// load pronouns
		proSubjMale = config.getString("pronouns.subj-male", proSubjMale);
		
		// load emotes
		Map<String,ConfigurationNode> nodes = config.getNodes("emotes");
		for (Map.Entry<String,ConfigurationNode> entry : nodes.entrySet()) {
			Emote emote = new Emote(entry.getValue());
			emotes.put(entry.getKey(), emote);
		}
	}
	
	public Emote getEmote(String emote) {
		return emotes.get(emote);
	}
	
	public static void formatAndSend(Player to, String message, PlayerCharacter actor) {
		formatAndSend(to, message, actor, null);
	}
	
	public static void formatAndSend(Player to, String message, PlayerCharacter actor, GameCharacter target) {
		message = message.replace("%actor", actor.getEmoteName());
		message = message.replace("%target", target.getEmoteName());
		if (actor.getSex() == Sex.Male) {
			message = message.replace("%subj", proSubjMale);
			message = message.replace("%obj", proObjMale);
			message = message.replace("%poss", proPossMale);
			message = message.replace("%poss2", proPoss2Male);
			message = message.replace("%refl", proReflMale);
		} else if (actor.getSex() == Sex.Female) {
			message = message.replace("%subj", proSubjFemale);
			message = message.replace("%obj", proObjFemale);
			message = message.replace("%poss", proPossFemale);
			message = message.replace("%poss2", proPoss2Female);
			message = message.replace("%refl", proReflFemale);
		} else {
			message = message.replace("%subj", proSubjUnknown);
			message = message.replace("%obj", proObjUnknown);
			message = message.replace("%poss", proPossUnknown);
			message = message.replace("%poss2", proPoss2Unknown);
			message = message.replace("%refl", proReflUnknown);
		}
		to.sendMessage(message);
	}
	
}
