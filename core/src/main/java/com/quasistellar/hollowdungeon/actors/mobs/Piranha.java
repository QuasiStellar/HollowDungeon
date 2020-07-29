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

package com.quasistellar.hollowdungeon.actors.mobs;

import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.Statistics;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.sprites.PiranhaSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Piranha extends com.quasistellar.hollowdungeon.actors.mobs.Mob {
	
	{
		spriteClass = PiranhaSprite.class;

		baseSpeed = 2f;
		
		SLEEPING = new Sleeping();
		WANDERING = new Wandering();
		HUNTING = new Hunting();
		
		state = SLEEPING;
		
		properties.add(Char.Property.BLOB_IMMUNE);
	}
	
	public Piranha() {
		super();
		
		HP = HT = 10;
	}
	
	@Override
	protected boolean act() {
		
		if (!Dungeon.level.water[pos]) {
			die( null );
			return true;
		} else {
			return super.act();
		}
	}
	
	@Override
	public int damageRoll() {
		return 2;
	}
	
	@Override
	public void die( Object cause ) {
		super.die( cause );
		
		Statistics.piranhasKilled++;
		Badges.validatePiranhasKilled();
	}

	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		
		if (rooted) {
			return false;
		}
		
		int step = Dungeon.findStep( this, target, Dungeon.level.water, fieldOfView, true );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected boolean getFurther( int target ) {
		int step = Dungeon.flee( this, target, Dungeon.level.water, fieldOfView, true );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	{
		immunities.add( Burning.class );
	}
	
	//if there is not a path to the enemy, piranhas act as if they can't see them
	private class Sleeping extends com.quasistellar.hollowdungeon.actors.mobs.Mob.Sleeping {
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV) {
				PathFinder.buildDistanceMap(enemy.pos, Dungeon.level.water, viewDistance);
				enemyInFOV = PathFinder.distance[pos] != Integer.MAX_VALUE;
			}
			
			return super.act(enemyInFOV, justAlerted);
		}
	}
	
	private class Wandering extends com.quasistellar.hollowdungeon.actors.mobs.Mob.Wandering {
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV) {
				PathFinder.buildDistanceMap(enemy.pos, Dungeon.level.water, viewDistance);
				enemyInFOV = PathFinder.distance[pos] != Integer.MAX_VALUE;
			}
			
			return super.act(enemyInFOV, justAlerted);
		}
	}
	
	private class Hunting extends Mob.Hunting {
		
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV) {
				PathFinder.buildDistanceMap(enemy.pos, com.quasistellar.hollowdungeon.Dungeon.level.water, viewDistance);
				enemyInFOV = PathFinder.distance[pos] != Integer.MAX_VALUE;
			}
			
			return super.act(enemyInFOV, justAlerted);
		}
	}
}
