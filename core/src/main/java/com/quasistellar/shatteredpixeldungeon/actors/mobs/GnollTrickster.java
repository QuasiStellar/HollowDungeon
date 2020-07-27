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

package com.quasistellar.shatteredpixeldungeon.actors.mobs;

import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.Char;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.Fire;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Burning;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.Poison;
import com.quasistellar.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.quasistellar.shatteredpixeldungeon.items.Generator;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.quasistellar.shatteredpixeldungeon.scenes.GameScene;
import com.quasistellar.shatteredpixeldungeon.sprites.GnollTricksterSprite;
import com.quasistellar.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollTrickster extends Gnoll {

	{
		spriteClass = GnollTricksterSprite.class;

		HP = HT = 20;
		defenseSkill = 5;

		EXP = 5;

		state = WANDERING;

		//at half quantity, see createLoot()
		loot = Generator.Category.MISSILE;
		lootChance = 1f;

		properties.add(Property.MINIBOSS);
	}

	private int combo = 0;

	@Override
	public int attackSkill( com.quasistellar.shatteredpixeldungeon.actors.Char target ) {
		return 16;
	}

	@Override
	protected boolean canAttack( com.quasistellar.shatteredpixeldungeon.actors.Char enemy ) {
		Ballistica attack = new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE);
		return !com.quasistellar.shatteredpixeldungeon.Dungeon.level.adjacent(pos, enemy.pos) && attack.collisionPos == enemy.pos;
	}

	@Override
	public int attackProc(Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		//The gnoll's attacks get more severe the more the player lets it hit them
		combo++;
		int effect = Random.Int(4)+combo;

		if (effect > 2) {

			if (effect >=6 && enemy.buff(com.quasistellar.shatteredpixeldungeon.actors.buffs.Burning.class) == null){

				if (Dungeon.level.flamable[enemy.pos])
					GameScene.add(Blob.seed(enemy.pos, 4, Fire.class));
				com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.affect(enemy, Burning.class).reignite( enemy );

			} else
				Buff.affect( enemy, Poison.class).set((effect-2) );

		}
		return damage;
	}

	@Override
	protected boolean getCloser( int target ) {
		combo = 0; //if he's moving, he isn't attacking, reset combo.
		if (state == HUNTING) {
			return enemySeen && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}
	
	@Override
	protected Item createLoot() {
		com.quasistellar.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon drop = (MissileWeapon)super.createLoot();
		//half quantity, rounded up
		drop.quantity((drop.quantity()+1)/2);
		return drop;
	}
	
	@Override
	public void die( Object cause ) {
		super.die( cause );

		Ghost.Quest.process();
	}

	private static final String COMBO = "combo";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(COMBO, combo);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		combo = bundle.getInt( COMBO );
	}

}
