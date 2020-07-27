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

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.Lightning;
import com.quasistellar.hollowdungeon.items.Item;
import com.quasistellar.hollowdungeon.items.weapon.enchantments.Shocking;
import com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff;
import com.quasistellar.hollowdungeon.sprites.ItemSpriteSheet;
import com.quasistellar.hollowdungeon.tiles.DungeonTilemap;
import com.quasistellar.hollowdungeon.utils.BArray;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.effects.particles.SparkParticle;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandOfLightning extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_LIGHTNING;
	}
	
	private ArrayList<com.quasistellar.hollowdungeon.actors.Char> affected = new ArrayList<>();

	private ArrayList<com.quasistellar.hollowdungeon.effects.Lightning.Arc> arcs = new ArrayList<>();

	public int min(int lvl){
		return 5+lvl;
	}

	public int max(int lvl){
		return 10+5*lvl;
	}
	
	@Override
	protected void onZap( Ballistica bolt ) {

		//lightning deals less damage per-target, the more targets that are hit.
		float multipler = 0.4f + (0.6f/affected.size());
		//if the main target is in water, all affected take full damage
		if (Dungeon.level.water[bolt.collisionPos]) multipler = 1f;

		for (com.quasistellar.hollowdungeon.actors.Char ch : affected){
			if (ch == Dungeon.hero) Camera.main.shake( 2, 0.3f );
			ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 3 );
			ch.sprite.flash();

			if (ch != com.quasistellar.hollowdungeon.items.Item.curUser && ch.alignment == com.quasistellar.hollowdungeon.items.Item.curUser.alignment && ch.pos != bolt.collisionPos){
				continue;
			}
			processSoulMark(ch, chargesPerCast());
			if (ch == com.quasistellar.hollowdungeon.items.Item.curUser) {
				ch.damage(Math.round(damageRoll() * multipler * 0.67f), this);
			} else {
				ch.damage(Math.round(damageRoll() * multipler), this);
			}
		}

		if (!com.quasistellar.hollowdungeon.items.Item.curUser.isAlive()) {
			Dungeon.fail( getClass() );
			GLog.n(Messages.get(this, "ondeath"));
		}
	}

	@Override
	public void onHit(com.quasistellar.hollowdungeon.items.weapon.melee.MagesStaff staff, com.quasistellar.hollowdungeon.actors.Char attacker, com.quasistellar.hollowdungeon.actors.Char defender, int damage) {
		//acts like shocking enchantment
		new Shocking().proc(staff, attacker, defender, damage);
	}

	private void arc( com.quasistellar.hollowdungeon.actors.Char ch ) {

		int dist = (Dungeon.level.water[ch.pos] && !ch.flying) ? 2 : 1;

		ArrayList<com.quasistellar.hollowdungeon.actors.Char> hitThisArc = new ArrayList<>();
		PathFinder.buildDistanceMap( ch.pos, BArray.not( Dungeon.level.solid, null ), dist );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE){
				com.quasistellar.hollowdungeon.actors.Char n = Actor.findChar( i );
				if (n == com.quasistellar.hollowdungeon.Dungeon.hero && PathFinder.distance[i] > 1)
					//the hero is only zapped if they are adjacent
					continue;
				else if (n != null && !affected.contains( n )) {
					hitThisArc.add(n);
				}
			}
		}
		
		affected.addAll(hitThisArc);
		for (com.quasistellar.hollowdungeon.actors.Char hit : hitThisArc){
			arcs.add(new com.quasistellar.hollowdungeon.effects.Lightning.Arc(ch.sprite.center(), hit.sprite.center()));
			arc(hit);
		}
	}
	
	@Override
	protected void fx( Ballistica bolt, Callback callback ) {

		affected.clear();
		arcs.clear();

		int cell = bolt.collisionPos;

		Char ch = com.quasistellar.hollowdungeon.actors.Actor.findChar( cell );
		if (ch != null) {
			affected.add( ch );
			arcs.add( new com.quasistellar.hollowdungeon.effects.Lightning.Arc(com.quasistellar.hollowdungeon.items.Item.curUser.sprite.center(), ch.sprite.center()));
			arc(ch);
		} else {
			arcs.add( new com.quasistellar.hollowdungeon.effects.Lightning.Arc(com.quasistellar.hollowdungeon.items.Item.curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(bolt.collisionPos)));
			CellEmitter.center( cell ).burst( com.quasistellar.hollowdungeon.effects.particles.SparkParticle.FACTORY, 3 );
		}

		//don't want to wait for the effect before processing damage.
		Item.curUser.sprite.parent.addToFront( new Lightning( arcs, null ) );
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		callback.call();
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0xFFFFFF);
		particle.am = 0.6f;
		particle.setLifespan(0.6f);
		particle.acc.set(0, +10);
		particle.speed.polar(-Random.Float(3.1415926f), 6f);
		particle.setSize(0f, 1.5f);
		particle.sizeJitter = 1f;
		particle.shuffleXY(1f);
		float dst = Random.Float(1f);
		particle.x -= dst;
		particle.y += dst;
	}
	
}
