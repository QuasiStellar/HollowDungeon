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

package com.quasistellar.hollowdungeon.levels.rooms.secret;

import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.painters.Painter;
import com.watabou.utils.Point;

public class SecretHoneypotRoom extends SecretRoom {
	
	@Override
	public void paint(com.quasistellar.hollowdungeon.levels.Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill(level, this, 1, com.quasistellar.hollowdungeon.levels.Terrain.EMPTY );
		
		Point brokenPotPos = center();
		
		brokenPotPos.x = (brokenPotPos.x + entrance().x) / 2;
		brokenPotPos.y = (brokenPotPos.y + entrance().y) / 2;
		
		entrance().set(Door.Type.HIDDEN);
	}
	
	private void placeItem(Item item, Level level){
		int itemPos;
		do {
			itemPos = level.pointToCell(random());
		} while (level.heaps.get(itemPos) != null);
		
		level.drop(item, itemPos);
	}
}
