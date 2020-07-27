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

import com.quasistellar.shatteredpixeldungeon.Assets;
import com.quasistellar.shatteredpixeldungeon.Badges;
import com.quasistellar.shatteredpixeldungeon.Dungeon;
import com.quasistellar.shatteredpixeldungeon.actors.Actor;
import com.quasistellar.shatteredpixeldungeon.actors.Char;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.Electricity;
import com.quasistellar.shatteredpixeldungeon.actors.blobs.Fire;
import com.quasistellar.shatteredpixeldungeon.actors.buffs.*;
import com.quasistellar.shatteredpixeldungeon.effects.*;
import com.quasistellar.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.quasistellar.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.quasistellar.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.quasistellar.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.quasistellar.shatteredpixeldungeon.items.Heap;
import com.quasistellar.shatteredpixeldungeon.items.Item;
import com.quasistellar.shatteredpixeldungeon.items.TomeOfMastery;
import com.quasistellar.shatteredpixeldungeon.levels.Level;
import com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel;
import com.quasistellar.shatteredpixeldungeon.scenes.GameScene;
import com.quasistellar.shatteredpixeldungeon.sprites.CharSprite;
import com.quasistellar.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.quasistellar.shatteredpixeldungeon.sprites.MissileSprite;
import com.quasistellar.shatteredpixeldungeon.sprites.TenguSprite;
import com.quasistellar.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.quasistellar.shatteredpixeldungeon.ui.BossHealthBar;
import com.quasistellar.shatteredpixeldungeon.utils.BArray;
import com.quasistellar.shatteredpixeldungeon.utils.GLog;
import com.quasistellar.shatteredpixeldungeon.actors.hero.Hero;
import com.quasistellar.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.quasistellar.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.quasistellar.shatteredpixeldungeon.items.artifacts.LloydsBeacon;
import com.quasistellar.shatteredpixeldungeon.items.bombs.Bomb;
import com.quasistellar.shatteredpixeldungeon.mechanics.Ballistica;
import com.quasistellar.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.HashSet;

public class NewTengu extends com.quasistellar.shatteredpixeldungeon.actors.mobs.Mob {
	
	{
		spriteClass = TenguSprite.class;
		
		HP = HT = 160;
		EXP = 20;
		defenseSkill = 15;
		
		HUNTING = new Hunting();
		
		flying = true; //doesn't literally fly, but he is fleet-of-foot enough to avoid hazards
		
		properties.add(com.quasistellar.shatteredpixeldungeon.actors.Char.Property.BOSS);
		
		viewDistance = 12;
	}
	
