package com.nisovin.magicspells.spells;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.magicspells.CommandSpell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.bookworm.Book;
import com.nisovin.bookworm.BookWorm;
import com.nisovin.bookworm.event.BookReadEvent;
import com.nisovin.bookworm.event.BookWormListener;

public class TomeSpell extends CommandSpell {

	private boolean cancelReadOnLearn;
	private boolean allowOverwrite;
	private int defaultUses;
	private int maxUses;
	private boolean requireTeachPerm;
	private String strUsage;
	private String strNoSpell;
	private String strCantTeach;
	private String strNoBook;
	private String strAlreadyHasSpell;
	private String strAlreadyKnown;
	private String strCantLearn;
	private String strLearned;
	
	private BookWormListener listener;
	
	public TomeSpell(Configuration config, String spellName) {
		super(config, spellName);
		listener = new BookListener();
		BookWorm.registerListener(listener);
		
		cancelReadOnLearn = getConfigBoolean("cancel-read-on-learn", true);
		allowOverwrite = getConfigBoolean("allow-overwrite", false);
		defaultUses = getConfigInt("default-uses", -1);
		maxUses = getConfigInt("max-uses", 5);
		requireTeachPerm = getConfigBoolean("require-teach-perm", true);
		strUsage = getConfigString("str-usage", "Usage: While holding a book, /cast " + name + " <spell> [uses]");
		strNoSpell = getConfigString("str-no-spell", "You do not know a spell with that name.");
		strCantTeach = getConfigString("str-cant-teach", "You cannot create a tome with that spell.");
		strNoBook = getConfigString("str-no-book", "You must be holding a book.");
		strAlreadyHasSpell = getConfigString("str-already-has-spell", "That book already contains a spell.");
		strAlreadyKnown = getConfigString("str-already-known", "You already know the %s spell.");
		strCantLearn = getConfigString("str-cant-learn", "You cannot learn the spell in this tome.");
		strLearned = getConfigString("str-learned", "You have learned the %s spell.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Spell spell;
			if (args == null || args.length == 0) {
				// fail -- no args
				sendMessage(player, strUsage);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				spell = MagicSpells.getSpellByInGameName(args[0]);
				if (spell == null || spellbook == null || !spellbook.hasSpell(spell)) {
					// fail -- no spell
					sendMessage(player, strNoSpell);
					return PostCastAction.ALREADY_HANDLED;
				} else if (requireTeachPerm && !MagicSpells.getSpellbook(player).canTeach(spell)) {
					sendMessage(player, strCantTeach);
					return PostCastAction.ALREADY_HANDLED;
				}
			}
			
			Book book = BookWorm.getBook(player.getItemInHand());
			if (book == null) {
				// fail -- no book
				sendMessage(player, strNoBook);
				return PostCastAction.ALREADY_HANDLED;
			} else if (!allowOverwrite && book.hasHiddenData("MagicSpell")) {
				// fail -- already has a spell
				sendMessage(player, strAlreadyHasSpell);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				int uses = defaultUses;
				if (args.length > 1 && args[1].matches("^[0-9]+$")) {
					uses = Integer.parseInt(args[1]);
				}
				if (uses > maxUses || (maxUses > 0 && uses < 0)) {
					uses = maxUses;
				}
				book.addHiddenData("MagicSpell", spell.getInternalName() + (uses>0?","+uses:""));
				book.save();
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	@Override
	protected void turnOff() {
		if (listener != null) {
			BookWorm.unregisterListener(listener);
		}
	}
	
	private class BookListener extends BookWormListener {
		@Override
		public void onBookRead(BookReadEvent event) {
			String spellData = event.getBook().getHiddenData("MagicSpell");
			if (spellData != null && !spellData.equals("")) {
				String[] data = spellData.split(",");
				Spell spell = MagicSpells.getSpellByInternalName(data[0]);
				int uses = -1;
				if (data.length > 1) {
					uses = Integer.parseInt(data[1]);
				}
				Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
				if (spell != null && spellbook != null) {
					if (spellbook.hasSpell(spell)) {
						// fail -- already known
						sendMessage(event.getPlayer(), formatMessage(strAlreadyKnown, "%s", spell.getName()));
					} else if (!spellbook.canLearn(spell)) {
						// fail -- can't learn
						sendMessage(event.getPlayer(), formatMessage(strCantLearn, "%s", spell.getName()));
					} else {
						// give spell
						spellbook.addSpell(spell);
						sendMessage(event.getPlayer(), formatMessage(strLearned, "%s", spell.getName()));
						if (cancelReadOnLearn) {
							event.setCancelled(true);
						}
						// remove use
						if (uses > 0) {
							uses--;
							if (uses > 0) {
								event.getBook().addHiddenData("MagicSpell", data[0] + "," + uses);
							} else {
								event.getBook().removeHiddenData("MagicSpell");
							}							
						}
					}
				}
			}
		}
	}

}