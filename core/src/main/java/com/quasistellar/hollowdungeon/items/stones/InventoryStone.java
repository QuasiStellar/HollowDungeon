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
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public abstract class InventoryStone extends Runestone {
	
	protected String inventoryTitle = Messages.get(this, "inv_title");
	protected com.quasistellar.hollowdungeon.windows.WndBag.Mode mode = WndBag.Mode.ALL;
	
	{
		defaultAction = AC_USE;
	}
	
	public static final String AC_USE	= "USE";
	
	@Override
	public ArrayList<String> actions(com.quasistellar.hollowdungeon.actors.hero.Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_USE );
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_USE)){
			Item.curItem = detach( hero.belongings.backpack );
			activate(Item.curUser.pos);
		}
	}
	
	@Override
	protected void activate(int cell) {
		GameScene.selectItem( itemSelector, mode, inventoryTitle );
	}
	
	protected void useAnimation() {
		Item.curUser.spend( 1f );
		Item.curUser.busy();
		Item.curUser.sprite.operate(Item.curUser.pos);
		
		Sample.INSTANCE.play( Assets.Sounds.READ );
		Invisibility.dispel();
	}
	
	protected abstract void onItemSelected( com.quasistellar.hollowdungeon.items.Item item );
	
	protected static com.quasistellar.hollowdungeon.windows.WndBag.Listener itemSelector = new com.quasistellar.hollowdungeon.windows.WndBag.Listener() {
		@Override
		public void onSelect( com.quasistellar.hollowdungeon.items.Item item ) {
			
			//FIXME this safety check shouldn't be necessary
			//it would be better to eliminate the curItem static variable.
			if (!(com.quasistellar.hollowdungeon.items.Item.curItem instanceof InventoryStone)){
				return;
			}
			
			if (item != null) {
				
				((InventoryStone) com.quasistellar.hollowdungeon.items.Item.curItem).onItemSelected( item );
				
			} else{
				com.quasistellar.hollowdungeon.items.Item.curItem.collect( com.quasistellar.hollowdungeon.items.Item.curUser.belongings.backpack );
			}
		}
	};
	
}
