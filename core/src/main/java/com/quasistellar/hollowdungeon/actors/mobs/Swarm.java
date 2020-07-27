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

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.actors.buffs.Corruption;
import com.quasistellar.hollowdungeon.actors.buffs.Poison;
import com.quasistellar.hollowdungeon.effects.Pushing;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.SwarmSprite;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Swarm extends Mob {

	{
		spriteClass = SwarmSprite.class;
		
		HP = HT = 50;
		
		flying = true;

		loot = new PotionOfHealing();
		lootChance = 0.1667f; //by default, see rollToDropLoot()
	}
	
	private static final float SPLIT_DELAY	= 1f;
	
	int generation	= 0;
	
	private static final String GENERATION	= "generation";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( GENERATION, generation );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		generation = bundle.getInt( GENERATION );
	}

	private Swarm split() {
		Swarm clone = new Swarm();
		clone.generation = generation + 1;
		if (buff( com.quasistellar.hollowdungeon.actors.buffs.Burning.class ) != null) {
			Buff.affect( clone, Burning.class ).reignite( clone );
		}
		if (buff( com.quasistellar.hollowdungeon.actors.buffs.Poison.class ) != null) {
			Buff.affect( clone, Poison.class ).set(2);
		}
		if (buff(com.quasistellar.hollowdungeon.actors.buffs.Corruption.class ) != null) {
			com.quasistellar.hollowdungeon.actors.buffs.Buff.affect( clone, Corruption.class);
		}
		return clone;
	}
	
	@Override
	public void rollToDropLoot() {
		lootChance = 1f/(6 * (generation+1) );
		lootChance *= (5f - Dungeon.LimitedDrops.SWARM_HP.count) / 5f;
		super.rollToDropLoot();
	}
	
	@Override
	protected Item createLoot(){
		com.quasistellar.hollowdungeon.Dungeon.LimitedDrops.SWARM_HP.count++;
		return super.createLoot();
	}
}
