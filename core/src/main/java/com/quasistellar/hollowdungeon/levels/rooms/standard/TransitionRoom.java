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

package com.quasistellar.hollowdungeon.levels.rooms.standard;

import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.quasistellar.hollowdungeon.levels.rooms.Room;
import com.watabou.utils.Random;

public class TransitionRoom extends StandardRoom {

	@Override
	public int minWidth() {
		return Math.max(super.minWidth(), 6);
	}

	@Override
	public int minHeight() {
		return Math.max(super.minHeight(), 6);
	}

	public void paint(com.quasistellar.hollowdungeon.levels.Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		for (Room.Door door : connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}

		level.transition = level.pointToCell(random( 3 ));
		Painter.set( level, level.transition, Terrain.UNLOCKED_EXIT );
		Painter.set( level, level.transition + 1, Terrain.CHASM );
		Painter.set( level, level.transition - 1, Terrain.CHASM );
		Painter.set( level, level.transition + level.width(), Terrain.CHASM );
		Painter.set( level, level.transition - level.width(), Terrain.CHASM );
		Painter.set( level, level.transition + level.width() + 1, Terrain.CHASM );
		Painter.set( level, level.transition + level.width() - 1, Terrain.CHASM );
		Painter.set( level, level.transition - level.width() + 1, Terrain.CHASM );
		Painter.set( level, level.transition - level.width() - 1, Terrain.CHASM );
	}
	
}