	@Override
	protected void onAdd() {
		//when he's removed and re-added to the fight, his time is always set to now.
		spend(-cooldown());
		super.onAdd();
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 6, 12 );
	}
	
	@Override
	public int attackSkill( com.quasistellar.shatteredpixeldungeon.actors.Char target ) {
		if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.adjacent(pos, target.pos)){
			return 12;
		} else {
			return 18;
		}
	}
	
	@Override
	public int drRoll() {
		return Random.NormalIntRange(0, 5);
	}

	//Tengu is immune to debuffs and damage when removed from the level
	@Override
	public void add(com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff buff) {
		if (com.quasistellar.shatteredpixeldungeon.actors.Actor.chars().contains(this) || buff instanceof Doom){
			super.add(buff);
		}
	}

	@Override
	public void damage(int dmg, Object src) {
		if (!com.quasistellar.shatteredpixeldungeon.Dungeon.level.mobs.contains(this)){
			return;
		}

		com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel.State state = ((com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel) com.quasistellar.shatteredpixeldungeon.Dungeon.level).state();
		
		int hpBracket = 20;
		
		int beforeHitHP = HP;
		super.damage(dmg, src);
		dmg = beforeHitHP - HP;
		
		//tengu cannot be hit through multiple brackets at a time
		if ((beforeHitHP/hpBracket - HP/hpBracket) >= 2){
			HP = hpBracket * ((beforeHitHP/hpBracket)-1) + 1;
		}
		
		com.quasistellar.shatteredpixeldungeon.actors.buffs.LockedFloor lock = com.quasistellar.shatteredpixeldungeon.Dungeon.hero.buff(LockedFloor.class);
		if (lock != null) {
			int multiple = state == com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel.State.FIGHT_START ? 1 : 4;
			lock.addTime(dmg*multiple);
		}
		
		//phase 2 of the fight is over
		if (HP == 0 && state == com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel.State.FIGHT_ARENA) {
			//let full attack action complete first
			com.quasistellar.shatteredpixeldungeon.actors.Actor.add(new com.quasistellar.shatteredpixeldungeon.actors.Actor() {
				
				{
					actPriority = VFX_PRIO;
				}
				
				@Override
				protected boolean act() {
					com.quasistellar.shatteredpixeldungeon.actors.Actor.remove(this);
					((com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel) com.quasistellar.shatteredpixeldungeon.Dungeon.level).progress();
					return true;
				}
			});
			return;
		}
		
		//phase 1 of the fight is over
		if (state == com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel.State.FIGHT_START && HP <= HT/2){
			HP = (HT/2);
			yell(Messages.get(this, "interesting"));
			((com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel) com.quasistellar.shatteredpixeldungeon.Dungeon.level).progress();
			com.quasistellar.shatteredpixeldungeon.ui.BossHealthBar.bleed(true);
			
			//if tengu has lost a certain amount of hp, jump
		} else if (beforeHitHP / hpBracket != HP / hpBracket) {
			jump();
		}
	}
	
	@Override
	public boolean isAlive() {
		return HP > 0 || com.quasistellar.shatteredpixeldungeon.Dungeon.level.mobs.contains(this); //Tengu has special death rules, see prisonbosslevel.progress()
	}

	@Override
	public void die( Object cause ) {
		
		if (com.quasistellar.shatteredpixeldungeon.Dungeon.hero.subClass == HeroSubClass.NONE) {
			com.quasistellar.shatteredpixeldungeon.Dungeon.level.drop( new TomeOfMastery(), pos ).sprite.drop();
		}
		
		com.quasistellar.shatteredpixeldungeon.scenes.GameScene.bossSlain();
		super.die( cause );
		
		Badges.validateBossSlain();
		
		LloydsBeacon beacon = com.quasistellar.shatteredpixeldungeon.Dungeon.hero.belongings.getItem(LloydsBeacon.class);
		if (beacon != null) {
			beacon.upgrade();
		}
		
		yell( Messages.get(this, "defeated") );
	}
	
	@Override
	protected boolean canAttack( com.quasistellar.shatteredpixeldungeon.actors.Char enemy ) {
		return new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
	}
	
	//tengu's attack is always visible
	@Override
	protected boolean doAttack(com.quasistellar.shatteredpixeldungeon.actors.Char enemy) {
		sprite.attack( enemy.pos );
		spend( attackDelay() );
		return false;
	}
	
	private void jump() {
		
		//in case tengu hasn't had a chance to act yet
		if (fieldOfView == null || fieldOfView.length != com.quasistellar.shatteredpixeldungeon.Dungeon.level.length()){
			fieldOfView = new boolean[com.quasistellar.shatteredpixeldungeon.Dungeon.level.length()];
			com.quasistellar.shatteredpixeldungeon.Dungeon.level.updateFieldOfView( this, fieldOfView );
		}
		
		if (enemy == null) enemy = chooseEnemy();
		if (enemy == null) return;
		
		int newPos;
		if (com.quasistellar.shatteredpixeldungeon.Dungeon.level instanceof com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel){
			com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel level = (com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel) com.quasistellar.shatteredpixeldungeon.Dungeon.level;
			
			//if we're in phase 1, want to warp around within the room
			if (level.state() == com.quasistellar.shatteredpixeldungeon.levels.NewPrisonBossLevel.State.FIGHT_START) {
				
				level.cleanTenguCell();
				
				do {
					newPos = ((NewPrisonBossLevel) com.quasistellar.shatteredpixeldungeon.Dungeon.level).randomTenguCellPos();
				} while ( level.trueDistance(newPos, enemy.pos) <= 3.5f
						|| level.trueDistance(newPos, com.quasistellar.shatteredpixeldungeon.Dungeon.hero.pos) <= 3.5f
						|| com.quasistellar.shatteredpixeldungeon.actors.Actor.findChar(newPos) != null);
				
				if (level.heroFOV[pos]) com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get( pos ).burst( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.WOOL ), 6 );
				
				sprite.move( pos, newPos );
				move( newPos );
				
				if (level.heroFOV[newPos]) com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get( newPos ).burst( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.WOOL ), 6 );
				Sample.INSTANCE.play( com.quasistellar.shatteredpixeldungeon.Assets.Sounds.PUFF );
				
				float fill = 0.9f - 0.5f*((HP-80)/80f);
				level.placeTrapsInTenguCell(fill);
				
			//otherwise, jump in a larger possible area, as the room is bigger
			} else {
				
				do {
					newPos = Random.Int(level.length());
				} while (
						level.solid[newPos] ||
								level.distance(newPos, enemy.pos) < 5 ||
								level.distance(newPos, enemy.pos) > 7 ||
								level.distance(newPos, com.quasistellar.shatteredpixeldungeon.Dungeon.hero.pos) < 5 ||
								level.distance(newPos, com.quasistellar.shatteredpixeldungeon.Dungeon.hero.pos) > 7 ||
								level.distance(newPos, pos) < 6 ||
								com.quasistellar.shatteredpixeldungeon.actors.Actor.findChar(newPos) != null ||
								com.quasistellar.shatteredpixeldungeon.Dungeon.level.heaps.get(newPos) != null);
				
				if (level.heroFOV[pos]) com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get( pos ).burst( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.WOOL ), 6 );
				
				sprite.move( pos, newPos );
				move( newPos );
				
				if (arenaJumps < 4) arenaJumps++;
				
				if (level.heroFOV[newPos]) com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get( newPos ).burst( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.WOOL ), 6 );
				Sample.INSTANCE.play( com.quasistellar.shatteredpixeldungeon.Assets.Sounds.PUFF );
				
			}
			
		//if we're on another type of level
		} else {
			Level level = com.quasistellar.shatteredpixeldungeon.Dungeon.level;
			
			newPos = level.randomRespawnCell( this );
			
			if (level.heroFOV[pos]) com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get( pos ).burst( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.WOOL ), 6 );
			
			sprite.move( pos, newPos );
			move( newPos );
			
			if (level.heroFOV[newPos]) com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.get( newPos ).burst( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( com.quasistellar.shatteredpixeldungeon.effects.Speck.WOOL ), 6 );
			Sample.INSTANCE.play( com.quasistellar.shatteredpixeldungeon.Assets.Sounds.PUFF );
			
		}
		
	}
	
	@Override
	public void notice() {
		super.notice();
		if (!com.quasistellar.shatteredpixeldungeon.ui.BossHealthBar.isAssigned()) {
			com.quasistellar.shatteredpixeldungeon.ui.BossHealthBar.assignBoss(this);
			if (HP <= HT/2) com.quasistellar.shatteredpixeldungeon.ui.BossHealthBar.bleed(true);
			if (HP == HT) {
				yell(Messages.get(this, "notice_gotcha", com.quasistellar.shatteredpixeldungeon.Dungeon.hero.name()));
				for (com.quasistellar.shatteredpixeldungeon.actors.Char ch : com.quasistellar.shatteredpixeldungeon.actors.Actor.chars()){
					if (ch instanceof DriedRose.GhostHero){
						((DriedRose.GhostHero) ch).sayBoss();
					}
				}
			} else {
				yell(Messages.get(this, "notice_have", com.quasistellar.shatteredpixeldungeon.Dungeon.hero.name()));
			}
		}
	}
	
	{
		immunities.add( Blindness.class );
		immunities.add( Terror.class );
	}
	
	private static final String LAST_ABILITY     = "last_ability";
	private static final String ABILITIES_USED   = "abilities_used";
	private static final String ARENA_JUMPS      = "arena_jumps";
	private static final String ABILITY_COOLDOWN = "ability_cooldown";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( LAST_ABILITY, lastAbility );
		bundle.put( ABILITIES_USED, abilitiesUsed );
		bundle.put( ARENA_JUMPS, arenaJumps );
		bundle.put( ABILITY_COOLDOWN, abilityCooldown );
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastAbility = bundle.getInt( LAST_ABILITY );
		abilitiesUsed = bundle.getInt( ABILITIES_USED );
		arenaJumps = bundle.getInt( ARENA_JUMPS );
		abilityCooldown = bundle.getInt( ABILITY_COOLDOWN );
		
		com.quasistellar.shatteredpixeldungeon.ui.BossHealthBar.assignBoss(this);
		if (HP <= HT/2) BossHealthBar.bleed(true);
	}
	
	//don't bother bundling this, as its purely cosmetic
	private boolean yelledCoward = false;
	
	//tengu is always hunting
	private class Hunting extends Mob.Hunting {
		
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			
			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {
				
				if (canUseAbility()){
					return useAbility();
				}
				
				return doAttack( enemy );
				
			} else {
				
				if (enemyInFOV) {
					target = enemy.pos;
				} else {
					chooseEnemy();
					if (enemy == null){
						//if nothing else can be targeted, target hero
						enemy = com.quasistellar.shatteredpixeldungeon.Dungeon.hero;
					}
					target = enemy.pos;
				}
				
				//if not charmed, attempt to use an ability, even if the enemy can't be seen
				if (canUseAbility()){
					return useAbility();
				}
				
				spend( com.quasistellar.shatteredpixeldungeon.actors.Actor.TICK );
				return true;
				
			}
		}
	}
	
	//*****************************************************************************************
	//***** Tengu abilities. These are expressed in game logic as buffs, blobs, and items *****
	//*****************************************************************************************
	
	//so that mobs can also use this
	private static com.quasistellar.shatteredpixeldungeon.actors.Char throwingChar;
	
	private int lastAbility = -1;
	private int abilitiesUsed = 0;
	private int arenaJumps = 0;
	
	//starts at 2, so one turn and then first ability
	private int abilityCooldown = 2;
	
	private static final int BOMB_ABILITY    = 0;
	private static final int FIRE_ABILITY    = 1;
	private static final int SHOCKER_ABILITY = 2;
	
	//expects to be called once per turn;
	public boolean canUseAbility(){
		
		if (HP > HT/2) return false;
		
		if (abilitiesUsed >= targetAbilityUses()){
			return false;
		} else {
			
			abilityCooldown--;
			
			if (targetAbilityUses() - abilitiesUsed >= 4){
				//Very behind in ability uses, use one right away!
				abilityCooldown = 0;
				
			} else if (targetAbilityUses() - abilitiesUsed >= 3){
				//moderately behind in uses, use one every other action.
				if (abilityCooldown == -1 || abilityCooldown > 1) abilityCooldown = 1;
				
			} else {
				//standard delay before ability use, 1-4 turns
				if (abilityCooldown == -1) abilityCooldown = Random.IntRange(1, 4);
			}
			
			if (abilityCooldown == 0){
				return true;
			} else {
				return false;
			}
		}
	}
	
	private int targetAbilityUses(){
		//1 base ability use, plus 2 uses per jump
		int targetAbilityUses = 1 + 2*arenaJumps;
		
		//and ane extra 2 use for jumps 3 and 4
		targetAbilityUses += Math.max(0, arenaJumps-2);
		
		return targetAbilityUses;
	}
	
	public boolean useAbility(){
		boolean abilityUsed = false;
		int abilityToUse = -1;
		
		while (!abilityUsed){
			
			if (abilitiesUsed == 0){
				abilityToUse = BOMB_ABILITY;
			} else if (abilitiesUsed == 1){
				abilityToUse = SHOCKER_ABILITY;
			} else {
				abilityToUse = Random.Int(3);
			}
			
			//If we roll the same ability as last time, 9/10 chance to reroll
			if (abilityToUse != lastAbility || Random.Int(10) == 0){
				switch (abilityToUse){
					case BOMB_ABILITY : default:
						abilityUsed = throwBomb(NewTengu.this, enemy);
						//if Tengu cannot use his bomb ability first, use fire instead.
						if (abilitiesUsed == 0 && !abilityUsed){
							abilityToUse = FIRE_ABILITY;
							abilityUsed = throwFire(NewTengu.this, enemy);
						}
						break;
					case FIRE_ABILITY:
						abilityUsed = throwFire(NewTengu.this, enemy);
						break;
					case SHOCKER_ABILITY:
						abilityUsed = throwShocker(NewTengu.this, enemy);
						//if Tengu cannot use his shocker ability second, use fire instead.
						if (abilitiesUsed == 1 && !abilityUsed){
							abilityToUse = FIRE_ABILITY;
							abilityUsed = throwFire(NewTengu.this, enemy);
						}
						break;
				}
			}
			
		}
		
		//spend only 1 turn if seriously behind on ability uses
		if (targetAbilityUses() - abilitiesUsed >= 4){
			spend(com.quasistellar.shatteredpixeldungeon.actors.Actor.TICK);
		} else {
			spend(2 * com.quasistellar.shatteredpixeldungeon.actors.Actor.TICK);
		}
		
		lastAbility = abilityToUse;
		abilitiesUsed++;
		return lastAbility == FIRE_ABILITY;
	}
	
	//******************
	//***Bomb Ability***
	//******************
	
	//returns true if bomb was thrown
	public static boolean throwBomb(final com.quasistellar.shatteredpixeldungeon.actors.Char thrower, final com.quasistellar.shatteredpixeldungeon.actors.Char target){
		
		int targetCell = -1;
		
		//Targets closest cell which is adjacent to target, and at least 3 tiles away
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = target.pos + i;
			if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.distance(cell, thrower.pos) >= 3 && !com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid[cell]){
				if (targetCell == -1 ||
						com.quasistellar.shatteredpixeldungeon.Dungeon.level.trueDistance(cell, thrower.pos) < com.quasistellar.shatteredpixeldungeon.Dungeon.level.trueDistance(targetCell, thrower.pos)){
					targetCell = cell;
				}
			}
		}
		
		if (targetCell == -1){
			return false;
		}
		
		final int finalTargetCell = targetCell;
		throwingChar = thrower;
		final BombAbility.BombItem item = new BombAbility.BombItem();
		thrower.sprite.zap(finalTargetCell);
		((com.quasistellar.shatteredpixeldungeon.sprites.MissileSprite) thrower.sprite.parent.recycle(com.quasistellar.shatteredpixeldungeon.sprites.MissileSprite.class)).
				reset(thrower.sprite,
						finalTargetCell,
						item,
						new Callback() {
							@Override
							public void call() {
								item.onThrow(finalTargetCell);
								thrower.next();
							}
						});
		return true;
	}
	
	public static class BombAbility extends com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff {
		
		public int bombPos;
		private int timer = 3;
		
		@Override
		public boolean act() {
			
			PointF p = DungeonTilemap.raisedTileCenterToWorld(bombPos);
			if (timer == 3) {
				com.quasistellar.shatteredpixeldungeon.effects.FloatingText.show(p.x, p.y, bombPos, "3...", com.quasistellar.shatteredpixeldungeon.sprites.CharSprite.NEUTRAL);
				PathFinder.buildDistanceMap( bombPos, BArray.not( com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid, null ), 2 );
				for (int i = 0; i < PathFinder.distance.length; i++) {
					if (PathFinder.distance[i] < Integer.MAX_VALUE) {
						com.quasistellar.shatteredpixeldungeon.scenes.GameScene.add(com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob.seed(i, 4, BombBlob.class));
					}
				}
			} else if (timer == 2){
				com.quasistellar.shatteredpixeldungeon.effects.FloatingText.show(p.x, p.y, bombPos, "2...", com.quasistellar.shatteredpixeldungeon.sprites.CharSprite.WARNING);
			} else if (timer == 1){
				FloatingText.show(p.x, p.y, bombPos, "1...", CharSprite.NEGATIVE);
			} else {
				com.quasistellar.shatteredpixeldungeon.items.Heap h = com.quasistellar.shatteredpixeldungeon.Dungeon.level.heaps.get(bombPos);
				if (h != null){
					for (com.quasistellar.shatteredpixeldungeon.items.Item i : h.items.toArray(new com.quasistellar.shatteredpixeldungeon.items.Item[0])){
						if (i instanceof BombItem){
							h.remove(i);
						}
					}
				}
				detach();
				return true;
			}
			
			timer--;
			spend(com.quasistellar.shatteredpixeldungeon.actors.Actor.TICK);
			return true;
		}
		
		private static final String BOMB_POS = "bomb_pos";
		private static final String TIMER = "timer";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( BOMB_POS, bombPos );
			bundle.put( TIMER, timer );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			bombPos = bundle.getInt( BOMB_POS );
			timer = bundle.getInt( TIMER );
		}
		
		public static class BombBlob extends com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob {
			{
				actPriority = com.quasistellar.shatteredpixeldungeon.actors.Actor.BUFF_PRIO - 1;
				alwaysVisible = true;
			}
			
			@Override
			protected void evolve() {
				
				boolean exploded = false;
				
				int cell;
				for (int i = area.left; i < area.right; i++){
					for (int j = area.top; j < area.bottom; j++){
						cell = i + j* com.quasistellar.shatteredpixeldungeon.Dungeon.level.width();
						off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;
						
						if (off[cell] > 0) {
							volume += off[cell];
						}
						
						if (cur[cell] > 0 && off[cell] == 0){
							
							com.quasistellar.shatteredpixeldungeon.actors.Char ch = com.quasistellar.shatteredpixeldungeon.actors.Actor.findChar(cell);
							if (ch != null && !(ch instanceof NewTengu)){
								int dmg = Random.NormalIntRange(5 + com.quasistellar.shatteredpixeldungeon.Dungeon.depth, 10 + com.quasistellar.shatteredpixeldungeon.Dungeon.depth*2);
								dmg -= ch.drRoll();
								
								if (dmg > 0) {
									ch.damage(dmg, Bomb.class);
								}
								
								if (ch == com.quasistellar.shatteredpixeldungeon.Dungeon.hero && !ch.isAlive()) {
									com.quasistellar.shatteredpixeldungeon.Dungeon.fail(NewTengu.class);
								}
							}

							Heap h = com.quasistellar.shatteredpixeldungeon.Dungeon.level.heaps.get(cell);
							if (h != null){
								for (com.quasistellar.shatteredpixeldungeon.items.Item it : h.items.toArray(new com.quasistellar.shatteredpixeldungeon.items.Item[0])){
									if (it instanceof BombItem){
										h.remove(it);
									}
								}
							}
							
							exploded = true;
							com.quasistellar.shatteredpixeldungeon.effects.CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
						}
					}
				}
				
				if (exploded){
					Sample.INSTANCE.play(com.quasistellar.shatteredpixeldungeon.Assets.Sounds.BLAST);
				}
				
			}
			
			@Override
			public void use(com.quasistellar.shatteredpixeldungeon.effects.BlobEmitter emitter) {
				super.use(emitter);
				
				emitter.pour( com.quasistellar.shatteredpixeldungeon.effects.particles.SmokeParticle.FACTORY, 0.25f );
			}
			
			@Override
			public String tileDesc() {
				return Messages.get(this, "desc");
			}
		}
		
		public static class BombItem extends com.quasistellar.shatteredpixeldungeon.items.Item {
			
			{
				dropsDownHeap = true;
				unique = true;
				
				image = com.quasistellar.shatteredpixeldungeon.sprites.ItemSpriteSheet.TENGU_BOMB;
			}
			
			@Override
			public boolean doPickUp( Hero hero ) {
				com.quasistellar.shatteredpixeldungeon.utils.GLog.w( Messages.get(this, "cant_pickup") );
				return false;
			}
			
			@Override
			protected void onThrow(int cell) {
				super.onThrow(cell);
				if (throwingChar != null){
					com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.append(throwingChar, BombAbility.class).bombPos = cell;
					throwingChar = null;
				} else {
					com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.append(curUser, BombAbility.class).bombPos = cell;
				}
			}
			
			@Override
			public Emitter emitter() {
				Emitter emitter = new Emitter();
				emitter.pos(7.5f, 3.5f);
				emitter.fillTarget = false;
				emitter.pour(SmokeParticle.SPEW, 0.05f);
				return emitter;
			}
		}
	}
	
	//******************
	//***Fire Ability***
	//******************
	
	public static boolean throwFire(final com.quasistellar.shatteredpixeldungeon.actors.Char thrower, final com.quasistellar.shatteredpixeldungeon.actors.Char target){
		
		Ballistica aim = new Ballistica(thrower.pos, target.pos, Ballistica.WONT_STOP);
		
		for (int i = 0; i < PathFinder.CIRCLE8.length; i++){
			if (aim.sourcePos+PathFinder.CIRCLE8[i] == aim.path.get(1)){
				thrower.sprite.zap(target.pos);
				com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.append(thrower, NewTengu.FireAbility.class).direction = i;
				
				thrower.sprite.emitter().start(com.quasistellar.shatteredpixeldungeon.effects.Speck.factory(com.quasistellar.shatteredpixeldungeon.effects.Speck.STEAM), .03f, 10);
				return true;
			}
		}
		
		return false;
	}
	
	public static class FireAbility extends com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff {
		
		public int direction;
		private int[] curCells;
		
		HashSet<Integer> toCells = new HashSet<>();
		
		@Override
		public boolean act() {

			toCells.clear();

			if (curCells == null){
				curCells = new int[1];
				curCells[0] = target.pos;
				spreadFromCell( curCells[0] );

			} else {
				for (Integer c : curCells) {
					if (FireBlob.volumeAt(c, FireBlob.class) > 0) spreadFromCell(c);
				}
			}
			
			for (Integer c : curCells){
				toCells.remove(c);
			}
			
			if (toCells.isEmpty()){
				detach();
			} else {
				curCells = new int[toCells.size()];
				int i = 0;
				for (Integer c : toCells){
					com.quasistellar.shatteredpixeldungeon.scenes.GameScene.add(com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob.seed(c, 2, FireBlob.class));
					curCells[i] = c;
					i++;
				}
			}
			
			spend(com.quasistellar.shatteredpixeldungeon.actors.Actor.TICK);
			return true;
		}
		
		private void spreadFromCell( int cell ){
			if (!com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid[cell + PathFinder.CIRCLE8[left(direction)]]){
				toCells.add(cell + PathFinder.CIRCLE8[left(direction)]);
			}
			if (!com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid[cell + PathFinder.CIRCLE8[direction]]){
				toCells.add(cell + PathFinder.CIRCLE8[direction]);
			}
			if (!com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid[cell + PathFinder.CIRCLE8[right(direction)]]){
				toCells.add(cell + PathFinder.CIRCLE8[right(direction)]);
			}
		}
		
		private int left(int direction){
			return direction == 0 ? 7 : direction-1;
		}
		
		private int right(int direction){
			return direction == 7 ? 0 : direction+1;
		}
		
		private static final String DIRECTION = "direction";
		private static final String CUR_CELLS = "cur_cells";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( DIRECTION, direction );
			bundle.put( CUR_CELLS, curCells );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			direction = bundle.getInt( DIRECTION );
			curCells = bundle.getIntArray( CUR_CELLS );
		}
		
		public static class FireBlob extends com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob {
			
			{
				actPriority = com.quasistellar.shatteredpixeldungeon.actors.Actor.BUFF_PRIO - 1;
				alwaysVisible = true;
			}
			
			@Override
			protected void evolve() {
				
				boolean observe = false;
				boolean burned = false;
				
				int cell;
				for (int i = area.left; i < area.right; i++){
					for (int j = area.top; j < area.bottom; j++){
						cell = i + j* com.quasistellar.shatteredpixeldungeon.Dungeon.level.width();
						off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;
						
						if (off[cell] > 0) {
							volume += off[cell];
						}
						
						if (cur[cell] > 0 && off[cell] == 0){
							
							com.quasistellar.shatteredpixeldungeon.actors.Char ch = com.quasistellar.shatteredpixeldungeon.actors.Actor.findChar( cell );
							if (ch != null && !ch.isImmune(Fire.class) && !(ch instanceof NewTengu)) {
								com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.affect( ch, Burning.class ).reignite( ch );
							}
							
							if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.flamable[cell]){
								com.quasistellar.shatteredpixeldungeon.Dungeon.level.destroy( cell );
								
								observe = true;
								com.quasistellar.shatteredpixeldungeon.scenes.GameScene.updateMap( cell );
							}
							
							burned = true;
							CellEmitter.get(cell).start(FlameParticle.FACTORY, 0.03f, 10);
						}
					}
				}
				
				if (observe) {
					com.quasistellar.shatteredpixeldungeon.Dungeon.observe();
				}
				
				if (burned){
					Sample.INSTANCE.play(com.quasistellar.shatteredpixeldungeon.Assets.Sounds.BURNING);
				}
			}
			
			@Override
			public void use(com.quasistellar.shatteredpixeldungeon.effects.BlobEmitter emitter) {
				super.use(emitter);
				
				emitter.pour( com.quasistellar.shatteredpixeldungeon.effects.Speck.factory( Speck.STEAM ), 0.2f );
			}
			
			@Override
			public String tileDesc() {
				return Messages.get(this, "desc");
			}
		}
	}
	
	//*********************
	//***Shocker Ability***
	//*********************
	
	//returns true if shocker was thrown
	public static boolean throwShocker(final com.quasistellar.shatteredpixeldungeon.actors.Char thrower, final com.quasistellar.shatteredpixeldungeon.actors.Char target){
		
		int targetCell = -1;
		
		//Targets closest cell which is adjacent to target, and not adjacent to thrower or another shocker
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = target.pos + i;
			if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.distance(cell, thrower.pos) >= 2 && !com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid[cell]){
				boolean validTarget = true;
				for (ShockerAbility s : thrower.buffs(ShockerAbility.class)){
					if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.distance(cell, s.shockerPos) < 2){
						validTarget = false;
						break;
					}
				}
				if (validTarget && com.quasistellar.shatteredpixeldungeon.Dungeon.level.trueDistance(cell, thrower.pos) < com.quasistellar.shatteredpixeldungeon.Dungeon.level.trueDistance(targetCell, thrower.pos)){
					targetCell = cell;
				}
			}
		}
		
		if (targetCell == -1){
			return false;
		}
		
		final int finalTargetCell = targetCell;
		throwingChar = thrower;
		final ShockerAbility.ShockerItem item = new ShockerAbility.ShockerItem();
		thrower.sprite.zap(finalTargetCell);
		((com.quasistellar.shatteredpixeldungeon.sprites.MissileSprite) thrower.sprite.parent.recycle(MissileSprite.class)).
				reset(thrower.sprite,
						finalTargetCell,
						item,
						new Callback() {
							@Override
							public void call() {
								item.onThrow(finalTargetCell);
								thrower.next();
							}
						});
		return true;
	}
	
	public static class ShockerAbility extends com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff {
	
		public int shockerPos;
		private Boolean shockingOrdinals = null;
		
		@Override
		public boolean act() {
			
			if (shockingOrdinals == null){
				shockingOrdinals = Random.Int(2) == 1;
				
				spreadblob();
			} else if (shockingOrdinals){
				
				target.sprite.parent.add(new com.quasistellar.shatteredpixeldungeon.effects.Lightning(shockerPos - 1 - com.quasistellar.shatteredpixeldungeon.Dungeon.level.width(), shockerPos + 1 + com.quasistellar.shatteredpixeldungeon.Dungeon.level.width(), null));
				target.sprite.parent.add(new com.quasistellar.shatteredpixeldungeon.effects.Lightning(shockerPos - 1 + com.quasistellar.shatteredpixeldungeon.Dungeon.level.width(), shockerPos + 1 - com.quasistellar.shatteredpixeldungeon.Dungeon.level.width(), null));
				
				if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.distance(com.quasistellar.shatteredpixeldungeon.Dungeon.hero.pos, shockerPos) <= 1){
					Sample.INSTANCE.play( com.quasistellar.shatteredpixeldungeon.Assets.Sounds.LIGHTNING );
				}
				
				shockingOrdinals = false;
				spreadblob();
			} else {
				
				target.sprite.parent.add(new com.quasistellar.shatteredpixeldungeon.effects.Lightning(shockerPos - com.quasistellar.shatteredpixeldungeon.Dungeon.level.width(), shockerPos + com.quasistellar.shatteredpixeldungeon.Dungeon.level.width(), null));
				target.sprite.parent.add(new Lightning(shockerPos - 1, shockerPos + 1, null));
				
				if (com.quasistellar.shatteredpixeldungeon.Dungeon.level.distance(com.quasistellar.shatteredpixeldungeon.Dungeon.hero.pos, shockerPos) <= 1){
					Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
				}
				
				shockingOrdinals = true;
				spreadblob();
			}
			
			spend(com.quasistellar.shatteredpixeldungeon.actors.Actor.TICK);
			return true;
		}
		
		private void spreadblob(){
			com.quasistellar.shatteredpixeldungeon.scenes.GameScene.add(com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob.seed(shockerPos, 1, ShockerBlob.class));
			for (int i = shockingOrdinals ? 0 : 1; i < PathFinder.CIRCLE8.length; i += 2){
				if (!com.quasistellar.shatteredpixeldungeon.Dungeon.level.solid[shockerPos+PathFinder.CIRCLE8[i]]) {
					GameScene.add(com.quasistellar.shatteredpixeldungeon.actors.blobs.Blob.seed(shockerPos + PathFinder.CIRCLE8[i], 2, ShockerBlob.class));
				}
			}
		}
		
		private static final String SHOCKER_POS = "shocker_pos";
		private static final String SHOCKING_ORDINALS = "shocking_ordinals";
		
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( SHOCKER_POS, shockerPos );
			bundle.put( SHOCKING_ORDINALS, shockingOrdinals );
		}
		
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			shockerPos = bundle.getInt( SHOCKER_POS );
			shockingOrdinals = bundle.getBoolean( SHOCKING_ORDINALS );
		}
		
		public static class ShockerBlob extends Blob {
			
			{
				actPriority = com.quasistellar.shatteredpixeldungeon.actors.Actor.BUFF_PRIO - 1;
				alwaysVisible = true;
			}
			
			@Override
			protected void evolve() {
				
				int cell;
				for (int i = area.left; i < area.right; i++){
					for (int j = area.top; j < area.bottom; j++){
						cell = i + j* com.quasistellar.shatteredpixeldungeon.Dungeon.level.width();
						off[cell] = cur[cell] > 0 ? cur[cell] - 1 : 0;
						
						if (off[cell] > 0) {
							volume += off[cell];
						}
						
						if (cur[cell] > 0 && off[cell] == 0){
							
							Char ch = Actor.findChar(cell);
							if (ch != null && !(ch instanceof NewTengu)){
								ch.damage(2 + com.quasistellar.shatteredpixeldungeon.Dungeon.depth, new Electricity());
								
								if (ch == com.quasistellar.shatteredpixeldungeon.Dungeon.hero && !ch.isAlive()) {
									Dungeon.fail(NewTengu.class);
								}
							}
							
						}
					}
				}
				
			}
			
			@Override
			public void use(BlobEmitter emitter) {
				super.use(emitter);
				
				emitter.pour( com.quasistellar.shatteredpixeldungeon.effects.particles.SparkParticle.STATIC, 0.10f );
			}
			
			@Override
			public String tileDesc() {
				return Messages.get(this, "desc");
			}
		}
		
		public static class ShockerItem extends Item {
			
			{
				dropsDownHeap = true;
				unique = true;
				
				image = ItemSpriteSheet.TENGU_SHOCKER;
			}
			
			@Override
			public boolean doPickUp( Hero hero ) {
				GLog.w( Messages.get(this, "cant_pickup") );
				return false;
			}
			
			@Override
			protected void onThrow(int cell) {
				super.onThrow(cell);
				if (throwingChar != null){
					com.quasistellar.shatteredpixeldungeon.actors.buffs.Buff.append(throwingChar, ShockerAbility.class).shockerPos = cell;
					throwingChar = null;
				} else {
					Buff.append(curUser, ShockerAbility.class).shockerPos = cell;
				}
			}
			
			@Override
			public Emitter emitter() {
				Emitter emitter = new Emitter();
				emitter.pos(5, 5);
				emitter.fillTarget = false;
				emitter.pour(SparkParticle.FACTORY, 0.1f);
				return emitter;
			}
		}
		
	}
}
