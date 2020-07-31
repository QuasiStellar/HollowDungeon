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

import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.painters.Painter;

public class PoolRoom extends SpecialRoom {

	private static final int NPIRANHAS	= 3;
	
	@Override
	public int minWidth() {
		return 6;
	}
	
	@Override
	public int minHeight() {
		return 6;
	}
	
	public void paint(com.quasistellar.hollowdungeon.levels.Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.WATER );
		
		Door door = entrance();
		door.set( Door.Type.REGULAR );

		int x = -1;
		int y = -1;
		if (door.x == left) {
			
			x = right - 1;
			y = top + height() / 2;
			Painter.fill(level, left+1, top+1, 1, height()-2, Terrain.EMPTY_SP);
			
		} else if (door.x == right) {
			
			x = left + 1;
			y = top + height() / 2;
			Painter.fill(level, right-1, top+1, 1, height()-2, Terrain.EMPTY_SP);
			
		} else if (door.y == top) {
			
			x = left + width() / 2;
			y = bottom - 1;
			Painter.fill(level, left+1, top+1, width()-2, 1, Terrain.EMPTY_SP);
			
		} else if (door.y == bottom) {
			
			x = left + width() / 2;
			y = top + 1;
			Painter.fill(level, left+1, bottom-1, width()-2, 1, Terrain.EMPTY_SP);
			
		}
	}
}
