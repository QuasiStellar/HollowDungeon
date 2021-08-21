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

import com.quasistellar.hollowdungeon.effects.BlobEmitter;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.messages.Messages;

public class Inferno extends Blob {
	
	@Override
	protected void evolve() {
		super.evolve();
		
		int cell;
		boolean observe = false;
		
		com.quasistellar.hollowdungeon.actors.blobs.Fire fire = (com.quasistellar.hollowdungeon.actors.blobs.Fire) Dungeon.level.blobs.get( com.quasistellar.hollowdungeon.actors.blobs.Fire.class );
		com.quasistellar.hollowdungeon.actors.blobs.Freezing freeze = (com.quasistellar.hollowdungeon.actors.blobs.Freezing) Dungeon.level.blobs.get( Freezing.class );
		
		com.quasistellar.hollowdungeon.actors.blobs.Blizzard bliz = (com.quasistellar.hollowdungeon.actors.blobs.Blizzard) Dungeon.level.blobs.get( Blizzard.class );
		
		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j * Dungeon.level.width();
				if (cur[cell] > 0) {
					
					if (fire != null)   fire.clear(cell);
					if (freeze != null) freeze.clear(cell);
					
					if (bliz != null && bliz.volume > 0 && bliz.cur[cell] > 0){
						bliz.clear(cell);
						off[cell] = cur[cell] = 0;
						continue;
					}
					
					com.quasistellar.hollowdungeon.actors.blobs.Fire.burn(cell);
					
				} else if (Dungeon.level.flamable[cell]
						&& (cur[cell-1] > 0
						|| cur[cell+1] > 0
						|| cur[cell- Dungeon.level.width()] > 0
						|| cur[cell+ Dungeon.level.width()] > 0)) {
					Fire.burn(cell);
					Dungeon.level.destroy( cell );
					
					observe = true;
					GameScene.updateMap( cell );
				}
			}
		}
		
		if (observe) {
			com.quasistellar.hollowdungeon.Dungeon.observe();
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		
		emitter.pour( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.INFERNO, true ), 0.4f );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
	
}
