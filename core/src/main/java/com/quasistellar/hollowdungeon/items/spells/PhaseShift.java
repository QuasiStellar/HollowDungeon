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

package com.quasistellar.hollowdungeon.items.spells;

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;

public class PhaseShift extends TargetedSpell {
	
	{
		image = ItemSpriteSheet.PHASE_SHIFT;
	}
	
	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {
		final com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(bolt.collisionPos);
		
		if (ch == hero){
			ScrollOfTeleportation.teleportHero(curUser);
		} else if (ch != null) {
			int count = 10;
			int pos;
			do {
				pos = Dungeon.level.randomRespawnCell( hero );
				if (count-- <= 0) {
					break;
				}
			} while (pos == -1);
			
//			if (pos == -1 || Dungeon.bossLevel()) {
//
//				GLog.w( Messages.get(com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation.class, "no_tele") );
//
//			} else if (ch.properties().contains(Char.Property.IMMOVABLE)) {
//
//				com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get(this, "tele_fail") );
//
//			} else  {
//
//				ch.pos = pos;
//				if (ch instanceof com.quasistellar.hollowdungeon.actors.mobs.Mob && ((com.quasistellar.hollowdungeon.actors.mobs.Mob) ch).state == ((com.quasistellar.hollowdungeon.actors.mobs.Mob) ch).HUNTING){
//					((com.quasistellar.hollowdungeon.actors.mobs.Mob) ch).state = ((Mob) ch).WANDERING;
//				}
//				ch.sprite.place(ch.pos);
//				ch.sprite.visible = com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[pos];
//
//			}
		}
	}
	
	@Override
	public int price() {
		//prices of ingredients, divided by output quantity
		return Math.round(quantity * ((30 + 40) / 8f));
	}
	
	public static class Recipe extends com.quasistellar.hollowdungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation.class, ArcaneCatalyst.class};
			inQuantity = new int[]{1, 1};
			
			cost = 6;
			
			output = PhaseShift.class;
			outQuantity = 8;
		}
		
	}
	
}
