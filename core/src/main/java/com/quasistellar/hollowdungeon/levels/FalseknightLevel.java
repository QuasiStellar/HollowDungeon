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
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnight1;
import com.quasistellar.hollowdungeon.actors.mobs.FalseKnight3;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.Vengefly;
import com.quasistellar.hollowdungeon.actors.mobs.WanderingHusk;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.TabletFocus;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.TabletSearch;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;

public class FalseknightLevel extends Level {

	private static final int SIZE = 32;
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;
	}

	public enum State {
		START,
		FIGHT,
		BREAK,
		WON
	}

	private State state;

	public State state(){
		return state;
	}

	private static final String STATE	        = "state";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( STATE, state );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		state = bundle.getEnum( STATE, State.class );
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
		
		setSize(20, 20);

		state = State.START;
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
		
		entrance = 42;
		exit = 357;
		transition = 0;
		
		return true;
	}

	@Override
	public void occupyCell(Char ch) {
		super.occupyCell(ch);

		if (ch == Dungeon.hero){
			switch (state){
				case START:
					if (cellToPoint(ch.pos).y + cellToPoint(ch.pos).x > 23){
						progress();
					}
					break;
			}
		}
	}

	public void progress(){
		//moving to the beginning of the fight
		if (state == State.START) {
			seal();
			set(50, Terrain.LOCKED_DOOR);
			GameScene.updateMap(50);
			set(182, Terrain.LOCKED_DOOR);
			GameScene.updateMap(182);
			set(256, Terrain.LOCKED_DOOR);
			GameScene.updateMap(256);
			set(332, Terrain.LOCKED_DOOR);
			GameScene.updateMap(332);

			FalseKnight1 fk1 = new FalseKnight1();
			fk1.state = fk1.HUNTING;
			fk1.pos = 189; //in the middle of the fight room
			GameScene.add(fk1);
			fk1.notice();
			Music.INSTANCE.play( Assets.Music.SURFACE, true );

			state = State.FIGHT;
		} else if (state == State.FIGHT) {
			set(314, Terrain.SECRET_DOOR);
			state = State.BREAK;
		} else {
			Music.INSTANCE.stop();
			state = State.WON;
		}
	}

	@Override
	public void unseal() {
		super.unseal();
		set(50, Terrain.DOOR);
		GameScene.updateMap(50);
		set(182, Terrain.DOOR);
		GameScene.updateMap(182);
		set(256, Terrain.DOOR);
		GameScene.updateMap(256);
		set(332, Terrain.DOOR);
		GameScene.updateMap(332);
	}

	@Override
	public Mob createMob() {
		return null;
	}

	@Override
	protected void createMobs() {

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
	{       W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W,
			W, e, e, e, W, W, e, e, e, e, e, W, W, W, W, W, W, W, W, W,
			W, e, E, e, e, e, e, W, W, W, D, W, W, W, W, W, W, W, W, W,
			W, e, e, e, W, W, W, W, W, e, e, W, W, W, W, W, W, W, W, W,
			W, e, W, W, W, W, W, e, e, e, e, e, e, W, W, W, W, W, W, W,
			W, e, W, W, W, e, e, e, e, e, e, e, e, e, e, W, W, W, W, W,
			W, e, W, W, W, e, e, e, e, e, e, e, e, e, e, W, W, W, W, W,
			W, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, W, W, W, W,
			W, e, W, W, e, e, e, e, e, e, e, e, e, e, e, e, W, W, W, W,
			W, e, D, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, W,
			W, W, W, e, e, e, e, e, e, e, e, e, e, e, e, e, e, W, W, W,
			W, W, W, W, e, e, e, e, e, e, e, e, e, e, e, e, W, W, W, W,
			W, W, W, W, e, e, e, e, e, e, e, e, e, e, e, e, D, e, W, W,
			W, W, W, W, W, e, e, e, e, e, e, e, e, e, e, W, W, e, W, W,
			W, W, W, W, W, e, e, e, e, e, e, e, e, e, e, W, W, e, W, W,
			W, W, W, W, W, W, W, e, e, e, e, e, e, W, W, W, W, e, W, W,
			W, W, W, W, W, W, W, W, W, e, e, W, D, W, e, W, e, e, e, W,
			W, W, W, W, W, W, W, W, W, W, W, W, e, e, e, W, e, Q, e, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, e, e, e, e, e, W,
			W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W
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
