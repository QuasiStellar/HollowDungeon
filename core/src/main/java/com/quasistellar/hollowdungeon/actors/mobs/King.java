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
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.effects.Flare;
import com.quasistellar.hollowdungeon.items.weapon.enchantments.Grim;
import com.quasistellar.hollowdungeon.levels.OldCityBossLevel;
import com.quasistellar.hollowdungeon.sprites.KingSprite;
import com.quasistellar.hollowdungeon.sprites.UndeadSprite;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.BossHealthBar;
import com.quasistellar.hollowdungeon.items.artifacts.DriedRose;
import com.quasistellar.hollowdungeon.items.artifacts.LloydsBeacon;
import com.quasistellar.hollowdungeon.items.keys.SkeletonKey;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.items.wands.WandOfDisintegration;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class King extends Mob {
	
	private static final int MAX_ARMY_SIZE	= 5;
	
	{
		spriteClass = KingSprite.class;
		
		HP = HT = 300;
		
		Undead.count = 0;

		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.BOSS);
		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.UNDEAD);
	}
	
	private boolean nextPedestal = true;
	
	private static final String PEDESTAL = "pedestal";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PEDESTAL, nextPedestal );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		nextPedestal = bundle.getBoolean( PEDESTAL );
		BossHealthBar.assignBoss(this);
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 25, 40 );
	}

	@Override
	protected boolean getCloser( int target ) {
		return canTryToSummon() ?
			super.getCloser( ((com.quasistellar.hollowdungeon.levels.OldCityBossLevel) Dungeon.level).pedestal( nextPedestal ) ) :
			super.getCloser( target );
	}
	
	@Override
	protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		return canTryToSummon() ?
				pos == ((com.quasistellar.hollowdungeon.levels.OldCityBossLevel) Dungeon.level).pedestal( nextPedestal ) :
				Dungeon.level.adjacent( pos, enemy.pos );
	}

	protected boolean canTryToSummon() {
		if (paralysed <= 0 && Undead.count < maxArmySize()) {
			com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar( ((com.quasistellar.hollowdungeon.levels.OldCityBossLevel) Dungeon.level).pedestal( nextPedestal ) );
			return ch == this || ch == null;
		} else {
			return false;
		}
	}
	
	@Override
	protected boolean act() {
		if (canTryToSummon() && pos == ((com.quasistellar.hollowdungeon.levels.OldCityBossLevel) Dungeon.level).pedestal( nextPedestal )) {
			summon();
			return true;
		} else {
			if (enemy != null && canTryToSummon() && Actor.findChar( ((OldCityBossLevel) Dungeon.level).pedestal( nextPedestal ) ) == enemy) {
				nextPedestal = !nextPedestal;
			}
			return super.act();
		}
	}

	@Override
	public void damage(int dmg, Object src) {
		super.damage(dmg, src);
		com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.LockedFloor.class);
		if (lock != null) lock.addTime(dmg);
	}
	
	@Override
	public void die( Object cause ) {

		GameScene.bossSlain();
		Dungeon.level.drop( new SkeletonKey( Dungeon.depth ), pos ).sprite.drop();
		
		super.die( cause );
		
		Badges.validateBossSlain();

		LloydsBeacon beacon = Dungeon.hero.belongings.getItem(LloydsBeacon.class);
		if (beacon != null) {
			beacon.upgrade();
		}
		
		yell( Messages.get(this, "defeated", Dungeon.hero.name()) );
	}

	@Override
	public void aggro(com.quasistellar.hollowdungeon.actors.Char ch) {
		super.aggro(ch);
		for (Mob mob : Dungeon.level.mobs){
			if (mob instanceof Undead){
				mob.aggro(ch);
			}
		}
	}

	protected int maxArmySize() {
		return 1 + MAX_ARMY_SIZE * (HT - HP) / HT;
	}
	
	private void summon() {

		nextPedestal = !nextPedestal;
		
		sprite.centerEmitter().start( Speck.factory( com.quasistellar.hollowdungeon.effects.Speck.SCREAM ), 0.4f, 2 );
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		
		boolean[] passable = Dungeon.level.passable.clone();
		for (com.quasistellar.hollowdungeon.actors.Char c : Actor.chars()) {
			passable[c.pos] = false;
		}
		
		int undeadsToSummon = maxArmySize() - Undead.count;

		PathFinder.buildDistanceMap( pos, passable, undeadsToSummon );
		PathFinder.distance[pos] = Integer.MAX_VALUE;
		int dist = 1;
		
	undeadLabel:
		for (int i=0; i < undeadsToSummon; i++) {
			do {
				for (int j = 0; j < Dungeon.level.length(); j++) {
					if (PathFinder.distance[j] == dist) {
						
						Undead undead = new Undead();
						undead.pos = j;
						com.quasistellar.hollowdungeon.scenes.GameScene.add( undead );
						
						ScrollOfTeleportation.appear( undead, j );
						new Flare( 3, 32 ).color( 0x000000, false ).show( undead.sprite, 2f ) ;
						
						PathFinder.distance[j] = Integer.MAX_VALUE;
						
						continue undeadLabel;
					}
				}
				dist++;
			} while (dist < undeadsToSummon);
		}
		
		yell( Messages.get(this, "arise") );
		spend( com.quasistellar.hollowdungeon.actors.Actor.TICK );
	}
	
	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			com.quasistellar.hollowdungeon.ui.BossHealthBar.assignBoss(this);
			yell(Messages.get(this, "notice"));
			for (com.quasistellar.hollowdungeon.actors.Char ch : com.quasistellar.hollowdungeon.actors.Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
		}
	}
	
	{
		resistances.add( WandOfDisintegration.class );
		resistances.add( com.quasistellar.hollowdungeon.actors.blobs.ToxicGas.class );
		resistances.add( com.quasistellar.hollowdungeon.actors.buffs.Burning.class );
	}
	
	{
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Paralysis.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Vertigo.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Blindness.class );
		immunities.add( com.quasistellar.hollowdungeon.actors.buffs.Terror.class );
	}
	
	public static class Undead extends Mob {
		
		public static int count = 0;
		
		{
			spriteClass = UndeadSprite.class;
			
			HP = HT = 28;
			
			state = WANDERING;

			properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.UNDEAD);
			properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.INORGANIC);
		}
		
		@Override
		protected void onAdd() {
			count++;
			super.onAdd();
		}
		
		@Override
		protected void onRemove() {
			count--;
			super.onRemove();
		}
		
		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 15, 25 );
		}

		@Override
		public int attackProc(Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );
			if (Random.Int( MAX_ARMY_SIZE ) == 0) {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong( enemy, com.quasistellar.hollowdungeon.actors.buffs.Paralysis.class, 1 );
			}
			
			return damage;
		}
		
		@Override
		public void damage( int dmg, Object src ) {
			super.damage( dmg, src );
			if (src instanceof com.quasistellar.hollowdungeon.actors.blobs.ToxicGas) {
				((ToxicGas)src).clear( pos );
			}
		}
		
		@Override
		public void die( Object cause ) {
			super.die( cause );
			
			if (com.quasistellar.hollowdungeon.Dungeon.level.heroFOV[pos]) {
				Sample.INSTANCE.play( com.quasistellar.hollowdungeon.Assets.Sounds.BONES );
			}
		}

		{
			immunities.add( Grim.class );
			immunities.add( Paralysis.class );
		}
	}
}
