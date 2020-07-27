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
import com.quasistellar.hollowdungeon.actors.buffs.Light;
import com.quasistellar.hollowdungeon.actors.buffs.Terror;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.particles.PurpleParticle;
import com.quasistellar.hollowdungeon.items.Dewdrop;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.EyeSprite;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.items.wands.WandOfDisintegration;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Eye extends Mob {
	
	{
		spriteClass = com.quasistellar.hollowdungeon.sprites.EyeSprite.class;
		
		HP = HT = 100;
		viewDistance = Light.DISTANCE;
		
		flying = true;

		HUNTING = new Hunting();
		
		loot = new com.quasistellar.hollowdungeon.items.Dewdrop();
		lootChance = 1f;

		properties.add(com.quasistellar.hollowdungeon.actors.Char.Property.DEMONIC);
	}

	@Override
	public int damageRoll() {
		return 2;
	}

	private Ballistica beam;
	private int beamTarget = -1;
	private int beamCooldown;
	public boolean beamCharged;

	@Override
	protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {

		if (beamCooldown == 0) {
			Ballistica aim = new Ballistica(pos, enemy.pos, Ballistica.STOP_TERRAIN);

			if (enemy.invisible == 0 && !isCharmedBy(enemy) && fieldOfView[enemy.pos] && aim.subPath(1, aim.dist).contains(enemy.pos)){
				beam = aim;
				beamTarget = aim.collisionPos;
				return true;
			} else
				//if the beam is charged, it has to attack, will aim at previous location of target.
				return beamCharged;
		} else
			return super.canAttack(enemy);
	}

	@Override
	protected boolean act() {
		if (beamCharged && state != HUNTING){
			beamCharged = false;
		}
		if (beam == null && beamTarget != -1) {
			beam = new Ballistica(pos, beamTarget, Ballistica.STOP_TERRAIN);
			sprite.turnTo(pos, beamTarget);
		}
		if (beamCooldown > 0)
			beamCooldown--;
		return super.act();
	}

	@Override
	protected boolean doAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {

		if (beamCooldown > 0) {
			return super.doAttack(enemy);
		} else if (!beamCharged){
			((EyeSprite)sprite).charge( enemy.pos );
			spend( attackDelay()*2f );
			beamCharged = true;
			return true;
		} else {

			spend( attackDelay() );
			
			beam = new Ballistica(pos, beamTarget, Ballistica.STOP_TERRAIN);
			if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[beam.collisionPos] ) {
				sprite.zap( beam.collisionPos );
				return false;
			} else {
				deathGaze();
				return true;
			}
		}

	}

	@Override
	public void damage(int dmg, Object src) {
		if (beamCharged) dmg /= 4;
		super.damage(dmg, src);
	}
	
	//used so resistances can differentiate between melee and magical attacks
	public static class DeathGaze{}

	public void deathGaze(){
		if (!beamCharged || beamCooldown > 0 || beam == null)
			return;

		beamCharged = false;
		beamCooldown = Random.IntRange(4, 6);

		boolean terrainAffected = false;

		for (int pos : beam.subPath(1, beam.dist)) {

			if (Dungeon.level.flamable[pos]) {

				Dungeon.level.destroy( pos );
				GameScene.updateMap( pos );
				terrainAffected = true;

			}

			Char ch = Actor.findChar( pos );
			if (ch == null) {
				continue;
			}

			if (Char.hit( this, ch, true )) {
				ch.damage( Random.NormalIntRange( 30, 50 ), new DeathGaze() );

				if (Dungeon.level.heroFOV[pos]) {
					ch.sprite.flash();
					CellEmitter.center( pos ).burst( PurpleParticle.BURST, Random.IntRange( 1, 2 ) );
				}

				if (!ch.isAlive() && ch == Dungeon.hero) {
					Dungeon.fail( getClass() );
					GLog.n( Messages.get(this, "deathgaze_kill") );
				}
			} else {
				ch.sprite.showStatus( CharSprite.NEUTRAL,  ch.defenseVerb() );
			}
		}

		if (terrainAffected) {
			com.quasistellar.hollowdungeon.Dungeon.observe();
		}

		beam = null;
		beamTarget = -1;
	}

	//generates an average of 1 dew, 0.25 seeds, and 0.25 stones
	@Override
	protected com.quasistellar.hollowdungeon.items.Item createLoot() {
		Item loot;
		switch(Random.Int(4)){
			case 0: case 1: default:
				loot = new Dewdrop().quantity(2);
				break;
			case 3:
				loot = Generator.random(Generator.Category.SEED);
				break;
			case 4:
				loot = Generator.random(com.quasistellar.hollowdungeon.items.Generator.Category.STONE);
				break;
		}
		return loot;
	}

	private static final String BEAM_TARGET     = "beamTarget";
	private static final String BEAM_COOLDOWN   = "beamCooldown";
	private static final String BEAM_CHARGED    = "beamCharged";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( BEAM_TARGET, beamTarget);
		bundle.put( BEAM_COOLDOWN, beamCooldown );
		bundle.put( BEAM_CHARGED, beamCharged );
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(BEAM_TARGET))
			beamTarget = bundle.getInt(BEAM_TARGET);
		beamCooldown = bundle.getInt(BEAM_COOLDOWN);
		beamCharged = bundle.getBoolean(BEAM_CHARGED);
	}

	{
		resistances.add( WandOfDisintegration.class );
	}
	
	{
		immunities.add( Terror.class );
	}

	private class Hunting extends Mob.Hunting{
		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			//even if enemy isn't seen, attack them if the beam is charged
			if (beamCharged && enemy != null && canAttack(enemy)) {
				enemySeen = enemyInFOV;
				return doAttack(enemy);
			}
			return super.act(enemyInFOV, justAlerted);
		}
	}
}
