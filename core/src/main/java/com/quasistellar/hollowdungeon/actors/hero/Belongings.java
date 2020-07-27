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

package com.quasistellar.hollowdungeon.actors.hero;

import com.quasistellar.hollowdungeon.GamesInProgress;
import com.quasistellar.hollowdungeon.items.EquipableItem;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.KindOfWeapon;
import com.quasistellar.hollowdungeon.items.KindofMisc;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.items.bags.Bag;
import com.quasistellar.hollowdungeon.items.keys.Key;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRemoveCurse;
import com.quasistellar.hollowdungeon.items.wands.Wand;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<com.quasistellar.hollowdungeon.items.Item> {

	private com.quasistellar.hollowdungeon.actors.hero.Hero owner;
	
	public Bag backpack;

	public com.quasistellar.hollowdungeon.items.KindOfWeapon weapon = null;
	public com.quasistellar.hollowdungeon.items.KindofMisc misc1 = null;
	public com.quasistellar.hollowdungeon.items.KindofMisc misc2 = null;
	
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
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String MISC1       = "misc1";
	private static final String MISC2       = "misc2";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( MISC1, misc1);
		bundle.put( MISC2, misc2);
	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon != null) {
			weapon.activate(owner);
		}
		
		misc1 = (com.quasistellar.hollowdungeon.items.KindofMisc)bundle.get(MISC1);
		if (misc1 != null) {
			misc1.activate( owner );
		}
		
		misc2 = (KindofMisc)bundle.get(MISC2);
		if (misc2 != null) {
			misc2.activate( owner );
		}
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
	
	public com.quasistellar.hollowdungeon.items.Item getSimilar(com.quasistellar.hollowdungeon.items.Item similar ){
		
		for (com.quasistellar.hollowdungeon.items.Item item : this) {
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
	
	public void observe() {
		if (weapon != null) {
			weapon.identify();
			Badges.validateItemLevelAquired( weapon );
		}
		if (misc1 != null) {
			misc1.identify();
			Badges.validateItemLevelAquired(misc1);
		}
		if (misc2 != null) {
			misc2.identify();
			com.quasistellar.hollowdungeon.Badges.validateItemLevelAquired(misc2);
		}
		for (com.quasistellar.hollowdungeon.items.Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, weapon, misc1, misc2);
	}
	
	public com.quasistellar.hollowdungeon.items.Item randomUnequipped() {
		return Random.element( backpack.items );
	}
	
	public void resurrect( int depth ) {

		for (com.quasistellar.hollowdungeon.items.Item item : backpack.items.toArray( new com.quasistellar.hollowdungeon.items.Item[0])) {
			if (item instanceof Key) {
				if (((Key)item).depth == depth) {
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
		
		if (weapon != null) {
			weapon.cursed = false;
			weapon.activate( owner );
		}

		if (misc1 != null) {
			misc1.cursed = false;
			misc1.activate( owner );
		}
		if (misc2 != null) {
			misc2.cursed = false;
			misc2.activate( owner );
		}
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<com.quasistellar.hollowdungeon.items.Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<com.quasistellar.hollowdungeon.items.Item> {

		private int index = 0;
		
		private Iterator<com.quasistellar.hollowdungeon.items.Item> backpackIterator = backpack.iterator();
		
		private com.quasistellar.hollowdungeon.items.Item[] equipped = {weapon, misc1, misc2};
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
			switch (index) {
			case 0:
				equipped[0] = weapon = null;
				break;
			//TODO: case 1 replace armor with smth
			case 2:
				equipped[2] = misc1 = null;
				break;
			case 3:
				equipped[3] = misc2 = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
