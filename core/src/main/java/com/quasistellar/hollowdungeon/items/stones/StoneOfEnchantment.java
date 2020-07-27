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

import com.quasistellar.hollowdungeon.effects.Enchanting;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;

public class StoneOfEnchantment extends InventoryStone {
	
	{
		mode = WndBag.Mode.ENCHANTABLE;
		image = ItemSpriteSheet.STONE_ENCHANT;
	}
	
	@Override
	protected void onItemSelected(com.quasistellar.hollowdungeon.items.Item item) {

		((com.quasistellar.hollowdungeon.items.weapon.Weapon)item).enchant();

		com.quasistellar.hollowdungeon.items.Item.curUser.sprite.emitter().start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.LIGHT ), 0.1f, 5 );
		Enchanting.show( Item.curUser, item );
		
		GLog.p(Messages.get(this, "weapon"));

		useAnimation();
		
	}
	
	@Override
	public int price() {
		return 30 * quantity;
	}
}
