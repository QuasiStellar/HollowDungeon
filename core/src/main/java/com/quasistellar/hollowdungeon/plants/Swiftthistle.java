/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Hollow Dungeon
 * Copyright (C) 2020-2021 Pierre Schrodinger
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

package com.quasistellar.hollowdungeon.plants;

import com.quasistellar.hollowdungeon.actors.hero.HeroClass;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Haste;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class Swiftthistle extends com.quasistellar.hollowdungeon.plants.Plant {
	
	{
		image = 2;
		seedClass = Seed.class;
	}
	
	@Override
	public void activate( Char ch ) {
		if (ch == Dungeon.hero) {
			Buff.affect(ch, TimeBubble.class).reset();
			if (Dungeon.hero.heroClass == HeroClass.HORNET){
				Buff.affect(ch, Haste.class, 1f);
			}
		}
	}
	
	public static class Seed extends Plant.Seed {
		{
			image = ItemSpriteSheet.SEED_SWIFTTHISTLE;
			
			plantClass = Swiftthistle.class;
		}
	}
	
	//FIXME lots of copypasta from time freeze here
	
	public static class TimeBubble extends Buff {
		
		{
			type = buffType.POSITIVE;
			announced = true;
		}
		
		private float left;
		ArrayList<Integer> presses = new ArrayList<>();
		
		@Override
		public int icon() {
			return BuffIndicator.SLOW;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (6f - left) / 6f);
		}
		
		public void reset(){
			left = 7f;
		}
		
		@Override
		public String toString() {
			return Messages.get(this, "name");
		}
		
		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(left));
		}
		
		public void processTime(float time){
			left -= time;
			
			if (left <= 0){
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
					for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0]))
						mob.sprite.add(CharSprite.State.PARALYSED);
				GameScene.freezeEmitters = true;
				return true;
			} else {
				return false;
			}
		}
		
		@Override
		public void detach(){
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0]))
				if (mob.paralysed <= 0) mob.sprite.remove(com.quasistellar.hollowdungeon.sprites.CharSprite.State.PARALYSED);
			com.quasistellar.hollowdungeon.scenes.GameScene.freezeEmitters = false;
			
			super.detach();
			triggerPresses();
			target.next();
		}
		
		private static final String PRESSES = "presses";
		private static final String LEFT = "left";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			
			int[] values = new int[presses.size()];
			for (int i = 0; i < values.length; i ++)
				values[i] = presses.get(i);
			bundle.put( PRESSES , values );
			
			bundle.put( LEFT, left);
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			
			int[] values = bundle.getIntArray( PRESSES );
			for (int value : values)
				presses.add(value);
			
			left = bundle.getFloat(LEFT);
		}
		
	}
}
