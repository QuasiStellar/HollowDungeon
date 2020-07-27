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

package com.quasistellar.shatteredpixeldungeon.levels.rooms.sewerboss;

import com.quasistellar.shatteredpixeldungeon.levels.Level;
import com.quasistellar.shatteredpixeldungeon.levels.Terrain;
import com.quasistellar.shatteredpixeldungeon.levels.rooms.connection.PerimeterRoom;
import com.quasistellar.shatteredpixeldungeon.actors.mobs.Goo;
import com.quasistellar.shatteredpixeldungeon.levels.painters.Painter;

public class ThinPillarsGooRoom extends GooBossRoom {
	
	@Override
	public void paint(Level level) {
		
		Painter.fill( level, this, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL );
		Painter.fill( level, this, 1 , com.quasistellar.shatteredpixeldungeon.levels.Terrain.WATER );
		
		int pillarW = (width() == 14 ? 4: 2) + width()%2;
		int pillarH = (height() == 14 ? 4: 2) + height()%2;
		
		if (height() < 12){
			Painter.fill(level, left + (width()-pillarW)/2, top+2, pillarW, 1, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
			Painter.fill(level, left + (width()-pillarW)/2, bottom-2, pillarW, 1, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
		} else {
			Painter.fill(level, left + (width()-pillarW)/2, top+3, pillarW, 1, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
			Painter.fill(level, left + (width()-pillarW)/2, bottom-3, pillarW, 1, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
		}
		
		if (width() < 12){
			Painter.fill(level, left + 2, top + (height() - pillarH)/2, 1, pillarH, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
			Painter.fill(level, right - 2, top + (height() - pillarH)/2, 1, pillarH, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
		} else {
			Painter.fill(level, left + 3, top + (height() - pillarH)/2, 1, pillarH, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
			Painter.fill(level, right - 3, top + (height() - pillarH)/2, 1, pillarH, com.quasistellar.shatteredpixeldungeon.levels.Terrain.WALL);
		}
		
		PerimeterRoom.fillPerimiterPaths(level, this, Terrain.EMPTY_SP);
		
		for (Door door : connected.values()) {
			door.set(Door.Type.REGULAR);
		}
		
		setupGooNest(level);
		
		Goo boss = new Goo();
		boss.pos = level.pointToCell(center());
		level.mobs.add( boss );
		
	}
	
}
