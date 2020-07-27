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

package com.quasistellar.shatteredpixeldungeon.items.scrolls;

import com.quasistellar.shatteredpixeldungeon.Assets;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.quasistellar.shatteredpixeldungeon.actors.mobs.Mob;
import com.quasistellar.shatteredpixeldungeon.effects.Speck;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.quasistellar.shatteredpixeldungeon.utils.GLog;
import com.quasistellar.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;

public class ScrollOfLullaby extends Scroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_LULLABY;
	}

	@Override
	public void doRead() {
		
		Item.curUser.sprite.centerEmitter().start( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.NOTE ), 0.3f, 5 );
		Sample.INSTANCE.play( Assets.Sounds.LULLABY );
		Invisibility.dispel();

		for (com.quasistellar.shatteredpixeldungeon.actors.mobs.Mob mob : com.quasistellar.shatteredpixeldungeon.Dungeon.level.mobs.toArray( new com.quasistellar.shatteredpixeldungeon.actors.mobs.Mob[0] )) {
			if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.heroFOV[mob.pos]) {
				com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.affect( mob, com.quasistellar.shatteredpixeldungeon.actors.buffs.Drowsy.class );
				mob.sprite.centerEmitter().start( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( Speck.NOTE ), 0.3f, 5 );
			}
		}

		com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.affect( Item.curUser, com.quasistellar.shatteredpixeldungeon.actors.buffs.Drowsy.class );

		GLog.i( Messages.get(this, "sooth") );

		setKnown();

		readAnimation();
	}
	
	@Override
	public void empoweredRead() {
		doRead();
		for (com.quasistellar.shatteredpixeldungeon.actors.mobs.Mob mob : com.quasistellar.shatteredpixeldungeon.Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.heroFOV[mob.pos]) {
				Buff drowsy = mob.buff(Drowsy.class);
				if (drowsy != null) drowsy.act();
			}
		}
	}
	
	@Override
	public int price() {
		return isKnown() ? 40 * quantity : super.price();
	}
}
