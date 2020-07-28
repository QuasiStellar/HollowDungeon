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
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.effects.Flare;
import com.quasistellar.hollowdungeon.effects.particles.ShadowParticle;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.windows.WndBag;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Degrade;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public class ScrollOfRemoveCurse extends InventoryScroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_REMCURSE;
		mode = WndBag.Mode.UNCURSABLE;
	}
	
	@Override
	public void empoweredRead() {
		for (com.quasistellar.hollowdungeon.items.Item item : Item.curUser.belongings){
			if (item.cursed){
				item.cursedKnown = true;
			}
		}
		Sample.INSTANCE.play( Assets.Sounds.READ );
		Invisibility.dispel();
		doRead();
	}
	
	@Override
	protected void onItemSelected(com.quasistellar.hollowdungeon.items.Item item) {
		new Flare( 6, 32 ).show( Item.curUser.sprite, 2f ) ;

		boolean procced = uncurse( Item.curUser, item );

		Degrade.detach( Item.curUser, com.quasistellar.hollowdungeon.actors.buffs.Degrade.class );

		if (procced) {
			GLog.p( Messages.get(this, "cleansed") );
		} else {
			com.quasistellar.hollowdungeon.utils.GLog.i( Messages.get(this, "not_cleansed") );
		}
	}

	public static boolean uncurse(Hero hero, com.quasistellar.hollowdungeon.items.Item... items ) {
		
		boolean procced = false;
		for (com.quasistellar.hollowdungeon.items.Item item : items) {
			if (item != null) {
				item.cursedKnown = true;
				if (item.cursed) {
					procced = true;
					item.cursed = false;
				}
			}
		}
		
		if (procced && hero != null) {
			hero.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10 );
			hero.updateHT( false ); //for ring of might
			Item.updateQuickslot();
		}
		
		return procced;
	}
	
	public static boolean uncursable( com.quasistellar.hollowdungeon.items.Item item ){
		if (item.isEquipped(Dungeon.hero) && com.quasistellar.hollowdungeon.Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.Degrade.class) != null) {
			return true;
		} else if (item.level() != item.buffedLvl()) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int price() {
		return isKnown() ? 30 * quantity : super.price();
	}
}
