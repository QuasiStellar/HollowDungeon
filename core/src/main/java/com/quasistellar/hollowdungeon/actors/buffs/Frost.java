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

package com.quasistellar.hollowdungeon.actors.buffs;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Thief;
import com.quasistellar.hollowdungeon.items.food.FrozenCarpaccio;
import com.quasistellar.hollowdungeon.items.food.MysteryMeat;
import com.quasistellar.hollowdungeon.items.potions.Potion;
import com.quasistellar.hollowdungeon.items.potions.PotionOfStrength;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Frost extends FlavourBuff {

	public static final float DURATION	= 10f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}
	
	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo( target )) {
			
			target.paralysed++;
			com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( target, Burning.class );
			com.quasistellar.hollowdungeon.actors.buffs.Buff.detach( target, com.quasistellar.hollowdungeon.actors.buffs.Chill.class );

			if (target instanceof Hero) {

				Hero hero = (Hero)target;
				ArrayList<com.quasistellar.hollowdungeon.items.Item> freezable = new ArrayList<>();
				//does not reach inside of containers
				for (com.quasistellar.hollowdungeon.items.Item i : hero.belongings.backpack.items){
					if ((i instanceof Potion && !(i instanceof PotionOfStrength))
						|| i instanceof MysteryMeat){
						freezable.add(i);
					}
				}
				
				if (!freezable.isEmpty()){
					com.quasistellar.hollowdungeon.items.Item toFreeze = Random.element(freezable).detach( hero.belongings.backpack );
					GLog.w( Messages.get(this, "freezes", toFreeze.toString()) );
					if (toFreeze instanceof Potion){
						((Potion) toFreeze).shatter(hero.pos);
					} else if (toFreeze instanceof MysteryMeat){
						FrozenCarpaccio carpaccio = new FrozenCarpaccio();
						if (!carpaccio.collect( hero.belongings.backpack )) {
							Dungeon.level.drop( carpaccio, target.pos ).sprite.drop();
						}
					}
				}
				
			} else if (target instanceof Thief) {

				Item item = ((Thief) target).item;

				if (item instanceof Potion && !(item instanceof PotionOfStrength)) {
					((Potion) ((Thief) target).item).shatter(target.pos);
					((Thief) target).item = null;
				} else if (item instanceof MysteryMeat){
					((Thief) target).item = new FrozenCarpaccio();
				}

			}

			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		super.detach();
		if (target.paralysed > 0)
			target.paralysed--;
		if (com.quasistellar.hollowdungeon.Dungeon.level.water[target.pos])
			Buff.prolong(target, com.quasistellar.hollowdungeon.actors.buffs.Chill.class, Chill.DURATION/2f);
	}
	
	@Override
	public int icon() {
		return BuffIndicator.FROST;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0f, 0.75f, 1f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.FROZEN);
		else target.sprite.remove(com.quasistellar.hollowdungeon.sprites.CharSprite.State.FROZEN);
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns());
	}

}
