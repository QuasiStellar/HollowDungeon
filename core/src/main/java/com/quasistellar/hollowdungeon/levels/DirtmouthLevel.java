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
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.Elderbug;
import com.quasistellar.hollowdungeon.effects.particles.FlameParticle;
import com.quasistellar.hollowdungeon.tiles.CustomTilemap;
import com.quasistellar.hollowdungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.PointF;

public class DirtmouthLevel extends Level {

	private static final int SIZE = 64;
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}
	
	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_CAVES;
	}
	
	@Override
	public String waterTex() {
		return Assets.Environment.WATER_HALLS;
	}
	
	@Override
	protected boolean build() {
		
		setSize(64, 64);

		map = MAP_TOWN.clone();

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
		
		entrance = 323;
		exit = 2284;
		transition = 3516;
		
		return true;
	}
	
	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {
		Elderbug elderbug = new Elderbug();
		elderbug.pos = 19 * 64 + 35;
		mobs.add(elderbug);
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
	//private static final int X = Terrain.EXIT;
	private static final int M = Terrain.EMPTY_SP;
	private static final int Q = Terrain.EXIT;
	private static final int E = Terrain.ENTRANCE;
	private static final int S = Terrain.STATUE;

	private static final int[] MAP_TOWN =
	{       W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,M,M,M,M,M,M,M,M,M,M,M,M,e,e,e,M,M,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,E,M,M,M,M,M,M,M,M,M,e,e,M,M,M,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,M,M,M,M,M,M,M,e,M,M,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,W,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,W,W,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,
			W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,W,W,e,e,e,e,e,e,e,W,W,W,W,W,W,W,
			W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,L,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,L,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,W,W,W,W,
			W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,W,
			W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,
			W,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,e,e,e,e,e,e,e,e,W,W,W,W,e,e,e,e,e,e,e,W,e,e,e,e,e,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,W,W,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,W,W,e,e,e,e,e,e,e,e,L,e,e,e,e,e,e,W,e,e,e,e,e,S,e,e,e,e,e,e,e,e,W,W,
			W,e,e,e,e,e,e,e,e,W,e,e,W,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,L,e,e,e,e,e,e,e,e,W,W,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,
			W,W,e,e,e,e,e,e,e,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,
			W,W,e,e,e,e,e,e,e,e,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,
			W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,S,e,e,e,e,W,W,
			W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,
			W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,W,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,S,e,e,e,e,e,e,e,W,W,
			W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,W,e,e,e,e,e,e,e,e,W,W,W,e,L,e,e,e,e,e,e,e,M,M,M,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,W,e,e,e,e,e,e,e,e,W,e,e,e,W,W,e,e,e,e,e,e,M,Q,M,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,W,e,e,e,e,e,e,e,e,W,W,e,e,e,W,e,e,e,e,e,e,M,M,M,e,e,e,e,e,S,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,W,e,W,W,e,e,e,e,e,e,e,e,e,e,e,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,S,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,S,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,S,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,M,e,M,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,M,M,e,M,M,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,M,M,e,M,M,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e,M,e,M,e,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,L,W,W,e,e,e,e,e,e,e,e,e,e,e,e,e,e,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,W,W,W,W,W,e,e,e,e,e,e,W,W,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,W,W,W,e,e,e,e,e,e,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,e,W,W,e,e,e,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,e,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,e,e,e,e,e,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,
			W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W
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
