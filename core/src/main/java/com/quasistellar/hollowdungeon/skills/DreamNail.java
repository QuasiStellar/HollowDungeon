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
import com.quasistellar.hollowdungeon.actors.Char;
import com.quasistellar.hollowdungeon.actors.buffs.Buff;
import com.quasistellar.hollowdungeon.mechanics.Ballistica;
import com.quasistellar.hollowdungeon.mechanics.Utils;
import com.quasistellar.hollowdungeon.messages.Messages;
import com.quasistellar.hollowdungeon.scenes.CellSelector;
import com.quasistellar.hollowdungeon.scenes.GameScene;
import com.quasistellar.hollowdungeon.ui.AttackIndicator;
import com.quasistellar.hollowdungeon.utils.GLog;
import com.watabou.input.GameAction;

public class DreamNail extends Skill {

    @Override
    public void act() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        if (delay == null) {
            GLog.w(Messages.get(this, "delay"));
            return;
        }
        GameScene.selectCell(selector);
    }

    private CellSelector.Listener selector = new CellSelector.Listener() {

        @Override
        public void onSelect(Integer cell) {
            if (cell == null) return;
            final Char enemy = Actor.findChar( cell );
            if (enemy == null
                    || !Dungeon.level.heroFOV[cell]
                    || !Dungeon.level.adjacent(Dungeon.hero.pos, enemy.pos)
                    || Dungeon.hero.isCharmedBy( enemy )){
                GLog.w( Messages.get(DreamNail.class, "bad_target") );
            } else {
                Dungeon.hero.sprite.attack(cell, () -> doAttack(enemy));
            }
        }

        private void doAttack(final Char enemy){

            AttackIndicator.target(enemy);

            Ballistica trajectory = new Ballistica(Dungeon.hero.pos, enemy.pos, Ballistica.STOP_TARGET);
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
            Utils.throwChar(enemy, trajectory, 2);

            enemy.sprite.flash();

            Dungeon.hero.earnMana(33);

            Dungeon.hero.spendAndNext(1f);
        }

        @Override
        public String prompt() {
            return Messages.get(DreamNail.class, "prompt");
        }
    };

    @Override
    public int manaCost() {
        return 0;
    }

    @Override
    public boolean visible() {
        Buff delay = Dungeon.hero.buff(Utils.OneTurnDelay.class);
        return super.visible() && delay != null;
    }

    @Override
    public int icon() {
        return Skill.DREAM_NAIL;
    }

    @Override
    public GameAction action() {
        return HDAction.DREAM_NAIL;
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
