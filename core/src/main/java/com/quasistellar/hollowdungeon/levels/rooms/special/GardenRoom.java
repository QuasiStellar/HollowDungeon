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
import com.quasistellar.hollowdungeon.plants.BlandfruitBush;
import com.quasistellar.hollowdungeon.plants.Sungrass;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.actors.blobs.Foliage;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class GardenRoom extends SpecialRoom {

	public void paint( com.quasistellar.hollowdungeon.levels.Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.HIGH_GRASS );
		Painter.fill( level, this, 2, com.quasistellar.hollowdungeon.levels.Terrain.GRASS );
		
		entrance().set( Door.Type.REGULAR );

		if (Random.Int(2) == 0){
			level.plant(new com.quasistellar.hollowdungeon.plants.Sungrass.Seed(), plantPos( level ));
		}
		
		Foliage light = (Foliage)level.blobs.get( Foliage.class );
		if (light == null) {
			light = new Foliage();
		}
		for (int i=top + 1; i < bottom; i++) {
			for (int j=left + 1; j < right; j++) {
				light.seed( level, j + level.width() * i, 1 );
			}
		}
		level.blobs.put( Foliage.class, light );
	}
	
	private int plantPos( Level level ){
		int pos;
		do{
			pos = level.pointToCell(random());
		} while (level.plants.get(pos) != null);
		return pos;
	}
}
