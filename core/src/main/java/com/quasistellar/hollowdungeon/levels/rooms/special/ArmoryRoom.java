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

package com.quasistellar.hollowdungeon.levels.rooms.special;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.Ankh;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.items.keys.IronKey;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class ArmoryRoom extends SpecialRoom {

	public void paint( com.quasistellar.hollowdungeon.levels.Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		
		Door entrance = entrance();
		Point statue = null;
		if (entrance.x == left) {
			statue = new Point( right-1, Random.Int( 2 ) == 0 ? top+1 : bottom-1 );
		} else if (entrance.x == right) {
			statue = new Point( left+1, Random.Int( 2 ) == 0 ? top+1 : bottom-1 );
		} else if (entrance.y == top) {
			statue = new Point( Random.Int( 2 ) == 0 ? left+1 : right-1, bottom-1 );
		} else if (entrance.y == bottom) {
			statue = new Point( Random.Int( 2 ) == 0 ? left+1 : right-1, top+1 );
		}
		if (statue != null) {
			Painter.set( level, statue, Terrain.STATUE );
		}
		
		entrance.set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey( Dungeon.location ) );
	}
}
