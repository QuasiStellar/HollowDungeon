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

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Amok;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public class ScrollOfRage extends Scroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_RAGE;
	}

	@Override
	public void doRead() {

		for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray( new com.quasistellar.hollowdungeon.actors.mobs.Mob[0] )) {
			mob.beckon( com.quasistellar.hollowdungeon.items.Item.curUser.pos );
			if (mob.alignment != Char.Alignment.ALLY && Dungeon.level.heroFOV[mob.pos]) {
				Buff.prolong(mob, com.quasistellar.hollowdungeon.actors.buffs.Amok.class, 5f);
			}
		}

		GLog.w( Messages.get(this, "roar") );
		setKnown();
		
		com.quasistellar.hollowdungeon.items.Item.curUser.sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		Invisibility.dispel();

		readAnimation();
	}
	
	@Override
	public void empoweredRead() {
		for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[mob.pos]) {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong(mob, Amok.class, 5f);
			}
		}
		
		setKnown();
		
		Item.curUser.sprite.centerEmitter().start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.SCREAM ), 0.3f, 3 );
		Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.READ );
		com.quasistellar.hollowdungeon.actors.buffs.Invisibility.dispel();
		
		readAnimation();
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}
