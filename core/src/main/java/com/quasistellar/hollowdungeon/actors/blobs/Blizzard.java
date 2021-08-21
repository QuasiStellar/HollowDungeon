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
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.messages.Messages;

public class Blizzard extends Blob {
	
	@Override
	protected void evolve() {
		super.evolve();
		
		int cell;
		
		Fire fire = (Fire) Dungeon.level.blobs.get( Fire.class );
		Freezing freeze = (Freezing) Dungeon.level.blobs.get( Freezing.class );
		
		Inferno inf = (Inferno) Dungeon.level.blobs.get( Inferno.class );
		
		for (int i = area.left; i < area.right; i++) {
			for (int j = area.top; j < area.bottom; j++) {
				cell = i + j * com.quasistellar.hollowdungeon.Dungeon.level.width();
				if (cur[cell] > 0) {
					
					if (fire != null)   fire.clear(cell);
					if (freeze != null) freeze.clear(cell);
					
					if (inf != null && inf.volume > 0 && inf.cur[cell] > 0){
						inf.clear(cell);
						off[cell] = cur[cell] = 0;
						continue;
					}
					
					Freezing.freeze(cell);
					Freezing.freeze(cell);
					
				}
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.BLIZZARD, true ), 0.4f );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
	
	
}
