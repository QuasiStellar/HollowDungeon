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

package com.quasistellar.hollowdungeon.items.food;

import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Random;

public class MysteryMeat extends Food {

	{
		image = ItemSpriteSheet.MEAT;
	}
	
	@Override
	protected void satisfy(com.quasistellar.hollowdungeon.actors.hero.Hero hero) {
		super.satisfy(hero);
		effect(hero);
	}

	public int price() {
		return 5 * quantity;
	}

	public static void effect(Hero hero){
		switch (Random.Int( 5 )) {
			case 0:
				GLog.w( Messages.get(MysteryMeat.class, "hot") );
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( hero, com.quasistellar.hollowdungeon.actors.buffs.Burning.class ).reignite( hero );
				break;
			case 1:
				GLog.w( Messages.get(MysteryMeat.class, "legs") );
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong( hero, com.quasistellar.hollowdungeon.actors.buffs.Roots.class, com.quasistellar.hollowdungeon.actors.buffs.Roots.DURATION*2f );
				break;
			case 2:
				GLog.w( Messages.get(MysteryMeat.class, "not_well") );
				Buff.affect( hero, com.quasistellar.hollowdungeon.actors.buffs.Poison.class ).set( hero.HT / 5 );
				break;
			case 3:
				com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(MysteryMeat.class, "stuffed") );
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong( hero, com.quasistellar.hollowdungeon.actors.buffs.Slow.class, com.quasistellar.hollowdungeon.actors.buffs.Slow.DURATION );
				break;
		}
	}
	
	public static class PlaceHolder extends MysteryMeat {
		
		{
			image = com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet.FOOD_HOLDER;
		}
		
		@Override
		public boolean isSimilar(Item item) {
			return item instanceof MysteryMeat || item instanceof StewedMeat
					|| item instanceof ChargrilledMeat || item instanceof FrozenCarpaccio;
		}
		
		@Override
		public String info() {
			return "";
		}
	}
}
