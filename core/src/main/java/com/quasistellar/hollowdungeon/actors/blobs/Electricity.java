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

package com.quasistellar.hollowdungeon.actors.blobs;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.effects.particles.SparkParticle;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Electricity extends Blob {
	
	{
		//acts after mobs, to give them a chance to resist paralysis
		actPriority = Actor.MOB_PRIO - 1;
	}
	
	private boolean[] water;
	
	@Override
	protected void evolve() {
		
		water = Dungeon.level.water;
		int cell;
		
		//spread first..
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j* Dungeon.level.width();
				
				if (cur[cell] > 0) {
					spreadFromCell(cell, cur[cell]);
				}
			}
		}
		
		//..then decrement/shock
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j* Dungeon.level.width();
				if (cur[cell] > 0) {
					Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar( cell );
					if (ch != null && !ch.isImmune(this.getClass())) {
						Buff.prolong( ch, Paralysis.class, 1f);
						if (cur[cell] % 2 == 1) {
							ch.damage(1, this);
							if (!ch.isAlive() && ch == Dungeon.hero){
								Dungeon.fail( getClass() );
								GLog.n( Messages.get(this, "ondeath") );
							}
						}
					}
					
					off[cell] = cur[cell] - 1;
					volume += off[cell];
				} else {
					off[cell] = 0;
				}
			}
		}
		
	}
	
	private void spreadFromCell( int cell, int power ){
		if (cur[cell] == 0) {
			area.union(cell % Dungeon.level.width(), cell / com.quasistellar.hollowdungeon.Dungeon.level.width());
		}
		cur[cell] = Math.max(cur[cell], power);
		
		for (int c : PathFinder.NEIGHBOURS4){
			if (water[cell + c] && cur[cell + c] < power){
				spreadFromCell(cell + c, power);
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( SparkParticle.FACTORY, 0.05f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
	
}
