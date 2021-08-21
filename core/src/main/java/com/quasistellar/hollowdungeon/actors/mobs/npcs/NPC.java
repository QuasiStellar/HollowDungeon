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

package com.quasistellar.hollowdungeon.actors.mobs.npcs;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class NPC extends Mob {

	{
		HP = HT = 1;

		alignment = Char.Alignment.NEUTRAL;
		state = PASSIVE;
	}

	protected void throwItem() {
		com.quasistellar.hollowdungeon.items.Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null && heap.type == Heap.Type.HEAP) {
			int n;
			do {
				n = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			} while (!Dungeon.level.passable[n] && !Dungeon.level.avoid[n]);
			com.quasistellar.hollowdungeon.Dungeon.level.drop( heap.pickUp(), n ).sprite.drop( pos );
		}
	}

	@Override
	public void beckon( int cell ) {
	}
	
}