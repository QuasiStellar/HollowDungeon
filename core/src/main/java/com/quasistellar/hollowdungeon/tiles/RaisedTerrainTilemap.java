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

package com.quasistellar.hollowdungeon.tiles;

import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.Dungeon;

public class RaisedTerrainTilemap extends DungeonTilemap {
	
	public RaisedTerrainTilemap() {
		super(Dungeon.level.tilesTex());
		map( Dungeon.level.map, Dungeon.level.width() );
	}
	
	@Override
	protected int getTileVisual(int pos, int tile, boolean flat) {
		
		if (flat) return -1;
		
		if (tile == Terrain.HIGH_GRASS){
			return com.quasistellar.hollowdungeon.tiles.DungeonTileSheet.getVisualWithAlts(
					com.quasistellar.hollowdungeon.tiles.DungeonTileSheet.RAISED_HIGH_GRASS,
					pos) + 2;
		} else if (tile == com.quasistellar.hollowdungeon.levels.Terrain.FURROWED_GRASS){
			return com.quasistellar.hollowdungeon.tiles.DungeonTileSheet.getVisualWithAlts(
					DungeonTileSheet.RAISED_FURROWED_GRASS,
					pos) + 2;
		}
		
		
		return -1;
	}
}
