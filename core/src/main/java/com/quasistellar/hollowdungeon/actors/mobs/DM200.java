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

import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.sprites.DM200Sprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class DM200 extends Mob {

	{
		spriteClass = DM200Sprite.class;

		HP = HT = 70;

		lootChance = 0.125f; //initially, see rollToDropLoot

		properties.add(Property.INORGANIC);
		properties.add(Property.LARGE);

		HUNTING = new Hunting();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 25 );
	}

	@Override
	public void rollToDropLoot() {
		//each drop makes future drops 1/2 as likely
		// so loot chance looks like: 1/8, 1/16, 1/32, 1/64, etc.
		lootChance *= Math.pow(1/2f, Dungeon.LimitedDrops.DM200_EQUIP.count);
		super.rollToDropLoot();
	}

	protected Item createLoot() {
		com.quasistellar.hollowdungeon.Dungeon.LimitedDrops.DM200_EQUIP.count++;
		//uses probability tables for dwarf city
		return Generator.randomWeapon(4);
	}

	private int ventCooldown = 0;

	private static final String VENT_COOLDOWN = "vent_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VENT_COOLDOWN, ventCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ventCooldown = bundle.getInt( VENT_COOLDOWN );
	}

	@Override
	protected boolean act() {
		//ensures toxic gas acts at the appropriate time when added
		//TODO we have this check in 2 places now, can we just ensure that blobs spend an extra turn when added?
		GameScene.add(Blob.seed(pos, 0, com.quasistellar.hollowdungeon.actors.blobs.ToxicGas.class));
		ventCooldown--;
		return super.act();
	}

	public void onZapComplete(){
		zap();
		next();
	}

	private void zap( ){
		spend( Actor.TICK );
		ventCooldown = 30;

		Ballistica trajectory = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);

		for (int i : trajectory.subPath(0, trajectory.dist)){
			GameScene.add(Blob.seed(i, 20, com.quasistellar.hollowdungeon.actors.blobs.ToxicGas.class));
		}

		GLog.w(Messages.get(this, "vent"));
		com.quasistellar.hollowdungeon.scenes.GameScene.add(com.quasistellar.hollowdungeon.actors.blobs.Blob.seed(trajectory.collisionPos, 100, ToxicGas.class));

	}

	private class Hunting extends Mob.Hunting{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV || canAttack(enemy)) {
				return super.act(enemyInFOV, justAlerted);
			} else {
				enemySeen = true;
				target = enemy.pos;

				int oldPos = pos;

				if (ventCooldown <= 0 && Random.Int(100/distance(enemy)) == 0){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.pos );
						return false;
					} else {
						zap();
						return true;
					}

				} else if (getCloser( target )) {
					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else if (ventCooldown <= 0) {
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.pos );
						return false;
					} else {
						zap();
						return true;
					}

				} else {
					spend( Actor.TICK );
					return true;
				}

			}
		}
	}

}
