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
import com.quasistellar.hollowdungeon.actors.Actor;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.actors.buffs.FlavourBuff;
import com.quasistellar.hollowdungeon.actors.buffs.Invisibility;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.input.GameAction;
import com.watabou.utils.Callback;

public class MothwingCloak extends Skill {

    @Override
    public void act() {
        if (Dungeon.hero.buff(MothwingCloakDelay.class) != null) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        GameScene.selectCell( leaper );
    }

    protected CellSelector.Listener leaper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {
            if (target != null && target != Dungeon.hero.pos) {

                Ballistica route = new Ballistica(Dungeon.hero.pos, target, Ballistica.MAGIC_BOLT);
                int cell = route.collisionPos;

                if (route.dist > 3) {
                    cell = route.path.get(3);
                }

                //can't occupy the same cell as another char, so move back one.
                if (Actor.findChar( cell ) != null && cell != Dungeon.hero.pos)
                    cell = route.path.get(route.dist > 3 ? 2 : route.dist-1);

                Invisibility.dispel();

                final int dest = cell;
                Dungeon.hero.busy();
                Dungeon.hero.sprite.jump(Dungeon.hero.pos, cell, () -> {
                    Dungeon.hero.move(dest);
                    Dungeon.level.occupyCell(Dungeon.hero);
                    Dungeon.observe();
                    GameScene.updateFog();

                    //Camera.main.shake(2, 0.5f);

                    Dungeon.hero.spendAndNext(1);
                    Buff.prolong(Dungeon.hero, MothwingCloakDelay.class, 2.9f);
                }, 0, Dungeon.level.trueDistance( Dungeon.hero.pos, cell ) * 0.05f);
            }
        }

        @Override
        public String prompt() {
            return Messages.get(MothwingCloak.class, "prompt");
        }
    };

    public static class MothwingCloakDelay extends FlavourBuff {
        //does nothing
    }

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public boolean visible() {
        return super.visible() && Dungeon.hero.buff(MothwingCloakDelay.class) == null;
    }

    @Override
    public int icon() {
        return Skill.MOTHWING_CLOAK;
    }

    @Override
    public GameAction action() {
        return HDAction.MOTHWING_CLOAK;
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
