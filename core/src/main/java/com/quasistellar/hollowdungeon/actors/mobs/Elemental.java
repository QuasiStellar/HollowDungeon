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

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.effects.Lightning;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.ElementalSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.blobs.Freezing;
import com.quasistellar.hollowdungeon.effects.Splash;
import com.quasistellar.hollowdungeon.items.potions.PotionOfFrost;
import com.quasistellar.hollowdungeon.items.potions.PotionOfLiquidFlame;
import com.quasistellar.hollowdungeon.items.quest.Embers;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfRecharging;
import com.quasistellar.hollowdungeon.items.scrolls.ScrollOfTransmutation;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public abstract class Elemental extends Mob {

	{
		HP = HT = 60;
		
		flying = true;
	}

	private int rangedCooldown = Random.NormalIntRange( 3, 5 );
	
	@Override
	protected boolean act() {
		if (state == HUNTING){
			rangedCooldown--;
		}
		
		return super.act();
	}
	
	@Override
	protected boolean canAttack( com.quasistellar.hollowdungeon.actors.Char enemy ) {
		if (rangedCooldown <= 0) {
			return new Ballistica( pos, enemy.pos, Ballistica.MAGIC_BOLT ).collisionPos == enemy.pos;
		} else {
			return super.canAttack( enemy );
		}
	}
	
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
	
	@Override
	public int attackProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		meleeProc( enemy, damage );
		
		return damage;
	}
	
	private void zap() {
		spend( 1f );
		
		if (hit( this, enemy, true )) {
			
			rangedProc( enemy );
			
		} else {
			enemy.sprite.showStatus( CharSprite.NEUTRAL,  enemy.defenseVerb() );
		}

		rangedCooldown = Random.NormalIntRange( 3, 5 );
	}
	
	public void onZapComplete() {
		zap();
		next();
	}
	
	@Override
	public void add( com.quasistellar.hollowdungeon.actors.buffs.Buff buff ) {
		if (harmfulBuffs.contains( buff.getClass() )) {
			damage( Random.NormalIntRange( HT/2, HT * 3/5 ), buff );
		} else {
			super.add( buff );
		}
	}
	
	protected abstract void meleeProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage );
	protected abstract void rangedProc( com.quasistellar.hollowdungeon.actors.Char enemy );
	
	protected ArrayList<Class<? extends com.quasistellar.hollowdungeon.actors.buffs.Buff>> harmfulBuffs = new ArrayList<>();
	
	private static final String COOLDOWN = "cooldown";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( COOLDOWN, rangedCooldown );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( COOLDOWN )){
			rangedCooldown = bundle.getInt( COOLDOWN );
		}
	}
	
	public static class FireElemental extends Elemental {
		
		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.ElementalSprite.Fire.class;
			
			loot = new PotionOfLiquidFlame();
			lootChance = 1/8f;
			
			properties.add( Property.FIERY );
			
			harmfulBuffs.add( com.quasistellar.hollowdungeon.actors.buffs.Frost.class );
			harmfulBuffs.add( com.quasistellar.hollowdungeon.actors.buffs.Chill.class );
		}
		
		@Override
		protected void meleeProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
			if (Random.Int( 2 ) == 0 && !Dungeon.level.water[enemy.pos]) {
				com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Burning.class ).reignite( enemy );
				Splash.at( enemy.sprite.center(), sprite.blood(), 5);
			}
		}
		
		@Override
		protected void rangedProc( com.quasistellar.hollowdungeon.actors.Char enemy ) {
			if (!Dungeon.level.water[enemy.pos]) {
				Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Burning.class ).reignite( enemy, 4f );
			}
			Splash.at( enemy.sprite.center(), sprite.blood(), 5);
		}
	}
	
	//used in wandmaker quest
	public static class NewbornFireElemental extends FireElemental {
		
		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.ElementalSprite.NewbornFire.class;
			
			HT = 60;
			HP = HT/2; //32

			loot = new Embers();
			lootChance = 1f;
			
			properties.add(Property.MINIBOSS);
		}

		@Override
		public boolean reset() {
			return true;
		}
		
	}
	
	public static class FrostElemental extends Elemental {
		
		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.ElementalSprite.Frost.class;
			
			loot = new PotionOfFrost();
			lootChance = 1/8f;
			
			properties.add( Property.ICY );
			
			harmfulBuffs.add( com.quasistellar.hollowdungeon.actors.buffs.Burning.class );
		}
		
		@Override
		protected void meleeProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
			if (Random.Int( 3 ) == 0 || Dungeon.level.water[enemy.pos]) {
				Freezing.freeze( enemy.pos );
				Splash.at( enemy.sprite.center(), sprite.blood(), 5);
			}
		}
		
		@Override
		protected void rangedProc( com.quasistellar.hollowdungeon.actors.Char enemy ) {
			com.quasistellar.hollowdungeon.actors.blobs.Freezing.freeze( enemy.pos );
			com.quasistellar.hollowdungeon.effects.Splash.at( enemy.sprite.center(), sprite.blood(), 5);
		}
	}
	
	public static class ShockElemental extends Elemental {
		
		{
			spriteClass = com.quasistellar.hollowdungeon.sprites.ElementalSprite.Shock.class;
			
			loot = new ScrollOfRecharging();
			lootChance = 1/4f;
			
			properties.add( Property.ELECTRIC );
		}
		
		@Override
		protected void meleeProc(com.quasistellar.hollowdungeon.actors.Char enemy, int damage ) {
			ArrayList<com.quasistellar.hollowdungeon.actors.Char> affected = new ArrayList<>();
			ArrayList<com.quasistellar.hollowdungeon.effects.Lightning.Arc> arcs = new ArrayList<>();
			
			if (!Dungeon.level.water[enemy.pos]) {
				affected.remove( enemy );
			}
			
			for (com.quasistellar.hollowdungeon.actors.Char ch : affected) {
				ch.damage( Math.round( damage * 0.4f ), this );
			}
			
			sprite.parent.addToFront( new Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		}
		
		@Override
		protected void rangedProc( com.quasistellar.hollowdungeon.actors.Char enemy ) {
			com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( enemy, com.quasistellar.hollowdungeon.actors.buffs.Blindness.class, com.quasistellar.hollowdungeon.actors.buffs.Blindness.DURATION/2f );
			if (enemy == com.quasistellar.hollowdungeon.Dungeon.hero) {
				GameScene.flash(0xFFFFFF);
			}
		}
	}
	
	public static Class<? extends Elemental> random(){
		float roll = Random.Float();
		if (roll < 0.4f){
			return FireElemental.class;
		} else if (roll < 0.8f){
			return FrostElemental.class;
		} else {
			return ShockElemental.class;
		}
	}
}
