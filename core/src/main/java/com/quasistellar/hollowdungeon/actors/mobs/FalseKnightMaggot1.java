/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

import com.badlogic.gdx.audio.Sound;
import com.quasistellar.hollowdungeon.Assets;
import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.blobs.Blob;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Amok;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.actors.buffs.Sleep;
import com.quasistellar.hollowdungeon.actors.buffs.Terror;
import com.quasistellar.hollowdungeon.actors.buffs.Vertigo;
import com.quasistellar.hollowdungeon.levels.FalseknightLevel;
import com.quasistellar.hollowdungeon.levels.Terrain;
import com.quasistellar.hollowdungeon.plants.Rotberry;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.MaggotSprite;
import com.quasistellar.hollowdungeon.sprites.RotHeartSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class FalseKnightMaggot1 extends Mob {

    {
        spriteClass = MaggotSprite.class;

        HP = HT = 16;

        state = PASSIVE;

        properties.add(Property.IMMOVABLE);
        properties.add(Property.MINIBOSS);
    }

    @Override
    public void damage(int dmg, Object src) {
        //TODO: when effect properties are done, change this to FIRE
        if (src instanceof Burning) {
            destroy();
            sprite.die();
        } else {
            super.damage(dmg, src);
        }
    }

    @Override
    public void beckon(int cell) {
        //do nothing
    }

    @Override
    protected boolean getCloser(int target) {
        return false;
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        FalseKnight2 falseKnight2;
        falseKnight2 = new FalseKnight2();
        falseKnight2.pos = this.pos;
        GameScene.add( falseKnight2 );
        Dungeon.level.occupyCell( falseKnight2 );
        Sample.INSTANCE.play(Assets.Sounds.MIMIC);
        ((FalseknightLevel)Dungeon.level).progress();
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public int damageRoll() {
        return 0;
    }

    {
        immunities.add( Paralysis.class );
        immunities.add( Amok.class );
        immunities.add( Sleep.class );
        immunities.add( ToxicGas.class );
        immunities.add( Terror.class );
        immunities.add( Vertigo.class );
    }

}