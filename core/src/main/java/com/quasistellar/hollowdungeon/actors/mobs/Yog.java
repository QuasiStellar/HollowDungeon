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

import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.actors.blobs.Fire;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Vertigo;
import com.quasistellar.hollowdungeon.effects.Pushing;
import com.quasistellar.hollowdungeon.effects.particles.ShadowParticle;
import com.quasistellar.hollowdungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.quasistellar.hollowdungeon.items.weapon.enchantments.Grim;
import com.quasistellar.hollowdungeon.levels.traps.GrimTrap;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.FistSprite;
import com.quasistellar.hollowdungeon.sprites.LarvaSprite;
import com.quasistellar.hollowdungeon.sprites.YogSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.BossHealthBar;
import com.quasistellar.hollowdungeon.items.artifacts.DriedRose;
import com.quasistellar.hollowdungeon.items.keys.SkeletonKey;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRetribution;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public class Yog extends com.quasistellar.hollowdungeon.actors.mobs.Mob {
	
	{
		spriteClass = YogSprite.class;
		
		HP = HT = 300;

		state = PASSIVE;

		properties.add(Char.Property.BOSS);
		properties.add(Char.Property.IMMOVABLE);
		properties.add(Char.Property.DEMONIC);
	}
	
	public Yog() {
		super();
	}
	
	public void spawnFists() {
		RottingFist fist1 = new RottingFist();
		BurningFist fist2 = new BurningFist();
		
		do {
			fist1.pos = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			fist2.pos = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
		} while (!Dungeon.level.passable[fist1.pos] || !Dungeon.level.passable[fist2.pos] || fist1.pos == fist2.pos);
		
		GameScene.add( fist1 );
		GameScene.add( fist2 );

		notice();
	}

	@Override
	protected boolean act() {
		//heals 1 health per turn
		HP = Math.min( HT, HP+1 );

		return super.act();
	}

	@Override
	public void damage( int dmg, Object src ) {

		HashSet<com.quasistellar.hollowdungeon.actors.mobs.Mob> fists = new HashSet<>();

		for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : Dungeon.level.mobs)
			if (mob instanceof RottingFist || mob instanceof BurningFist)
				fists.add( mob );

		dmg >>= fists.size();
		
		super.damage( dmg, src );

		com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.LockedFloor.class);
		if (lock != null) lock.addTime(dmg*0.5f);

	}
	
	@Override
	public void beckon( int cell ) {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void die( Object cause ) {

		for (com.quasistellar.hollowdungeon.actors.mobs.Mob mob : (Iterable<com.quasistellar.hollowdungeon.actors.mobs.Mob>) Dungeon.level.mobs.clone()) {
			if (mob instanceof BurningFist || mob instanceof RottingFist) {
				mob.die( cause );
			}
		}
		
		GameScene.bossSlain();
		Dungeon.level.drop( new SkeletonKey( Dungeon.depth ), pos ).sprite.drop();
		super.die( cause );
		
		yell( Messages.get(this, "defeated") );
	}
	
	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss(this);
			yell(Messages.get(this, "notice"));
			for (com.quasistellar.hollowdungeon.actors.Char ch : com.quasistellar.hollowdungeon.actors.Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
		}
	}
	
	{
		immunities.add( Grim.class );
		immunities.add( GrimTrap.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Terror.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Amok.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Charm.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Sleep.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Burning.class );
		immunities.add( ToxicGas.class );
		immunities.add( ScrollOfRetribution.class );
		immunities.add( ScrollOfPsionicBlast.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Vertigo.class );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		com.quasistellar.hollowdungeon.ui.BossHealthBar.assignBoss(this);
	}

	public static class RottingFist extends com.quasistellar.hollowdungeon.actors.mobs.Mob {
	
		private static final int REGENERATION	= 4;
		
		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.FistSprite.Rotting.class;
			
			HP = HT = 300;
			
			state = WANDERING;

			properties.add(Char.Property.MINIBOSS);
			properties.add(Char.Property.DEMONIC);
			properties.add(Char.Property.ACIDIC);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 20, 50 );
		}

		@Override
		public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			
			if (Random.Int( 3 ) == 0) {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Ooze.class ).set( com.quasistellar.hollowdungeon.actors.buffs.Ooze.DURATION );
				enemy.sprite.burst( 0xFF000000, 5 );
			}
			
			return damage;
		}
		
		@Override
		public boolean act() {
			
			if (Dungeon.level.water[pos] && HP < HT) {
				sprite.emitter().burst( ShadowParticle.UP, 2 );
				HP += REGENERATION;
			}
			
			return super.act();
		}

		@Override
		public void damage(int dmg, Object src) {
			super.damage(dmg, src);
			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.LockedFloor.class);
			if (lock != null) lock.addTime(dmg*0.5f);
		}
		
		{
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Paralysis.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Amok.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Sleep.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Terror.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Poison.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Vertigo.class );
		}
	}
	
	public static class BurningFist extends com.quasistellar.hollowdungeon.actors.mobs.Mob {
		
		{
			spriteClass = FistSprite.Burning.class;
			
			HP = HT = 200;
			
			state = WANDERING;

			properties.add(Char.Property.MINIBOSS);
			properties.add(Char.Property.DEMONIC);
			properties.add(Char.Property.FIERY);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 26, 32 );
		}

		@Override
		protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
			return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		}
		
		//used so resistances can differentiate between melee and magical attacks
		public static class DarkBolt{}

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

		private void zap() {
			spend( 1f );

			if (Char.hit( this, enemy, true )) {

				int dmg = damageRoll();
				enemy.damage( dmg, new DarkBolt() );

				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Dungeon.fail( getClass() );
					GLog.n( Messages.get(com.quasistellar.hollowdungeon.actors.Char.class, "kill", name()) );
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
		public boolean act() {
			
			for (int i=0; i < PathFinder.NEIGHBOURS9.length; i++) {
				com.quasistellar.hollowdungeon.scenes.GameScene.add( Blob.seed( pos + PathFinder.NEIGHBOURS9[i], 2, Fire.class ) );
			}
			
			return super.act();
		}

		@Override
		public void damage(int dmg, Object src) {
			super.damage(dmg, src);
			com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = com.quasistellar.hollowdungeon.Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.LockedFloor.class);
			if (lock != null) lock.addTime(dmg*0.5f);
		}
		
		{
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Amok.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Sleep.class );
			immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Terror.class );
			immunities.add( Vertigo.class );
		}
	}
	
	public static class Larva extends Mob {
		
		{
			spriteClass = LarvaSprite.class;
			
			HP = HT = 25;
			
			state = HUNTING;

			properties.add(Char.Property.DEMONIC);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 22, 30 );
		}

	}
}
