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

package com.quasistellar.shatteredpixeldungeon.items.bags;

import com.quasistellar.shatteredpixeldungeon.Badges;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.Char;
import com.quasistellar.shatteredpixeldungeon.actors.hero.Hero;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.scenes.GameScene;
import com.quasistellar.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Iterator;

public class Bag extends com.quasistellar.shatteredpixeldungeon.items.Item implements Iterable<com.quasistellar.shatteredpixeldungeon.items.Item> {

	public static final String AC_OPEN	= "OPEN";
	
	{
		image = 11;
		
		defaultAction = AC_OPEN;

		unique = true;
	}
	
	public Char owner;
	
	public ArrayList<com.quasistellar.shatteredpixeldungeon.items.Item> items = new ArrayList<>();

	public int capacity(){
		return 20; // default container size
	}
	
	@Override
	public void execute(Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_OPEN )) {
			
			GameScene.show( new com.quasistellar.shatteredpixeldungeon.windows.WndBag( this, null, WndBag.Mode.ALL, null ) );
			
		}
	}
	
	@Override
	public boolean collect( Bag container ) {

		for (com.quasistellar.shatteredpixeldungeon.items.Item item : container.items.toArray( new com.quasistellar.shatteredpixeldungeon.items.Item[0] )) {
			if (canHold( item )) {
				int slot = com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.getSlot(item);
				item.detachAll(container);
				if (!item.collect(this)) {
					item.collect(container);
				}
				if (slot != -1) {
					com.quasistellar.shatteredpixeldungeon.Dungeon.quickslot.setSlot(slot, item);
				}
			}
		}

		if (super.collect( container )) {
			
			owner = container.owner;
			
			Badges.validateAllBagsBought( this );
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onDetach( ) {
		this.owner = null;
		for (com.quasistellar.shatteredpixeldungeon.items.Item item : items)
			Dungeon.quickslot.clearItem(item);
		updateQuickslot();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	public void clear() {
		items.clear();
	}
	
	public void resurrect() {
		for (com.quasistellar.shatteredpixeldungeon.items.Item item : items.toArray(new com.quasistellar.shatteredpixeldungeon.items.Item[0])){
			if (!item.unique) items.remove(item);
		}
	}
	
	private static final String ITEMS	= "inventory";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEMS, items );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		for (Bundlable item : bundle.getCollection( ITEMS )) {
			if (item != null) ((com.quasistellar.shatteredpixeldungeon.items.Item)item).collect( this );
		}
	}
	
	public boolean contains( com.quasistellar.shatteredpixeldungeon.items.Item item ) {
		for (com.quasistellar.shatteredpixeldungeon.items.Item i : items) {
			if (i == item) {
				return true;
			} else if (i instanceof Bag && ((Bag)i).contains( item )) {
				return true;
			}
		}
		return false;
	}

	public boolean canHold( com.quasistellar.shatteredpixeldungeon.items.Item item ){
		if (items.contains(item) || item instanceof Bag || items.size() < capacity()){
			return true;
		} else if (item.stackable) {
			for (com.quasistellar.shatteredpixeldungeon.items.Item i : items) {
				if (item.isSimilar( i )) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Iterator<com.quasistellar.shatteredpixeldungeon.items.Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<com.quasistellar.shatteredpixeldungeon.items.Item> {

		private int index = 0;
		private Iterator<com.quasistellar.shatteredpixeldungeon.items.Item> nested = null;
		
		@Override
		public boolean hasNext() {
			if (nested != null) {
				return nested.hasNext() || index < items.size();
			} else {
				return index < items.size();
			}
		}

		@Override
		public com.quasistellar.shatteredpixeldungeon.items.Item next() {
			if (nested != null && nested.hasNext()) {
				
				return nested.next();
				
			} else {
				
				nested = null;
				
				Item item = items.get( index++ );
				if (item instanceof Bag) {
					nested = ((Bag)item).iterator();
				}
				
				return item;
			}
		}

		@Override
		public void remove() {
			if (nested != null) {
				nested.remove();
			} else {
				items.remove( index );
			}
		}
	}
}