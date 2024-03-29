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
import com.quasistellar.hollowdungeon.plants.Firebloom;
import com.quasistellar.hollowdungeon.plants.Plant;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class PlantsRoom extends StandardRoom {
	
	@Override
	public int minWidth() {
		return Math.max(super.minWidth(), 5);
	}
	
	@Override
	public int minHeight() {
		return Math.max(super.minHeight(), 5);
	}
	
	@Override
	public float[] sizeCatProbs() {
		return new float[]{3, 1, 0};
	}
	
	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.GRASS );
		Painter.fill( level, this, 2, Terrain.HIGH_GRASS );
		
		if (Math.min(width(), height()) >= 7){
			Painter.fill( level, this, 3, Terrain.GRASS );
		}
		
		Point center = center();
		
		//place at least 2 plants for rooms with at least 9 in one dimensions
		if (Math.max(width(), height()) >= 9){
			
			//place 4 plants for very large rooms
			if (Math.min(width(), height()) >= 11) {
				Painter.drawLine(level, new Point(left+2, center.y), new Point(right-2, center.y), Terrain.HIGH_GRASS);
				Painter.drawLine(level, new Point(center.x, top+2), new Point(center.x, bottom-2), Terrain.HIGH_GRASS);
			
			//place 2 plants otherwise
			//left/right
			} else if (width() > height() || (width() == height() && Random.Int(2) == 0)){
				Painter.drawLine(level, new Point(center.x, top+2), new Point(center.x, bottom-2), Terrain.HIGH_GRASS);
			
			//top/bottom
			} else {
				Painter.drawLine(level, new Point(left+2, center.y), new Point(right-2, center.y), com.quasistellar.hollowdungeon.levels.Terrain.HIGH_GRASS);
			
			}
			
		//place just one plant for smaller sized rooms
		} else {
		}
		
		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}
	}
}
