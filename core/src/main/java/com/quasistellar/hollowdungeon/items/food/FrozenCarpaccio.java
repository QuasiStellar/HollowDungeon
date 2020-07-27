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

import com.quasistellar.hollowdungeon.actors.buffs.Barkskin;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHealing;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Random;

public class FrozenCarpaccio extends com.quasistellar.hollowdungeon.items.food.Food {

	{
		image = ItemSpriteSheet.CARPACCIO;
	}
	
	@Override
	protected void satisfy(com.quasistellar.hollowdungeon.actors.hero.Hero hero) {
		super.satisfy(hero);
		effect(hero);
	}
	
	public int price() {
		return 10 * quantity;
	}

	public static void effect(Hero hero){
		switch (Random.Int( 5 )) {
			case 0:
				GLog.i( Messages.get(FrozenCarpaccio.class, "invis") );
				Buff.affect( hero, com.quasistellar.hollowdungeon.actors.buffs.Invisibility.class, Invisibility.DURATION );
				break;
			case 1:
				GLog.i( Messages.get(FrozenCarpaccio.class, "hard") );
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( hero, Barkskin.class ).set( hero.HT / 4, 1 );
				break;
			case 2:
				GLog.i( Messages.get(FrozenCarpaccio.class, "refresh") );
				PotionOfHealing.cure(hero);
				break;
			case 3:
				com.quasistellar.hollowdungeon.utils.GLog.i( Messages.get(FrozenCarpaccio.class, "better") );
				if (hero.HP < hero.HT) {
					hero.HP = Math.min( hero.HP + hero.HT / 4, hero.HT );
					hero.sprite.emitter().burst( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.HEALING ), 1 );
				}
				break;
		}
	}
	
	public static Food cook(MysteryMeat ingredient ) {
		FrozenCarpaccio result = new FrozenCarpaccio();
		result.quantity = ingredient.quantity();
		return result;
	}
}
