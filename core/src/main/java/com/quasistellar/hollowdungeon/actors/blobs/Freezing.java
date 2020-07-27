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

package com.quasistellar.hollowdungeon.actors.blobs;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Chill;
import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Frost;
import com.quasistellar.hollowdungeon.effects.particles.SnowParticle;
import com.quasistellar.hollowdungeon.messages.Messages;

public class Freezing extends Blob {
	
	@Override
	protected void evolve() {
		
		int cell;
		
		com.quasistellar.hollowdungeon.actors.blobs.Fire fire = (com.quasistellar.hollowdungeon.actors.blobs.Fire) Dungeon.level.blobs.get( com.quasistellar.hollowdungeon.actors.blobs.Fire.class );
		
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j* Dungeon.level.width();
				if (cur[cell] > 0) {
					
					if (fire != null && fire.volume > 0 && fire.cur[cell] > 0){
						fire.clear(cell);
						off[cell] = cur[cell] = 0;
						continue;
					}
					
					Freezing.freeze(cell);
					
					off[cell] = cur[cell] - 1;
					volume += off[cell];
				} else {
					off[cell] = 0;
				}
			}
		}
	}
	
	public static void freeze( int cell ){
		com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar( cell );
		if (ch != null && !ch.isImmune(Freezing.class)) {
			if (ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Frost.class) != null){
				Buff.affect(ch, com.quasistellar.hollowdungeon.actors.buffs.Frost.class, 2f);
			} else {
				Buff.affect(ch, com.quasistellar.hollowdungeon.actors.buffs.Chill.class, Dungeon.level.water[cell] ? 5f : 3f);
				com.quasistellar.hollowdungeon.actors.buffs.Chill chill = ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Chill.class);
				if (chill != null && chill.cooldown() >= Chill.DURATION){
					Buff.affect(ch, com.quasistellar.hollowdungeon.actors.buffs.Frost.class, Frost.DURATION);
				}
			}
		}
		
		com.quasistellar.hollowdungeon.items.Heap heap = Dungeon.level.heaps.get( cell );
		if (heap != null) heap.freeze();
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( SnowParticle.FACTORY, 0.05f, 0 );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
	
	//legacy functionality from before this was a proper blob. Returns true if this cell is visible
	public static boolean affect( int cell, Fire fire ) {
		
		Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar( cell );
		if (ch != null) {
			if (Dungeon.level.water[ch.pos]){
				Buff.prolong(ch, com.quasistellar.hollowdungeon.actors.buffs.Frost.class, Frost.DURATION * 3);
			} else {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong(ch, com.quasistellar.hollowdungeon.actors.buffs.Frost.class, com.quasistellar.hollowdungeon.actors.buffs.Frost.DURATION);
			}
		}
		
		if (fire != null) {
			fire.clear( cell );
		}
		
		Heap heap = Dungeon.level.heaps.get( cell );
		if (heap != null) {
			heap.freeze();
		}
		
		if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[cell]) {
			CellEmitter.get( cell ).start( com.quasistellar.hollowdungeon.effects.particles.SnowParticle.FACTORY, 0.2f, 6 );
			return true;
		} else {
			return false;
		}
	}
}
