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

package com.quasistellar.hollowdungeon.items.wands;

import com.quasistellar.hollowdungeon.actors.buffs.Amok;
import com.quasistellar.hollowdungeon.actors.buffs.Corruption;
import com.quasistellar.hollowdungeon.actors.hero.Hero;
import com.quasistellar.hollowdungeon.actors.mobs.Mob;
import com.quasistellar.hollowdungeon.actors.mobs.npcs.NPC;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.sprites.EarthGuardianSprite;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.ui.BuffIndicator;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class WandOfLivingEarth extends DamageWand {
	
	{
		image = ItemSpriteSheet.WAND_LIVING_EARTH;
	}
	
	@Override
	public int min(int lvl) {
		return 4;
	}
	
	@Override
	public int max(int lvl) {
		return 6 + 2*lvl;
	}
	
	@Override
	protected void onZap(Ballistica bolt) {
		com.quasistellar.hollowdungeon.actors.Char ch = Actor.findChar(bolt.collisionPos);
		int damage = damageRoll();
		int armorToAdd = damage;

		EarthGuardian guardian = null;
		for (com.quasistellar.hollowdungeon.actors.mobs.Mob m : Dungeon.level.mobs){
			if (m instanceof EarthGuardian){
				guardian = (EarthGuardian) m;
				break;
			}
		}

		RockArmor buff = com.quasistellar.hollowdungeon.items.Item.curUser.buff(RockArmor.class);
		if (ch == null){
			armorToAdd = 0;
		} else {
			if (buff == null && guardian == null) {
				buff = Buff.affect(com.quasistellar.hollowdungeon.items.Item.curUser, RockArmor.class);
			}
			if (buff != null) {
				buff.addArmor( buffedLvl(), armorToAdd);
			}
		}

		//shooting at the guardian
		if (guardian != null && guardian == ch){
			guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl() / 2);
			guardian.setInfo(com.quasistellar.hollowdungeon.items.Item.curUser, buffedLvl(), armorToAdd);
			processSoulMark(guardian, chargesPerCast());
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.9f * Random.Float(0.87f, 1.15f) );

		//shooting the guardian at a location
		} else if ( guardian == null && buff != null && buff.armor >= buff.armorToGuardian()){

			//create a new guardian
			guardian = new EarthGuardian();
			guardian.setInfo(com.quasistellar.hollowdungeon.items.Item.curUser, buffedLvl(), buff.armor);

			//if the collision pos is occupied (likely will be), then spawn the guardian in the
			//adjacent cell which is closes to the user of the wand.
			if (ch != null){

				ch.sprite.centerEmitter().burst(MagicMissile.EarthParticle.BURST, 5 + buffedLvl()/2);

				processSoulMark(ch, chargesPerCast());
				ch.damage(damage, this);

				int closest = -1;
				boolean[] passable = Dungeon.level.passable;

				for (int n : PathFinder.NEIGHBOURS9) {
					int c = bolt.collisionPos + n;
					if (passable[c] && com.quasistellar.hollowdungeon.actors.Actor.findChar( c ) == null
						&& (closest == -1 || (Dungeon.level.trueDistance(c, com.quasistellar.hollowdungeon.items.Item.curUser.pos) < (Dungeon.level.trueDistance(closest, com.quasistellar.hollowdungeon.items.Item.curUser.pos))))) {
						closest = c;
					}
				}

				if (closest == -1){
					com.quasistellar.hollowdungeon.items.Item.curUser.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl()/2);
					return; //do not spawn guardian or detach buff
				} else {
					guardian.pos = closest;
					GameScene.add(guardian, 1);
					Dungeon.level.occupyCell(guardian);
				}

				if (ch.alignment == Char.Alignment.ENEMY || ch.buff(com.quasistellar.hollowdungeon.actors.buffs.Amok.class) != null) {
					guardian.aggro(ch);
				}

			} else {
				guardian.pos = bolt.collisionPos;
				com.quasistellar.hollowdungeon.scenes.GameScene.add(guardian, 1);
				Dungeon.level.occupyCell(guardian);
			}

			guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl()/2);
			buff.detach();
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.9f * Random.Float(0.87f, 1.15f) );

		//shooting at a location/enemy with no guardian being shot
		} else {

			if (ch != null) {

				ch.sprite.centerEmitter().burst(MagicMissile.EarthParticle.BURST, 5 + buffedLvl() / 2);

				processSoulMark(ch, chargesPerCast());
				ch.damage(damage, this);
				Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 0.8f * Random.Float(0.87f, 1.15f) );
				
				if (guardian == null) {
					com.quasistellar.hollowdungeon.items.Item.curUser.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl() / 2);
				} else {
					guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl() / 2);
					guardian.setInfo(com.quasistellar.hollowdungeon.items.Item.curUser, buffedLvl(), armorToAdd);
					if (ch.alignment == Char.Alignment.ENEMY || ch.buff(Amok.class) != null) {
						guardian.aggro(ch);
					}
				}

			} else {
				Dungeon.level.pressCell(bolt.collisionPos);
			}
		}

	}
	
	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(com.quasistellar.hollowdungeon.items.Item.curUser.sprite.parent,
				MagicMissile.EARTH,
				Item.curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.ZAP);
	}
	
	@Override
	public void onHit(com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff staff, com.quasistellar.hollowdungeon.actors.Char attacker, com.quasistellar.hollowdungeon.actors.Char defender, int damage) {
		EarthGuardian guardian = null;
		for (com.quasistellar.hollowdungeon.actors.mobs.Mob m : Dungeon.level.mobs){
			if (m instanceof EarthGuardian){
				guardian = (EarthGuardian) m;
				break;
			}
		}
		
		int armor = Math.round(damage*0.25f);

		if (guardian != null){
			guardian.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl() / 2);
			guardian.setInfo(Dungeon.hero, buffedLvl(), armor);
		} else {
			attacker.sprite.centerEmitter().burst(MagicMissile.EarthParticle.ATTRACT, 8 + buffedLvl() / 2);
			Buff.affect(attacker, RockArmor.class).addArmor( buffedLvl(), armor);
		}
	}
	
	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		if (Random.Int(10) == 0){
			particle.color(ColorMath.random(0xFFF568, 0x80791A));
		} else {
			particle.color(ColorMath.random(0x805500, 0x332500));
		}
		particle.am = 1f;
		particle.setLifespan(2f);
		particle.setSize( 1f, 2f);
		particle.shuffleXY(0.5f);
		float dst = Random.Float(11f);
		particle.x -= dst;
		particle.y += dst;
	}

	public static class RockArmor extends com.quasistellar.hollowdungeon.actors.buffs.Buff {

		private int wandLevel;
		private int armor;

		private void addArmor( int wandLevel, int toAdd ){
			this.wandLevel = Math.max(this.wandLevel, wandLevel);
			armor += toAdd;
			armor = Math.min(armor, 2*armorToGuardian());
		}

		private int armorToGuardian(){
			return 8 + wandLevel*4;
		}

		public int absorb( int damage ) {
			int block = damage - damage/2;
			if (armor <= block) {
				detach();
				return damage - armor;
			} else {
				armor -= block;
				return damage - block;
			}
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (armorToGuardian() - armor) / (float)armorToGuardian());
		}

		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get( this, "desc", armor, armorToGuardian());
		}

		private static final String WAND_LEVEL = "wand_level";
		private static final String ARMOR = "armor";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WAND_LEVEL, wandLevel);
			bundle.put(ARMOR, armor);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			wandLevel = bundle.getInt(WAND_LEVEL);
			armor = bundle.getInt(ARMOR);
		}
	}

	public static class EarthGuardian extends NPC {

		{
			spriteClass = EarthGuardianSprite.class;

			alignment = Char.Alignment.ALLY;
			state = HUNTING;
			intelligentAlly = true;
			WANDERING = new Wandering();

			//before other mobs
			actPriority = com.quasistellar.hollowdungeon.actors.Actor.MOB_PRIO + 1;

			HP = HT = 0;
		}

		private int wandLevel = -1;

		private void setInfo(Hero hero, int wandLevel, int healthToAdd){
			if (wandLevel > this.wandLevel) {
				this.wandLevel = wandLevel;
				HT = 16 + 8 * wandLevel;
			}
			HP = Math.min(HT, HP + healthToAdd);
		}

		@Override
		public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage) {
			if (enemy instanceof com.quasistellar.hollowdungeon.actors.mobs.Mob) ((com.quasistellar.hollowdungeon.actors.mobs.Mob)enemy).aggro(this);
			return super.attackProc(enemy, damage);
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange(2, 4 + Dungeon.depth/2);
		}

		@Override
		public String description() {
			if (Dungeon.isChallenged(com.quasistellar.hollowdungeon.Challenges.NO_ARMOR)){
				return Messages.get(this, "desc", wandLevel, 2 + wandLevel);
			} else {
				return Messages.get(this, "desc", wandLevel, 3 + 3*wandLevel);
			}
			
		}
		
		{
			immunities.add( Corruption.class );
		}

		private static final String DEFENSE = "defense";
		private static final String WAND_LEVEL = "wand_level";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(WAND_LEVEL, wandLevel);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			wandLevel = bundle.getInt(WAND_LEVEL);
		}

		private class Wandering extends Mob.Wandering {

			@Override
			public boolean act(boolean enemyInFOV, boolean justAlerted) {
				if (!enemyInFOV){
					com.quasistellar.hollowdungeon.actors.buffs.Buff.affect(Dungeon.hero, RockArmor.class).addArmor(wandLevel, HP);
					com.quasistellar.hollowdungeon.Dungeon.hero.sprite.centerEmitter().burst(com.quasistellar.hollowdungeon.effects.MagicMissile.EarthParticle.ATTRACT, 8 + wandLevel/2);
					destroy();
					sprite.die();
					return true;
				} else {
					return super.act(enemyInFOV, justAlerted);
				}
			}

		}

	}
}
