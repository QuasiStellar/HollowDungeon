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

package com.quasistellar.hollowdungeon.items.scrolls;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.windows.WndOptions;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public abstract class InventoryScroll extends com.quasistellar.hollowdungeon.items.scrolls.Scroll {

	protected String inventoryTitle = Messages.get(this, "inv_title");
	protected com.quasistellar.hollowdungeon.windows.WndBag.Mode mode = WndBag.Mode.ALL;
	
	@Override
	public void doRead() {
		
		if (!isKnown()) {
			setKnown();
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}
		
		GameScene.selectItem( itemSelector, mode, inventoryTitle );
	}
	
	private void confirmCancelation() {
		GameScene.show(new WndOptions( Messages.titleCase(name()), Messages.get(this, "warning"),
				Messages.get(this, "yes"), Messages.get(this, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
				case 0:
					Item.curUser.spendAndNext( TIME_TO_READ );
					identifiedByUse = false;
					break;
				case 1:
					com.quasistellar.hollowdungeon.scenes.GameScene.selectItem( itemSelector, mode, inventoryTitle );
					break;
				}
			}
			public void onBackPressed() {}
		} );
	}
	
	protected abstract void onItemSelected( com.quasistellar.hollowdungeon.items.Item item );
	
	protected static boolean identifiedByUse = false;
	protected static com.quasistellar.hollowdungeon.windows.WndBag.Listener itemSelector = new com.quasistellar.hollowdungeon.windows.WndBag.Listener() {
		@Override
		public void onSelect( com.quasistellar.hollowdungeon.items.Item item ) {
			
			//FIXME this safety check shouldn't be necessary
			//it would be better to eliminate the curItem static variable.
			if (!(com.quasistellar.hollowdungeon.items.Item.curItem instanceof InventoryScroll)){
				return;
			}
			
			if (item != null) {
				
				((InventoryScroll) com.quasistellar.hollowdungeon.items.Item.curItem).onItemSelected( item );
				((InventoryScroll) com.quasistellar.hollowdungeon.items.Item.curItem).readAnimation();
				
				Sample.INSTANCE.play( Assets.Sounds.READ );
				Invisibility.dispel();
				
			} else if (identifiedByUse && !((com.quasistellar.hollowdungeon.items.scrolls.Scroll) com.quasistellar.hollowdungeon.items.Item.curItem).anonymous) {
				
				((InventoryScroll) com.quasistellar.hollowdungeon.items.Item.curItem).confirmCancelation();
				
			} else if (!((Scroll) com.quasistellar.hollowdungeon.items.Item.curItem).anonymous) {
				
				com.quasistellar.hollowdungeon.items.Item.curItem.collect( com.quasistellar.hollowdungeon.items.Item.curUser.belongings.backpack );
				
			}
		}
	};
}
