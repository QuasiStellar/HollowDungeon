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

package com.quasistellar.hollowdungeon.items.scrolls.exotic;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Charm;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.watabou.noosa.audio.Sample;

public class ScrollOfAffection extends ExoticScroll {
	
	{
		icon = ItemSpriteSheet.Icons.SCROLL_AFFECTION;
	}
	
	@Override
	public void doRead() {
		
		com.quasistellar.hollowdungeon.items.Item.curUser.sprite.centerEmitter().start( Speck.factory( Speck.HEART ), 0.2f, 5 );
		Sample.INSTANCE.play( Assets.Sounds.CHARMS );
		Invisibility.dispel();
		
		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[mob.pos]) {
				Buff.affect( mob, Charm.class, Charm.DURATION*2f ).object = Item.curUser.id();
				mob.sprite.centerEmitter().start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.HEART ), 0.2f, 5 );
			}
		}
		
		//GLog.i( Messages.get(this, "sooth") );
		
		setKnown();
		
		readAnimation();
		
	}
	
}
