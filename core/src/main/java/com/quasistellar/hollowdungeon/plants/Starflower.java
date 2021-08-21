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

package com.quasistellar.hollowdungeon.plants;

import com.quasistellar.hollowdungeon.actors.hero.HeroClass;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Bless;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Recharging;
import com.quasistellar.hollowdungeon.actors.hero.Hero;

public class Starflower extends com.quasistellar.hollowdungeon.plants.Plant {

	{
		image = 9;
		seedClass = Seed.class;
	}

	@Override
	public void activate( Char ch ) {

		if (ch != null) {
			Buff.prolong(ch, Bless.class, Bless.DURATION);
			if (ch instanceof Hero && ((Hero) ch).heroClass == HeroClass.HORNET){
				Buff.prolong(ch, Recharging.class, Recharging.DURATION);
			}
		}

	}

	public static class Seed extends Plant.Seed {

		{
			image = ItemSpriteSheet.SEED_STARFLOWER;

			plantClass = Starflower.class;
		}
		
		@Override
		public int price() {
			return 30 * quantity;
		}
	}
}
