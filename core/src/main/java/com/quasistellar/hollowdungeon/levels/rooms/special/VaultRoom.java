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

package com.quasistellar.hollowdungeon.levels.rooms.special;

import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.actors.mobs.CrystalMimic;
import com.quasistellar.hollowdungeon.actors.mobs.Mimic;
import com.quasistellar.hollowdungeon.items.keys.CrystalKey;
import com.quasistellar.hollowdungeon.items.keys.IronKey;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class VaultRoom extends SpecialRoom {

	//size is reduced slightly to remove rare AI issues with crystal mimics
	@Override
	public int maxHeight() { return 8; }
	public int maxWidth() { return 8; }

	public void paint( com.quasistellar.hollowdungeon.levels.Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );
		Painter.fill( level, this, 2, com.quasistellar.hollowdungeon.levels.Terrain.EMPTY );
		
		int cx = (left + right) / 2;
		int cy = (top + bottom) / 2;
		int c = cx + cy * level.width();
		
		Random.shuffle(prizeClasses);
		
		com.quasistellar.hollowdungeon.items.Item i1, i2;
		i1 = prize( level );
		i2 = prize( level );
		level.drop( i1, c ).type = Heap.Type.CRYSTAL_CHEST;
		if (Random.Int(10) == 0){
			level.mobs.add(Mimic.spawnAt(c + PathFinder.NEIGHBOURS8[Random.Int(8)], i2, CrystalMimic.class));
		} else {
			level.drop(i2, c + PathFinder.NEIGHBOURS8[Random.Int(8)]).type = com.quasistellar.hollowdungeon.items.Heap.Type.CRYSTAL_CHEST;
		}
		level.addItemToSpawn( new CrystalKey( Dungeon.depth ) );
		
		entrance().set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey( com.quasistellar.hollowdungeon.Dungeon.depth ) );
	}
	
	private com.quasistellar.hollowdungeon.items.Item prize(Level level ) {
		com.quasistellar.hollowdungeon.items.Generator.Category cat = prizeClasses.remove(0);
		Item prize = null;
		do {
			prize = Generator.random(cat);
		} while (prize == null || Challenges.isItemBlocked(prize));
		return prize;
	}
	
	private ArrayList<com.quasistellar.hollowdungeon.items.Generator.Category> prizeClasses = new ArrayList<>(
			Arrays.asList(Generator.Category.WAND,
					Generator.Category.RING,
					com.quasistellar.hollowdungeon.items.Generator.Category.ARTIFACT));
}
