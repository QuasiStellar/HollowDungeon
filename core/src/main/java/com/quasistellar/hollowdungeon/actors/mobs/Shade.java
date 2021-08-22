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
import com.quasistellar.hollowdungeon.items.Geo;
import com.quasistellar.hollowdungeon.sprites.RatSprite;
import com.quasistellar.hollowdungeon.sprites.ShadeSprite;
import com.watabou.utils.Bundle;

public class Shade extends Mob {

    {
        spriteClass = ShadeSprite.class;

        HP = HT = 15;
    }

    public int geo;

    @Override
    public void die( Object cause ) {

        super.die( cause );

        if (geo > 0) {
            Dungeon.level.drop( new Geo(geo), pos ).sprite.drop();
        }
    }

    private static final String GEO     = "geo";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( GEO, geo);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        geo = bundle.getInt(GEO);
    }
}