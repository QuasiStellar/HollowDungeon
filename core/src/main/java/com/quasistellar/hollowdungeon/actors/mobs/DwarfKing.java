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


import com.quasistellar.hollowdungeon.Badges;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.Beam;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Pushing;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.levels.NewCityBossLevel;
import com.quasistellar.hollowdungeon.sprites.KingSprite;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.effects.particles.ElmoParticle;
import com.quasistellar.hollowdungeon.effects.particles.ShadowParticle;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.BossHealthBar;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTeleportation;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashSet;

public class DwarfKing extends Mob {

	{
		spriteClass = KingSprite.class;

		HP = HT = 300;

		properties.add(Char.Property.BOSS);
		properties.add(Char.Property.UNDEAD);
	}

	@Override
	public int damageRoll() {
		return 2;
	}

	private int phase = 1;
	private int summonsMade = 0;

	private float summonCooldown = 0;
	private float abilityCooldown = 0;
	private static final int MIN_COOLDOWN = 10;
	private static final int MAX_COOLDOWN = 14;

	private int lastAbility = 0;
	private static final int NONE = 0;
	private static final int LINK = 1;
	private static final int TELE = 2;

	private static final String PHASE = "phase";
	private static final String SUMMONS_MADE = "summons_made";

	private static final String SUMMON_CD = "summon_cd";
	private static final String ABILITY_CD = "ability_cd";
	private static final String LAST_ABILITY = "last_ability";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( PHASE, phase );
		bundle.put( SUMMONS_MADE, summonsMade );
		bundle.put( SUMMON_CD, summonCooldown );
		bundle.put( ABILITY_CD, abilityCooldown );
		bundle.put( LAST_ABILITY, lastAbility );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		phase = bundle.getInt( PHASE );
		summonsMade = bundle.getInt( SUMMONS_MADE );
		summonCooldown = bundle.getFloat( SUMMON_CD );
		abilityCooldown = bundle.getFloat( ABILITY_CD );
		lastAbility = bundle.getInt( LAST_ABILITY );

