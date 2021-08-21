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
import com.quasistellar.hollowdungeon.scenes.AlchemyScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.journal.Notes;

public class Alchemy extends Blob implements AlchemyScene.AlchemyProvider {

	protected int pos;
	
	@Override
	protected void evolve() {
		int cell;
		for (int i=area.top-1; i <= area.bottom; i++) {
			for (int j = area.left-1; j <= area.right; j++) {
				cell = j + i* Dungeon.level.width();
				if (Dungeon.level.insideMap(cell)) {
					off[cell] = cur[cell];
					volume += off[cell];
					if (off[cell] > 0 && com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[cell]){
						Notes.add( com.quasistellar.hollowdungeon.journal.Notes.Landmark.ALCHEMY );
					}
				}
			}
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.BUBBLE ), 0.33f, 0 );
	}
	
	public static int alchPos;
	
	//1 volume is kept in reserve
	
	@Override
	public int getEnergy() {
		return Math.max(0, cur[alchPos] - 1);
	}
	
	@Override
	public void spendEnergy(int reduction) {
		cur[alchPos] = Math.max(1, cur[alchPos] - reduction);
	}
}
