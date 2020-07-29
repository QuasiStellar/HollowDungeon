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

import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.GooWarn;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.LockedFloor;
import com.quasistellar.hollowdungeon.actors.buffs.Ooze;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.particles.ElmoParticle;
import com.quasistellar.hollowdungeon.sprites.GooSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.ui.BossHealthBar;
import com.quasistellar.hollowdungeon.utils.BArray;
import com.quasistellar.hollowdungeon.items.keys.SkeletonKey;
import com.quasistellar.hollowdungeon.items.quest.GooBlob;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Goo extends Mob {

	{
		HP = HT = 100;
		spriteClass = com.quasistellar.hollowdungeon.sprites.GooSprite.class;

		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.BOSS);
		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.DEMONIC);
		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.ACIDIC);
	}

	private int pumpedUp = 0;

	@Override
	public int damageRoll() {
		if (pumpedUp > 0) {
			pumpedUp = 0;
			PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 2 );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE)
					CellEmitter.get(i).burst(ElmoParticle.FACTORY, 10);
			}
			Sample.INSTANCE.play( Assets.Sounds.BURNING );
			return 2;
		} else {
			return 1;
		}
	}

	@Override
	public boolean act() {
		
		//ensures goo warning blob acts at the correct times
		//as normally blobs act one extra time when initialized if they normally act before
		//whatever spawned them
		GameScene.add(Blob.seed(pos, 0, com.quasistellar.hollowdungeon.actors.blobs.GooWarn.class));

		if (Dungeon.level.water[pos] && HP < HT) {
			sprite.emitter().burst( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.HEALING ), 1 );
			if (HP*2 == HT) {
				BossHealthBar.bleed(false);
				((com.quasistellar.hollowdungeon.sprites.GooSprite)sprite).spray(false);
			}
			HP++;
		}
		
		if (state != SLEEPING){
			Dungeon.level.seal();
		}
		
		//prevents goo pump animation from persisting when it shouldn't
		sprite.idle();

		return super.act();
	}

	@Override
	protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		return (pumpedUp > 0) ? distance( enemy ) <= 2 : super.canAttack(enemy);
	}

	@Override
	public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		if (Random.Int( 3 ) == 0) {
			Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Ooze.class ).set( Ooze.DURATION );
			enemy.sprite.burst( 0x000000, 5 );
		}

		if (pumpedUp > 0) {
			Camera.main.shake( 3, 0.2f );
		}

		return damage;
	}

	@Override
	protected boolean doAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		if (pumpedUp == 1) {
			((com.quasistellar.hollowdungeon.sprites.GooSprite)sprite).pumpUp();
			PathFinder.buildDistanceMap( pos, com.quasistellar.hollowdungeon.utils.BArray.not( Dungeon.level.solid, null ), 2 );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE)
					GameScene.add(Blob.seed(i, 1, com.quasistellar.hollowdungeon.actors.blobs.GooWarn.class));
			}
			pumpedUp++;
			Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );

			spend( attackDelay() );

			return true;
		} else if (pumpedUp >= 2 || Random.Int( (HP*2 <= HT) ? 2 : 5 ) > 0) {

			boolean visible = Dungeon.level.heroFOV[pos];

			if (visible) {
				if (pumpedUp >= 2) {
					((com.quasistellar.hollowdungeon.sprites.GooSprite) sprite).pumpAttack();
				}
				else
					sprite.attack( enemy.pos );
			} else {
				attack( enemy );
			}

			spend( attackDelay() );

			return !visible;

		} else {

			pumpedUp++;

			((com.quasistellar.hollowdungeon.sprites.GooSprite)sprite).pumpUp();

			for (int i=0; i < PathFinder.NEIGHBOURS9.length; i++) {
				int j = pos + PathFinder.NEIGHBOURS9[i];
				if (!Dungeon.level.solid[j]) {
					GameScene.add(com.quasistellar.hollowdungeon.actors.blobs.Blob.seed(j, 1, GooWarn.class));
				}
			}

			if (Dungeon.level.heroFOV[pos]) {
				sprite.showStatus( CharSprite.NEGATIVE, Messages.get(this, "!!!") );
				GLog.n( Messages.get(this, "pumpup") );
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.CHARGEUP, 1f, 0.8f );
			}

			spend( attackDelay() );

			return true;
		}
	}

	@Override
	public boolean attack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		boolean result = super.attack( enemy );
		pumpedUp = 0;
		return result;
	}

	@Override
	protected boolean getCloser( int target ) {
		pumpedUp = 0;
		return super.getCloser( target );
	}

	@Override
	public void damage(int dmg, Object src) {
		if (!BossHealthBar.isAssigned()){
			BossHealthBar.assignBoss( this );
		}
		boolean bleeding = (HP*2 <= HT);
		super.damage(dmg, src);
		if ((HP*2 <= HT) && !bleeding){
			BossHealthBar.bleed(true);
			sprite.showStatus(com.quasistellar.hollowdungeon.sprites.CharSprite.NEGATIVE, Messages.get(this, "enraged"));
			((GooSprite)sprite).spray(true);
			yell(Messages.get(this, "gluuurp"));
		}
		com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
		if (lock != null) lock.addTime(dmg*2);
	}

	@Override
	public void die( Object cause ) {
		
		super.die( cause );
		
		Dungeon.level.unseal();
		
		com.quasistellar.hollowdungeon.scenes.GameScene.bossSlain();
		Dungeon.level.drop( new SkeletonKey( Dungeon.location ), pos ).sprite.drop();
		
		//60% chance of 2 blobs, 30% chance of 3, 10% chance for 4. Average of 2.5
		int blobs = Random.chances(new float[]{0, 0, 6, 3, 1});
		for (int i = 0; i < blobs; i++){
			int ofs;
			do {
				ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
			} while (!Dungeon.level.passable[pos + ofs]);
			com.quasistellar.hollowdungeon.Dungeon.level.drop( new GooBlob(), pos + ofs ).sprite.drop( pos );
		}
		
		Badges.validateBossSlain();
		
		yell( Messages.get(this, "defeated") );
	}
	
	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss(this);
			yell(Messages.get(this, "notice"));
		}
	}

	private final String PUMPEDUP = "pumpedup";

	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put( PUMPEDUP , pumpedUp );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {

		super.restoreFromBundle( bundle );

		pumpedUp = bundle.getInt( PUMPEDUP );
		if (state != SLEEPING) BossHealthBar.assignBoss(this);
		if ((HP*2 <= HT)) com.quasistellar.hollowdungeon.ui.BossHealthBar.bleed(true);

	}
	
}
