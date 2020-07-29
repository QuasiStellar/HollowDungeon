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

package com.quasistellar.hollowdungeon.items.bombs;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Lightning;
import com.quasistellar.hollowdungeon.effects.particles.SparkParticle;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.tiles.DungeonTilemap;
import com.quasistellar.hollowdungeon.utils.BArray;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ShockBomb extends Bomb {
	
	{
		image = ItemSpriteSheet.SHOCK_BOMB;
	}
	
	@Override
	public void explode(int cell) {
		super.explode(cell);

		ArrayList<com.quasistellar.hollowdungeon.actors.Char> affected = new ArrayList<>();
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 3 );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE
				&& Actor.findChar(i) != null) {
				affected.add(com.quasistellar.hollowdungeon.actors.Actor.findChar(i));
			}
		}

		for (com.quasistellar.hollowdungeon.actors.Char ch : affected.toArray(new com.quasistellar.hollowdungeon.actors.Char[0])){
			Ballistica LOS = new Ballistica(cell, ch.pos, Ballistica.PROJECTILE);
			if (LOS.collisionPos != ch.pos){
				affected.remove(ch);
			}
		}

		ArrayList<com.quasistellar.hollowdungeon.effects.Lightning.Arc> arcs = new ArrayList<>();
		for (Char ch : affected){
			int power = 16 - 4* Dungeon.level.distance(ch.pos, cell);
			if (power > 0){
				//32% to 8% regular bomb damage
				int damage = 8;
				ch.damage(damage, this);
				if (ch.isAlive()) Buff.prolong(ch, Paralysis.class, power);
				arcs.add(new com.quasistellar.hollowdungeon.effects.Lightning.Arc(DungeonTilemap.tileCenterToWorld(cell), ch.sprite.center()));
			}
		}

		CellEmitter.center(cell).burst(SparkParticle.FACTORY, 20);
		com.quasistellar.hollowdungeon.Dungeon.hero.sprite.parent.addToFront(new Lightning(arcs, null));
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
	}
	
	@Override
	public int price() {
		//prices of ingredients
		return quantity * (20 + 30);
	}
}
