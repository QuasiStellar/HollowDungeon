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

package com.quasistellar.hollowdungeon.ui;

import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.watabou.input.GameAction;

public class LootIndicator extends Tag {
	
	private com.quasistellar.hollowdungeon.ui.ItemSlot slot;
	
	private com.quasistellar.hollowdungeon.items.Item lastItem = null;
	private int lastQuantity = 0;
	
	public LootIndicator() {
		super( 0x1F75CC );
		
		setSize( 24, 24 );
		
		visible = false;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		slot = new com.quasistellar.hollowdungeon.ui.ItemSlot() {
			protected void onClick() {
				if (Dungeon.hero.handle(Dungeon.hero.pos)){
					Dungeon.hero.next();
				}

			}

			@Override
			public GameAction keyAction() {
				return HDAction.TAG_LOOT;
			}
		};
		slot.showExtraInfo( false );
		add( slot );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		slot.setRect( x + 2, y + 3, width - 3, height - 6 );
	}
	
	@Override
	public void update() {
		
		if (Dungeon.hero.ready) {
			com.quasistellar.hollowdungeon.items.Heap heap = Dungeon.level.heaps.get( Dungeon.hero.pos );
			if (heap != null) {
				
				Item item =
					heap.type == Heap.Type.CHEST || heap.type == Heap.Type.MIMIC ? com.quasistellar.hollowdungeon.ui.ItemSlot.CHEST :
					heap.type == Heap.Type.LOCKED_CHEST ? com.quasistellar.hollowdungeon.ui.ItemSlot.LOCKED_CHEST :
					heap.type == Heap.Type.CRYSTAL_CHEST ? com.quasistellar.hollowdungeon.ui.ItemSlot.CRYSTAL_CHEST :
					heap.type == Heap.Type.TOMB ? com.quasistellar.hollowdungeon.ui.ItemSlot.TOMB :
					heap.type == Heap.Type.SKELETON ? com.quasistellar.hollowdungeon.ui.ItemSlot.SKELETON :
					heap.type == com.quasistellar.hollowdungeon.items.Heap.Type.REMAINS ? ItemSlot.REMAINS :
					heap.peek();
				if (item != lastItem || item.quantity() != lastQuantity) {
					lastItem = item;
					lastQuantity = item.quantity();
					
					slot.item( item );
					flash();
				}
				visible = true;
				
			} else {
				
				lastItem = null;
				visible = false;
				
			}
		}
		
		slot.enable( visible && Dungeon.hero.ready );
		
		super.update();
	}
}
