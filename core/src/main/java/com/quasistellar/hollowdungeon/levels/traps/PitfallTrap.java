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

package com.quasistellar.hollowdungeon.levels.traps;

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.particles.PitfallParticle;
import com.quasistellar.hollowdungeon.levels.features.Chasm;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class PitfallTrap extends Trap {

	{
		color = RED;
		shape = DIAMOND;
	}

	@Override
	public void activate() {
		
//		if( Dungeon.bossLevel() || Dungeon.depth > 25){
//			GLog.w(Messages.get(this, "no_pit"));
//			return;
//		}

		DelayedPit p = Buff.affect(Dungeon.hero, DelayedPit.class, 1);
		//p.depth = Dungeon.depth;
		p.pos = pos;

		for (int i : PathFinder.NEIGHBOURS9){
			if (!Dungeon.level.solid[pos+i] || Dungeon.level.passable[pos+i]){
				CellEmitter.floor(pos+i).burst(PitfallParticle.FACTORY4, 8);
			}
		}

		if (pos == Dungeon.hero.pos){
			GLog.n(Messages.get(this, "triggered_hero"));
		} else if (Dungeon.level.heroFOV[pos]){
			com.quasistellar.hollowdungeon.utils.GLog.n(Messages.get(this, "triggered"));
		}

	}

	public static class DelayedPit extends FlavourBuff {

		int pos;
		int depth;

		@Override
		public boolean act() {
//			if (depth == Dungeon.depth) {
//				for (int i : PathFinder.NEIGHBOURS9) {
//
//					int cell = pos + i;
//
//					if (Dungeon.level.solid[pos+i] && !Dungeon.level.passable[pos+i]){
//						continue;
//					}
//
//					com.quasistellar.hollowdungeon.effects.CellEmitter.floor(pos+i).burst(com.quasistellar.hollowdungeon.effects.particles.PitfallParticle.FACTORY8, 12);
//
//					Heap heap = Dungeon.level.heaps.get(cell);
//
//					if (heap != null) {
//						for (Item item : heap.items) {
//							Dungeon.dropToChasm(item);
//						}
//						heap.sprite.kill();
//						GameScene.discard(heap);
//						Dungeon.level.heaps.remove(cell);
//					}
//
//					com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(cell);
//
//					//don't trigger on flying chars, or immovable neutral chars
//					if (ch != null && !ch.flying
//						&& !(ch.alignment == Char.Alignment.NEUTRAL && Char.hasProp(ch, com.quasistellar.hollowdungeon.actors.Char.Property.IMMOVABLE))) {
//						if (ch == com.quasistellar.hollowdungeon.Dungeon.hero) {
//							Chasm.heroFall(cell);
//						} else {
//							com.quasistellar.hollowdungeon.levels.features.Chasm.mobFall((Mob) ch);
//						}
//					}
//
//				}
//			}

			detach();
			return true;
		}

		private static final String POS = "pos";
		private static final String DEPTH = "depth";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(POS, pos);
			bundle.put(DEPTH, depth);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pos = bundle.getInt(POS);
			depth = bundle.getInt(DEPTH);
		}

	}

	//TODO these used to become chasms when disarmed, but the functionality was problematic
	//because it could block routes, perhaps some way to make this work elegantly?
}
