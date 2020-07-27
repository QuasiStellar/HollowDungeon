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

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Hunger;
import com.quasistellar.hollowdungeon.actors.buffs.LockedFloor;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class TimekeepersHourglass extends com.quasistellar.hollowdungeon.items.artifacts.Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_HOURGLASS;

		levelCap = 5;

		charge = 5+level();
		partialCharge = 0;
		chargeCap = 5+level();

		defaultAction = AC_ACTIVATE;
	}

	public static final String AC_ACTIVATE = "ACTIVATE";

	//keeps track of generated sandbags.
	public int sandBags = 0;

	@Override
	public ArrayList<String> actions( com.quasistellar.hollowdungeon.actors.hero.Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && charge > 0 && !cursed)
			actions.add(AC_ACTIVATE);
		return actions;
	}

	@Override
	public void execute(com.quasistellar.hollowdungeon.actors.hero.Hero hero, String action ) {

		super.execute(hero, action);

		if (action.equals(AC_ACTIVATE)){

			if (!isEquipped( hero ))        GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (activeBuff != null) {
				if (activeBuff instanceof timeStasis) { //do nothing
				} else {
					activeBuff.detach();
					GLog.i( Messages.get(this, "deactivate") );
				}
			} else if (charge <= 0)         GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                GLog.i( Messages.get(this, "cursed") );
			else GameScene.show(
						new WndOptions( Messages.get(this, "name"),
								Messages.get(this, "prompt"),
								Messages.get(this, "stasis"),
								Messages.get(this, "freeze")) {
							@Override
							protected void onSelect(int index) {
								if (index == 0) {
									GLog.i( Messages.get(TimekeepersHourglass.class, "onstasis") );
									GameScene.flash(0xFFFFFF);
									Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

									activeBuff = new timeStasis();
									activeBuff.attachTo(Dungeon.hero);
								} else if (index == 1) {
									GLog.i( Messages.get(TimekeepersHourglass.class, "onfreeze") );
									GameScene.flash(0xFFFFFF);
									Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

									activeBuff = new timeFreeze();
									activeBuff.attachTo(Dungeon.hero);
									((timeFreeze)activeBuff).processTime(0f);
								}
							}
						}
				);
		}
	}

	@Override
	public void activate(com.quasistellar.hollowdungeon.actors.Char ch) {
		super.activate(ch);
		if (activeBuff != null)
			activeBuff.attachTo(ch);
	}

	@Override
	public boolean doUnequip(com.quasistellar.hollowdungeon.actors.hero.Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){
			if (activeBuff != null){
				activeBuff.detach();
				activeBuff = null;
			}
			return true;
		} else
			return false;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new hourglassRecharge();
	}
	
	@Override
	public void charge(com.quasistellar.hollowdungeon.actors.hero.Hero target) {
		if (charge < chargeCap){
			partialCharge += 0.25f;
			if (partialCharge >= 1){
				partialCharge--;
				charge++;
				Item.updateQuickslot();
			}
		}
	}

	@Override
	public com.quasistellar.hollowdungeon.items.Item upgrade() {
		chargeCap+= 1;

		//for artifact transmutation.
		while (level()+1 > sandBags)
			sandBags ++;

		return super.upgrade();
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped( Dungeon.hero )){
			if (!cursed) {
				if (level() < levelCap )
					desc += "\n\n" + Messages.get(this, "desc_hint");

			} else
				desc += "\n\n" + Messages.get(this, "desc_cursed");
		}
		return desc;
	}


	private static final String SANDBAGS =  "sandbags";
	private static final String BUFF =      "buff";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( SANDBAGS, sandBags );

		if (activeBuff != null)
			bundle.put( BUFF , activeBuff );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		sandBags = bundle.getInt( SANDBAGS );

		//these buffs belong to hourglass, need to handle unbundling within the hourglass class.
		if (bundle.contains( BUFF )){
			Bundle buffBundle = bundle.getBundle( BUFF );

			if (buffBundle.contains( timeFreeze.PRESSES ))
				activeBuff = new timeFreeze();
			else
				activeBuff = new timeStasis();

			activeBuff.restoreFromBundle(buffBundle);
		}
	}

	public class hourglassRecharge extends ArtifactBuff {
		@Override
		public boolean act() {

			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 1 / (90f - (chargeCap - charge)*3f);

				if (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			} else if (cursed && Random.Int(10) == 0)
				((com.quasistellar.hollowdungeon.actors.hero.Hero) target).spend( com.quasistellar.hollowdungeon.actors.Actor.TICK );

			Item.updateQuickslot();

			spend( Actor.TICK );

			return true;
		}
	}

	public class timeStasis extends ArtifactBuff {
		
		{
			type = Buff.buffType.POSITIVE;
		}

		@Override
		public boolean attachTo(com.quasistellar.hollowdungeon.actors.Char target) {

			if (super.attachTo(target)) {

				int usedCharge = Math.min(charge, 2);
				//buffs always act last, so the stasis buff should end a turn early.
				spend((5*usedCharge) - 1);
				((com.quasistellar.hollowdungeon.actors.hero.Hero) target).spendAndNext(5*usedCharge);

				//shouldn't punish the player for going into stasis frequently
				com.quasistellar.hollowdungeon.actors.buffs.Hunger hunger = com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(target, Hunger.class);
				if (hunger != null && !hunger.isStarving())
					hunger.satisfy(5*usedCharge);

				charge -= usedCharge;

				target.invisible++;

				Item.updateQuickslot();

				Dungeon.observe();

				return true;
			} else {
				return false;
			}
		}

		@Override
		public boolean act() {
			detach();
			return true;
		}

		@Override
		public void detach() {
			if (target.invisible > 0)
				target.invisible --;
			super.detach();
			activeBuff = null;
			Dungeon.observe();
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add( CharSprite.State.INVISIBLE );
			else if (target.invisible == 0) target.sprite.remove( CharSprite.State.INVISIBLE );
		}
	}

	public class timeFreeze extends ArtifactBuff {
		
		{
			type = com.quasistellar.hollowdungeon.actors.buffs.Buff.buffType.POSITIVE;
		}

		float turnsToCost = 0f;

		ArrayList<Integer> presses = new ArrayList<>();

		public void processTime(float time){
			turnsToCost -= time;

			while (turnsToCost < 0f){
				turnsToCost += 2f;
				charge --;
			}

			Item.updateQuickslot();

			if (charge < 0){
				charge = 0;
				detach();
			}

		}

		public void setDelayedPress(int cell){
			if (!presses.contains(cell))
				presses.add(cell);
		}

		private void triggerPresses(){
			for (int cell : presses)
				Dungeon.level.pressCell(cell);

			presses = new ArrayList<>();
		}

		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)){
				if (Dungeon.level != null)
					for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray(new com.quasistellar.hollowdungeon.actors.mobs.Mob[0]))
						mob.sprite.add(CharSprite.State.PARALYSED);
				GameScene.freezeEmitters = true;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void detach(){
			for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : com.quasistellar.hollowdungeon.Dungeon.level.mobs.toArray(new Mob[0]))
				if (mob.paralysed <= 0) mob.sprite.remove(com.quasistellar.hollowdungeon.sprites.CharSprite.State.PARALYSED);
			com.quasistellar.hollowdungeon.scenes.GameScene.freezeEmitters = false;

			Item.updateQuickslot();
			super.detach();
			activeBuff = null;
			triggerPresses();
			target.next();
		}

		private static final String PRESSES = "presses";
		private static final String TURNSTOCOST = "turnsToCost";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);

			int[] values = new int[presses.size()];
			for (int i = 0; i < values.length; i ++)
				values[i] = presses.get(i);
			bundle.put( PRESSES , values );

			bundle.put( TURNSTOCOST , turnsToCost);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);

			int[] values = bundle.getIntArray( PRESSES );
			for (int value : values)
				presses.add(value);

			turnsToCost = bundle.getFloat( TURNSTOCOST );
		}
	}

	public static class sandBag extends com.quasistellar.hollowdungeon.items.Item {

		{
			image = com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet.SANDBAG;
		}

		@Override
		public boolean doPickUp( Hero hero ) {
			TimekeepersHourglass hourglass = hero.belongings.getItem( TimekeepersHourglass.class );
			if (hourglass != null && !hourglass.cursed) {
				hourglass.upgrade();
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.DEWDROP );
				if (hourglass.level() == hourglass.levelCap)
					GLog.p( Messages.get(this, "maxlevel") );
				else
					GLog.i( Messages.get(this, "levelup") );
				hero.spendAndNext(TIME_TO_PICK_UP);
				return true;
			} else {
				com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(this, "no_hourglass") );
				return false;
			}
		}

		@Override
		public int price() {
			return 20;
		}
	}


}
