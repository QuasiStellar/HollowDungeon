/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.quasistellar.hollowdungeon.items.artifacts;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Blindness;
import com.quasistellar.hollowdungeon.actors.buffs.LockedFloor;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.particles.ElmoParticle;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.items.scrolls.Scroll;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfIdentify;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfMagicMapping;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRemoveCurse;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTransmutation;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;

public class UnstableSpellbook extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_SPELLBOOK;

		levelCap = 10;

		charge = (int)(level()*0.6f)+2;
		partialCharge = 0;
		chargeCap = (int)(level()*0.6f)+2;

		defaultAction = AC_READ;
	}

	public static final String AC_READ = "READ";
	public static final String AC_ADD = "ADD";

	private final ArrayList<Class> scrolls = new ArrayList<>();

	protected com.quasistellar.hollowdungeon.windows.WndBag.Mode mode = WndBag.Mode.SCROLL;

	public UnstableSpellbook() {
		super();

		Class<?>[] scrollClasses = Generator.Category.SCROLL.classes;
		float[] probs = Generator.Category.SCROLL.defaultProbs.clone(); //array of primitives, clone gives deep copy.
		int i = Random.chances(probs);

		while (i != -1){
			scrolls.add(scrollClasses[i]);
			probs[i] = 0;

			i = Random.chances(probs);
		}
		scrolls.remove(ScrollOfTransmutation.class);
	}

	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && charge > 0 && !cursed)
			actions.add(AC_READ);
		if (isEquipped( hero ) && level() < levelCap && !cursed)
			actions.add(AC_ADD);
		return actions;
	}

	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_READ )) {

			if (hero.buff( Blindness.class ) != null) GLog.w( Messages.get(this, "blinded") );
			else if (!isEquipped( hero ))             GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge <= 0)                     GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                          GLog.i( Messages.get(this, "cursed") );
			else {
				charge--;

				Scroll scroll;
				do {
					scroll = (Scroll) Generator.randomUsingDefaults(com.quasistellar.hollowdungeon.items.Generator.Category.SCROLL);
				} while (scroll == null
						//reduce the frequency of these scrolls by half
						||((scroll instanceof ScrollOfIdentify ||
							scroll instanceof ScrollOfRemoveCurse ||
							scroll instanceof ScrollOfMagicMapping) && Random.Int(2) == 0)
						//don't roll teleportation scrolls on boss floors
						|| (scroll instanceof ScrollOfTeleportation && Dungeon.bossLevel())
						//cannot roll transmutation
						|| (scroll instanceof ScrollOfTransmutation));
				
				scroll.anonymize();
				Item.curItem = scroll;
				Item.curUser = hero;

				//if there are charges left and the scroll has been given to the book
				if (charge > 0 && !scrolls.contains(scroll.getClass())) {
					final Scroll fScroll = scroll;

					final ExploitHandler handler = Buff.affect(hero, ExploitHandler.class);
					handler.scroll = scroll;

					GameScene.show(new WndOptions(
							Messages.get(this, "prompt"),
							Messages.get(this, "read_empowered"),
							scroll.trueName(),
							Messages.get(ExoticScroll.regToExo.get(scroll.getClass()), "name")){
						@Override
						protected void onSelect(int index) {
							handler.detach();
							if (index == 1){
								Scroll scroll = Reflection.newInstance(com.quasistellar.hollowdungeon.items.scrolls.exotic.ExoticScroll.regToExo.get(fScroll.getClass()));
								charge--;
								scroll.doRead();
							} else {
								fScroll.doRead();
							}
						}
						
						@Override
						public void onBackPressed() {
							//do nothing
						}
					});
				} else {
					scroll.doRead();
				}
				Item.updateQuickslot();
			}

		} else if (action.equals( AC_ADD )) {
			com.quasistellar.hollowdungeon.scenes.GameScene.selectItem(itemSelector, mode, Messages.get(this, "prompt"));
		}
	}

	//forces the reading of a regular scroll if the player tried to exploit by quitting the game when the menu was up
	public static class ExploitHandler extends com.quasistellar.hollowdungeon.actors.buffs.Buff {
		{ actPriority = com.quasistellar.hollowdungeon.actors.Actor.VFX_PRIO; }

		public Scroll scroll;

		@Override
		public boolean act() {
			Item.curUser = Dungeon.hero;
			Item.curItem = scroll;
			scroll.anonymize();
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					scroll.doRead();
				}
			});
			detach();
			return true;
		}

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( "scroll", scroll );
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			scroll = (Scroll)bundle.get("scroll");
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new bookRecharge();
	}
	
	@Override
	public void charge(com.quasistellar.hollowdungeon.actors.hero.Hero target) {
		if (charge < chargeCap){
			partialCharge += 0.1f;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				Item.updateQuickslot();
			}
		}
	}

	@Override
	public com.quasistellar.hollowdungeon.items.Item upgrade() {
		chargeCap = (int)((level()+1)*0.6f)+2;

		//for artifact transmutation.
		while (!scrolls.isEmpty() && scrolls.size() > (levelCap-1-level()))
			scrolls.remove(0);

		return super.upgrade();
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped(Dungeon.hero)) {
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			}
			
			if (level() < levelCap && scrolls.size() > 0) {
				desc += "\n\n" + Messages.get(this, "desc_index");
				desc += "\n" + "_" + Messages.get(scrolls.get(0), "name") + "_";
				if (scrolls.size() > 1)
					desc += "\n" + "_" + Messages.get(scrolls.get(1), "name") + "_";
			}
		}
		
		if (level() > 0) {
			desc += "\n\n" + Messages.get(this, "desc_empowered");
		}

		return desc;
	}

	private static final String SCROLLS =   "scrolls";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( SCROLLS, scrolls.toArray(new Class[scrolls.size()]) );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		scrolls.clear();
		Collections.addAll(scrolls, bundle.getClassArray(SCROLLS));
	}

	public class bookRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 1 / (120f - (chargeCap - charge)*5f);

				if (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			}

			Item.updateQuickslot();

			spend( Actor.TICK );

			return true;
		}
	}

	protected com.quasistellar.hollowdungeon.windows.WndBag.Listener itemSelector = new com.quasistellar.hollowdungeon.windows.WndBag.Listener() {
		@Override
		public void onSelect(com.quasistellar.hollowdungeon.items.Item item) {
			if (item != null && item instanceof Scroll && item.isIdentified()){
				Hero hero = com.quasistellar.hollowdungeon.Dungeon.hero;
				for (int i = 0; ( i <= 1 && i < scrolls.size() ); i++){
					if (scrolls.get(i).equals(item.getClass())){
						hero.sprite.operate( hero.pos );
						hero.busy();
						hero.spend( 2f );
						Sample.INSTANCE.play(Assets.Sounds.BURNING);
						hero.sprite.emitter().burst( ElmoParticle.FACTORY, 12 );

						scrolls.remove(i);
						item.detach(hero.belongings.backpack);

						upgrade();
						GLog.i( Messages.get(UnstableSpellbook.class, "infuse_scroll") );
						return;
					}
				}
				GLog.w( Messages.get(UnstableSpellbook.class, "unable_scroll") );
			} else if (item instanceof Scroll && !item.isIdentified())
				com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(UnstableSpellbook.class, "unknown_scroll") );
		}
	};
}
