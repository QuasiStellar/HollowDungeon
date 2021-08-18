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

package com.quasistellar.hollowdungeon.levels;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.Vengefly;
import com.quasistellar.hollowdungeon.actors.mobs.WanderingHusk;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Elderbug;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.TabletFocus;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.TabletSearch;

public class KingspassLevel extends Level {

	private static final int SIZE = 32;
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}
	
	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_SEWERS;
	}
	
	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
	}
	
	@Override
	protected boolean build() {
		
		setSize(SIZE, SIZE);

		map = MAP_PASS.clone();

//		CustomTilemap vis = new townBehind();
//		vis.pos(0, 0);
//		customTiles.add(vis);
//		//((GameScene) RPD.scene()).addCustomTile(vis);
//
//		vis = new townAbove();
//		vis.pos(0, 0);
//		customWalls.add(vis);
//		//((GameScene) RPD.scene()).addCustomWall(vis);

		buildFlagMaps();
		cleanWalls();
		
		entrance = 71;
		exit = 891;
		transition = 0;
		
		return true;
	}
	
	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
		TabletSearch tabletSearch = new TabletSearch();
		tabletSearch.pos = 8 * 32 + 23;
		mobs.add(tabletSearch);

		TabletFocus tabletFocus = new TabletFocus();
		tabletFocus.pos = 27 * 32 + 21;
		mobs.add(tabletFocus);

		WanderingHusk husk1 = new WanderingHusk();
		husk1.pos = 15 * 32 + 16;
		mobs.add(husk1);

		WanderingHusk husk2 = new WanderingHusk();
		husk2.pos = 18 * 32 + 20;
		mobs.add(husk2);

		Vengefly vengefly = new Vengefly();
		vengefly.pos = 27 * 32 + 10;
		mobs.add(vengefly);
	}

	public Actor respawner() {
		return null;
	}

	@Override
	protected void createItems() {
	}
	
	@Override
	public int randomRespawnCell( Char ch ) {
		return entrance-width();
	}

	private static final int W = Terrain.WALL;
	private static final int D = Terrain.DOOR;
	private static final int e = Terrain.EMPTY;
	private static final int L = Terrain.LOCKED_DOOR;
	private static final int R = Terrain.SECRET_DOOR;
	private static final int M = Terrain.EMPTY_SP;
	private static final int Q = Terrain.EXIT;
	private static final int E = Terrain.ENTRANCE;
	private static final int S = Terrain.STATUE;
	private static final int C = Terrain.CHASM;

	private static final int[] MAP_PASS =
	{       W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,e,E,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,W,W,W,W,W,W,W,
			W,W,W,W,W,R,W,C,C,C,C,C,C,W,W,W,W,W,W,W,e,e,e,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,C,C,C,e,e,e,e,C,C,W,W,W,W,W,e,e,e,e,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,C,e,e,e,e,e,e,e,C,W,W,W,W,W,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,C,C,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,C,C,C,e,e,e,e,e,C,C,W,W,W,W,W,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,W,C,C,C,C,C,C,C,C,W,W,W,W,W,W,e,e,e,e,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,C,C,C,W,W,W,W,W,W,W,W,W,e,e,e,e,e,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,e,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,W,W,W,W,W,W,W,W,W,
			W,W,W,e,W,W,W,e,W,W,W,W,W,W,W,e,e,e,W,W,W,D,W,W,W,W,W,W,W,W,W,W,
			W,W,W,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,
			W,W,W,e,e,e,e,e,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,
			W,W,W,e,W,W,W,e,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,C,W,W,W,W,W,W,W,W,
			W,W,W,e,W,W,W,e,W,W,W,W,W,W,C,C,C,e,e,e,e,e,C,C,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,e,W,W,W,W,W,W,W,W,C,C,C,e,e,e,C,W,W,W,W,W,W,W,W,W,
			W,W,e,e,W,W,W,e,W,W,W,W,W,W,W,W,W,W,W,W,e,e,W,W,W,W,W,W,W,W,W,W,
			W,e,e,e,e,D,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,e,e,e,e,e,W,W,W,e,C,C,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,e,e,e,e,e,e,e,e,e,C,C,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,C,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,e,e,e,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,C,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,Q,W,
			W,W,W,W,W,C,C,C,e,e,e,e,e,e,W,W,W,W,W,W,e,e,e,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,C,C,C,C,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W
	};

//	public static class townBehind extends CustomTilemap {
//
//		{
//			texture = Assets.TOWN_BEHIND;
//
//			tileW = 64;
//			tileH = 64;
//		}
//
//		final int TEX_WIDTH = 64*16;
//
//		@Override
//		public Tilemap create() {
//
//			Tilemap v = super.create();
//
//			int[] data = mapSimpleImage(0, 0, TEX_WIDTH);
//
//			v.map(data, tileW);
//			return v;
//		}
//
//	}
//
//	public static class townAbove extends CustomTilemap {
//
//		{
//			texture = Assets.TOWN_ABOVE;
//
//			tileW = 64;
//			tileH = 64;
//		}
//
//		final int TEX_WIDTH = 64*16;
//
//		@Override
//		public Tilemap create() {
//
//			Tilemap v = super.create();
//
//			int[] data = mapSimpleImage(0, 0, TEX_WIDTH);
//
//			v.map(data, tileW);
//			return v;
//		}
//
//	}
}
