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

package com.quasistellar.hollowdungeon.levels.painters;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Cornifer;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.rooms.Room;
import com.quasistellar.hollowdungeon.levels.rooms.standard.EntranceRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class HallownestPainter extends RegularPainter {
	
	@Override
	protected void decorate(Level level, ArrayList<Room> rooms) {

		if (Dungeon.location.equals("Forgotten Crossroads 2"))
			for (Room r : rooms) {
				if (r instanceof EntranceRoom) {
					Cornifer npc = new Cornifer();
					boolean validPos;
					//Do not spawn wandmaker on the entrance, a trap, or in front of a door.
					do {
						validPos = true;
						npc.pos = level.pointToCell(r.random());
						if (level.trueDistance( npc.pos, level.entrance ) <= 1){
							validPos = false;
						}
						for (Point door : r.connected.values()){
							if (level.trueDistance( npc.pos, level.pointToCell( door ) ) <= 1){
								validPos = false;
							}
						}
						if (level.traps.get(npc.pos) != null){
							validPos = false;
						}
					} while (!validPos);
					level.mobs.add( npc );
					break;
				}
			}

		int[] map = level.map;
		int w = level.width();
		int l = level.length();
		
		for (int i=0; i < w; i++) {
			if (map[i] == Terrain.WALL &&
					map[i + w] == Terrain.WATER &&
					Random.Int( 4 ) == 0) {
				
				map[i] = Terrain.WALL_DECO;
			}
		}
		
		for (int i=w; i < l - w; i++) {
			if (map[i] == Terrain.WALL &&
					map[i - w] == Terrain.WALL &&
					map[i + w] == Terrain.WATER &&
					Random.Int( 2 ) == 0) {
				
				map[i] = Terrain.WALL_DECO;
			}
		}
		
		for (int i=w + 1; i < l - w - 1; i++) {
			if (map[i] == Terrain.EMPTY) {
				
				int count =
						(map[i + 1] == Terrain.WALL ? 1 : 0) +
								(map[i - 1] == Terrain.WALL ? 1 : 0) +
								(map[i + w] == Terrain.WALL ? 1 : 0) +
								(map[i - w] == Terrain.WALL ? 1 : 0);
				
				if (Random.Int( 16 ) < count * count) {
					map[i] = com.quasistellar.hollowdungeon.levels.Terrain.EMPTY_DECO;
				}
			}
			if (map[i] == Terrain.DOOR) {
				map[i] = Terrain.EMPTY;
			}
		}
	}
}
