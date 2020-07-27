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

import com.quasistellar.hollowdungeon.effects.particles.ShadowParticle;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.WraithSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Wraith extends Mob {

	private static final float SPAWN_DELAY	= 2f;
	
	private int level;
	
	{
		spriteClass = WraithSprite.class;
		
		HP = HT = 1;
		
		flying = true;

		properties.add(Char.Property.UNDEAD);
	}
	
	private static final String LEVEL = "level";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEVEL, level );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		level = bundle.getInt( LEVEL );
		adjustStats( level );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1 + level/2, 2 + level );
	}

	public void adjustStats( int level ) {
		this.level = level;
		enemySeen = true;
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		state = WANDERING;
		return true;
	}
	
	public static void spawnAround( int pos ) {
		for (int n : PathFinder.NEIGHBOURS4) {
			int cell = pos + n;
			if (Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) {
				spawnAt( cell );
			}
		}
	}
	
	public static Wraith spawnAt( int pos ) {
		if (Dungeon.level.passable[pos] && com.quasistellar.hollowdungeon.actors.Actor.findChar( pos ) == null) {
			
			Wraith w = new Wraith();
			w.adjustStats( com.quasistellar.hollowdungeon.Dungeon.depth );
			w.pos = pos;
			w.state = w.HUNTING;
			GameScene.add( w, SPAWN_DELAY );
			
			w.sprite.alpha( 0 );
			w.sprite.parent.add( new AlphaTweener( w.sprite, 1, 0.5f ) );
			
			w.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
			
			return w;
		} else {
			return null;
		}
	}

}
