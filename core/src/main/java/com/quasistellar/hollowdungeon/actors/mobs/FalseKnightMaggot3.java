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

package com.quasistellar.hollowdungeon.actors.mobs;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.actors.blobs.ToxicGas;
import com.quasistellar.hollowdungeon.actors.buffs.Amok;
import com.quasistellar.hollowdungeon.actors.buffs.Burning;
import com.quasistellar.hollowdungeon.actors.buffs.Paralysis;
import com.quasistellar.hollowdungeon.actors.buffs.Sleep;
import com.quasistellar.hollowdungeon.actors.buffs.Terror;
import com.quasistellar.hollowdungeon.actors.buffs.Vertigo;
import com.quasistellar.hollowdungeon.items.CityCrest;
import com.quasistellar.hollowdungeon.levels.FalseknightLevel;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.MaggotSprite;
import com.quasistellar.hollowdungeon.sprites.RotHeartSprite;

public class FalseKnightMaggot3 extends Mob {

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
        Dungeon.level.unseal();
        ((FalseknightLevel)Dungeon.level).progress();
        GameScene.bossSlain();
        Dungeon.level.drop( new CityCrest(), pos ).sprite.drop();
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