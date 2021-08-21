/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * Hollow Dungeon
 * Copyright (C) 2020-2021 Pierre Schrodinger
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

package com.quasistellar.hollowdungeon.levels.traps;

import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.effects.CellEmitter;
import com.quasistellar.hollowdungeon.effects.MagicMissile;
import com.quasistellar.hollowdungeon.tiles.DungeonTilemap;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.effects.particles.ShadowParticle;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GrimTrap extends Trap {

	{
		color = GREY;
		shape = LARGE_DOT;
		
		canBeHidden = false;
	}

	@Override
	public void activate() {
		com.quasistellar.hollowdungeon.actors.Char target = Actor.findChar(pos);

		//find the closest char that can be aimed at
		if (target == null){
			for (com.quasistellar.hollowdungeon.actors.Char ch : Actor.chars()){
				Ballistica bolt = new Ballistica(pos, ch.pos, Ballistica.PROJECTILE);
				if (bolt.collisionPos == ch.pos &&
						(target == null || Dungeon.level.trueDistance(pos, ch.pos) < Dungeon.level.trueDistance(pos, target.pos))){
					target = ch;
				}
			}
		}

		if (target != null){
			final Char finalTarget = target;
			final GrimTrap trap = this;
			int damage;
			
			//almost kill the player
			if (finalTarget == Dungeon.hero && ((float)finalTarget.HP/finalTarget.HT) >= 0.9f){
				damage = finalTarget.HP-1;
			//kill 'em
			} else {
				damage = finalTarget.HP;
			}
			
			final int finalDmg = damage;
			
			Actor.add(new com.quasistellar.hollowdungeon.actors.Actor() {
				
				{
					//it's a visual effect, gets priority no matter what
					actPriority = VFX_PRIO;
				}
				
				@Override
				protected boolean act() {
					final com.quasistellar.hollowdungeon.actors.Actor toRemove = this;
					((com.quasistellar.hollowdungeon.effects.MagicMissile)finalTarget.sprite.parent.recycle(com.quasistellar.hollowdungeon.effects.MagicMissile.class)).reset(
							MagicMissile.SHADOW,
							DungeonTilemap.tileCenterToWorld(pos),
							finalTarget.sprite.center(),
							new Callback() {
								@Override
								public void call() {
									finalTarget.damage(finalDmg, trap);
									if (finalTarget == Dungeon.hero) {
										Sample.INSTANCE.play(Assets.Sounds.CURSED);
										if (!finalTarget.isAlive()) {
											com.quasistellar.hollowdungeon.Dungeon.fail( GrimTrap.class );
											GLog.n( Messages.get(GrimTrap.class, "ondeath") );
										}
									} else {
										Sample.INSTANCE.play(Assets.Sounds.BURNING);
									}
									finalTarget.sprite.emitter().burst(ShadowParticle.UP, 10);
									com.quasistellar.hollowdungeon.actors.Actor.remove(toRemove);
									next();
								}
							});
					return false;
				}
			});
		} else {
			CellEmitter.get(pos).burst(com.quasistellar.hollowdungeon.effects.particles.ShadowParticle.UP, 10);
			Sample.INSTANCE.play(com.quasistellar.hollowdungeon.Assets.Sounds.BURNING);
		}
	}
}
