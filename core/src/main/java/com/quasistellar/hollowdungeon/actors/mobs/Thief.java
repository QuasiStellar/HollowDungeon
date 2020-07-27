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

import com.quasistellar.hollowdungeon.actors.buffs.Corruption;
import com.quasistellar.hollowdungeon.actors.buffs.Terror;
import com.quasistellar.hollowdungeon.items.Gold;
import com.quasistellar.hollowdungeon.items.Honeypot;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ThiefSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Thief extends com.quasistellar.hollowdungeon.actors.mobs.Mob {
	
	public com.quasistellar.hollowdungeon.items.Item item;
	
	{
		spriteClass = ThiefSprite.class;
		
		HP = HT = 20;

		loot = Random.oneOf(Generator.Category.RING, com.quasistellar.hollowdungeon.items.Generator.Category.ARTIFACT);
		lootChance = 0.03f; //initially, see rollToDropLoot

		WANDERING = new Wandering();
		FLEEING = new Fleeing();

		properties.add(Char.Property.UNDEAD);
	}

	private static final String ITEM = "item";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEM, item );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		item = (com.quasistellar.hollowdungeon.items.Item)bundle.get( ITEM );
	}

	@Override
	public float speed() {
		if (item != null) return (5*super.speed())/6;
		else return super.speed();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 10 );
	}

	@Override
	protected float attackDelay() {
		return super.attackDelay()*0.5f;
	}
	
	@Override
	public void rollToDropLoot() {
		if (item != null) {
			Dungeon.level.drop( item, pos ).sprite.drop();
			//updates position
			if (item instanceof com.quasistellar.hollowdungeon.items.Honeypot.ShatteredPot) ((com.quasistellar.hollowdungeon.items.Honeypot.ShatteredPot)item).dropPot( this, pos );
			item = null;
		}
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/33, 1/100, 1/300, 1/900, etc.
		lootChance *= Math.pow(1/3f, Dungeon.LimitedDrops.THEIF_MISC.count);
		super.rollToDropLoot();
	}

	@Override
	protected com.quasistellar.hollowdungeon.items.Item createLoot() {
		Dungeon.LimitedDrops.THEIF_MISC.count++;
		return super.createLoot();
	}

	@Override
	public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		
		if (alignment == Char.Alignment.ENEMY && item == null
				&& enemy instanceof Hero && steal( (Hero)enemy )) {
			state = FLEEING;
		}

		return damage;
	}

	protected boolean steal( Hero hero ) {

		Item item = hero.belongings.randomUnequipped();

		if (item != null && !item.unique && item.level() < 1 ) {

			GLog.w( Messages.get(Thief.class, "stole", item.name()) );
			if (!item.stackable) {
				Dungeon.quickslot.convertToPlaceholder(item);
			}
			item.updateQuickslot();

			if (item instanceof com.quasistellar.hollowdungeon.items.Honeypot){
				this.item = ((com.quasistellar.hollowdungeon.items.Honeypot)item).shatter(this, this.pos);
				item.detach( hero.belongings.backpack );
			} else {
				this.item = item.detach( hero.belongings.backpack );
				if ( item instanceof com.quasistellar.hollowdungeon.items.Honeypot.ShatteredPot)
					((Honeypot.ShatteredPot)item).pickupPot(this);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public String description() {
		String desc = super.description();

		if (item != null) {
			desc += Messages.get(this, "carries", item.name() );
		}

		return desc;
	}
	
	private class Wandering extends com.quasistellar.hollowdungeon.actors.mobs.Mob.Wandering {
		
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			super.act(enemyInFOV, justAlerted);
			
			//if an enemy is just noticed and the thief posses an item, run, don't fight.
			if (state == HUNTING && item != null){
				state = FLEEING;
			}
			
			return true;
		}
	}

	private class Fleeing extends com.quasistellar.hollowdungeon.actors.mobs.Mob.Fleeing {
		@Override
		protected void nowhereToRun() {
			if (buff( Terror.class ) == null && buff( Corruption.class ) == null) {
				if (enemySeen) {
					sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Mob.class, "rage"));
					state = HUNTING;
				} else if (item != null
						&& !Dungeon.level.heroFOV[pos]
						&& Dungeon.level.distance(Dungeon.hero.pos, pos) >= 6) {

					int count = 32;
					int newPos;
					do {
						newPos = Dungeon.level.randomRespawnCell( Thief.this );
						if (count-- <= 0) {
							break;
						}
					} while (newPos == -1 || Dungeon.level.heroFOV[newPos] || Dungeon.level.distance(newPos, pos) < (count/3));

					if (newPos != -1) {

						if (Dungeon.level.heroFOV[pos]) CellEmitter.get(pos).burst(Speck.factory(Speck.WOOL), 6);
						pos = newPos;
						sprite.place( pos );
						sprite.visible = Dungeon.level.heroFOV[pos];
						if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[pos]) com.quasistellar.hollowdungeon.effects.CellEmitter.get(pos).burst(Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.WOOL), 6);

					}

					if (item != null) com.quasistellar.hollowdungeon.utils.GLog.n( Messages.get(Thief.class, "escapes", item.name()));
					item = null;
					state = WANDERING;
				} else {
					state = WANDERING;
				}
			} else {
				super.nowhereToRun();
			}
		}
	}
}
