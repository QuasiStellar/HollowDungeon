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

package com.quasistellar.hollowdungeon.actors.hero;

import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.bags.Bag;
import com.quasistellar.hollowdungeon.items.keys.Key;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<com.quasistellar.hollowdungeon.items.Item> {

	private com.quasistellar.hollowdungeon.actors.hero.Hero owner;
	
	public Bag backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Bag() {
			{
				name = Messages.get(Bag.class, "name");
			}
			public int capacity(){
				int cap = super.capacity();
				for (com.quasistellar.hollowdungeon.items.Item item : items){
					if (item instanceof Bag){
						cap++;
					}
				}
				return cap;
			}
		};
		backpack.owner = owner;
	}

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );

	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );

	}
	
	public static void preview(GamesInProgress.Info info, Bundle bundle ) {
		info.armorTier = 0;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends com.quasistellar.hollowdungeon.items.Item> T getItem(Class<T> itemClass ) {

		for (com.quasistellar.hollowdungeon.items.Item item : this) {
			if (itemClass.isInstance( item )) {
				return (T)item;
			}
		}
		
		return null;
	}
	
	public boolean contains( com.quasistellar.hollowdungeon.items.Item contains ){
		
		for (com.quasistellar.hollowdungeon.items.Item item : this) {
			if (contains == item ) {
				return true;
			}
		}
		
		return false;
	}

	public Item getSimilar( Item similar ){

		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				return item;
			}
		}

		return null;
	}
	
	public ArrayList<com.quasistellar.hollowdungeon.items.Item> getAllSimilar(com.quasistellar.hollowdungeon.items.Item similar ){
		ArrayList<com.quasistellar.hollowdungeon.items.Item> result = new ArrayList<>();
		
		for (com.quasistellar.hollowdungeon.items.Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				result.add(item);
			}
		}
		
		return result;
	}
	
	public void identify() {
		for (com.quasistellar.hollowdungeon.items.Item item : this) {
			item.identify();
		}
	}

	public com.quasistellar.hollowdungeon.items.Item randomUnequipped() {
		return Random.element( backpack.items );
	}
	
	public void resurrect( String location ) {

		for (com.quasistellar.hollowdungeon.items.Item item : backpack.items.toArray( new com.quasistellar.hollowdungeon.items.Item[0])) {
			if (item instanceof Key) {
				if (((Key) item).location.equals(location)) {
					item.detachAll( backpack );
				}
			} else if (item.unique) {
				item.detachAll(backpack);
				//you keep the bag itself, not its contents.
				if (item instanceof Bag){
					((Bag)item).resurrect();
				}
				item.collect();
			} else if (!item.isEquipped( owner )) {
				item.detachAll( backpack );
			}
		}
	}

	@Override
	public Iterator<com.quasistellar.hollowdungeon.items.Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<com.quasistellar.hollowdungeon.items.Item> {

		private int index = 0;
		
		private Iterator<com.quasistellar.hollowdungeon.items.Item> backpackIterator = backpack.iterator();

		private com.quasistellar.hollowdungeon.items.Item[] equipped = {};
		private int backpackIndex = equipped.length;

		@Override
		public boolean hasNext() {

			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}

			return backpackIterator.hasNext();
		}

		@Override
		public com.quasistellar.hollowdungeon.items.Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			backpackIterator.remove();
		}
	}
}
