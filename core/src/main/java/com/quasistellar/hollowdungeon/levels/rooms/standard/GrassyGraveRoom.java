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

import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.Geo;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class GrassyGraveRoom extends StandardRoom {
	
	@Override
	public void paint(Level level) {
		
		Painter.fill( level, this, Terrain.WALL );
		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}
		
		Painter.fill( level, this, 1 , com.quasistellar.hollowdungeon.levels.Terrain.GRASS );
		
		int w = width() - 2;
		int h = height() - 2;
		int nGraves = Math.max( w, h ) / 2;
		
		int index = Random.Int( nGraves );
		
		int shift = Random.Int( 2 );
		for (int i=0; i < nGraves; i++) {
			int pos = w > h ?
					left + 1 + shift + i * 2 + (top + 2 + Random.Int( h-2 )) * level.width() :
					(left + 2 + Random.Int( w-2 )) + (top + 1 + shift + i * 2) * level.width();
			level.drop( i == index ? Generator.random() : new Geo().random(), pos ).type = Heap.Type.TOMB;
		}
	}
}
