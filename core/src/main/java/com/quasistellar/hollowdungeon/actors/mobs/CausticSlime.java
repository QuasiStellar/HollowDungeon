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
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Ooze;
import com.quasistellar.hollowdungeon.sprites.CausticSlimeSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.quest.GooBlob;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class CausticSlime extends Slime {
	
	{
		spriteClass = CausticSlimeSprite.class;
		
		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.ACIDIC);
	}
	
	@Override
	public int attackProc(Char enemy, int damage ) {
		if (Random.Int( 2 ) == 0) {
			Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Ooze.class ).set( Ooze.DURATION );
			enemy.sprite.burst( 0x000000, 5 );
		}
		
		return super.attackProc( enemy, damage );
	}
	
	@Override
	public void rollToDropLoot() {
		super.rollToDropLoot();
		
		int ofs;
		do {
			ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
		} while (!Dungeon.level.passable[pos + ofs]);
		com.quasistellar.hollowdungeon.Dungeon.level.drop( new GooBlob(), pos + ofs ).sprite.drop( pos );
	}
}
