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

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Degrade;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.WarlockSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHealing;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Warlock extends Mob implements Callback {
	
	private static final float TIME_TO_ZAP	= 1f;
	
	{
		spriteClass = WarlockSprite.class;
		
		HP = HT = 70;
		
		loot = Generator.Category.POTION;
		lootChance = 0.5f;

		properties.add(Char.Property.UNDEAD);
	}

	@Override
	protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
	}
	
	protected boolean doAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {

		if (Dungeon.level.adjacent( pos, enemy.pos )) {
			
			return super.doAttack( enemy );
			
		} else {
			
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class DarkBolt{}
	
	private void zap() {
		spend( TIME_TO_ZAP );
		
		if (com.quasistellar.hollowdungeon.actors.Char.hit( this, enemy, true )) {
			//TODO would be nice for this to work on ghost/statues too
			if (enemy == Dungeon.hero && Random.Int( 2 ) == 0) {
				Buff.prolong( enemy, com.quasistellar.hollowdungeon.actors.buffs.Degrade.class, Degrade.DURATION );
				Sample.INSTANCE.play( Assets.Sounds.DEBUFF );
			}
			
			int dmg = Random.NormalIntRange( 12, 18 );
			enemy.damage( dmg, new DarkBolt() );
			
			if (enemy == Dungeon.hero && !enemy.isAlive()) {
				Dungeon.fail( getClass() );
				GLog.n( Messages.get(this, "bolt_kill") );
			}
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}
	}
	
	public void onZapComplete() {
		zap();
		next();
	}
	
	@Override
	public void call() {
		next();
	}

	@Override
	public com.quasistellar.hollowdungeon.items.Item createLoot(){

		// 1/6 chance for healing, scaling to 0 over 8 drops
		if (Random.Int(2) == 0 && Random.Int(8) > Dungeon.LimitedDrops.WARLOCK_HP.count ){
			com.quasistellar.hollowdungeon.Dungeon.LimitedDrops.WARLOCK_HP.drop();
			return new PotionOfHealing();
		} else {
			Item i = Generator.random(Generator.Category.POTION);
			int healingTried = 0;
			while (i instanceof PotionOfHealing){
				healingTried++;
				i = Generator.random(Generator.Category.POTION);
			}

			//return the attempted healing potion drops to the pool
			if (healingTried > 0){
				for (int j = 0; j < Generator.Category.POTION.classes.length; j++){
					if (Generator.Category.POTION.classes[j] == PotionOfHealing.class){
						com.quasistellar.hollowdungeon.items.Generator.Category.POTION.probs[j] += healingTried;
					}
				}
			}

			return i;
		}

	}
}
