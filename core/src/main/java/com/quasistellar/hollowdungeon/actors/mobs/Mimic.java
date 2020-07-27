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

import com.quasistellar.hollowdungeon.Challenges;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.Gold;
import com.quasistellar.hollowdungeon.items.Heap;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.plants.Swiftthistle;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.sprites.MimicSprite;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.items.Generator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.items.artifacts.TimekeepersHourglass;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Mimic extends Mob {
	
	private int level;
	
	{
		spriteClass = com.quasistellar.hollowdungeon.sprites.MimicSprite.class;

		properties.add(Char.Property.DEMONIC);
		
		//mimics are neutral when hidden
		alignment = Char.Alignment.NEUTRAL;
		state = PASSIVE;
	}
	
	public ArrayList<com.quasistellar.hollowdungeon.items.Item> items;
	
	private static final String LEVEL	= "level";
	private static final String ITEMS	= "items";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		if (items != null) bundle.put( ITEMS, items );
		bundle.put( LEVEL, level );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		if (bundle.contains( ITEMS )) {
			items = new ArrayList<>((Collection<com.quasistellar.hollowdungeon.items.Item>) ((Collection<?>) bundle.getCollection(ITEMS)));
		}
		level = bundle.getInt( LEVEL );
		adjustStats(level);
		super.restoreFromBundle(bundle);
		if (state != PASSIVE && alignment == Char.Alignment.NEUTRAL){
			alignment = Char.Alignment.ENEMY;
		}
	}

	@Override
	public void add(com.quasistellar.hollowdungeon.actors.buffs.Buff buff) {
		super.add(buff);
		if (buff.type == Buff.buffType.NEGATIVE && alignment == Char.Alignment.NEUTRAL){
			alignment = Char.Alignment.ENEMY;
			stopHiding();
			if (sprite != null) sprite.idle();
		}
	}

	@Override
	public String name() {
		if (alignment == Char.Alignment.NEUTRAL){
			return Messages.get(com.quasistellar.hollowdungeon.items.Heap.class, "chest");
		} else {
			return super.name();
		}
	}

	@Override
	public String description() {
		if (alignment == Char.Alignment.NEUTRAL){
			return Messages.get(Heap.class, "chest_desc") + "\n\n" + Messages.get(this, "hidden_hint");
		} else {
			return super.description();
		}
	}

	@Override
	protected boolean act() {
		if (alignment == Char.Alignment.NEUTRAL && state != PASSIVE){
			alignment = Char.Alignment.ENEMY;
			GLog.w(Messages.get(this, "reveal") );
			CellEmitter.get(pos).burst(Speck.factory(Speck.STAR), 10);
			Sample.INSTANCE.play(Assets.Sounds.MIMIC);
		}
		return super.act();
	}

	@Override
	public CharSprite sprite() {
		com.quasistellar.hollowdungeon.sprites.MimicSprite sprite = (MimicSprite) super.sprite();
		if (alignment == Char.Alignment.NEUTRAL) sprite.hideMimic();
		return sprite;
	}

	@Override
	public boolean interact(com.quasistellar.hollowdungeon.actors.Char c) {
		if (alignment != Char.Alignment.NEUTRAL || c != Dungeon.hero){
			return super.interact(c);
		}
		stopHiding();

		Dungeon.hero.busy();
		Dungeon.hero.sprite.operate(pos);
		if (Dungeon.hero.invisible <= 0
				&& Dungeon.hero.buff(Swiftthistle.TimeBubble.class) == null
				&& Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) == null){
			return doAttack(Dungeon.hero);
		} else {
			sprite.idle();
			alignment = Char.Alignment.ENEMY;
			Dungeon.hero.spendAndNext(1f);
			return true;
		}
	}

	@Override
	public void onAttackComplete() {
		super.onAttackComplete();
		if (alignment == Char.Alignment.NEUTRAL){
			alignment = Char.Alignment.ENEMY;
			Dungeon.hero.spendAndNext(1f);
		}
	}

	@Override
	public void damage(int dmg, Object src) {
		if (state == PASSIVE){
			alignment = Char.Alignment.ENEMY;
			stopHiding();
		}
		super.damage(dmg, src);
	}

	public void stopHiding(){
		state = HUNTING;
		if (Actor.chars().contains(this) && Dungeon.level.heroFOV[pos]) {
			enemy = Dungeon.hero;
			target = Dungeon.hero.pos;
			enemySeen = true;
			com.quasistellar.hollowdungeon.utils.GLog.w(Messages.get(this, "reveal") );
			com.quasistellar.hollowdungeon.effects.CellEmitter.get(pos).burst(Speck.factory(com.quasistellar.hollowdungeon.effects.Speck.STAR), 10);
			Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.MIMIC);
		}
	}

	@Override
	public int damageRoll() {
		if (alignment == Char.Alignment.NEUTRAL){
			return Random.NormalIntRange( 2 + 2*level, 2 + 2*level);
		} else {
			return Random.NormalIntRange( 1 + level, 2 + 2*level);
		}
	}

	@Override
	public void beckon( int cell ) {
		// Do nothing
	}

	public void setLevel( int level ){
		this.level = level;
		adjustStats(level);
	}
	
	public void adjustStats( int level ) {
		HP = HT = (1 + level) * 6;
		
		enemySeen = true;
	}
	
	@Override
	public void rollToDropLoot(){
		
		if (items != null) {
			for (com.quasistellar.hollowdungeon.items.Item item : items) {
				Dungeon.level.drop( item, pos ).sprite.drop();
			}
			items = null;
		}
		super.rollToDropLoot();
	}

	@Override
	public float spawningWeight() {
		return 0f;
	}

	@Override
	public boolean reset() {
		if (state != PASSIVE) state = WANDERING;
		return true;
	}

	public static Mimic spawnAt( int pos, com.quasistellar.hollowdungeon.items.Item item ){
		return spawnAt( pos, Arrays.asList(item), Mimic.class);
	}

	public static Mimic spawnAt(int pos, com.quasistellar.hollowdungeon.items.Item item, Class mimicType ){
		return spawnAt( pos, Arrays.asList(item), mimicType);
	}

	public static Mimic spawnAt( int pos, List<com.quasistellar.hollowdungeon.items.Item> items ) {
		return spawnAt( pos, items, Mimic.class);
	}

	public static Mimic spawnAt(int pos, List<com.quasistellar.hollowdungeon.items.Item> items, Class mimicType ) {

		Mimic m;
		if (mimicType == com.quasistellar.hollowdungeon.actors.mobs.GoldenMimic.class){
			m = new GoldenMimic();
		} else if (mimicType == com.quasistellar.hollowdungeon.actors.mobs.CrystalMimic.class) {
			m = new CrystalMimic();
		} else {
			m = new Mimic();
		}

		m.items = new ArrayList<>( items );
		m.setLevel( com.quasistellar.hollowdungeon.Dungeon.depth );
		m.pos = pos;

		//generate an extra reward for killing the mimic
		m.generatePrize();
		
		return m;
	}

	protected void generatePrize(){
		Item reward = null;
		do {
			switch (Random.Int(5)) {
				case 0:
					reward = new Gold().random();
					break;
				case 1: case 2:
					reward = Generator.randomMissile();
					break;
				case 3:
					reward = Generator.randomWeapon();
					break;
				case 4:
					reward = Generator.random(com.quasistellar.hollowdungeon.items.Generator.Category.RING);
					break;
			}
		} while (reward == null || Challenges.isItemBlocked(reward));
		items.add(reward);
	}

}