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

package com.quasistellar.hollowdungeon.mechanics;

import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.effects.Pushing;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.levels.features.Door;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Utils {
    public static void throwChar(final Char ch, final Ballistica trajectory, int power){
        throwChar(ch, trajectory, power, true);
    }

    public static void throwChar(final Char ch, final Ballistica trajectory, int power, boolean collideDmg){

        int dist = Math.min(trajectory.dist, power);

        boolean collided = dist == trajectory.dist;

        if (dist == 0 || ch.properties().contains(Char.Property.IMMOVABLE)) return;

        //large characters cannot be moved into non-open space
        if (Char.hasProp(ch, Char.Property.LARGE)) {
            for (int i = 1; i <= dist; i++) {
                if (!Dungeon.level.openSpace[trajectory.path.get(i)]){
                    dist = i-1;
                    collided = true;
                    break;
                }
            }
        }

        if (Actor.findChar(trajectory.path.get(dist)) != null){
            dist--;
            collided = true;
        }

        if (dist < 0) return;

        final int newPos = trajectory.path.get(dist);

        if (newPos == ch.pos) return;

        final int finalDist = dist;
        final boolean finalCollided = collided && collideDmg;
        final int initialpos = ch.pos;

        Actor.addDelayed(new Pushing(ch, ch.pos, newPos, new Callback() {
            public void call() {
                if (initialpos != ch.pos) {
                    //something caused movement before pushing resolved, cancel to be safe.
                    ch.sprite.place(ch.pos);
                    return;
                }
                int oldPos = ch.pos;
                ch.pos = newPos;
//                if (finalCollided && ch.isAlive()) {
//                    ch.damage(Random.NormalIntRange((finalDist + 1) / 2, finalDist), this);
//                    Paralysis.prolong(ch, Paralysis.class, Random.NormalIntRange((finalDist + 1) / 2, finalDist));
//                }
                if (Dungeon.level.map[oldPos] == Terrain.OPEN_DOOR){
                    Door.leave(oldPos);
                }
                Dungeon.level.occupyCell(ch);
                if (ch == Dungeon.hero){
                    //FIXME currently no logic here if the throw effect kills the hero
                    Dungeon.observe();
                }
            }
        }), -1);
    }

    public static void appear( Char ch, int pos ) {

        ch.sprite.interruptMotion();

        if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.pos]){
            Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
        }

        ch.move( pos );
        if (ch.pos == pos) ch.sprite.place( pos );

        if (ch.invisible == 0) {
            ch.sprite.alpha( 0 );
            ch.sprite.parent.add( new AlphaTweener( ch.sprite, 1, 0.4f ) );
        }

        if (Dungeon.level.heroFOV[pos] || ch == Dungeon.hero ) {
            ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3);
        }
    }

    public static class OneTurnDelay extends FlavourBuff {
        //does nothing
    }
}