		if (phase == 2) properties.add(Char.Property.IMMOVABLE);
	}

	@Override
	protected boolean act() {
		if (phase == 1) {

			if (summonCooldown <= 0 && summonSubject(3)){
				summonsMade++;
				summonCooldown += Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN);
			} else if (summonCooldown > 0){
				summonCooldown--;
			}

			if (paralysed > 0){
				spend(Actor.TICK);
				return true;
			}

			if (abilityCooldown <= 0){

				if (lastAbility == NONE) {
					//50/50 either ability
					lastAbility = Random.Int(2) == 0 ? LINK : TELE;
				} else if (lastAbility == LINK) {
					//more likely to use tele
					lastAbility = Random.Int(8) == 0 ? LINK : TELE;
				} else {
					//more likely to use link
					lastAbility = Random.Int(8) != 0 ? LINK : TELE;
				}

				if (lastAbility == LINK && lifeLinkSubject()){
					abilityCooldown += Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN);
					spend(Actor.TICK);
					return true;
				} else if (teleportSubject()) {
					lastAbility = TELE;
					abilityCooldown += Random.NormalIntRange(MIN_COOLDOWN, MAX_COOLDOWN);
					spend(Actor.TICK);
					return true;
				}

			} else {
				abilityCooldown--;
			}

		} else if (phase == 2){
			if (summonsMade < 4){
				if (summonsMade == 0){
					sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );
					Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
					yell(Messages.get(this, "wave_1"));
				}
				summonSubject(3, DKGhoul.class);
				spend(3* Actor.TICK);
				summonsMade++;
				return true;
			} else if (shielding() <= 200 && summonsMade < 8){
				if (summonsMade == 4){
					sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );
					Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
					yell(Messages.get(this, "wave_2"));
				}
				if (summonsMade == 7){
					summonSubject(3, Random.Int(2) == 0 ? DKMonk.class : DKWarlock.class);
				} else {
					summonSubject(3, DKGhoul.class);
				}
				summonsMade++;
				spend(Actor.TICK);
				return true;
			} else if (shielding() <= 100 && summonsMade < 12) {
				sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );
				Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
				yell(Messages.get(this, "wave_3"));
				summonSubject(4, DKWarlock.class);
				summonSubject(4, DKMonk.class);
				summonSubject(4, DKGhoul.class);
				summonSubject(4, DKGhoul.class);
				summonsMade = 12;
				spend(Actor.TICK);
				return true;
			} else {
				spend(Actor.TICK);
				return true;
			}
		} else if (phase == 3 && buffs(Summoning.class).size() < 4){
			if (summonSubject(3)) summonsMade++;
		}

		return super.act();
	}

	private boolean summonSubject( int delay ){
		//4th summon is always a monk or warlock, otherwise ghoul
		if (summonsMade % 4 == 3){
			return summonSubject( delay, Random.Int(2) == 0 ? DKMonk.class : DKWarlock.class );
		} else {
			return summonSubject( delay, DKGhoul.class );
		}
	}

	private boolean summonSubject( int delay, Class<?extends Mob> type ){
		Summoning s = new Summoning();
		s.pos = ((com.quasistellar.hollowdungeon.levels.NewCityBossLevel) Dungeon.level).getSummoningPos();
		if (s.pos == -1) return false;
		s.summon = type;
		s.delay = delay;
		s.attachTo(this);
		return true;
	}

	private HashSet<Mob> getSubjects(){
		HashSet<Mob> subjects = new HashSet<>();
		for (Mob m : Dungeon.level.mobs){
			if (m.alignment == alignment && (m instanceof Ghoul || m instanceof Monk || m instanceof Warlock)){
				subjects.add(m);
			}
		}
		return subjects;
	}

	private boolean lifeLinkSubject(){
		Mob furthest = null;

		for (Mob m : getSubjects()){
			boolean alreadyLinked = false;
			for (com.quasistellar.hollowdungeon.actors.buffs.LifeLink l : m.buffs(com.quasistellar.hollowdungeon.actors.buffs.LifeLink.class)){
				if (l.object == id()) alreadyLinked = true;
			}
			if (!alreadyLinked) {
				if (furthest == null || Dungeon.level.distance(pos, furthest.pos) < Dungeon.level.distance(pos, m.pos)){
					furthest = m;
				}
			}
		}

		if (furthest != null) {
			com.quasistellar.hollowdungeon.actors.buffs.Buff.append(furthest, com.quasistellar.hollowdungeon.actors.buffs.LifeLink.class, 100f).object = id();
			com.quasistellar.hollowdungeon.actors.buffs.Buff.append(this, com.quasistellar.hollowdungeon.actors.buffs.LifeLink.class, 100f).object = furthest.id();
			yell(Messages.get(this, "lifelink_" + Random.IntRange(1, 2)));
			sprite.parent.add(new Beam.HealthRay(sprite.destinationCenter(), furthest.sprite.destinationCenter()));
			return true;

		}
		return false;
	}

	private boolean teleportSubject(){
		if (enemy == null) return false;

		Mob furthest = null;

		for (Mob m : getSubjects()){
			if (furthest == null || Dungeon.level.distance(pos, furthest.pos) < Dungeon.level.distance(pos, m.pos)){
				furthest = m;
			}
		}

		if (furthest != null){

			float bestDist;
			int bestPos = pos;

			Ballistica trajectory = new Ballistica(enemy.pos, pos, Ballistica.STOP_TARGET);
			int targetCell = trajectory.path.get(trajectory.dist+1);
			//if the position opposite the direction of the hero is open, go there
			if (Actor.findChar(targetCell) == null && !Dungeon.level.solid[targetCell]){
				bestPos = targetCell;

			//Otherwise go to the neighbour cell that's open and is furthest
			} else {
				bestDist = Dungeon.level.trueDistance(pos, enemy.pos);

				for (int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(pos+i) == null
							&& !Dungeon.level.solid[pos+i]
							&& Dungeon.level.trueDistance(pos+i, enemy.pos) > bestDist){
						bestPos = pos+i;
						bestDist = Dungeon.level.trueDistance(pos+i, enemy.pos);
					}
				}
			}

			Actor.add(new Pushing(this, pos, bestPos));
			pos = bestPos;

			//find closest cell that's adjacent to enemy, place subject there
			bestDist = Dungeon.level.trueDistance(enemy.pos, pos);
			bestPos = enemy.pos;
			for (int i : PathFinder.NEIGHBOURS8){
				if (Actor.findChar(enemy.pos+i) == null
						&& !Dungeon.level.solid[enemy.pos+i]
						&& Dungeon.level.trueDistance(enemy.pos+i, pos) < bestDist){
					bestPos = enemy.pos+i;
					bestDist = Dungeon.level.trueDistance(enemy.pos+i, pos);
				}
			}

			if (bestPos != enemy.pos) ScrollOfTeleportation.appear(furthest, bestPos);
			yell(Messages.get(this, "teleport_" + Random.IntRange(1, 2)));
			return true;
		}
		return false;
	}

	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			com.quasistellar.hollowdungeon.ui.BossHealthBar.assignBoss(this);
			yell(Messages.get(this, "notice"));
		}
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return phase == 2 && effect != KingDamager.class;
	}

	@Override
	public void damage(int dmg, Object src) {
		if (isInvulnerable(src.getClass())){
			super.damage(dmg, src);
			return;
		}
		int preHP = HP;
		super.damage(dmg, src);

		com.quasistellar.hollowdungeon.actors.buffs.LockedFloor lock = Dungeon.hero.buff(com.quasistellar.hollowdungeon.actors.buffs.LockedFloor.class);
		if (lock != null && !isImmune(src.getClass())) lock.addTime(dmg/3);

		if (phase == 1) {
			int dmgTaken = preHP - HP;
			abilityCooldown -= dmgTaken/8f;
			summonCooldown -= dmgTaken/8f;
			if (HP <= 50) {
				HP = 50;
				sprite.showStatus(com.quasistellar.hollowdungeon.sprites.CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
				ScrollOfTeleportation.appear(this, NewCityBossLevel.throne);
				properties.add(Char.Property.IMMOVABLE);
				phase = 2;
				summonsMade = 0;
				sprite.idle();
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(this, DKBarrior.class).setShield(HT);
				for (Summoning s : buffs(Summoning.class)) {
					s.detach();
				}
				for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
					if (m instanceof Ghoul || m instanceof Monk || m instanceof Warlock) {
						m.die(null);
					}
				}
			}
		} else if (phase == 2 && shielding() == 0) {
			properties.remove(Char.Property.IMMOVABLE);
			phase = 3;
			summonsMade = 1; //monk/warlock on 3rd summon
			sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );
			Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
			yell(  Messages.get(this, "enraged", Dungeon.hero.name()) );
		} else if (phase == 3 && preHP > 20 && HP < 20){
			yell( Messages.get(this, "losing") );
		}
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || phase != 3;
	}

	@Override
	public void die(Object cause) {

		GameScene.bossSlain();

		super.die( cause );

		if (Dungeon.level.solid[pos]){
			Heap h = Dungeon.level.heaps.get(pos);
			if (h != null) {
				for (Item i : h.items) {
					Dungeon.level.drop(i, pos + Dungeon.level.width());
				}
				h.destroy();
			}
		}

		Badges.validateBossSlain();

		Dungeon.level.unseal();

		for (Mob m : getSubjects()){
			m.die(null);
		}

		yell( Messages.get(this, "defeated") );
	}

	@Override
	public boolean isImmune(Class effect) {
		//immune to damage amplification from doomed in 2nd phase or later, but it can still be applied
		if (phase > 1 && effect == com.quasistellar.hollowdungeon.actors.buffs.Doom.class && buff(com.quasistellar.hollowdungeon.actors.buffs.Doom.class) != null ){
			return true;
		}
		return super.isImmune(effect);
	}

	public static class DKGhoul extends Ghoul {
		{
			state = HUNTING;
		}

		@Override
		protected boolean act() {
			partnerID = -2; //no partners
			return super.act();
		}
	}

	public static class DKMonk extends Monk {
		{
			state = HUNTING;
		}
	}

	public static class DKWarlock extends Warlock {
		{
			state = HUNTING;
		}
	}

	public static class Summoning extends com.quasistellar.hollowdungeon.actors.buffs.Buff {

		private int delay;
		private int pos;
		private Class<?extends Mob> summon;

		private Emitter particles;

		public int getPos() {
			return pos;
		}

		@Override
		public boolean act() {
			delay--;

			if (delay <= 0){

				if (summon == DKWarlock.class){
					particles.burst(ShadowParticle.CURSE, 10);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				} else if (summon == DKMonk.class){
					particles.burst(ElmoParticle.FACTORY, 10);
					Sample.INSTANCE.play(Assets.Sounds.BURNING);
				} else {
					particles.burst(Speck.factory(Speck.BONE), 10);
					Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.BONES);
				}
				particles = null;

				if (Actor.findChar(pos) != null){
					ArrayList<Integer> candidates = new ArrayList<>();
					for (int i : PathFinder.NEIGHBOURS8){
						if (Dungeon.level.passable[pos+i] && Actor.findChar(pos+i) == null){
							candidates.add(pos+i);
						}
					}
					if (!candidates.isEmpty()){
						pos = Random.element(candidates);
					}
				}

				if (Actor.findChar(pos) == null) {
					Mob m = Reflection.newInstance(summon);
					m.pos = pos;
					com.quasistellar.hollowdungeon.scenes.GameScene.add(m);
					m.state = m.HUNTING;
					if (((DwarfKing)target).phase == 2){
						Buff.affect(m, KingDamager.class);
					}
				} else {
					com.quasistellar.hollowdungeon.actors.Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar(pos);
					ch.damage(Random.NormalIntRange(20, 40), summon);
					if (((DwarfKing)target).phase == 2){
						target.damage(target.HT/12, new KingDamager());
					}
				}

				detach();
			}

			spend(com.quasistellar.hollowdungeon.actors.Actor.TICK);
			return true;
		}

		@Override
		public void fx(boolean on) {
			if (on && particles == null) {
				particles = CellEmitter.get(pos);

				if (summon == DKWarlock.class){
					particles.pour(com.quasistellar.hollowdungeon.effects.particles.ShadowParticle.UP, 0.1f);
				} else if (summon == DKMonk.class){
					particles.pour(com.quasistellar.hollowdungeon.effects.particles.ElmoParticle.FACTORY, 0.1f);
				} else {
					particles.pour(Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.RATTLE), 0.1f);
				}

			} else if (!on && particles != null) {
				particles.on = false;
			}
		}

		private static final String DELAY = "delay";
		private static final String POS = "pos";
		private static final String SUMMON = "summon";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(DELAY, delay);
			bundle.put(POS, pos);
			bundle.put(SUMMON, summon);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			delay = bundle.getInt(DELAY);
			pos = bundle.getInt(POS);
			summon = bundle.getClass(SUMMON);
		}
	}

	public static class KingDamager extends com.quasistellar.hollowdungeon.actors.buffs.Buff {

		@Override
		public boolean act() {
			if (target.alignment != com.quasistellar.hollowdungeon.actors.Char.Alignment.ENEMY){
				detach();
			}
			spend( com.quasistellar.hollowdungeon.actors.Actor.TICK );
			return true;
		}

		@Override
		public void detach() {
			super.detach();
			for (Mob m : com.quasistellar.hollowdungeon.Dungeon.level.mobs){
				if (m instanceof DwarfKing){
					m.damage(m.HT/12, this);
				}
			}
		}
	}

	public static class DKBarrior extends com.quasistellar.hollowdungeon.actors.buffs.Barrier {

		@Override
		public boolean act() {
			incShield();
			return super.act();
		}

		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}
	}

}
