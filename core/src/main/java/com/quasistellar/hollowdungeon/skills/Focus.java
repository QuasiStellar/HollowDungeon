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
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.effects.Speck;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.sprites.CharSprite;
import com.quasistellar.hollowdungeon.ui.HpIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.input.GameAction;
import com.watabou.noosa.Camera;
import com.watabou.utils.Callback;

public class Focus extends Skill {

    @Override
    public boolean spell() { return true; }

    @Override
    public void act() {
//        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
//        if (delay == null) {
//            GLog.w(Messages.get(this, "delay"));
//            return;
//        }
        int effect = Math.min( Dungeon.hero.HT - Dungeon.hero.HP, 1 );
        if (effect > 0) {
            HDSettings.focus(true);
            Dungeon.hero.HP += effect;
            Dungeon.hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
            Dungeon.hero.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "value", effect) );
            HpIndicator.refreshHero();
            Dungeon.hero.spendAndNext(2);
            Dungeon.hero.loseMana(manaCost());
        } else {
            GLog.i( Messages.get(this, "already_full") );
        }
    }

    @Override
    public int manaCost() {
        return 33;
    }

    @Override
    public boolean visible() {
        return Dungeon.hero.MP >= 33;// && Dungeon.hero.buff(Utils.OneTurnDelay.class) != null;
    }

    @Override
    public int icon() {
        return Skill.FOCUS;
    }

    @Override
    public GameAction action() {
        return HDAction.FOCUS;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

}
