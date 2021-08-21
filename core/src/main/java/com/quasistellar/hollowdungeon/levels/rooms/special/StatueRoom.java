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
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.items.keys.IronKey;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class StatueRoom extends SpecialRoom {

	public void paint( Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		Point c = center();
		int cx = c.x;
		int cy = c.y;
		
		Door door = entrance();
		
		door.set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey( Dungeon.location ) );
		
		if (door.x == left) {
			
			Painter.fill( level, right - 1, top + 1, 1, height() - 2 , Terrain.STATUE );
			cx = right - 2;
			
		} else if (door.x == right) {
			
			Painter.fill( level, left + 1, top + 1, 1, height() - 2 , Terrain.STATUE );
			cx = left + 2;
			
		} else if (door.y == top) {
			
			Painter.fill( level, left + 1, bottom - 1, width() - 2, 1 , Terrain.STATUE );
			cy = bottom - 2;
			
		} else if (door.y == bottom) {
			
			Painter.fill( level, left + 1, top + 1, width() - 2, 1 , com.quasistellar.hollowdungeon.levels.Terrain.STATUE );
			cy = top + 2;
			
		}
	}
}
