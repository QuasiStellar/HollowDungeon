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

import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.journal.Notes;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class WellWater extends Blob {

	@Override
	protected void evolve() {
		int cell;
		boolean seen = false;
		for (int i=area.top-1; i <= area.bottom; i++) {
			for (int j = area.left-1; j <= area.right; j++) {
				cell = j + i* Dungeon.level.width();
				if (Dungeon.level.insideMap(cell)) {
					off[cell] = cur[cell];
					volume += off[cell];
					if (off[cell] > 0 && Dungeon.level.visited[cell]) {
						seen = true;
					}
				}
			}
		}
		if (seen){
			Notes.add(record());
		} else {
			Notes.remove(record());
		}
	}
	
	protected boolean affect( int pos ) {
		
		Heap heap;
		
		if (pos == Dungeon.hero.pos && affectHero( Dungeon.hero )) {
			
			cur[pos] = 0;
			return true;
			
		} else if ((heap = Dungeon.level.heaps.get( pos )) != null) {
			
			com.quasistellar.hollowdungeon.items.Item oldItem = heap.peek();
			com.quasistellar.hollowdungeon.items.Item newItem = affectItem( oldItem, pos );
			
			if (newItem != null) {
				
				if (newItem == oldItem) {

				} else if (oldItem.quantity() > 1) {

					oldItem.quantity( oldItem.quantity() - 1 );
					heap.drop( newItem );
					
				} else {
					heap.replace( oldItem, newItem );
				}
				
				heap.sprite.link();
				cur[pos] = 0;
				
				return true;
				
			} else {
				
				int newPlace;
				do {
					newPlace = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
				} while (!Dungeon.level.passable[newPlace] && !Dungeon.level.avoid[newPlace]);
				Dungeon.level.drop( heap.pickUp(), newPlace ).sprite.drop( pos );
				
				return false;
				
			}
			
		} else {
			
			return false;
			
		}
	}
	
	protected abstract boolean affectHero( Hero hero );
	
	protected abstract com.quasistellar.hollowdungeon.items.Item affectItem(Item item, int pos );
	
	protected abstract com.quasistellar.hollowdungeon.journal.Notes.Landmark record();
	
	public static void affectCell( int cell ) {
		
		Class<?>[] waters = {WaterOfHealth.class, WaterOfAwareness.class};
		
		for (Class<?>waterClass : waters) {
			WellWater water = (WellWater) com.quasistellar.hollowdungeon.Dungeon.level.blobs.get( waterClass );
			if (water != null &&
				water.volume > 0 &&
				water.cur[cell] > 0 &&
				water.affect( cell )) {
				
				Level.set( cell, Terrain.EMPTY_WELL );
				GameScene.updateMap( cell );
				
				return;
			}
		}
	}

	public static void affectHeroAnyway(WellWater water) {
		water.affectHero(Dungeon.hero);
	}
}
