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

package com.quasistellar.hollowdungeon.items.stones;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.levels.traps.Trap;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.mechanics.ShadowCaster;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StoneOfDisarming extends Runestone {
	
	private static final int DIST = 8;
	
	{
		image = ItemSpriteSheet.STONE_DISARM;
	}
	
	@Override
	protected void activate(final int cell) {
		boolean[] FOV = new boolean[Dungeon.level.length()];
		Point c = Dungeon.level.cellToPoint(cell);
		ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, DIST);
		
		int sX = Math.max(0, c.x - DIST);
		int eX = Math.min(Dungeon.level.width()-1, c.x + DIST);
		
		int sY = Math.max(0, c.y - DIST);
		int eY = Math.min(Dungeon.level.height()-1, c.y + DIST);
		
		ArrayList<com.quasistellar.hollowdungeon.levels.traps.Trap> disarmCandidates = new ArrayList<>();
		
		for (int y = sY; y <= eY; y++){
			int curr = y* Dungeon.level.width() + sX;
			for ( int x = sX; x <= eX; x++){
				
				if (FOV[curr]){
					
					com.quasistellar.hollowdungeon.levels.traps.Trap t = Dungeon.level.traps.get(curr);
					if (t != null && t.active){
						disarmCandidates.add(t);
					}
					
				}
				curr++;
			}
		}
		
		Collections.sort(disarmCandidates, new Comparator<com.quasistellar.hollowdungeon.levels.traps.Trap>() {
			@Override
			public int compare(com.quasistellar.hollowdungeon.levels.traps.Trap o1, com.quasistellar.hollowdungeon.levels.traps.Trap o2) {
				float diff = Dungeon.level.trueDistance(cell, o1.pos) - com.quasistellar.hollowdungeon.Dungeon.level.trueDistance(cell, o2.pos);
				if (diff < 0){
					return -1;
				} else if (diff == 0){
					return Random.Int(2) == 0 ? -1 : 1;
				} else {
					return 1;
				}
			}
		});
		
		//disarms at most nine traps
		while (disarmCandidates.size() > 9){
			disarmCandidates.remove(9);
		}
		
		for ( Trap t : disarmCandidates){
			t.reveal();
			t.disarm();
			CellEmitter.get(t.pos).burst(Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.STEAM), 6);
		}
		
		Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
	}
}
