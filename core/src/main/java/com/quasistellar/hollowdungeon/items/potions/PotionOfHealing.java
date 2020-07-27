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

package com.quasistellar.hollowdungeon.items.potions;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;

public class PotionOfHealing extends Potion {

	{
		icon = ItemSpriteSheet.Icons.POTION_HEALING;

		bones = true;
	}
	
	@Override
	public void apply( Hero hero ) {
		setKnown();
		//starts out healing 30 hp, equalizes with hero health total at level 11
		com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( hero, com.quasistellar.hollowdungeon.actors.buffs.Healing.class ).setHeal((int)(0.8f*hero.HT + 14), 0.25f, 0);
		cure( hero );
		GLog.p( Messages.get(this, "heal") );
	}
	
	public static void cure( Char ch ) {
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Poison.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Cripple.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Weakness.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Vulnerable.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Bleeding.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Blindness.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Drowsy.class );
		Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Slow.class );
		com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( ch, com.quasistellar.hollowdungeon.actors.buffs.Vertigo.class);
	}

	@Override
	public int price() {
		return isKnown() ? 30 * quantity : super.price();
	}
}
