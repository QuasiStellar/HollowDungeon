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

package com.quasistellar.hollowdungeon.levels.rooms.connection;

import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.features.Maze;
import com.quasistellar.hollowdungeon.levels.painters.Painter;

public class MazeConnectionRoom extends ConnectionRoom {
	
	@Override
	public void paint(Level level) {
		Painter.fill(level, this, 1, Terrain.EMPTY);
		
		//true = space, false = wall
		Maze.allowDiagonals = false;
		boolean[][] maze = Maze.generate(this);
		
		Painter.fill(level, this, 1, Terrain.EMPTY);
		for (int x = 0; x < maze.length; x++)
			for (int y = 0; y < maze[0].length; y++) {
				if (maze[x][y] == Maze.FILLED) {
					Painter.fill(level, x + left, y + top, 1, 1, com.quasistellar.hollowdungeon.levels.Terrain.WALL);
				}
			}
		
		for (Door door : connected.values()) {
			door.set( Door.Type.HIDDEN );
		}
	}
	
	@Override
	public int maxConnections(int direction) {
		return 2;
	}
}
