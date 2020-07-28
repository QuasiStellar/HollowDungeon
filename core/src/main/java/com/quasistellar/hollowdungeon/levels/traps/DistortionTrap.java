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

package com.quasistellar.hollowdungeon.levels.traps;

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.RatKing;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.mobs.Acidic;
import com.quasistellar.hollowdungeon.actors.mobs.Albino;
import com.quasistellar.hollowdungeon.actors.mobs.ArmoredBrute;
import com.quasistellar.hollowdungeon.actors.mobs.Bandit;
import com.quasistellar.hollowdungeon.actors.mobs.Bestiary;
import com.quasistellar.hollowdungeon.actors.mobs.CausticSlime;
import com.quasistellar.hollowdungeon.actors.mobs.DM201;
import com.quasistellar.hollowdungeon.actors.mobs.Elemental;
import com.quasistellar.hollowdungeon.actors.mobs.Mimic;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.Piranha;
import com.quasistellar.hollowdungeon.actors.mobs.Senior;
import com.quasistellar.hollowdungeon.actors.mobs.Wraith;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class DistortionTrap extends Trap{

	private static final float DELAY = 2f;

	{
		color = TEAL;
		shape = LARGE_DOT;
	}

	private static final ArrayList<Class<?extends Mob>> RARE = new ArrayList<>(Arrays.asList(
			Albino.class, CausticSlime.class,
			Bandit.class,
			ArmoredBrute.class, DM201.class,
			Senior.class,
			Acidic.class));

	@Override
	public void activate() {

		int nMobs = 3;
		if (Random.Int( 2 ) == 0) {
			nMobs++;
			if (Random.Int( 2 ) == 0) {
				nMobs++;
			}
		}

		ArrayList<Integer> candidates = new ArrayList<>();

		for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
			int p = pos + PathFinder.NEIGHBOURS8[i];
			if (Actor.findChar( p ) == null && (Dungeon.level.passable[p] || Dungeon.level.avoid[p])) {
				candidates.add( p );
			}
		}

		ArrayList<Integer> respawnPoints = new ArrayList<>();

		while (nMobs > 0 && candidates.size() > 0) {
			int index = Random.index( candidates );

			respawnPoints.add( candidates.remove( index ) );
			nMobs--;
		}

		ArrayList<Mob> mobs = new ArrayList<>();

		int summoned = 0;
		for (Integer point : respawnPoints) {
			summoned++;
			Mob mob;
			switch (summoned){
				case 1:
					if (Dungeon.depth != 5 && Random.Int(100) == 0){
						mob = new RatKing();
						break;
					}
				case 3: case 5 : default:
					int floor;
					do {
						floor = Random.Int(25);
					} while( Dungeon.bossLevel(floor));
					mob = Reflection.newInstance(Bestiary.getMobRotation(floor).get(0));
					break;
				case 2:
					switch (Random.Int(4)){
						case 0: default:
							Wraith.spawnAt(point);
							continue; //wraiths spawn themselves, no need to do more
						case 1:
							//yes it's intended that these are likely to die right away
							mob = new Piranha();
							break;
						case 2:
							mob = Mimic.spawnAt(point, new ArrayList<>());
							((Mimic)mob).stopHiding();
							mob.alignment = Char.Alignment.ENEMY;
							break;
					}
					break;
				case 4:
					mob = Reflection.newInstance(Random.element(RARE));
					break;
			}

			if (Char.hasProp(mob, com.quasistellar.hollowdungeon.actors.Char.Property.LARGE) && !Dungeon.level.openSpace[point]){
				continue;
			}

			mob.state = mob.WANDERING;
			mob.pos = point;
			GameScene.add(mob, DELAY);
			mobs.add(mob);
		}

		//important to process the visuals and pressing of cells last, so spawned mobs have a chance to occupy cells first
		Trap t;
		for (Mob mob : mobs){
			//manually trigger traps first to avoid sfx spam
			if ((t = Dungeon.level.traps.get(mob.pos)) != null && t.active){
				t.disarm();
				t.reveal();
				t.activate();
			}
			ScrollOfTeleportation.appear(mob, mob.pos);
			com.quasistellar.hollowdungeon.Dungeon.level.occupyCell(mob);
		}

	}
}
