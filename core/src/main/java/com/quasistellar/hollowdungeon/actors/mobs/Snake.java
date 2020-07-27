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

package com.quasistellar.hollowdungeon.actors.mobs;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.sprites.SnakeSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Random;

public class Snake extends Mob {
	
	{
		spriteClass = SnakeSprite.class;
		
		HP = HT = 4;
		
		loot = Generator.Category.SEED;
		lootChance = 0.25f;
	}

	private static int dodges = 0;

	@Override
	public String defenseVerb() {
		dodges++;
		if (dodges >= 5 && !Badges.isUnlocked(com.quasistellar.hollowdungeon.Badges.Badge.BOSS_SLAIN_1)){
			GLog.w(Messages.get(this, "hint"));
			dodges = 0;
		}
		return super.defenseVerb();
	}
}
