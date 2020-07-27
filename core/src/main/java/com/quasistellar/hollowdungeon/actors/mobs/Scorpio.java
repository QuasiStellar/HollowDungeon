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

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Cripple;
import com.quasistellar.hollowdungeon.actors.buffs.Light;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ScorpioSprite;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHealing;
import com.quasistellar.hollowdungeon.items.potions.PotionOfStrength;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class Scorpio extends Mob {
	
	{
		spriteClass = ScorpioSprite.class;
		
		HP = HT = 95;
		viewDistance = Light.DISTANCE;
		
		loot = Generator.Category.POTION;
		lootChance = 0.5f;

		properties.add(Char.Property.DEMONIC);
	}

	@Override
	protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		Ballistica attack = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE);
		return !Dungeon.level.adjacent( pos, enemy.pos ) && attack.collisionPos == enemy.pos;
	}
	
	@Override
	public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (Random.Int( 2 ) == 0) {
			Buff.prolong( enemy, com.quasistellar.hollowdungeon.actors.buffs.Cripple.class, Cripple.DURATION );
		}
		
		return damage;
	}
	
	@Override
	protected boolean getCloser( int target ) {
		if (state == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	protected Item createLoot() {
		Class<?extends Potion> loot;
		do{
			loot = (Class<? extends Potion>) Random.oneOf(com.quasistellar.hollowdungeon.items.Generator.Category.POTION.classes);
		} while (loot == PotionOfHealing.class || loot == PotionOfStrength.class);

		return Reflection.newInstance(loot);
	}
	
}
