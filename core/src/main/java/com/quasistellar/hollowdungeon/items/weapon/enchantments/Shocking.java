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

package com.quasistellar.hollowdungeon.items.weapon.enchantments;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.Lightning;
import com.quasistellar.hollowdungeon.sprites.ItemSprite;
import com.quasistellar.hollowdungeon.utils.BArray;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.effects.particles.SparkParticle;
import com.quasistellar.hollowdungeon.items.weapon.Weapon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Shocking extends Weapon.Enchantment {

	private static com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing WHITE = new com.quasistellar.hollowdungeon.sprites.ItemSprite.Glowing( 0xFFFFFF, 0.5f );

	@Override
	public int proc(Weapon weapon, com.quasistellar.hollowdungeon.actors.Char attacker, com.quasistellar.hollowdungeon.actors.Char defender, int damage ) {
		// lvl 0 - 25%
		// lvl 1 - 40%
		// lvl 2 - 50%
		int level = Math.max( 0, weapon.buffedLvl() );
		
		if (Random.Int( level + 4 ) >= 3) {
			
			affected.clear();
			arcs.clear();
			
			arc(attacker, defender, 2, affected, arcs);
			
			affected.remove(defender); //defender isn't hurt by lightning
			for (com.quasistellar.hollowdungeon.actors.Char ch : affected) {
				if (ch.alignment != attacker.alignment) {
					ch.damage(Math.round(damage * 0.4f), this);
				}
			}

			attacker.sprite.parent.addToFront( new com.quasistellar.hollowdungeon.effects.Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
			
		}

		return damage;

	}

	@Override
	public ItemSprite.Glowing glowing() {
		return WHITE;
	}

	private ArrayList<com.quasistellar.hollowdungeon.actors.Char> affected = new ArrayList<>();

	private ArrayList<com.quasistellar.hollowdungeon.effects.Lightning.Arc> arcs = new ArrayList<>();
	
	public static void arc(com.quasistellar.hollowdungeon.actors.Char attacker, com.quasistellar.hollowdungeon.actors.Char defender, int dist, ArrayList<com.quasistellar.hollowdungeon.actors.Char> affected, ArrayList<com.quasistellar.hollowdungeon.effects.Lightning.Arc> arcs ) {
		
		affected.add(defender);
		
		defender.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
		defender.sprite.flash();
		
		PathFinder.buildDistanceMap( defender.pos, BArray.not( Dungeon.level.solid, null ), dist );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char n = Actor.findChar(i);
				if (n != null && n != attacker && !affected.contains(n)) {
					arcs.add(new Lightning.Arc(defender.sprite.center(), n.sprite.center()));
					arc(attacker, n, (com.quasistellar.hollowdungeon.Dungeon.level.water[n.pos] && !n.flying) ? 2 : 1, affected, arcs);
				}
			}
		}
	}
}
