/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2020 Evan Debenham
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

import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.sprites.FistSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.actors.blobs.Fire;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.effects.particles.LeafParticle;
import com.quasistellar.hollowdungeon.levels.Level;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class YogFist extends Mob {

	{
		HP = HT = 300;

		viewDistance = com.quasistellar.hollowdungeon.actors.buffs.Light.DISTANCE;

		state = HUNTING;

		properties.add(Char.Property.BOSS);
		properties.add(Char.Property.DEMONIC);
	}

	private float rangedCooldown;
	protected boolean canRangedInMelee = true;

	protected void incrementRangedCooldown(){
		rangedCooldown += Random.NormalFloat(8, 12);
	}

	@Override
	protected boolean act() {
		if (paralysed <= 0 && rangedCooldown > 0) rangedCooldown--;
		return super.act();
	}

	@Override
	protected boolean canAttack(com.quasistellar.hollowdungeon.actors.Char enemy) {
		if (rangedCooldown <= 0){
			return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos == enemy.pos;
		} else {
			return super.canAttack(enemy);
		}
	}

	private boolean invulnWarned = false;

	protected boolean isNearYog(){
		int yogPos = Dungeon.level.exit + 3* Dungeon.level.width();
		return Dungeon.level.distance(pos, yogPos) <= 4;
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		if (isNearYog() && !invulnWarned){
			invulnWarned = true;
			GLog.w(Messages.get(this, "invuln_warn"));
		}
		return isNearYog();
	}

	@Override
	protected boolean doAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {

		if (Dungeon.level.adjacent( pos, enemy.pos ) && (!canRangedInMelee || rangedCooldown > 0)) {

			return super.doAttack( enemy );

		} else {

			incrementRangedCooldown();
			if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
				sprite.zap( enemy.pos );
				return false;
			} else {
				zap();
				return true;
			}
		}
	}

	protected abstract void zap();

	public void onZapComplete(){
		zap();
		next();
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 18, 36 );
	}

	@Override
	public String description() {
		return Messages.get(YogFist.class, "desc") + "\n\n" + Messages.get(this, "desc");
	}

	public static final String RANGED_COOLDOWN = "ranged_cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(RANGED_COOLDOWN, rangedCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		rangedCooldown = bundle.getFloat(RANGED_COOLDOWN);
	}

	public static class BurningFist extends YogFist {

		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.FistSprite.Burning.class;

			properties.add(Char.Property.FIERY);
		}

		@Override
		public boolean act() {

			boolean result = super.act();

			if (Dungeon.level.map[pos] == Terrain.WATER){
				Level.set( pos, Terrain.EMPTY);
				GameScene.updateMap( pos );
				CellEmitter.get( pos ).burst( Speck.factory( Speck.STEAM ), 10 );
			}

			//1.33 evaporated tiles on average
			int evaporatedTiles = Random.chances(new float[]{0, 2, 1});

			for (int i = 0; i < evaporatedTiles; i++) {
				int cell = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
				if (Dungeon.level.map[cell] == Terrain.WATER){
					Level.set( cell, Terrain.EMPTY);
					GameScene.updateMap( cell );
					CellEmitter.get( cell ).burst( Speck.factory( Speck.STEAM ), 10 );
				}
			}

			for (int i : PathFinder.NEIGHBOURS9) {
				int vol = Fire.volumeAt(pos+i, com.quasistellar.hollowdungeon.actors.blobs.Fire.class);
				if (vol < 4 && !Dungeon.level.water[pos + i] && !Dungeon.level.solid[pos + i]){
					GameScene.add( Blob.seed( pos + i, 4 - vol, com.quasistellar.hollowdungeon.actors.blobs.Fire.class ) );
				}
			}

			return result;
		}

		@Override
		protected void zap() {
			spend( 1f );

			if (Dungeon.level.map[enemy.pos] == Terrain.WATER){
				Level.set( enemy.pos, Terrain.EMPTY);
				GameScene.updateMap( enemy.pos );
				CellEmitter.get( enemy.pos ).burst( Speck.factory( Speck.STEAM ), 10 );
			} else {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Burning.class ).reignite( enemy );
			}

			for (int i : PathFinder.NEIGHBOURS9){
				if (!Dungeon.level.water[enemy.pos+i] && !Dungeon.level.solid[enemy.pos+i]){
					int vol = Fire.volumeAt(enemy.pos+i, com.quasistellar.hollowdungeon.actors.blobs.Fire.class);
					if (vol < 4){
						GameScene.add( Blob.seed( enemy.pos + i, 4 - vol, com.quasistellar.hollowdungeon.actors.blobs.Fire.class ) );
					}
				}
			}

		}

	}

	public static class SoiledFist extends YogFist {

		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.FistSprite.Soiled.class;
		}

		@Override
		public boolean act() {

			boolean result = super.act();

			//1.33 grass tiles on average
			int furrowedTiles = Random.chances(new float[]{0, 2, 1});

			for (int i = 0; i < furrowedTiles; i++) {
				int cell = pos + PathFinder.NEIGHBOURS9[Random.Int(9)];
				if (Dungeon.level.map[cell] == Terrain.GRASS) {
					Level.set(cell, Terrain.FURROWED_GRASS);
					GameScene.updateMap(cell);
					CellEmitter.get(cell).burst(LeafParticle.GENERAL, 10);
				}
			}

			Dungeon.observe();

			for (int i : PathFinder.NEIGHBOURS9) {
				int cell = pos + i;
				if (canSpreadGrass(cell)){
					Level.set(pos+i, Terrain.GRASS);
					GameScene.updateMap( pos + i );
				}
			}

			return result;
		}

		@Override
		public void damage(int dmg, Object src) {
			int grassCells = 0;
			for (int i : PathFinder.NEIGHBOURS9) {
				if (Dungeon.level.map[pos+i] == Terrain.FURROWED_GRASS
				|| Dungeon.level.map[pos+i] == Terrain.HIGH_GRASS){
					grassCells++;
				}
			}
			if (grassCells > 0) dmg = Math.round(dmg * (6-grassCells)/6f);

			super.damage(dmg, src);
		}

		@Override
		protected void zap() {
			spend( 1f );

			if (Char.hit( this, enemy, true )) {

				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Roots.class, 3f );

			} else {

				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
			}

			for (int i : PathFinder.NEIGHBOURS9){
				int cell = enemy.pos + i;
				if (canSpreadGrass(cell)){
					if (Random.Int(5) == 0){
						Level.set(cell, Terrain.FURROWED_GRASS);
						GameScene.updateMap( cell );
					} else {
						com.quasistellar.hollowdungeon.levels.Level.set(cell, Terrain.GRASS);
						GameScene.updateMap( cell );
					}
					com.quasistellar.hollowdungeon.effects.CellEmitter.get( cell ).burst( com.quasistellar.hollowdungeon.effects.particles.LeafParticle.GENERAL, 10 );
				}
			}
			Dungeon.observe();

		}

		private boolean canSpreadGrass(int cell){
			int yogPos = Dungeon.level.exit + Dungeon.level.width()*3;
			return Dungeon.level.distance(cell, yogPos) > 4 && !Dungeon.level.solid[cell]
					&& !(Dungeon.level.map[cell] == Terrain.FURROWED_GRASS || Dungeon.level.map[cell] == com.quasistellar.hollowdungeon.levels.Terrain.HIGH_GRASS);
		}

		{
			resistances.add(com.quasistellar.hollowdungeon.actors.buffs.Burning.class);
		}


	}

	public static class RottingFist extends YogFist {

		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.FistSprite.Rotting.class;

			properties.add(Char.Property.ACIDIC);
		}

		@Override
		protected boolean act() {
			//ensures toxic gas acts at the appropriate time when added
			GameScene.add(Blob.seed(pos, 0, com.quasistellar.hollowdungeon.actors.blobs.ToxicGas.class));

			if (Dungeon.level.water[pos] && HP < HT) {
				sprite.emitter().burst( Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.HEALING), 3 );
				HP += HT/50;
			}

			return super.act();
		}

		@Override
		public void damage(int dmg, Object src) {
			if (!isInvulnerable(src.getClass()) && !(src instanceof com.quasistellar.hollowdungeon.actors.buffs.Bleeding)){
				com.quasistellar.hollowdungeon.actors.buffs.Bleeding b = buff(com.quasistellar.hollowdungeon.actors.buffs.Bleeding.class);
				if (b == null){
					b = new com.quasistellar.hollowdungeon.actors.buffs.Bleeding();
				}
				b.announced = false;
				b.set(dmg*.67f);
				b.attachTo(this);
				sprite.showStatus(CharSprite.WARNING, b.toString() + " " + (int)b.level());
			} else{
				super.damage(dmg, src);
			}
		}

		@Override
		protected void zap() {
			spend( 1f );
			GameScene.add(com.quasistellar.hollowdungeon.actors.blobs.Blob.seed(enemy.pos, 100, com.quasistellar.hollowdungeon.actors.blobs.ToxicGas.class));
		}

		@Override
		public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
			damage = super.attackProc( enemy, damage );

			if (Random.Int( 2 ) == 0) {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Ooze.class ).set( com.quasistellar.hollowdungeon.actors.buffs.Ooze.DURATION );
				enemy.sprite.burst( 0xFF000000, 5 );
			}

			return damage;
		}

		{
			immunities.add(ToxicGas.class);
		}

	}

	public static class RustedFist extends YogFist {

		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.FistSprite.Rusted.class;

			properties.add(Char.Property.LARGE);
			properties.add(Char.Property.INORGANIC);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 22, 44 );
		}

		@Override
		protected void zap() {
			spend( 1f );
			com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(enemy, com.quasistellar.hollowdungeon.actors.buffs.Cripple.class, 4f);
		}

	}

	public static class BrightFist extends YogFist {

		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.FistSprite.Bright.class;

			properties.add(Char.Property.ELECTRIC);

			canRangedInMelee = false;
		}

		@Override
		protected void incrementRangedCooldown() {
			//ranged attack has no cooldown
		}

		//used so resistances can differentiate between melee and magical attacks
		public static class LightBeam{}

		@Override
		protected void zap() {
			spend( 1f );

			if (Char.hit( this, enemy, true )) {

				enemy.damage( Random.NormalIntRange(10, 20), new LightBeam() );
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong( enemy, com.quasistellar.hollowdungeon.actors.buffs.Blindness.class, com.quasistellar.hollowdungeon.actors.buffs.Blindness.DURATION/2f );

				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Dungeon.fail( getClass() );
					GLog.n( Messages.get(com.quasistellar.hollowdungeon.actors.Char.class, "kill", name()) );
				}

			} else {

				enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
			}

		}

		@Override
		public void damage(int dmg, Object src) {
			int beforeHP = HP;
			super.damage(dmg, src);
			if (isAlive() && beforeHP > HT/2 && HP < HT/2){
				HP = HT/2;
				Buff.prolong( Dungeon.hero, com.quasistellar.hollowdungeon.actors.buffs.Blindness.class, com.quasistellar.hollowdungeon.actors.buffs.Blindness.DURATION*1.5f );
				int i;
				do {
					i = Random.Int(Dungeon.level.length());
				} while (Dungeon.level.heroFOV[i]
						|| Dungeon.level.solid[i]
						|| Actor.findChar(i) != null
						|| PathFinder.getStep(i, Dungeon.level.exit, Dungeon.level.passable) == -1);
				ScrollOfTeleportation.appear(this, i);
				state = WANDERING;
				GameScene.flash(0xFFFFFF);
				GLog.w( Messages.get( this, "teleport" ));
			} else if (!isAlive()){
				com.quasistellar.hollowdungeon.actors.buffs.Buff.prolong( Dungeon.hero, com.quasistellar.hollowdungeon.actors.buffs.Blindness.class, com.quasistellar.hollowdungeon.actors.buffs.Blindness.DURATION*3f );
				GameScene.flash(0xFFFFFF);
			}
		}

	}

	public static class DarkFist extends YogFist {

		{
			spriteClass = FistSprite.Dark.class;

			canRangedInMelee = false;
		}

		@Override
		protected void incrementRangedCooldown() {
			//ranged attack has no cooldown
		}

		//used so resistances can differentiate between melee and magical attacks
		public static class DarkBolt{}

		@Override
		protected void zap() {
			spend( 1f );

			if (Char.hit( this, enemy, true )) {

				enemy.damage( Random.NormalIntRange(10, 20), new DarkBolt() );

				com.quasistellar.hollowdungeon.actors.buffs.Light l = enemy.buff(com.quasistellar.hollowdungeon.actors.buffs.Light.class);
				if (l != null){
					l.weaken(50);
				}

				if (!enemy.isAlive() && enemy == Dungeon.hero) {
					Dungeon.fail( getClass() );
					GLog.n( Messages.get(com.quasistellar.hollowdungeon.actors.Char.class, "kill", name()) );
				}

			} else {

				enemy.sprite.showStatus( com.quasistellar.hollowdungeon.sprites.CharSprite.NEUTRAL,  enemy.defenseVerb() );
			}

		}

		@Override
		public void damage(int dmg, Object src) {
			int beforeHP = HP;
			super.damage(dmg, src);
			if (isAlive() && beforeHP > HT/2 && HP < HT/2){
				HP = HT/2;
				com.quasistellar.hollowdungeon.actors.buffs.Light l = Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.Light.class);
				if (l != null){
					l.detach();
				}
				int i;
				do {
					i = Random.Int(Dungeon.level.length());
				} while (Dungeon.level.heroFOV[i]
						|| Dungeon.level.solid[i]
						|| com.quasistellar.hollowdungeon.actors.Actor.findChar(i) != null
						|| PathFinder.getStep(i, Dungeon.level.exit, Dungeon.level.passable) == -1);
				ScrollOfTeleportation.appear(this, i);
				state = WANDERING;
				GameScene.flash(0, false);
				com.quasistellar.hollowdungeon.utils.GLog.w( Messages.get( this, "teleport" ));
			} else if (!isAlive()){
				com.quasistellar.hollowdungeon.actors.buffs.Light l = com.quasistellar.hollowdungeon.Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.Light.class);
				if (l != null){
					l.detach();
				}
				com.quasistellar.hollowdungeon.scenes.GameScene.flash(0, false);
			}
		}

	}

}
