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

package com.quasistellar.hollowdungeon.actors.mobs.npcs;

import com.quasistellar.hollowdungeon.sprites.RatKingSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.messages.Messages;

public class RatKing extends NPC {

	{
		spriteClass = com.quasistellar.hollowdungeon.sprites.RatKingSprite.class;
		
		state = SLEEPING;
	}

	@Override
	public float speed() {
		return 2f;
	}
	
	@Override
	protected com.quasistellar.hollowdungeon.actors.Char chooseEnemy() {
		return null;
	}
	
	@Override
	public void damage( int dmg, Object src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}
	
	@Override
	public boolean reset() {
		return true;
	}

	//***This functionality is for when rat king may be summoned by a distortion trap
//
//	@Override
//	protected void onAdd() {
//		super.onAdd();
//		if (Dungeon.depth != 5){
//			yell(Messages.get(this, "confused"));
//		}
//	}
//
//	@Override
//	protected boolean act() {
//		if (Dungeon.depth < 5){
//			if (pos == Dungeon.level.exit){
//				destroy();
//				sprite.killAndErase();
//			} else {
//				target = Dungeon.level.exit;
//			}
//		} else if (Dungeon.depth > 5){
//			if (pos == Dungeon.level.entrance){
//				destroy();
//				sprite.killAndErase();
//			} else {
//				target = Dungeon.level.entrance;
//			}
//		}
//		return super.act();
//	}

	//***

	@Override
	public boolean interact(com.quasistellar.hollowdungeon.actors.Char c) {
		sprite.turnTo( pos, c.pos );

		if (c != com.quasistellar.hollowdungeon.Dungeon.hero){
			return super.interact(c);
		}

		if (state == SLEEPING) {
			notice();
			yell( Messages.get(this, "not_sleeping") );
			state = WANDERING;
		} else {
			yell( Messages.get(this, "what_is_it") );
		}
		return true;
	}
	
	@Override
	public String description() {
		return ((RatKingSprite)sprite).festive ?
				Messages.get(this, "desc_festive")
				: super.description();
	}
}
