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

package com.quasistellar.hollowdungeon.items.potions.exotic;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.actors.buffs.Barkskin;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.hero.Hero;

public class PotionOfEarthenArmor extends ExoticPotion {
	
	{
		icon = ItemSpriteSheet.Icons.POTION_EARTHARMR;
	}
	
	@Override
	public void apply( Hero hero ) {
		setKnown();
		
		Buff.affect(hero, Barkskin.class).set( 2 + Dungeon.depth /3, 50 );
	}
	
}