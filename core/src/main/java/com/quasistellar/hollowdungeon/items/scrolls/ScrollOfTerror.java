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
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.effects.Flare;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Terror;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public class ScrollOfTerror extends Scroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_TERROR;
	}

	@Override
	public void doRead() {
		
		new Flare( 5, 32 ).color( 0xFF0000, true ).show( com.quasistellar.hollowdungeon.items.Item.curUser.sprite, 2f );
		Sample.INSTANCE.play( Assets.Sounds.READ );
		Invisibility.dispel();
		
		int count = 0;
		com.quasistellar.hollowdungeon.actors.mobs.Mob affected = null;
		for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray( new com.quasistellar.hollowdungeon.actors.mobs.Mob[0] )) {
			if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {
				Buff.affect( mob, com.quasistellar.hollowdungeon.actors.buffs.Terror.class, Terror.DURATION ).object = Item.curUser.id();

				if (mob.buff(com.quasistellar.hollowdungeon.actors.buffs.Terror.class) != null){
					count++;
					affected = mob;
				}
			}
		}
		
		switch (count) {
		case 0:
			GLog.i( Messages.get(this, "none") );
			break;
		case 1:
			GLog.i( Messages.get(this, "one", affected.name()) );
			break;
		default:
			com.quasistellar.hollowdungeon.utils.GLog.i( Messages.get(this, "many") );
		}
		setKnown();

		readAnimation();
	}
	
	@Override
	public void empoweredRead() {
		doRead();
		for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[mob.pos]) {
				com.quasistellar.hollowdungeon.actors.buffs.Terror t = mob.buff(com.quasistellar.hollowdungeon.actors.buffs.Terror.class);
				if (t != null){
					Buff.prolong(mob, com.quasistellar.hollowdungeon.actors.buffs.Terror.class, 15f);
					com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(mob, Paralysis.class, 5f);
				}
			}
		}
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}
