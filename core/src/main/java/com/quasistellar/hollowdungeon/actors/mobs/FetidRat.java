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

import com.quasistellar.hollowdungeon.actors.blobs.StenchGas;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Ooze;
import com.quasistellar.hollowdungeon.sprites.FetidRatSprite;
import com.watabou.utils.Random;

public class FetidRat extends Rat {

	{
		spriteClass = FetidRatSprite.class;

		HP = HT = 20;

		state = WANDERING;

		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.MINIBOSS);
		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.DEMONIC);
	}

	@Override
	public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (Random.Int(3) == 0) {
			Buff.affect(enemy, com.quasistellar.hollowdungeon.actors.buffs.Ooze.class).set( Ooze.DURATION );
		}

		return damage;
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );

	}
	
	{
		immunities.add( StenchGas.class );
	}
}