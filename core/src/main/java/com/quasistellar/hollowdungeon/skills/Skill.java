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

package com.quasistellar.hollowdungeon.skills;

import com.quasistellar.hollowdungeon.Dungeon;
import com.quasistellar.hollowdungeon.HDAction;
import com.quasistellar.hollowdungeon.HDSettings;
import com.watabou.input.GameAction;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

public class Skill implements Bundlable {

    public static final int MOTHWING_CLOAK = 0;
    public static final int CRYSTAL_HEART = 1;
    public static final int MONARCH_WINGS = 2;
    public static final int DREAM_NAIL = 3;
    public static final int DREAMGATE = 4;
    public static final int FOCUS = 5;
    public static final int VENGEFUL_SPIRIT = 6;
    public static final int DESOLATE_DIVE = 7;
    public static final int HOWLING_WRAITHS = 8;

    public static final int NONE = 10;

    public void act() {
        Dungeon.hero.spend(1);
        Dungeon.hero.busy();
        Dungeon.hero.sprite.operate( Dungeon.hero.pos );
    }

    public boolean spell() { return false; }

    public int manaCost() {
        return 1;
    }

    public boolean visible() {
        return HDSettings.skills();
    }

    public int icon() {
        return NONE;
    }

    public GameAction action() {
        return HDAction.NONE;
    }

    public String desc(){
        return "";
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
    }

}
